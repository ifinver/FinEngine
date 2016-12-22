#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 vTexCoord;
uniform sampler2D yTexture;
uniform sampler2D uvTexture;
uniform highp int uRotation;
uniform int mirror;

//微调
const float sampleDist = 0.6;
const float sampleStrength = 4.0;

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
    vec4 color = getBaseColor(mirrorCoord);

    //指向屏幕中间的向量
    vec2 dir = 0.5 - mirrorCoord;
    //距离
    float dist = sqrt(dir.x*dir.x + dir.y*dir.y);
    //归一化为单位向量
    dir = dir/dist;
    //累加色素并平均
    vec4 sum = color;
    float pos = -0.08;//取样位置
    for (int i = 0; i < 5; i++){
        sum += getBaseColor(mirrorCoord + dir * pos * sampleDist );
        pos += 0.04;
    }
    sum *= 1.0/6.0;
    //计算模糊程度
    float t = dist * sampleStrength;
    t = clamp( t ,0.0,1.0); //防止越界
    //插值
    gl_FragColor = mix( color, sum, t );
}