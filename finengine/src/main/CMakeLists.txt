#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载open cv
add_library(lib_opencv SHARED IMPORTED)
include_directories(${PROJECT_SOURCE_DIR}/../../src/main/cpp/include)
set_target_properties(lib_opencv
                      PROPERTIES IMPORTED_LOCATION
                      ${PROJECT_SOURCE_DIR}/../../libs/armeabi-v7a/libopencv_java3.so)

#加载人脸检测
add_library(arc_soft_spotlight SHARED IMPORTED)
include_directories(${PROJECT_SOURCE_DIR}/cpp/facedetect/arcsoft/inc/)
set_target_properties(arc_soft_spotlight
                      PROPERTIES IMPORTED_LOCATION
                      ${PROJECT_SOURCE_DIR}/../../libs/armeabi-v7a/libArcSoftSpotlight.so)


#加载fin engine lib
add_library(fin-engine-lib
            SHARED
            #===============utils blew===================================
            ${PROJECT_SOURCE_DIR}/cpp/glslutils.cpp
            #===============engine blew==================================
            ${PROJECT_SOURCE_DIR}/cpp/finengine/main.cpp
            ${PROJECT_SOURCE_DIR}/cpp/finengine/shaders.cpp
            ${PROJECT_SOURCE_DIR}/cpp/finengine/GLContextHolder.cpp
            #===============recorder blew================================
            ${PROJECT_SOURCE_DIR}/cpp/finrecorder/finrecorder.cpp
            ${PROJECT_SOURCE_DIR}/cpp/finrecorder/FinRecorderHolder.cpp
            #===============render blew==================================
            ${PROJECT_SOURCE_DIR}/cpp/finrender/finrender.cpp
            ${PROJECT_SOURCE_DIR}/cpp/finrender/FinRenderHolder.cpp
            #===============face detect blew=============================
            ${PROJECT_SOURCE_DIR}/cpp/facedetect/facedetector.cpp
            ${PROJECT_SOURCE_DIR}/cpp/facedetect/arcsoft/arcsoft.cpp
            #===============effects blew=================================
            ${PROJECT_SOURCE_DIR}/cpp/effects/effects.cpp
            ${PROJECT_SOURCE_DIR}/cpp/effects/faceswap/faceswap.cpp
            ${PROJECT_SOURCE_DIR}/cpp/effects/faceswap/FaceSwapper.cpp
            ${PROJECT_SOURCE_DIR}/cpp/effects/monalisa/monalisa.cpp
            ${PROJECT_SOURCE_DIR}/cpp/effects/monalisa/MonaLisaEffect.cpp
            )
target_link_libraries(fin-engine-lib
                    lib_opencv
                    arc_soft_spotlight
                    EGL
                    GLESv2
                    log
                    android)