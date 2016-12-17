#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载open cv
add_library(lib_opencv SHARED IMPORTED)
include_directories(D:/opencv-sdk/native/jni/include)
set_target_properties(lib_opencv
                      PROPERTIES IMPORTED_LOCATION
                      ${PROJECT_SOURCE_DIR}/libs/armeabi-v7a/libopencv_java3.so)


#加载fin engine lib
add_library(fin-engine-lib
            SHARED
            #==========utils blew
            ${PROJECT_SOURCE_DIR}/src/main/cpp/glslutils.cpp
            #==========engine blew
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finengine/main.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finengine/shaders.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finengine/GLContextHolder.cpp
            #==========recorder blew
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finrecorder/finrecorder.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finrecorder/FinRecorderHolder.cpp
            #==========render blew
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finrender/finrender.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/finrender/FinRenderHolder.cpp
            #==========open cv blew
            ${PROJECT_SOURCE_DIR}/src/main/cpp/opencv/xcv.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/opencv/xcvcore.cpp
            ${PROJECT_SOURCE_DIR}/src/main/cpp/opencv/FaceSwapper.cpp
            )
target_link_libraries(fin-engine-lib
                    lib_opencv
                    EGL
                    GLESv2
                    log
                    android)