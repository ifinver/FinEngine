#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

#        #支持-std=gnu++11
#        set(CMAKE_VERBOSE_MAKEFILE on)
#        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#
#加载opencv库
#add_library(lib_opencv SHARED IMPORTED)

#引入libopencv_java3.so文件
#set_target_properties(lib_opencv
#                      PROPERTIES IMPORTED_LOCATION
#                      E:/WorkSpace/OpenGLES_CV/MyOpenGLES/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

#Android Framework native code
include_directories(
    D:/GoogleSource/android/platform/frameworks/base/core/jni/android/graphics/
    D:/GoogleSource/android/platform/frameworks/base/include/
    D:/GoogleSource/android/platform/system/core/include/
    D:/GoogleSource/android/platform/hardware/libhardware/include/
#   D:/GoogleSource/android/platform/frameworks/base/native/include/
#   D:/GoogleSource/android/platform/frameworks/base/opengl/include/
)

#加载fin engine lib
add_library(fin-engine-lib
    SHARED
    finengine/finengine.cpp
    finengine/utils.cpp
    finengine/FinEngineHolder.cpp
    finengine/ShaderHolder.cpp
)
target_link_libraries(fin-engine-lib
    EGL
    GLESv2
    log
    android
    #ui
    #android_runtime
    jnigraphics
)