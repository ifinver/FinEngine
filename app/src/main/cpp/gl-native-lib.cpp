#include "gl-native-lib.h"
using namespace cv;

#define  LOG_TAG    "GLNativeLib"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_init(JNIEnv *env, jclass type, jint width, jint height) {
    setupGraphics(width, height);
}

JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_onDraw(JNIEnv *env, jclass type) {
    onDrawFrame();
}

//...............................................................................................................................

const char *vertexShader =
        "attribute vec4 vPosition;   \n"
                "void main()                 \n"
                "{                           \n"
                "   gl_Position = vPosition; \n"
                "}                           \n";
const char *fragmentShader =
        "precision mediump float;                    \n"
                "void main()                                 \n"
                "{                                           \n"
                "   gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); \n"
                "}                                           \n";

GLfloat vVertices[] = {
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        1.0f, 0.5f, 0.0f
};

GLuint gProgram;
//GLuint gvPositionHandle; 已经被glBindAttribLocation绑定在arri 0上了。

bool setupGraphics(int width, int height) {
    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);

    LOGI("setupGraphics(%d, %d)", width, height);
    gProgram = createProgram(vertexShader, fragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return false;
    }
    //已经被glBindAttribLocation绑定在arri 0上了。
//    gvPositionHandle = (GLuint) glGetAttribLocation(gProgram, "vPosition");
//    checkGlError("glGetAttribLocation");
//    LOGI("glGetAttribLocation(\"vPosition\") = %d\n",
//         gvPositionHandle);
    glUseProgram(gProgram);
    checkGlError("glUseProgram");

    glViewport(0, 0, width, height);
    checkGlError("glViewport");
    glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
    return true;
}

void onDrawFrame() {
    glClear(GL_COLOR_BUFFER_BIT);
    checkGlError("glClear");

    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3* sizeof(float), vVertices);
    checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(0);
    checkGlError("glEnableVertexAttribArray");
    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    checkGlError("glDrawArrays");
}

GLuint createProgram(const char *pVertexSrc, const char *pFragmentSrc) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSrc);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSrc);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        glBindAttribLocation(program, 0, "vPosition");
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char *buf = (char *) malloc((size_t) bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

static void checkGlError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

GLuint loadShader(GLenum shaderType, const char *shaderSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &shaderSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc((size_t) infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
                         shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

JNIEXPORT jintArray JNICALL
Java_com_ifinver_myopengles_GLNative_getGrayImage(JNIEnv *env, jclass type, jintArray pixels_, jint w, jint h) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    if (pixels == NULL) {
        return NULL;
    }
    cv::Mat imgData(h, w, CV_8UC4, pixels);
    uchar *ptr = imgData.ptr(0);
    for (int i = 0; i < w * h; i++) {
        int grayScale = (int) (ptr[4 * i + 2] * 0.299 + ptr[4 * i + 1] * 0.587
                               + ptr[4 * i + 0] * 0.114);
        ptr[4 * i + 1] = (uchar) grayScale;
        ptr[4 * i + 2] = (uchar) grayScale;
        ptr[4 * i + 0] = (uchar) grayScale;
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return result;
}

cv::Mat frameBufferMat;
cv::Mat rgbImg;

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_initProcesser(JNIEnv *env, jclass, jint mFrameWidth, jint mFrameHeight, jint imageFormat) {
    frameBufferMat = cv::Mat(mFrameHeight + (mFrameHeight / 2), mFrameWidth, CV_8UC1);
    rgbImg = cv::Mat();
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_processFrame(JNIEnv *env, jclass, int size, jbyteArray frameBuffer_) {
    jbyte *frameBuffer = env->GetByteArrayElements(frameBuffer_, NULL);

    //填充
    memcpy(frameBufferMat.data,frameBuffer,size* sizeof(unsigned char));
    //转换
    cv::cvtColor(frameBufferMat,rgbImg,CV_YUV2RGB_NV21,4);


    env->ReleaseByteArrayElements(frameBuffer_, frameBuffer, 0);
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_releaseProcesser(JNIEnv *env, jclass type) {
    frameBufferMat.release();
}

/**
 * 测试用的方法
 */
JNIEXPORT jlong JNICALL
Java_com_ifinver_myopengles_GLNative_processFrameMat(JNIEnv *env, jclass type, jint length, jbyteArray frameBuffer_) {
    jbyte *frameBuffer = env->GetByteArrayElements(frameBuffer_, NULL);

    //填充
    memcpy(frameBufferMat.data,frameBuffer,length* sizeof(unsigned char));
    //转换
    cv::cvtColor(frameBufferMat,rgbImg,CV_YUV2RGB_NV21,4);


    env->ReleaseByteArrayElements(frameBuffer_, frameBuffer, 0);
//    return reinterpret_cast<jlong>(&rgbImg);
    return (jlong)(&rgbImg);
}