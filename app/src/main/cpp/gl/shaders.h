//
// Created by iFinVer on 2016/11/22.
//

#ifndef MYOPENGLES_GL_SHADERS_H
#define MYOPENGLES_GL_SHADERS_H

#include <GLES2/gl2.h>

const GLfloat VERTICES_BASE[] =
        {
                -1.0f, 1.0f,   // Position 0
                0.0f, 0.0f,   // TexCoord 0
                -1.0f, -1.0f,  // Position 1
                0.0f, 1.0f,   // TexCoord 1
                1.0f, -1.0f,  // Position 2
                1.0f, 1.0f,   // TexCoord 2
                1.0f, 1.0f,   // Position 3
                1.0f, 0.0f    // TexCoord 3
        };
class ShaderBase{
public:
    const char *vertexShader;
    const char *fragmentShader;

    ShaderBase(){
        vertexShader =
                        "attribute vec2 aPosition;                          \n"
                        "attribute vec2 aTexCoord;                          \n"
                        "varying vec2 vTexCoord;                            \n"
                        "attribute vec4 aRotVector;                         \n"
                        "void main(){                                       \n"
                        "   mat2 rotMat = mat2(aRotVector.x,aRotVector.y,aRotVector.z,aRotVector.w);\n"
                        "   gl_Position = vec4(aPosition * rotMat,1,1);     \n"
                        "   vTexCoord = aTexCoord;                          \n"
                        "}                                                  \n";
    }

//    ~ShaderBase(){
//        delete vertexShader;
//        delete fragmentShader;
//    }
};
class ShaderYuv : public ShaderBase {
public:
    ShaderYuv() {
        fragmentShader =
                        "#extension GL_OES_EGL_image_external : require     \n"
                        "precision mediump float;                           \n"
                        "varying vec2 vTexCoord;                            \n"
                        "uniform sampler2D yTexture;                        \n"
                        "uniform sampler2D uvTexture;                       \n"
                        "void main(){                                       \n"
                        "   float r,g,b,y,u,v;                              \n"
                        "   y = texture2D(yTexture,vTexCoord).r;            \n"
                        "   vec4 uvColor = texture2D(uvTexture,vTexCoord);  \n"
                        "   u = uvColor.a - 0.5;                            \n"
                        "   v = uvColor.r - 0.5;                            \n"
                        "   r = y + 1.13983*v;                              \n"
                        "   g = y - 0.39465*u - 0.58060*v;                  \n"
                        "   b = y + 2.03211*u;                              \n"
                        "   gl_FragColor = vec4(r, g, b, 1.0);              \n"
                        "}                                                  \n";

    }
};
class ShaderCyan : public ShaderBase{
public:
    ShaderCyan(){
        fragmentShader =
                        "#extension GL_OES_EGL_image_external : require     \n"
                        "precision mediump float;                           \n"
                        "varying vec2 vTexCoord;                            \n"
                        "uniform sampler2D yTexture;                        \n"
                        "uniform sampler2D uvTexture;                       \n"
                        "const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);\n"
                        "const vec3 cyanFactor = vec3(0.8, 1.2, 1.2);       \n"
                        "                                                   \n"
                        "vec4 getBaseColor(in vec2 coord){                  \n"
                        "   float r,g,b,y,u,v;                              \n"
                        "   y = texture2D(yTexture,coord).r;                \n"
                        "   vec4 uvColor = texture2D(uvTexture,coord);      \n"
                        "   u = uvColor.a - 0.5;                            \n"
                        "   v = uvColor.r - 0.5;                            \n"
                        "   r = y + 1.13983*v;                              \n"
                        "   g = y - 0.39465*u - 0.58060*v;                  \n"
                        "   b = y + 2.03211*u;                              \n"
                        "   return vec4(r, g, b, 1.0);                      \n"
                        "}                                                  \n"
                        "                                                   \n"
                        "void main(){                                       \n"
                        "   vec4 color = getBaseColor(vTexCoord);           \n"
                        "   float monoColor = dot(color.rgb,monoMultiplier);\n"
                        "   gl_FragColor = vec4(clamp(vec3(monoColor, monoColor, monoColor)*cyanFactor, 0.0, 1.0), 1.0);\n"
                        "}                                                  \n";
    }
};
class ShaderFishEye : public ShaderBase{
public:
    ShaderFishEye(){
        fragmentShader =
                        "#extension GL_OES_EGL_image_external : require     \n"
                        "precision mediump float;                           \n"
                        "varying vec2 vTexCoord;                            \n"
                        "uniform sampler2D yTexture;                        \n"
                        "uniform sampler2D uvTexture;                       \n"
                        "const float PI = 3.1415926535;                     \n"
                        "const float aperture = 180.0;                      \n"
                        "const float apertureHalf = 0.5 * aperture * (PI / 180.0);\n"
                        "const float maxFactor = sin(apertureHalf);         \n"
                        "                                                   \n"
                        "vec4 getBaseColor(in vec2 coord){                  \n"
                        "   float r,g,b,y,u,v;                              \n"
                        "   y = texture2D(yTexture,coord).r;                \n"
                        "   vec4 uvColor = texture2D(uvTexture,coord);      \n"
                        "   u = uvColor.a - 0.5;                            \n"
                        "   v = uvColor.r - 0.5;                            \n"
                        "   r = y + 1.13983*v;                              \n"
                        "   g = y - 0.39465*u - 0.58060*v;                  \n"
                        "   b = y + 2.03211*u;                              \n"
                        "   return vec4(r, g, b, 1.0);                      \n"
                        "}                                                  \n"
                        "                                                   \n"
                        "void main() {                                      \n"
                        "   vec2 pos = 2.0 * vTexCoord.st - 1.0;            \n"
                        "   float l = length(pos);                          \n"
                        "   if (l > 1.0) {                                  \n"
                        "       gl_FragColor = vec4(0, 0, 0, 1);            \n"
                        "   }else {                                         \n"
                        "        float x = maxFactor * pos.x;               \n"
                        "        float y = maxFactor * pos.y;               \n"
                        "        float n = length(vec2(x, y));              \n"
                        "        float z = sqrt(1.0 - n * n);               \n"
                        "        float r = atan(n, z) / PI;                 \n"
                        "        float phi = atan(y, x);                    \n"
                        "        float u = r * cos(phi) + 0.5;              \n"
                        "        float v = r * sin(phi) + 0.5;              \n"
                        "                                                   \n"
                        "        gl_FragColor = getBaseColor(vec2(u, v));   \n"
                        "   }                                               \n"
                        "}                                                  \n";
    }
};
class ShaderGreyScale : public ShaderBase{
public:
    ShaderGreyScale(){
        fragmentShader =
                        "#extension GL_OES_EGL_image_external : require     \n"
                        "precision mediump float;                           \n"
                        "varying vec2 vTexCoord;                            \n"
                        "uniform sampler2D yTexture;                        \n"
                        "uniform sampler2D uvTexture;                       \n"
                        "                                                   \n"
                        "const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);\n"
                        "                                                   \n"
                        "vec4 getBaseColor(in vec2 coord){                  \n"
                        "   float r,g,b,y,u,v;                              \n"
                        "   y = texture2D(yTexture,coord).r;                \n"
                        "   vec4 uvColor = texture2D(uvTexture,coord);      \n"
                        "   u = uvColor.a - 0.5;                            \n"
                        "   v = uvColor.r - 0.5;                            \n"
                        "   r = y + 1.13983*v;                              \n"
                        "   g = y - 0.39465*u - 0.58060*v;                  \n"
                        "   b = y + 2.03211*u;                              \n"
                        "   return vec4(r, g, b, 1.0);                      \n"
                        "}                                                  \n"
                        "                                                   \n"
                        "void main() {                                      \n"
                        "   vec4 color = getBaseColor(vTexCoord);           \n"
                        "   float monoColor = dot(color.rgb,monoMultiplier);\n"
                        "   gl_FragColor = vec4(monoColor, monoColor, monoColor, 1.0);\n"
                        "}                                                  \n";
    }
};

#endif //MYOPENGLES_GL_SHADERS_H
