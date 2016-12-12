#include "main.h"
#include "utils.h"
#include <android/native_window_jni.h>
#include <sstream>
#include <android/asset_manager_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_FinEngine_nativeInit(JNIEnv *env, jclass, jobject jSurface) {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return JNI_FALSE;
    }
    EGLint majorVer, minVer;
    if (!eglInitialize(display, &majorVer, &minVer)) {
        checkGlError("eglInitialize");
        LOGE("eglInitialize");
        return JNI_FALSE;
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
        return JNI_FALSE;
    }
    ANativeWindow *surface = ANativeWindow_fromSurface(env, jSurface);
    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
    if (surface == EGL_NO_SURFACE) {
        checkGlError("eglCreateWindowSurface");
        return JNI_FALSE;
    }
    EGLint attrib_list[] =
            {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return JNI_FALSE;
    }


    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return JNI_FALSE;
    }

    //context create success,now create program
    ShaderBase shader = ShaderNV21();
    GLuint programYUV = createProgram(shader.vertexShader, shader.fragmentShader);
//    delete shader;
    if (programYUV == 0) {
        return JNI_FALSE;
    }

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    GLContextHolder *engineHolder = NULL;

    engineHolder = new GLContextHolder();
    //success
    engineHolder->eglDisplay = display;
    engineHolder->eglContext = eglContext;
    engineHolder->eglSurface = eglSurface;

    glUseProgram(programYUV);
    engineHolder->defaultProgram = engineHolder->targetProgram = engineHolder->currentProgram = programYUV;
    engineHolder->currentFilter = 0;

    engineHolder->posAttrVertices = (GLuint) glGetAttribLocation(programYUV, "aPosition");
    engineHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(programYUV, "aTexCoord");
    engineHolder->posAttrScaleX = (GLuint) glGetAttribLocation(programYUV, "aScaleX");
    engineHolder->posAttrScaleY = (GLuint) glGetAttribLocation(programYUV, "aScaleY");
    engineHolder->posUniTextureY = (GLuint) glGetUniformLocation(programYUV, "yTexture");
    engineHolder->posUniTextureUV = (GLuint) glGetUniformLocation(programYUV, "uvTexture");

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

    engineHolder->textureNums = 2;
    engineHolder->textures = textures;

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);

    return (jlong) engineHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRelease(JNIEnv *, jclass,jlong engine) {
    releaseGLContext((GLContextHolder *) engine);
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRender(JNIEnv *env, jclass, jlong engine,jbyteArray data_, jint frameWidth, jint frameHeight,
                                                  jint degree, jboolean mirror, jint outWidth, jint outHeight) {
    jbyte *data = env->GetByteArrayElements(data_, 0);
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    renderFrame(engineHolder,data, frameWidth, frameHeight, degree, mirror, outWidth, outHeight);

    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchFilter(JNIEnv *env, jobject ,jlong engine,jobject mAssetManager, jint mFilterType,jstring vertex_,jstring frag_){
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    if(mFilterType == engineHolder->currentFilter){
        LOGI("选择的滤镜和上一个滤镜相同");
        return;
    }
    if(mFilterType == 0){
        LOGI("切换至空滤镜");
        engineHolder->targetProgram = engineHolder->defaultProgram;
        engineHolder->currentFilter = 0;
        return;
    }
    AAssetManager* mgr = AAssetManager_fromJava(env, mAssetManager);
    if(mgr == NULL){
        LOGE("切换滤镜失败，AAssetManager不可用");
        return;
    }
    //load vertex
    const char *vertexName = env->GetStringUTFChars(vertex_, 0);
    AAsset* vertexAsset = AAssetManager_open(mgr, vertexName, AASSET_MODE_BUFFER);
    env->ReleaseStringUTFChars(vertex_, vertexName);
    off_t length = AAsset_getLength(vertexAsset);
    char* vertexContent = new char[length+1];
    AAsset_read(vertexAsset, vertexContent, (size_t) length);
    AAsset_close(vertexAsset);
    vertexContent[length] = '\0';

    //load fragment
    const char *fragmentName = env->GetStringUTFChars(frag_, 0);
    AAsset* fragAsset = AAssetManager_open(mgr, fragmentName, AASSET_MODE_BUFFER);
    env->ReleaseStringUTFChars(frag_, fragmentName);
    length = AAsset_getLength(fragAsset);
    char* fragmentContent = new char[length+1];
    AAsset_read(fragAsset, fragmentContent, (size_t) length);
    AAsset_close(fragAsset);
    fragmentContent[length] = '\0';

    GLuint targetP = createProgram(vertexContent,fragmentContent);
    if (targetP == 0) {
        LOGE("切换滤镜失败,编译出错");
        return ;
    }
    engineHolder->targetProgram = targetP;
    engineHolder->currentFilter = mFilterType;
}

//.........................................................................................................................
void renderFrame(GLContextHolder *engineHolder,jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth, jint outHeight) {
    if(engineHolder->targetProgram != engineHolder->currentProgram) {
        glUseProgram(engineHolder->targetProgram);
        if (engineHolder->currentProgram != engineHolder->defaultProgram) { //默认滤镜不删
            glDeleteProgram(engineHolder->currentProgram);
        }
        engineHolder->currentProgram = engineHolder->targetProgram;
        LOGI("切换滤镜成功！");
    }

    //输入顶点
    glEnableVertexAttribArray(engineHolder->posAttrVertices);
    glVertexAttribPointer(engineHolder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, VERTICES_COORD);

    //上传纹理 Y通道
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, engineHolder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, data);
    glUniform1i(engineHolder->posUniTextureY, 0);

    //上传UV通道
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, engineHolder->textures[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, data + (width * height));
    glUniform1i(engineHolder->posUniTextureUV, 1);

    //输入纹理坐标，处理旋转和镜像
    glEnableVertexAttribArray(engineHolder->posAttrTexCoords);
    if (engineHolder->frameDegree != degree) {
        engineHolder->frameDegree = degree;
        degree %= 360;
        if (degree < 0) degree += 360;
        int idx;
        const float *inputTextureCoord;
        if (mirror) {
            idx = degree / 90 * 2;
            inputTextureCoord = TEXTURE_COORD_MIRROR + idx;
        } else {
            degree = 360 - degree;
            idx = degree / 90 * 2;
            inputTextureCoord = TEXTURE_COORD_NOR + idx;
        }
        engineHolder->inputTextureCorrd = inputTextureCoord;
    }
    glVertexAttribPointer(engineHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, engineHolder->inputTextureCorrd);

    if(engineHolder->frameWidth != width
            || engineHolder->frameHeight != height
            || engineHolder->outWidth != outWidth
            || engineHolder->outHeight != outHeight){

        engineHolder->frameWidth = width;
        engineHolder->frameHeight = height;
        engineHolder->outWidth = outWidth;
        engineHolder->outHeight = outHeight;

        jint odd = degree / 90;
        if (odd == 1 || odd == 3) {
            //如果旋转了90°，交换长和宽
            int temp = width;
            width = height;
            height = temp;
        }

        float fixWidth, fixHeight;
        if ((float) width / height >= (float) outWidth / outHeight) {
            fixHeight = height;
            fixWidth = (float) height / outHeight * outWidth;
        } else {
            fixWidth = width;
            fixHeight = (float) width / outWidth * outHeight;
        }

        engineHolder->frameScaleX = width / fixWidth;
        engineHolder->frameScaleY = height / fixHeight;
    }


    glVertexAttrib1f(engineHolder->posAttrScaleX, engineHolder->frameScaleX);
    glVertexAttrib1f(engineHolder->posAttrScaleY, engineHolder->frameScaleY);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(engineHolder->posAttrVertices);
    glDisableVertexAttribArray(engineHolder->posAttrTexCoords);

//    glFinish();
    eglSwapBuffers(engineHolder->eglDisplay, engineHolder->eglSurface);
}

//释放指定上下文
void releaseGLContext(GLContextHolder *engineHolder) {
    if (engineHolder != NULL) {
        glDeleteTextures(engineHolder->textureNums, engineHolder->textures);
        glDeleteProgram(engineHolder->currentProgram);
        if(engineHolder->currentProgram != engineHolder->targetProgram){
            glDeleteProgram(engineHolder->targetProgram);
        }
        eglDestroySurface(engineHolder->eglDisplay, engineHolder->eglSurface);
        eglDestroyContext(engineHolder->eglDisplay, engineHolder->eglContext);
        delete (engineHolder);
    }

}