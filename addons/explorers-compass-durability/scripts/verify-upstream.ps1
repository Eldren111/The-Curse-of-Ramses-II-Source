param([string]$SourceCache = '')
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path $PSScriptRoot -Parent
$versions = Get-Content -LiteralPath (Join-Path $projectDir 'verification/upstream-versions.json') -Raw | ConvertFrom-Json
$required = @(
    'public void searchForTargets(Level level, Player player, SearchTarget searchTarget, List<ResourceLocation> keys, BlockPos pos, ItemStack stack)',
    'public void searchForGroup(Level level, Player player, SearchTarget searchTarget, ResourceLocation groupKey, BlockPos pos, ItemStack stack)',
    'public void searchForNext(Level level, Player player, BlockPos pos, ItemStack stack)',
    'public void cancelSearch(Level level, Player player, ItemStack stack)',
    'private void search(ServerLevel level, Player player, SearchTarget searchTarget, List<ResourceLocation> keys, ResourceLocation displayKey, boolean isGroup, BlockPos pos, ItemStack stack, List<BlockPos> prevPos, boolean ignoreNearStart)'
)
$report = foreach ($entry in $versions) {
    $url = "https://raw.githubusercontent.com/dreamforSh/ExplorersCompass/$($entry.commit)/src/main/java/com/chaosthedude/explorerscompass/items/ExplorersCompassItem.java"
    if ($SourceCache) {
        $source = Get-Content -LiteralPath (Join-Path $SourceCache "$($entry.commit).java") -Raw
    } else {
        $source = (Invoke-WebRequest -Uri $url -TimeoutSec 30).Content
    }
    foreach ($signature in $required) {
        if (!$source.Contains($signature)) { throw "Incompatible signature in $($entry.commit): $signature" }
    }
    if ($source -match 'public (?:int getMaxDamage|boolean isDamageable)\(ItemStack') {
        throw "Upstream now overrides a durability method: $($entry.commit)"
    }
    [ordered]@{
        version = $entry.version
        commit = $entry.commit
        result = 'SOURCE_SIGNATURE_PASS'
        verifiedSignatures = $required.Count
        source = $url
        gameRuntimeTested = $false
    }
}
$report | ConvertTo-Json -Depth 4 | Set-Content -Encoding utf8 -LiteralPath (Join-Path $projectDir 'verification/source-compatibility.json')
$report | ForEach-Object { "PASS $($_.version) $($_.commit) ($($_.verifiedSignatures) signatures)" }
