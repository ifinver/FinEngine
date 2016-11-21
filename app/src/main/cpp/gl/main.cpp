#include "main.h"
#include "shaders.h"
#include <android/native_window_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_myopengles_GLNative_createGLContext(JNIEnv *env, jclass, jobject jSurface) {
    GLContextHolder *pHolder = newGLContext(env, jSurface);
    if(pHolder == NULL){
        return 0;
    }
    return (jlong) pHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_releaseGLContext(JNIEnv *, jclass , jlong nativeContext) {
    releaseGLContext((GLContextHolder*)nativeContext);
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_renderOnContext(JNIEnv *env, jclass, jlong nativeGlContext, jbyteArray data_, jint frameWidth,
                                                     jint frameHeight, jint imageFormat) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    switch (imageFormat){
        case 0x11://ImageFormat.NV21
            renderFrame((GLContextHolder*)nativeGlContext,data,frameWidth,frameHeight);
            break;
        default:
            LOGE("不支持的视频编码格式！");
            break;
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}
//.........................................................................................................................
using namespace std;
#define  LOG_TAG    "GLNativeLib"

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height){
    glUseProgram(holder->program);
    glClear(GL_COLOR_BUFFER_BIT);



    eglSwapBuffers(holder->eglDisplay,holder->eglSurface);
}

//释放指定上下文
void releaseGLContext(GLContextHolder *holder) {
    eglDestroySurface(holder->eglDisplay,holder->eglSurface);
    eglDestroyContext(holder->eglDisplay,holder->eglContext);
}

//创建一个新的绘制上下文
GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface) {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    checkGlError("eglGetDisplay");
//    if (display == EGL_NO_DISPLAY) {
//        checkGlError("eglGetDisplay");
//        return NULL;
//    }
//    EGLint majorVer, minVer;
//    if (!eglInitialize(display, &majorVer, &minVer)) {
//        checkGlError("eglInitialize");
//        LOGE("eglInitialize");
//        return NULL;
//    }
//    checkGlError("eglGetDisplay");
//    LOGI("majorVer=%d,minVer=%d", majorVer, minVer);
//    // EGL attributes
//    const EGLint confAttr[] =
//            {
//                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// very important!
//                    EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
//                    EGL_RED_SIZE, 8,
//                    EGL_GREEN_SIZE, 8,
//                    EGL_BLUE_SIZE, 8,
//                    EGL_ALPHA_SIZE, 8,// if you need the alpha channel
//                    EGL_DEPTH_SIZE, 0,// if you need the depth buffer
//                    EGL_STENCIL_SIZE, 0,
//                    EGL_NONE
//            };
//
//    EGLConfig config;
//    EGLint numConfigs;
//    if (!eglChooseConfig(display, confAttr, &config, 1, &numConfigs)) {
//        checkGlError("eglChooseConfig");
//        return NULL;
//    }
//    checkGlError("eglGetDisplay");
//    ANativeWindow *surface = ANativeWindow_fromSurface(env, jSurface);
//    checkGlError("ANativeWindow_fromSurface");
//    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
//    if (surface == EGL_NO_SURFACE) {
//        checkGlError("eglCreateWindowSurface");
//        return NULL;
//    }
//    EGLint attrib_list[] =
//            {
//                    EGL_CONTEXT_CLIENT_VERSION, 2,
//                    EGL_NONE
//            };
//    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
//    if(eglContext == EGL_NO_CONTEXT){
//        checkGlError("eglCreateContext");
//        return NULL;
//    }
//
//    if (!eglMakeCurrent(display, surface, surface, eglContext)) {
//        checkGlError("eglMakeCurrent");
//        return NULL;
//    }
//
//    //context create success,now create program
//    ShaderYuv shaderYuv;
//    GLuint programYUV = createProgram(shaderYuv.vertexShader, shaderYuv.fragmentShader);
//    if(programYUV == 0){
//        return NULL;
//    }
//
//    //success
//    GLContextHolder* gl_holder = new GLContextHolder();
//    gl_holder->eglDisplay = display;
//    gl_holder->eglContext = eglContext;
//    gl_holder->eglSurface = eglSurface;
//    gl_holder->program = programYUV;
//
//    return gl_holder;
    return NULL;
}