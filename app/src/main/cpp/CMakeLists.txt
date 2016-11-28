#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

#        #支持-std=gnu++11
#        set(CMAKE_VERBOSE_MAKEFILE on)
#        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#
#加载opencv库
#add_library(lib_opencv SHARED IMPORTED)
#配置include头文件
#include_directories(include/)
#引入libopencv_java3.so文件
#set_target_properties(lib_opencv
#                      PROPERTIES IMPORTED_LOCATION
#                      E:/WorkSpace/OpenGLES_CV/MyOpenGLES/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)


#加载fin render lib
add_library(fin-render-lib
            SHARED
            finrender/main.cpp
            finrender/utils.cpp
            finrender/shaders.cpp
            finrender/GLContextHolder.cpp
            )
#加载fin engine lib
add_library(fin-engine-lib
            SHARED
            finengine/finengine.cpp
            finrender/utils.cpp
            finrender/shaders.cpp
            finrender/GLContextHolder.cpp
            )

target_link_libraries(fin-render-lib
                      fin-engine-lib
                      android
                      log
                      EGL
#                      lib_opencv
                      GLESv2)