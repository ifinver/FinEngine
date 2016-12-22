#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 vTexCoord;
uniform sampler2D yTexture;
uniform sampler2D uvTexture;

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
    if (mirrorCoord.x<0.5){
        mirrorCoord.x = 1.0 - mirrorCoord.x;
    }
    gl_FragColor = getBaseColor(mirrorCoord);
}