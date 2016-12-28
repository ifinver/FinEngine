#include "main.h"
#include "../glslutils.h"
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include "../effects/faceswap/faceswap.h"
#include "../effects/inc/faceresult.h"
#include "../facedetect/facedetector.h"
#include "../effects/monalisa/monalisa.h"

using namespace std;

jboolean initPrograms(GLContextHolder *engineHolder) {

    //rgb program
    ShaderRGB rgbShader;
    GLuint programRGB = createProgram(rgbShader.vertexShader, rgbShader.fragmentShader);
    if (programRGB == 0) {
        return JNI_FALSE;
    }
//    glUseProgram(programRGB);
    engineHolder->programRGB = programRGB;
    engineHolder->posRgbAttrVertices = (GLuint) glGetAttribLocation(programRGB, "aPosition");
    engineHolder->posRgbAttrTexCoords = (GLuint) glGetAttribLocation(programRGB, "aTexCoord");
    engineHolder->posRgbUniScaleX = (GLuint) glGetUniformLocation(programRGB, "uScaleX");
    engineHolder->posRgbUniScaleY = (GLuint) glGetUniformLocation(programRGB, "uScaleY");
    engineHolder->posRgbUniRotation = (GLuint) glGetUniformLocation(programRGB, "uRotation");
    engineHolder->posRgbUniMirror = (GLuint) glGetUniformLocation(programRGB, "mirror");
    engineHolder->posRgbUniTexture = (GLuint) glGetUniformLocation(programRGB, "sTexture");

    //yuv program
    ShaderNV21 yuvShader;
    GLuint programYUV = createProgram(yuvShader.vertexShader, yuvShader.fragmentShader);
    if (programYUV == 0) {
        return JNI_FALSE;
    }
    glUseProgram(programYUV);
    engineHolder->defaultProgram = engineHolder->targetProgram = engineHolder->currentProgram = programYUV;
    engineHolder->currentFilter = 0;

    engineHolder->posAttrVertices = (GLuint) glGetAttribLocation(programYUV, "aPosition");
    engineHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(programYUV, "aTexCoord");
    engineHolder->posUniScaleX = (GLuint) glGetUniformLocation(programYUV, "uScaleX");
    engineHolder->posUniScaleY = (GLuint) glGetUniformLocation(programYUV, "uScaleY");
    engineHolder->posUniRotation = (GLuint) glGetUniformLocation(programYUV, "uRotation");
    engineHolder->posUniMirror = (GLuint) glGetUniformLocation(programYUV, "mirror");
    engineHolder->posUniTextureY = (GLuint) glGetUniformLocation(programYUV, "yTexture");
    engineHolder->posUniTextureUV = (GLuint) glGetUniformLocation(programYUV, "uvTexture");

    //point program
    ShaderPoint pShader;
    GLuint programPoint = createProgram(pShader.vertexShader, pShader.fragmentShader);
    if (programPoint == 0) {
        return JNI_FALSE;
    }
//    glUseProgram(programPoint);
    engineHolder->programPoint = programPoint;
    engineHolder->posPointAttrVertices = (GLuint) glGetAttribLocation(programPoint, "aPosition");
    engineHolder->posPointAttrScaleX = (GLuint) glGetAttribLocation(programPoint, "aScaleX");
    engineHolder->posPointAttrScaleY = (GLuint) glGetAttribLocation(programPoint, "aScaleY");
    engineHolder->posPointUniColor = (GLuint) glGetUniformLocation(programPoint, "color");
    engineHolder->posPointUniRotation = (GLuint) glGetUniformLocation(programPoint, "uRotation");
    return JNI_TRUE;
}

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

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    GLContextHolder *engineHolder = NULL;

    engineHolder = new GLContextHolder();
    //success
    engineHolder->eglDisplay = display;
    engineHolder->eglContext = eglContext;
    engineHolder->eglSurface = eglSurface;

    if (!initPrograms(engineHolder)) {
        return JNI_FALSE;
    }

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

    //mode
    engineHolder->engineMode = ENGINE_MODE_NORMAL;

    return (jlong) engineHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRelease(JNIEnv *, jclass, jlong engine) {
    releaseGLContext((GLContextHolder *) engine);
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRender(JNIEnv *env, jclass, jlong engine, jbyteArray data_, jint frameWidth, jint frameHeight,
                                                  jint degree, jboolean mirror, jint outWidth, jint outHeight, jlong facePtr) {
    jbyte *data = env->GetByteArrayElements(data_, 0);
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    renderFrame(engineHolder, data, frameWidth, frameHeight, degree, mirror, outWidth, outHeight, facePtr);

    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchFilter(JNIEnv *env, jobject, jlong engine, jobject mAssetManager,
                                                                               jint mFilterType, jstring vertex_, jstring frag_) {
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    if (mFilterType == engineHolder->currentFilter) {
        LOGI("选择的滤镜和上一个滤镜相同");
        return;
    }
    if (mFilterType == 0) {
        LOGI("切换至空滤镜");
        engineHolder->targetProgram = engineHolder->defaultProgram;
        engineHolder->currentFilter = 0;
        return;
    }
    AAssetManager *mgr = AAssetManager_fromJava(env, mAssetManager);
    if (mgr == NULL) {
        LOGE("切换滤镜失败，AAssetManager不可用");
        return;
    }
    //load vertex
    const char *vertexName = env->GetStringUTFChars(vertex_, 0);
    AAsset *vertexAsset = AAssetManager_open(mgr, vertexName, AASSET_MODE_BUFFER);
    env->ReleaseStringUTFChars(vertex_, vertexName);
    off_t length = AAsset_getLength(vertexAsset);
    char *vertexContent = new char[length + 1];
    AAsset_read(vertexAsset, vertexContent, (size_t) length);
    AAsset_close(vertexAsset);
    vertexContent[length] = '\0';

    //load fragment
    const char *fragmentName = env->GetStringUTFChars(frag_, 0);
    AAsset *fragAsset = AAssetManager_open(mgr, fragmentName, AASSET_MODE_BUFFER);
    env->ReleaseStringUTFChars(frag_, fragmentName);
    length = AAsset_getLength(fragAsset);
    char *fragmentContent = new char[length + 1];
    AAsset_read(fragAsset, fragmentContent, (size_t) length);
    AAsset_close(fragAsset);
    fragmentContent[length] = '\0';

    GLuint targetP = createProgram(vertexContent, fragmentContent);
    if (targetP == 0) {
        LOGE("切换滤镜失败,编译出错");
        return;
    }
    engineHolder->targetProgram = targetP;
    engineHolder->currentFilter = mFilterType;
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeNormal(JNIEnv *env, jobject instance, jlong engine){
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    engineHolder->engineMode = ENGINE_MODE_NORMAL;
    LOGI("%s","switched mode to normal!");
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeFaceSwap(JNIEnv *env, jobject instance, jlong engine){
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    engineHolder->engineMode = ENGINE_MODE_FACE_SWAP;
    LOGI("%s","switched mode to swap face!");
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeMonaLisa(JNIEnv *env, jobject, jlong engine, jstring filePath_) {
    const char *filePath = env->GetStringUTFChars(filePath_, 0);
    LOGI("%s","switching mode to mona lisa ..");
    GLContextHolder *engineHolder = (GLContextHolder *) engine;
    if (engineHolder->monaFilePath.compare(filePath)) {
        //不相等才会走进来
        const Mat &monaMat = imread(filePath);
        if (engineHolder->pFaceOutlinePointOut == nullptr) {
            engineHolder->pFaceOutlinePointOut = new MPOINT[101];
            engineHolder->rcFaceRectOut = new MRECT();
            engineHolder->faceOrientOut = new MFloat[3];
        }

        int rc = face_processSingleFrame(monaMat.data, monaMat.cols, monaMat.rows, engineHolder->pFaceOutlinePointOut, engineHolder->rcFaceRectOut,
                                         engineHolder->faceOrientOut);
        if (rc == 0) {
            LOGE("%s", "检测图片成功！");

            engineHolder->monaFilePath = filePath;
//            engineHolder->engineMode = ENGINE_MODE_MONA_LISA;
        } else if (rc == 1) {
            LOGE("%s", "没有识别出人脸..");
        } else if (rc == -1) {
            LOGE("%s", "检测引擎未初始化");
        } else if (rc == -2) {
            LOGE("%s", "检测出错");
        }
    }


    env->ReleaseStringUTFChars(filePath_, filePath);
}

void caculateScale(GLContextHolder *engineHolder, jint outWidth, jint outHeight, jint odd, jint &width, jint &height);

GLfloat points[202];

void drawFacePoints(GLContextHolder *engineHolder, jlong facePtr, jint width, jint height, jint rot) {
    FaceDetectResult *faceData = NULL;
    try {
        if (facePtr != 0) {
            faceData = (FaceDetectResult *) facePtr;
        }
    } catch (...) {
        LOGE("%s", "不能转换出face detect result");
    }
    if (faceData == NULL) {
        return;
    }
    MInt32 localFaces = faceData->nFaceCountInOut;
    if (localFaces > 0) {
        glUseProgram(engineHolder->programPoint);

        for (int j = 0; j < localFaces; j++) {
            int idx = 0;
            for (int i = 0; i < faceData->faceOutlinePointCount; i++) {
                MPOINT ptIndex = faceData->pFaceOutlinePointOut[j * faceData->faceOutlinePointCount + i];
                points[idx++] = (GLfloat) ptIndex.x / width * 2 - 1;
                points[idx++] = (GLfloat) ptIndex.y / height * 2 - 1;
            }
            glEnableVertexAttribArray(engineHolder->posPointAttrVertices);
            glVertexAttribPointer(engineHolder->posPointAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, points);
            glVertexAttrib1f(engineHolder->posPointAttrScaleX, engineHolder->frameScaleX);
            glVertexAttrib1f(engineHolder->posPointAttrScaleY, engineHolder->frameScaleY);
            glUniform1i(engineHolder->posPointUniRotation, rot);
            glUniform4f(engineHolder->posPointUniColor, 0.0f, 1.0f, 0.0f, 1.0f);
            glDrawArrays(GL_POINTS, 0, 101);
            glDisableVertexAttribArray(engineHolder->posPointAttrVertices);
        }

        glUseProgram(engineHolder->currentProgram);
    }
}

//.........................................................................................................................
void renderFrame(GLContextHolder *engineHolder, jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth, jint outHeight,
                 jlong facePtr) {
    switch (engineHolder->engineMode) {
        default:
        case ENGINE_MODE_NORMAL: {
            renderYuv(engineHolder, data, width, height, degree, mirror, outWidth, outHeight, facePtr);
            break;
        }
        case ENGINE_MODE_FACE_SWAP: {
            unsigned char *swappedRgbaFrame = effect_swapFace(data, width, height, (long long) facePtr);
            if (swappedRgbaFrame == NULL) {
                renderYuv(engineHolder, data, width, height, degree, mirror, outWidth, outHeight, facePtr);
            } else {
                renderRgb(engineHolder, swappedRgbaFrame, width, height, degree, mirror, outWidth, outHeight, facePtr);
            }
            break;
        }
        case ENGINE_MODE_MONA_LISA: {
            unsigned char *monaLisaFrame = effect_monaLisa(data, width, height);
            if (monaLisaFrame == NULL) {
                renderYuv(engineHolder, data, width, height, degree, mirror, outWidth, outHeight, facePtr);
            } else {
                renderRgb(engineHolder, monaLisaFrame, width, height, degree, mirror, outWidth, outHeight, facePtr);
            }
            break;
        }
    }

}

void renderRgb(GLContextHolder *engineHolder, unsigned char *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight, jlong facePtr) {
    glUseProgram(engineHolder->programRGB);

    //输入顶点
    glEnableVertexAttribArray(engineHolder->posRgbAttrVertices);
    glVertexAttribPointer(engineHolder->posRgbAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, VERTICES_COORD);

    //输入纹理坐标，处理旋转和镜像
    glEnableVertexAttribArray(engineHolder->posRgbAttrTexCoords);
    glVertexAttribPointer(engineHolder->posRgbAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD);

    //上传RGB纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, engineHolder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, data);

    //处理旋转和镜像
    degree %= 360;
    if (degree < 0) degree += 360;
    jint odd = degree / 90;
    glUniform1i(engineHolder->posRgbUniRotation, odd);
    glUniform1i(engineHolder->posRgbUniMirror, mirror ? 1 : 0);
    caculateScale(engineHolder, outWidth, outHeight, odd, width, height);
    glUniform1f(engineHolder->posRgbUniScaleX, engineHolder->frameScaleX);
    glUniform1f(engineHolder->posRgbUniScaleY, engineHolder->frameScaleY);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(engineHolder->posRgbAttrVertices);
    glDisableVertexAttribArray(engineHolder->posRgbAttrTexCoords);

    glUseProgram(engineHolder->currentProgram);

    //画点
//    drawFacePoints(engineHolder, facePtr, width, height, odd);

    eglSwapBuffers(engineHolder->eglDisplay, engineHolder->eglSurface);

}

void caculateScale(GLContextHolder *engineHolder, jint outWidth, jint outHeight, jint odd, jint &width, jint &height) {
    if (engineHolder->frameWidth != width
        || engineHolder->frameHeight != height
        || engineHolder->outWidth != outWidth
        || engineHolder->outHeight != outHeight) {

        engineHolder->frameWidth = width;
        engineHolder->frameHeight = height;
        engineHolder->outWidth = outWidth;
        engineHolder->outHeight = outHeight;

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
}

void renderYuv(GLContextHolder *engineHolder, const jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight, jlong facePtr) {
    if (engineHolder->targetProgram != engineHolder->currentProgram) {
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
    //输入纹理
    glEnableVertexAttribArray(engineHolder->posAttrTexCoords);
    glVertexAttribPointer(engineHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD);

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

    //处理旋转和镜像
    degree %= 360;
    if (degree < 0) degree += 360;
    jint odd = degree / 90;
    glUniform1i(engineHolder->posUniRotation, odd);
    glUniform1i(engineHolder->posUniMirror, mirror ? 1 : 0);
    caculateScale(engineHolder, outWidth, outHeight, odd, width, height);
    glUniform1f(engineHolder->posUniScaleX, engineHolder->frameScaleX);
    glUniform1f(engineHolder->posUniScaleY, engineHolder->frameScaleY);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(engineHolder->posAttrVertices);
    glDisableVertexAttribArray(engineHolder->posAttrTexCoords);

    drawFacePoints(engineHolder, facePtr, width, height, odd);

//    glFinish();
    eglSwapBuffers(engineHolder->eglDisplay, engineHolder->eglSurface);
}

//释放指定上下文
void releaseGLContext(GLContextHolder *engineHolder) {
    if (engineHolder != NULL) {
        glDeleteTextures(engineHolder->textureNums, engineHolder->textures);
        glDeleteProgram(engineHolder->currentProgram);
        if (engineHolder->currentProgram != engineHolder->targetProgram) {
            glDeleteProgram(engineHolder->targetProgram);
        }
        eglDestroySurface(engineHolder->eglDisplay, engineHolder->eglSurface);
        eglDestroyContext(engineHolder->eglDisplay, engineHolder->eglContext);
        delete (engineHolder);
    }

}