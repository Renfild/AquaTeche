#version 150

uniform sampler2D DiffuseSampler;
uniform float Radius;
uniform vec3 Tint;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    float radius = max(Radius, 1.0);
    vec4 acc = vec4(0.0);
    float weightSum = 0.0;
    float sigma = radius * 0.4;
    float twoSigmaSq = 2.0 * sigma * sigma;

    for (float r = -radius; r <= radius; r += 1.0) {
        float w = exp(-(r * r) / twoSigmaSq);
        acc += texture(DiffuseSampler, texCoord + sampleStep * r) * w;
        weightSum += w;
    }

    vec3 rgb = acc.rgb / max(weightSum, 0.0001);
    rgb = mix(rgb, rgb * Tint, 0.22);
    fragColor = vec4(rgb, 1.0);
}
