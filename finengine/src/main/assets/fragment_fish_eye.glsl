#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 vTexCoord;
uniform sampler2D yTexture;
uniform sampler2D uvTexture;
uniform highp int uRotation;
uniform int mirror;

const float PI = 3.1415926535;
const float aperture = 180.0;
const float apertureHalf = 0.5 * aperture * (PI / 180.0);
const float maxFactor = sin(apertureHalf);

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

vec2 mirrorUV(){
    vec2 mirrorCoord = vTexCoord;
    if(mirror == 1){
        if(uRotation == 1 || uRotation == 3){
            mirrorCoord.y = 1.0 - mirrorCoord.y;
        }else{
            mirrorCoord.x = 1.0 - mirrorCoord.x;
        }
    }
    return mirrorCoord;
}

void main(){
    vec2 mirrorCoord = mirrorUV();
    vec2 pos = 2.0 * mirrorCoord.st - 1.0;
    float l = length(pos);

    if (l > 1.0) {
        gl_FragColor = vec4(0, 0, 0, 1);
    } else {
        float x = maxFactor * pos.x;
        float y = maxFactor * pos.y;
        float n = length(vec2(x, y));
        float z = sqrt(1.0 - n * n);
        float r = atan(n, z) / PI;
        float phi = atan(y, x);
        float u = r * cos(phi) + 0.5;
        float v = r * sin(phi) + 0.5;

        gl_FragColor = getBaseColor(vec2(u, v));
    }
}