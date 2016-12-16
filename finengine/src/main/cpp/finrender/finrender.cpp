//
// Created by iFinVer on 2016/12/6.
//
#include "finrender.h"
#include "FinRenderHolder.h"
#include "../log.h"
#include <GLES2/gl2.h>
#include <EGL/egl.h>
#include "../glslutils.h"
#include <android/native_window_jni.h>
#include <GLES2/gl2ext.h>

#define LOG_TAG "FinRender"

const GLfloat VERTICES_RENDER[] =
        {
                -1.0f, 1.0f,//pos0
                0.0f, 0.0f,//tex0
                -1.0f, -1.0f,//pos1
                0.0f, 1.0f,//tex1
                1.0f, -1.0f,//pos2
                1.0f, 1.0f,//tex2
                1.0f, 1.0f,//pos3
                1.0f, 0.0f,//tex3
        };
const char *vertexShader_render =
        "attribute vec4 aPosition;                          \n"
                "attribute vec2 aTexCoord;                          \n"
                "varying vec2 vTexCoord;                            \n"
                "void main(){                                       \n"
                "   vTexCoord = aTexCoord;                          \n"
                "   gl_Position = aPosition;                        \n"
                "}                                                  \n";
const char *fragmentShader_render =
        "#extension GL_OES_EGL_image_external : require     \n"
                "precision mediump float;                           \n"
                "varying vec2 vTexCoord;                            \n"
                "uniform samplerExternalOES sTexture;               \n"
                "void main(){                                       \n"
                "    gl_FragColor = texture2D(sTexture, vTexCoord); \n"
                "}                                                  \n";

JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRender_nativeCreate(JNIEnv *env, jobject, jobject output) {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return 0;
    }
    EGLint majorVer, minVer;
    if (!eglInitialize(display, &majorVer, &minVer)) {
        checkGlError("eglInitialize");
        LOGE("eglInitialize");
        return 0;
    }
    // EGL attributes
    const EGLint confAttr[] =
            {
                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// very important!
                    EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
                    EGL_RED_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_BLUE_SIZE, 8,
                    EGL_ALPHA_SIZE, 8,
                    EGL_DEPTH_SIZE, 0,
                    EGL_STENCIL_SIZE, 0,
                    EGL_NONE
            };

    EGLConfig config;
    EGLint numConfigs;
    if (!eglChooseConfig(display, confAttr, &config, 1, &numConfigs)) {
        checkGlError("eglChooseConfig");
        return 0;
    }
    ANativeWindow *surface = ANativeWindow_fromSurface(env, output);
    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
    if (surface == EGL_NO_SURFACE) {
        checkGlError("eglCreateWindowSurface");
        return 0;
    }
    EGLint attrib_list[] =
            {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return 0;
    }

    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return 0;
    }

    //context create success,now create program
    GLuint program = createProgram(vertexShader_render, fragmentShader_render);
//    delete shader;
    if (program == 0) {
        return 0;
    }

    glUseProgram(program);

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    //success
    FinRenderHolder *renderHolder = new FinRenderHolder();

    renderHolder->eglDisplay = display;
    renderHolder->eglContext = eglContext;
    renderHolder->eglSurface = eglSurface;
    renderHolder->program = program;

    renderHolder->posAttrVertices = (GLuint) glGetAttribLocation(program, "aPosition");
    renderHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(program, "aTexCoord");
    renderHolder->posUniTextureS = (GLuint) glGetUniformLocation(program, "sTexture");

    //tex
    GLuint textures;
    glGenTextures(1, &textures);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    renderHolder->inputTex = textures;

    GLuint vertexBuffer;
    glGenBuffers(1,&vertexBuffer);
    glBindBuffer(GL_ARRAY_BUFFER,vertexBuffer);
    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES_RENDER),VERTICES_RENDER,GL_STATIC_DRAW);
    renderHolder->vertexBuffer = vertexBuffer;
    renderHolder->vertexStride = 4 * sizeof(GLfloat);
    renderHolder->texOffset = 2* sizeof(GLfloat);

    //输入纹理的方法
    jclass jcSurfaceTexture = env->FindClass("android/graphics/SurfaceTexture");
    renderHolder->midAttachToGlContext = env->GetMethodID(jcSurfaceTexture, "attachToGLContext", "(I)V");
    renderHolder->midDetachFromGLContext = env->GetMethodID(jcSurfaceTexture, "detachFromGLContext", "()V");
    renderHolder->midUpdateTexImage = env->GetMethodID(jcSurfaceTexture, "updateTexImage", "()V");

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);

    glActiveTexture(GL_TEXTURE0);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    return (jlong) renderHolder;
}

void renderFrame(FinRenderHolder *pHolder) {
    //输入顶点
    glEnableVertexAttribArray(pHolder->posAttrVertices);
    glVertexAttribPointer(pHolder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, pHolder->vertexStride, 0);

    //输入纹理坐标
    glEnableVertexAttribArray(pHolder->posAttrTexCoords);
    glVertexAttribPointer(pHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, pHolder->vertexStride, (const void *) (pHolder->texOffset));

    glBindTexture(GL_TEXTURE_EXTERNAL_OES, pHolder->inputTex);
    glUniform1i(pHolder->posUniTextureS, 0);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(pHolder->posAttrVertices);
    glDisableVertexAttribArray(pHolder->posAttrTexCoords);

//    glFinish();
    eglSwapBuffers(pHolder->eglDisplay, pHolder->eglSurface);
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRender_nativeRenderOut(JNIEnv *env, jobject, jlong engine, jobject input) {
    FinRenderHolder *renderHolder = (FinRenderHolder *) engine;
//    env->CallVoidMethod(input, recorderHolder->midAttachToGlContext, recorderHolder->inputTex);
    env->CallVoidMethod(input, renderHolder->midUpdateTexImage);
    renderFrame(renderHolder);
//    env->CallVoidMethod(input, recorderHolder->midDetachFromGLContext);
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRender_nativeRelease(JNIEnv *env, jobject instance, jlong engine) {
    FinRenderHolder *pHolder = (FinRenderHolder *) engine;
    glDeleteTextures(1, &pHolder->inputTex);
    glDeleteProgram(pHolder->program);
    glDeleteBuffers(1,&pHolder->vertexBuffer);
    eglDestroySurface(pHolder->eglDisplay,pHolder->eglSurface);
    eglDestroyContext(pHolder->eglDisplay,pHolder->eglContext);
    delete (pHolder);
}

JNIEXPORT jint JNICALL Java_com_ifinver_finengine_FinRender_nativeGetInputTex(JNIEnv *env, jobject instance, jlong engine){
    FinRenderHolder *pHolder = (FinRenderHolder *) engine;
    return pHolder->inputTex;
}

JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRender_nativeGetEglContext(JNIEnv *env, jobject instance, jlong engine){
    FinRenderHolder *pHolder = (FinRenderHolder *) engine;
    return (jlong)pHolder->eglContext;
}