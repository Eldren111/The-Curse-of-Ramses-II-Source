#version 150

uniform float UiTime;
uniform float UiPulse;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 uiPosition;

out vec4 fragColor;

void main() {
    float sweep = 0.5 + 0.5 * sin(uiPosition.x * 0.055 + uiPosition.y * 0.019 - UiTime * 2.4);
    float edgeLight = smoothstep(0.78, 1.0, sweep) * (0.035 + UiPulse * 0.045);
    float pulse = 1.0 + UiPulse * 0.035 * sin(UiTime * 3.0 + uiPosition.y * 0.03);
    vec4 outputColor = vertexColor * ColorModulator;
    outputColor.rgb = clamp(outputColor.rgb * pulse + vec3(0.95, 0.72, 0.30) * edgeLight, 0.0, 1.0);
    fragColor = vec4(outputColor.rgb, 1.0);
}
