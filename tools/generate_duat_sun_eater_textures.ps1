param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDirectory,
    [Parameter(Mandatory = $true)]
    [string]$OutputDirectory
)

Add-Type -AssemblyName System.Drawing

function Clamp-Byte([double]$Value) {
    return [byte][Math]::Max(0, [Math]::Min(255, [Math]::Round($Value)))
}

function Mix([double]$From, [double]$To, [double]$Amount) {
    return $From + ($To - $From) * $Amount
}

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null

$sourcePath = Join-Path $SourceDirectory 'dragon.png'
$eyesSourcePath = Join-Path $SourceDirectory 'dragon_eyes.png'
$source = [System.Drawing.Bitmap]::FromFile($sourcePath)
$base = New-Object System.Drawing.Bitmap $source.Width, $source.Height
$glow = New-Object System.Drawing.Bitmap $source.Width, $source.Height
$transparentBlack = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)

try {
    for ($y = 0; $y -lt $source.Height; $y++) {
        for ($x = 0; $x -lt $source.Width; $x++) {
            $pixel = $source.GetPixel($x, $y)
            if ($pixel.A -eq 0) {
                $base.SetPixel($x, $y, $transparentBlack)
                $glow.SetPixel($x, $y, $transparentBlack)
                continue
            }

            $luminance = (0.2126 * $pixel.R + 0.7152 * $pixel.G + 0.0722 * $pixel.B) / 255.0
            if ($luminance -lt 0.42) {
                $tone = $luminance / 0.42
                $red = Mix 3 72 $tone
                $green = Mix 1 7 $tone
                $blue = Mix 9 58 $tone
            } else {
                $tone = ($luminance - 0.42) / 0.58
                $red = Mix 72 178 $tone
                $green = Mix 7 18 $tone
                $blue = Mix 58 42 $tone
            }

            $grain = ((($x * 73 + $y * 151 + $x * $y * 13) -band 15) - 7) * 0.65
            $red += $grain
            $green += $grain * 0.15
            $blue += $grain * 0.55

            $headZone = ($x -ge 108 -and $x -lt 244 -and $y -ge 24 -and $y -lt 94)
            if ($headZone) {
                $red = Mix $red 116 0.24
                $green = Mix $green 2 0.42
                $blue = Mix $blue 31 0.22
            }

            $veinField = [Math]::Sin($x * 0.117 + [Math]::Sin($y * 0.071) * 2.35) +
                    0.63 * [Math]::Sin($y * 0.181 - [Math]::Sin($x * 0.053) * 2.1)
            $veinGate = (($x * 41 + $y * 67 + $x * $y * 3) -band 15) -lt 11
            $faceField = [Math]::Sin($x * 0.29 + $y * 0.11) + 0.72 * [Math]::Sin($x * 0.08 - $y * 0.37)
            $faceRageLine = $headZone -and [Math]::Abs($faceField) -lt 0.19
            $ritualLine = ($luminance -gt 0.045 -and [Math]::Abs($veinField) -lt 0.105 -and $veinGate) -or $faceRageLine
            if ($ritualLine) {
                $lineStrength = 0.52 + [Math]::Min(0.24, $luminance * 0.35)
                $red = Mix $red 205 $lineStrength
                $green = Mix $green 5 $lineStrength
                $blue = Mix $blue 31 $lineStrength
                $glowAlpha = Clamp-Byte (58 + $luminance * 150)
                $glow.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($glowAlpha, 255, 7, 45))
            } else {
                $glow.SetPixel($x, $y, $transparentBlack)
            }

            $base.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(
                    $pixel.A, (Clamp-Byte $red), (Clamp-Byte $green), (Clamp-Byte $blue)))
        }
    }

    $base.Save((Join-Path $OutputDirectory 'duat_sun_eater.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    $glow.Save((Join-Path $OutputDirectory 'duat_sun_eater_glow.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $source.Dispose()
    $base.Dispose()
    $glow.Dispose()
}

$crownTexture = New-Object System.Drawing.Bitmap 64, 64
try {
    for ($y = 0; $y -lt 64; $y++) {
        for ($x = 0; $x -lt 64; $x++) {
            $noise = (($x * 29 + $y * 47 + $x * $y * 5) -band 15) - 7
            $scaleLine = ((($x + [Math]::Floor($y / 4) * 3) % 11) -le 1) -or
                    ((($x * 2 - $y + 128) % 17) -le 1)
            if ($scaleLine) {
                $color = [System.Drawing.Color]::FromArgb(255,
                        (Clamp-Byte (126 + $noise * 2.0)),
                        (Clamp-Byte (3 + [Math]::Max(0, $noise * 0.15))),
                        (Clamp-Byte (29 + $noise * 0.8)))
            } else {
                $color = [System.Drawing.Color]::FromArgb(255,
                        (Clamp-Byte (15 + $noise * 0.7)),
                        (Clamp-Byte (1 + [Math]::Max(0, $noise * 0.08))),
                        (Clamp-Byte (27 + $noise * 1.1)))
            }
            $crownTexture.SetPixel($x, $y, $color)
        }
    }
    $crownTexture.Save((Join-Path $OutputDirectory 'duat_sun_eater_crown.png'),
            [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $crownTexture.Dispose()
}

$eyesSource = [System.Drawing.Bitmap]::FromFile($eyesSourcePath)
$eyes = New-Object System.Drawing.Bitmap $eyesSource.Width, $eyesSource.Height
try {
    for ($y = 0; $y -lt $eyesSource.Height; $y++) {
        for ($x = 0; $x -lt $eyesSource.Width; $x++) {
            $pixel = $eyesSource.GetPixel($x, $y)
            if ($pixel.A -eq 0) {
                $eyes.SetPixel($x, $y, $transparentBlack)
                continue
            }
            $intensity = [Math]::Max($pixel.R, [Math]::Max($pixel.G, $pixel.B)) / 255.0
            $eyes.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(
                    $pixel.A, 255, (Clamp-Byte (7 + 35 * $intensity)), (Clamp-Byte (28 + 92 * $intensity))))
        }
    }
    $eyes.Save((Join-Path $OutputDirectory 'duat_sun_eater_eyes.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $eyesSource.Dispose()
    $eyes.Dispose()
}
