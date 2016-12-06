//
// Created by iFinVer on 2016/12/6.
//
#include "finrecorder.h"
#include "FinRecorderHolder.h"
#include <GLES2/gl2.h>
#include <EGL/egl.h>
#include "utils.h"
#include "log.h"
#include <android/native_window_jni.h>
#include <GLES2/gl2ext.h>

const GLfloat VERTICES_COORD[] =
        {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
        };
const GLfloat TEXTURE_COORD[] =
        {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };
const char *vertexShader =
        "attribute vec4 aPosition;                          \n"
                "attribute vec2 aTexCoord;                          \n"
                "varying vec2 vTexCoord;                            \n"
                "void main(){                                       \n"
                "   vTexCoord = aTexCoord;                          \n"
                "   gl_Position = aPosition;                        \n"
                "}                                                  \n";
const char *fragmentShader =
        "#extension GL_OES_EGL_image_external : require     \n"
                "precision mediump float;                           \n"
                "varying vec2 vTexCoord;                            \n"
                "uniform samplerExternalOES sTexture;               \n"
                "void main(){                                       \n"
                "    gl_FragColor = texture2D(sTexture, vTexCoord); \n"
                "}                                                  \n";

JNIEXPORT jlong JNICALL Java_com_ifinver_finrender_FinRender_nativeCreate(JNIEnv *env, jobject, jobject output) {
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
    FinRecorderHolder *recorderHolder = new FinRecorderHolder();

    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return 0;
    }

    //context create success,now create program
    GLuint program = createProgram(vertexShader, fragmentShader);
//    delete shader;
    if (program == 0) {
        return 0;
    }

    glUseProgram(program);

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    //success
    recorderHolder->eglDisplay = display;
    recorderHolder->eglContext = eglContext;
    recorderHolder->eglSurface = eglSurface;
    recorderHolder->program = program;

    recorderHolder->posAttrVertices = (GLuint) glGetAttribLocation(program, "aPosition");
    recorderHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(program, "aTexCoord");
    recorderHolder->posUniTextureS = (GLuint) glGetUniformLocation(program, "sTexture");

    //tex
    GLuint textures;
    glGenTextures(1, &textures);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    recorderHolder->inputTex = textures;

    //输入纹理的方法
    jclass jcSurfaceTexture = env->FindClass("android/graphics/SurfaceTexture");
    recorderHolder->midAttachToGlContext = env->GetMethodID(jcSurfaceTexture, "attachToGLContext", "(I)V");
    recorderHolder->midDetachFromGLContext = env->GetMethodID(jcSurfaceTexture, "detachFromGLContext", "()V");
    recorderHolder->midUpdateTexImage = env->GetMethodID(jcSurfaceTexture, "updateTexImage", "()V");

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    return (jlong) recorderHolder;
}

void renderFrame(FinRecorderHolder *pHolder) {
    //输入顶点
    glEnableVertexAttribArray(pHolder->posAttrVertices);
    glVertexAttribPointer(pHolder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, VERTICES_COORD);

    //输入纹理坐标
    glEnableVertexAttribArray(pHolder->posAttrTexCoords);
    glVertexAttribPointer(pHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, pHolder->inputTex);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glUniform1i(pHolder->posUniTextureS, 0);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(pHolder->posAttrVertices);
    glDisableVertexAttribArray(pHolder->posAttrTexCoords);

    glFinish();
    eglSwapBuffers(pHolder->eglDisplay, pHolder->eglSurface);
}

JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_nativeRenderOut(JNIEnv *env, jobject, jlong engine, jobject input) {
    FinRecorderHolder *recorderHolder = (FinRecorderHolder *) engine;
//    env->CallVoidMethod(input, recorderHolder->midAttachToGlContext, recorderHolder->inputTex);
    env->CallVoidMethod(input, recorderHolder->midUpdateTexImage);
    renderFrame(recorderHolder);
//    env->CallVoidMethod(input, recorderHolder->midDetachFromGLContext);
}

JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_nativeRelease(JNIEnv *env, jobject instance, jlong engine) {
    FinRecorderHolder *pHolder = (FinRecorderHolder *) engine;
    glDeleteTextures(1, &pHolder->inputTex);
    glDeleteProgram(pHolder->program);
    delete (pHolder);
}

JNIEXPORT jint JNICALL Java_com_ifinver_finrender_FinRender_getInputTex(JNIEnv *env, jobject instance, jlong engine){
    FinRecorderHolder *pHolder = (FinRecorderHolder *) engine;
    return pHolder->inputTex;
}

JNIEXPORT jlong JNICALL Java_com_ifinver_finrender_FinRender_getEglContext(JNIEnv *env, jobject instance, jlong engine){
    FinRecorderHolder *pHolder = (FinRecorderHolder *) engine;
    return (jlong)pHolder->eglContext;
}