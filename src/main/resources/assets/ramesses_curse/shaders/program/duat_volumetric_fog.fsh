#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InvProjMat;
uniform float Time;
uniform float Intensity;
uniform float CameraHeight;
uniform vec2 StormOffset;
uniform float StormRadius;
uniform vec2 Wind;
uniform vec4 CameraRotation;

in vec2 texCoord;
out vec4 fragColor;

float hash31(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 cell = floor(p);
    vec3 local = fract(p);
    local = local * local * (3.0 - 2.0 * local);
    float n000 = hash31(cell + vec3(0.0, 0.0, 0.0));
    float n100 = hash31(cell + vec3(1.0, 0.0, 0.0));
    float n010 = hash31(cell + vec3(0.0, 1.0, 0.0));
    float n110 = hash31(cell + vec3(1.0, 1.0, 0.0));
    float n001 = hash31(cell + vec3(0.0, 0.0, 1.0));
    float n101 = hash31(cell + vec3(1.0, 0.0, 1.0));
    float n011 = hash31(cell + vec3(0.0, 1.0, 1.0));
    float n111 = hash31(cell + vec3(1.0, 1.0, 1.0));
    float nx00 = mix(n000, n100, local.x);
    float nx10 = mix(n010, n110, local.x);
    float nx01 = mix(n001, n101, local.x);
    float nx11 = mix(n011, n111, local.x);
    return mix(mix(nx00, nx10, local.y), mix(nx01, nx11, local.y), local.z);
}

vec3 rotateByQuaternion(vec3 value, vec4 quaternion) {
    return value + 2.0 * cross(quaternion.xyz, cross(quaternion.xyz, value) + quaternion.w * value);
}

void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = InvProjMat * clip;
    view.xyz /= max(abs(view.w), 0.0001);

    float sceneDistance = depth >= 0.9999 ? 96.0 : min(length(view.xyz), 96.0);
    vec3 rayDirection = rotateByQuaternion(normalize(view.xyz), CameraRotation);
    float stepLength = sceneDistance / 10.0;
    float accumulatedDensity = 0.0;
    vec2 movingWind = Wind * Time * 18.0;

    for (int index = 0; index < 10; index++) {
        float distanceAlongRay = (float(index) + 0.5) * stepLength;
        vec3 samplePosition = rayDirection * distanceAlongRay;
        float radialDistance = length(samplePosition.xz - StormOffset);
        float radialMask = 1.0 - smoothstep(StormRadius * 0.35, StormRadius * 1.15, radialDistance);
        float worldHeight = CameraHeight + samplePosition.y;
        float heightMask = smoothstep(-16.0, 2.0, worldHeight) * (1.0 - smoothstep(150.0, 220.0, worldHeight));
        vec3 noisePosition = vec3(
                (samplePosition.x + movingWind.x) * 0.055,
                worldHeight * 0.075,
                (samplePosition.z + movingWind.y) * 0.055);
        float coarseNoise = noise3(noisePosition);
        float fineNoise = noise3(noisePosition * 2.13 + vec3(7.3, 2.1, 5.7));
        float bands = smoothstep(0.30, 0.86, coarseNoise * 0.72 + fineNoise * 0.28);
        accumulatedDensity += bands * radialMask * heightMask;
    }

    float opticalDepth = accumulatedDensity * stepLength * 0.075 * Intensity;
    float fogAmount = clamp(1.0 - exp(-opticalDepth), 0.0, 0.92 * Intensity);
    vec3 fogColor = vec3(0.66, 0.48, 0.24);
    vec3 result = mix(scene.rgb, fogColor, fogAmount);
    fragColor = vec4(result, scene.a);
}
