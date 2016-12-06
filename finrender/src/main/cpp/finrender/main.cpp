#include "main.h"
#include "utils.h"
#include <math.h>
#include <GLES2/gl2ext.h>
#include <android/native_window_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finrender_FinRender_createGLContext(JNIEnv *env, jclass, jobject jSurface, jboolean isSurfaceThreadExclusive,int frameFormat) {
    GLContextHolder *pHolder = NULL;

    pHolder = newGLContext(env, jSurface, isSurfaceThreadExclusive);

    if (pHolder == NULL) {
        return 0;
    }
    return (jlong) pHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finrender_FinRender_releaseGLContext(JNIEnv *, jclass, jlong nativeContext) {
    releaseGLContext((GLContextHolder *) nativeContext);
}

JNIEXPORT void JNICALL
Java_com_ifinver_finrender_FinRender_renderOnContext(JNIEnv *env, jclass, jlong nativeGlContext, jbyteArray data_,jint frameWidth,jint frameHeight,jint degree,jboolean mirror) {
//    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(data_, 0);
//    if (isCopy) {
//        LOGI("isCopy=true");
//    }else{
//        LOGI("isCopy=false");
//    }

    renderFrame((GLContextHolder *) nativeGlContext, data,frameWidth, frameHeight,degree,mirror);

//    if (isCopy) {
        env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
//    }
}
//.........................................................................................................................

void renderFrame(GLContextHolder *holder, jbyte *data ,jint width, jint height,jint degree,jboolean mirror) {
    if(!holder->isSurfaceThreadExclusive) {
        if (!eglMakeCurrent(holder->eglDisplay, holder->eglSurface, holder->eglSurface, holder->eglContext)) {
            LOGE("make current failed!!! [当前surface是多个线程共享的]");
            checkGlError("eglMakeCurrent");
            return ;
        }
    }
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
            LOGE("鏡像，rotation=%d",degree);
        } else {
            degree = 360 - degree;
            idx = degree / 90 * 2;
            glVertexAttribPointer(holder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD_NOR + idx);
            LOGE("沒有鏡像，rotation=%d",degree);
        }
    }

    //上传纹理 Y通道
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, holder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, data);
    glUniform1i(holder->posUniTextureY, 0);

    //上传UV通道
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,holder->textures[1]);
    glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, data + (width * height));
    glUniform1i(holder->posUniTextureUV,1);


    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(holder->posAttrVertices);
    glDisableVertexAttribArray(holder->posAttrTexCoords);

    glFinish();
    eglSwapBuffers(holder->eglDisplay, holder->eglSurface);
}

//释放指定上下文
void releaseGLContext(GLContextHolder *holder) {
//    glDeleteBuffers(1,&(holder->vertexBuff));
    glDeleteTextures(holder->textureNums, holder->textures);
    glDeleteProgram(holder->program);
    eglDestroySurface(holder->eglDisplay, holder->eglSurface);
    eglDestroyContext(holder->eglDisplay, holder->eglContext);
    delete[](holder->textures);
    delete (holder);
}

//创建一个新的绘制上下文
GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface, jboolean isSurfaceThreadExclusive) {
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
    GLContextHolder *gl_holder = new GLContextHolder();
    gl_holder->isSurfaceThreadExclusive = isSurfaceThreadExclusive;


    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return NULL;
    }
    //如果是线程独享的，不会被占用makeCurrent
    if(!gl_holder->isSurfaceThreadExclusive){
        LOGI("当前surface是多个线程共享的");
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

    //success
    gl_holder->eglDisplay = display;
    gl_holder->eglContext = eglContext;
    gl_holder->eglSurface = eglSurface;
    gl_holder->program = programYUV;

    gl_holder->posAttrVertices = (GLuint) glGetAttribLocation(programYUV, "aPosition");
    gl_holder->posAttrTexCoords = (GLuint) glGetAttribLocation(programYUV, "aTexCoord");
    gl_holder->posUniTextureY = (GLuint) glGetUniformLocation(programYUV, "yTexture");
    gl_holder->posUniTextureUV = (GLuint) glGetUniformLocation(programYUV, "uvTexture");

    //tex
    GLuint *textures = new GLuint[2];
    glGenTextures(2, textures);
    glBindTexture(GL_TEXTURE_2D, textures[0]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glBindTexture(GL_TEXTURE_2D, textures[1]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    gl_holder->textureNums = 2;
    gl_holder->textures = textures;

    //buff
//    glUseProgram(programYUV);
//    GLuint vertexBuff;
//    glGenBuffers(1,&vertexBuff);
//    glBindBuffer(GL_ARRAY_BUFFER,vertexBuff);
//    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES_COORD),VERTICES_COORD,GL_STATIC_DRAW);
//    gl_holder->vertexBuff = vertexBuff;
//    gl_holder->vertexStride = 4 * sizeof(GLfloat);
//    gl_holder->texStride = 4 * sizeof(GLfloat);
//    gl_holder->offsetVertex = 0;
//    gl_holder->offsetTex = 2 * sizeof(GLfloat);

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    return gl_holder;
}