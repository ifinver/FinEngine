//
// Created by iFinVer on 2016/12/7.
//
#include "finrecorder.h"
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "../utils.h"
#include "../log.h"
#include <android/native_window_jni.h>
#include "FinRecorderHolder.h"
#define LOG_TAG "FinRender"

const GLfloat VERTICES_RECORD[] =
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
const char *vertexShader_recorder =
        "attribute vec4 aPosition;                          \n"
                "attribute vec2 aTexCoord;                          \n"
                "varying vec2 vTexCoord;                            \n"
                "void main(){                                       \n"
                "   vTexCoord = aTexCoord;                          \n"
                "   gl_Position = aPosition;                        \n"
                "}                                                  \n";
const char *fragmentShader_recorder =
        "#extension GL_OES_EGL_image_external : require     \n"
                "precision mediump float;                           \n"
                "varying vec2 vTexCoord;                            \n"
                "uniform samplerExternalOES sTexture;               \n"
                "void main(){                                       \n"
                "    gl_FragColor = texture2D(sTexture, vTexCoord); \n"
                "}                                                  \n";

JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRecorder_nativeCreate(JNIEnv *env, jobject instance, jlong sharedCtx, jobject output){
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
    EGLContext eglContext = eglCreateContext(display, config, (EGLContext) sharedCtx, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return 0;
    }
    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return 0;
    }

    //context create success,now create program
    GLuint program = createProgram(vertexShader_recorder, fragmentShader_recorder);
//    delete shader;
    if (program == 0) {
        return 0;
    }

    glUseProgram(program);

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    //success
    FinRenderHolder *recorderHolder = new FinRenderHolder();

    recorderHolder->eglDisplay = display;
    recorderHolder->eglContext = eglContext;
    recorderHolder->eglSurface = eglSurface;
    recorderHolder->program = program;

    recorderHolder->posAttrVertices = (GLuint) glGetAttribLocation(program, "aPosition");
    recorderHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(program, "aTexCoord");
    recorderHolder->posUniTextureS = (GLuint) glGetUniformLocation(program, "sTexture");

    GLuint vertexBuffer;
    glGenBuffers(1,&vertexBuffer);
    glBindBuffer(GL_ARRAY_BUFFER,vertexBuffer);
    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES_RECORD),VERTICES_RECORD,GL_STATIC_DRAW);
    recorderHolder->vertexBuffer = vertexBuffer;
    recorderHolder->vertexStride = 4 * sizeof(GLfloat);
    recorderHolder->texOffset = 2* sizeof(GLfloat);

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

    return (jlong) recorderHolder;
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRecorder_nativeProcess(JNIEnv *env, jobject instance, jlong recorder, jint inputTex){
    FinRenderHolder *recorderHolder = (FinRenderHolder *) recorder;
    if (!eglMakeCurrent(recorderHolder->eglDisplay, recorderHolder->eglSurface, recorderHolder->eglSurface, recorderHolder->eglContext)) {
        checkGlError("eglMakeCurrent");
        LOGE("eglMakeCurrent failed!");
        return ;
    }
    //输入顶点
    glEnableVertexAttribArray(recorderHolder->posAttrVertices);
    glVertexAttribPointer(recorderHolder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, recorderHolder->vertexStride, 0);

    //输入纹理坐标
    glEnableVertexAttribArray(recorderHolder->posAttrTexCoords);
    glVertexAttribPointer(recorderHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, recorderHolder->vertexStride,(const void *) (recorderHolder->texOffset));

    glBindTexture(GL_TEXTURE_EXTERNAL_OES, (GLuint) inputTex);
    glUniform1i(recorderHolder->posUniTextureS, 0);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(recorderHolder->posAttrVertices);
    glDisableVertexAttribArray(recorderHolder->posAttrTexCoords);

//    glFinish();
    eglSwapBuffers(recorderHolder->eglDisplay,recorderHolder->eglSurface);
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRecorder_nativeRelease(JNIEnv *env, jobject instance, jlong recorder){
    FinRenderHolder *recorderHolder = (FinRenderHolder *) recorder;
    glDeleteProgram(recorderHolder->program);
    glDeleteBuffers(1,&recorderHolder->vertexBuffer);
    eglDestroySurface(recorderHolder->eglDisplay,recorderHolder->eglSurface);
    eglDestroyContext(recorderHolder->eglDisplay,recorderHolder->eglContext);
    delete (recorderHolder);
}

JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRecorder_nativeFetchGLCtx(JNIEnv *env, jobject instance) {
    EGLContext ctx = eglGetCurrentContext();
    if(ctx == EGL_NO_CONTEXT){
        return 0;
    }
    return (jlong) ctx;
}