#version 150

uniform float EffectTime;
uniform float Intensity;
uniform float CollapseProgress;
uniform float Engulf;
uniform float Visible;
uniform vec2 LensScreenCenter;
uniform vec2 LensScreenRadius;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 safeRadius = max(LensScreenRadius, vec2(0.001));
    vec2 lensDelta = (texCoord - LensScreenCenter) / safeRadius;
    float radial = length(lensDelta);
    vec3 premultiplied = vec3(0.0);
    float alpha = 0.0;

    if (Visible > 0.001) {
        float lensAngle = atan(lensDelta.y, lensDelta.x);
        float innerLens = 1.0 - smoothstep(0.0, 0.050, abs(radial - 0.31));
        float outerLens = 1.0 - smoothstep(0.0, 0.067, abs(radial - 0.43));
        float arcBreakup = 0.34 + 0.66 * pow(abs(cos(lensAngle * 1.5
                - EffectTime * 0.32)), 3.0);
        float lensAlpha = clamp((innerLens * 0.48 + outerLens * 0.24)
                * arcBreakup * Intensity, 0.0, 0.62);
        premultiplied += vec3(0.48, 0.22, 0.82) * lensAlpha;
        alpha = max(alpha, lensAlpha);

        vec2 diskPoint = vec2(lensDelta.x, lensDelta.y / 0.215);
        float diskRadius = length(diskPoint);
        float diskAngle = atan(diskPoint.y, diskPoint.x);
        float diskMask = smoothstep(0.22, 0.34, diskRadius)
                * (1.0 - smoothstep(0.82, 1.08, diskRadius));
        float filamentA = 0.5 + 0.5 * sin(
                diskAngle * 9.0 - EffectTime * 5.8
                + sin(diskRadius * 27.0 - EffectTime * 2.4) * 1.7);
        float filamentB = 0.5 + 0.5 * sin(
                diskAngle * 17.0 + EffectTime * 3.6 + diskRadius * 41.0);
        float matter = clamp(filamentA * 0.72 + filamentB * 0.46, 0.0, 1.0);
        float innerHeat = 1.0 - smoothstep(0.28, 0.78, diskRadius);
        float doppler = 0.68 + 0.42 * (0.5 + 0.5 * cos(diskAngle));
        vec3 stellarMatter = mix(vec3(0.19, 0.025, 0.43),
                vec3(1.0, 0.16, 0.025), innerHeat);
        stellarMatter = mix(stellarMatter, vec3(1.0, 0.78, 0.32),
                pow(matter, 3.5) * innerHeat);
        float diskAlpha = clamp(diskMask * (0.38 + matter * 0.86)
                * doppler * Intensity, 0.0, 0.92);
        premultiplied += stellarMatter * (1.15 + matter * 1.45) * diskAlpha;
        alpha = max(alpha, diskAlpha);

        float photonRing = (1.0 - smoothstep(0.0, 0.032,
                abs(radial - 0.245))) * Intensity;
        float photonAlpha = clamp(photonRing * 0.88, 0.0, 0.95);
        premultiplied += vec3(1.0, 0.34, 0.08) * photonAlpha;
        alpha = clamp(alpha + photonAlpha * (1.0 - alpha), 0.0, 1.0);

        float coreAlpha = (1.0 - smoothstep(0.15, 0.225, radial))
                * clamp(Intensity * 1.4, 0.0, 1.0);
        premultiplied *= 1.0 - coreAlpha;
        alpha = coreAlpha + alpha * (1.0 - coreAlpha);
    }

    float collapse = clamp(CollapseProgress, 0.0, 1.0);
    if (Visible > 0.001 && collapse > 0.0001) {
        float easedCollapse = collapse * collapse * (3.0 - 2.0 * collapse);
        float shockRadius = mix(0.18, 2.25, easedCollapse);
        float shockWidth = mix(0.085, 0.035, collapse);
        float shock = 1.0 - smoothstep(0.0, shockWidth,
                abs(radial - shockRadius));
        float shockAlpha = clamp(shock * (1.0 - collapse * 0.42), 0.0, 0.96);
        vec3 shockColor = mix(vec3(1.0, 0.15, 0.025),
                vec3(1.0, 0.92, 0.72), shock);
        premultiplied = shockColor * shockAlpha
                + premultiplied * (1.0 - shockAlpha);
        alpha = shockAlpha + alpha * (1.0 - shockAlpha);

        float flashAlpha = pow(1.0 - collapse, 7.0) * 0.52;
        premultiplied = vec3(1.0, 0.82, 0.56) * flashAlpha
                + premultiplied * (1.0 - flashAlpha);
        alpha = flashAlpha + alpha * (1.0 - flashAlpha);
    }

    float engulfAlpha = clamp(Engulf, 0.0, 1.0);
    premultiplied *= 1.0 - engulfAlpha;
    alpha = engulfAlpha + alpha * (1.0 - engulfAlpha);
    vec3 color = alpha > 0.0001 ? premultiplied / alpha : vec3(0.0);
    fragColor = vec4(color, alpha);
}
