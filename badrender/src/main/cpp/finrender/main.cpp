#include "main.h"
#include "utils.h"
#include <math.h>
#include <GLES2/gl2ext.h>
#include <android/native_window_jni.h>

GLContextHolder *badHolder = NULL;
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


JNIEXPORT jint JNICALL
Java_com_ifinver_badrender_BadRender_createGLContext(JNIEnv *env, jclass, jobject jSurface) {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return NULL;
    }
    EGLint majorVer, minVer;
    if (!eglInitialize(display, &majorVer, &minVer)) {
        checkGlError("eglInitialize");
        LOGE("eglInitialize");
        return NULL;
    }
//    LOGI("majorVer=%d,minVer=%d", majorVer, minVer);
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
        return NULL;
    }
    ANativeWindow *surface = ANativeWindow_fromSurface(env, jSurface);
    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
    if (surface == EGL_NO_SURFACE) {
        checkGlError("eglCreateWindowSurface");
        return NULL;
    }
    EGLint attrib_list[] =
            {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return NULL;
    }


    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return NULL;
    }

    //context create success,now create program
    ShaderBase shader = ShaderNV21();
    GLuint programYUV = createProgram(shader.vertexShader, shader.fragmentShader);
//    delete shader;
    if (programYUV == 0) {
        return NULL;
    }

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    badHolder = new GLContextHolder();
    //success
    badHolder->eglDisplay = display;
    badHolder->eglContext = eglContext;
    badHolder->eglSurface = eglSurface;
    badHolder->program = programYUV;

    badHolder->posAttrVertices = (GLuint) glGetAttribLocation(programYUV, "aPosition");
    badHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(programYUV, "aTexCoord");
    badHolder->posUniTextureY = (GLuint) glGetUniformLocation(programYUV, "yTexture");
    badHolder->posUniTextureUV = (GLuint) glGetUniformLocation(programYUV, "uvTexture");

    GLuint texture;
    glGenTextures(1, &texture);
    badHolder->inputTexture = texture;

    //buff

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    return badHolder->inputTexture;
}

JNIEXPORT void JNICALL
Java_com_ifinver_badrender_BadRender_release(JNIEnv *, jclass) {
    releaseGLContext();
}

JNIEXPORT void JNICALL
Java_com_ifinver_badrender_BadRender_render(JNIEnv *env, jclass, jlong nativeGlContext, jbyteArray data_, jint frameWidth, jint frameHeight,
                                            jint degree, jboolean mirror) {
    jbyte *data = env->GetByteArrayElements(data_, 0);

    renderFrame((GLContextHolder *) nativeGlContext, data, frameWidth, frameHeight, degree, mirror);

    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
}
//.........................................................................................................................

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height, jint degree, jboolean mirror) {
    glUseProgram(holder->program);
    //输入顶点
    glEnableVertexAttribArray(holder->posAttrVertices);
    glVertexAttribPointer(holder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, VERTICES_COORD);

    //输入纹理坐标，处理旋转和镜像
//    degree = 0;
    glEnableVertexAttribArray(holder->posAttrTexCoords);
    {
        degree %= 360;
        if (degree < 0) degree += 360;
        int idx;
        if (mirror) {
            idx = degree / 90 * 2;
            glVertexAttribPointer(holder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD_MIRROR + idx);
        } else {
            degree = 360 - degree;
            idx = degree / 90 * 2;
            glVertexAttribPointer(holder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD_NOR + idx);
        }
    }


    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(holder->posAttrVertices);
    glDisableVertexAttribArray(holder->posAttrTexCoords);

    glFinish();
    eglSwapBuffers(holder->eglDisplay, holder->eglSurface);
}

//释放指定上下文
void releaseGLContext() {
    glDeleteTextures(1, &badHolder->inputTexture);
    glDeleteProgram(badHolder->program);
    eglDestroySurface(badHolder->eglDisplay, badHolder->eglSurface);
    eglDestroyContext(badHolder->eglDisplay, badHolder->eglContext);
    delete (badHolder);
}