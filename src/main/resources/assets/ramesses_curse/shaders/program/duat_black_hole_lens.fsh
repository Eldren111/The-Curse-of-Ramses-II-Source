#version 150

uniform sampler2D DiffuseSampler;
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
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec3 result = scene.rgb;
    vec2 safeRadius = max(LensScreenRadius, vec2(0.001));
    vec2 lensDelta = (texCoord - LensScreenCenter) / safeRadius;
    float radial = length(lensDelta);
    vec2 radialDirection = lensDelta / max(radial, 0.001);
    float visibleStrength = clamp(Visible * Intensity, 0.0, 1.0);

    if (visibleStrength > 0.001 && radial < 1.08) {
        float outerMask = 1.0 - smoothstep(0.78, 1.06, radial);
        float coreGuard = smoothstep(0.14, 0.30, radial);
        float gravity = outerMask * coreGuard;
        float ripple = 0.84 + 0.16 * sin(radial * 31.0 - EffectTime * 7.5);
        float deflection = visibleStrength * gravity * ripple
                * (0.095 / max(radial, 0.18));
        vec2 orbitDirection = vec2(-radialDirection.y, radialDirection.x);
        float swirl = visibleStrength * gravity
                * sin(radial * 18.0 + EffectTime * 2.8) * 0.013;
        vec2 bentUv = texCoord
                - radialDirection * safeRadius * deflection
                + orbitDirection * safeRadius * swirl;
        if (bentUv.x >= 0.0 && bentUv.x <= 1.0
                && bentUv.y >= 0.0 && bentUv.y <= 1.0) {
            float dispersion = visibleStrength * gravity * 0.018;
            vec2 redUv = clamp(bentUv + radialDirection * safeRadius * dispersion,
                    vec2(0.001), vec2(0.999));
            vec2 blueUv = clamp(bentUv - radialDirection * safeRadius * dispersion,
                    vec2(0.001), vec2(0.999));
            vec3 bentScene = texture(DiffuseSampler, bentUv).rgb;
            result = vec3(texture(DiffuseSampler, redUv).r,
                    bentScene.g, texture(DiffuseSampler, blueUv).b);

            float innerEcho = 1.0 - smoothstep(0.0, 0.052, abs(radial - 0.31));
            float outerEcho = 1.0 - smoothstep(0.0, 0.065, abs(radial - 0.43));
            float echoStrength = clamp((innerEcho * 0.72 + outerEcho * 0.38)
                    * visibleStrength, 0.0, 0.78);
            vec2 echoUv = clamp(LensScreenCenter
                    - radialDirection * safeRadius * (0.39 + radial * 0.18),
                    vec2(0.001), vec2(0.999));
            result = mix(result, texture(DiffuseSampler, echoUv).rgb, echoStrength);
            result += vec3(0.34, 0.16, 0.52)
                    * (innerEcho + outerEcho * 0.45) * visibleStrength * 0.20;
        }
    }

    if (Visible > 0.001) {
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
        vec3 coldMatter = vec3(0.19, 0.025, 0.43);
        vec3 hotMatter = vec3(1.0, 0.16, 0.025);
        vec3 stellarMatter = mix(coldMatter, hotMatter, innerHeat);
        stellarMatter = mix(stellarMatter, vec3(1.0, 0.78, 0.32),
                pow(matter, 3.5) * innerHeat);
        float diskAlpha = diskMask * (0.38 + matter * 0.86)
                * doppler * Intensity;
        result = mix(result, stellarMatter * (1.15 + matter * 1.45),
                clamp(diskAlpha, 0.0, 0.94));

        float photonRing = 1.0 - smoothstep(0.0, 0.032, abs(radial - 0.245));
        result += vec3(1.0, 0.34, 0.08) * photonRing * Intensity * 0.88;
        float eventHorizon = (1.0 - smoothstep(0.15, 0.225, radial))
                * clamp(Intensity * 1.4, 0.0, 1.0);
        result = mix(result, vec3(0.0), eventHorizon);
    }

    float collapse = clamp(CollapseProgress, 0.0, 1.0);
    if (Visible > 0.001 && collapse > 0.0001) {
        float easedCollapse = collapse * collapse * (3.0 - 2.0 * collapse);
        float shockRadius = mix(0.18, 2.25, easedCollapse);
        float shockWidth = mix(0.085, 0.035, collapse);
        float shock = 1.0 - smoothstep(0.0, shockWidth,
                abs(radial - shockRadius));
        vec2 chromaOffset = radialDirection * safeRadius
                * shock * (1.0 - collapse) * 0.055;
        vec2 redUv = clamp(texCoord + chromaOffset, vec2(0.001), vec2(0.999));
        vec2 blueUv = clamp(texCoord - chromaOffset, vec2(0.001), vec2(0.999));
        vec3 splitScene = vec3(
                texture(DiffuseSampler, redUv).r,
                result.g,
                texture(DiffuseSampler, blueUv).b);
        result = mix(result, splitScene, shock * 0.72);
        result += vec3(1.0, 0.31, 0.07) * shock * (1.0 - collapse * 0.45) * 2.1;
        float ignitionFlash = pow(1.0 - collapse, 7.0);
        result = mix(result, vec3(1.0, 0.84, 0.58), ignitionFlash * 0.58);
    }

    result = mix(result, vec3(0.0), clamp(Engulf, 0.0, 1.0));
    fragColor = vec4(result, scene.a);
}
