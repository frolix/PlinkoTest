#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 dir;
uniform float resolution;
uniform float radius;

void main() {
    vec4 sum = vec4(0.0);
    float blur = radius / resolution;

    for (float i = -10.0; i <= 10.0; i++) {
        sum += texture2D(u_texture, v_texCoords + dir * i * blur) * (1.0 - abs(i) / 10.0);
    }
    gl_FragColor = sum / 11.0;

}
