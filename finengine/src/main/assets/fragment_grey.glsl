#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 vTexCoord;
uniform sampler2D yTexture;
uniform sampler2D uvTexture;

const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);

vec4 getBaseColor(in vec2 coord){
    float r,g,b,y,u,v;
    y = texture2D(yTexture,coord).r;
    vec4 uvColor = texture2D(uvTexture,coord);
    u = uvColor.a - 0.5;
    v = uvColor.r - 0.5;
    r = y + 1.13983*v;
    g = y - 0.39465*u - 0.58060*v;
    b = y + 2.03211*u;
    return vec4(r, g, b, 1.0);
}

void main(){
    vec4 color = getBaseColor(vTexCoord);
    float monoColor = dot(color.rgb,monoMultiplier);
    gl_FragColor = vec4(monoColor, monoColor, monoColor, 1.0);
}