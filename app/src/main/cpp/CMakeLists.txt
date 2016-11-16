#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

#支持-std=gnu++11
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

#加载本项目自己的库
add_library(gl-native-lib SHARED gl-native-lib.cpp )

#加载opencv库
add_library(lib_opencv SHARED IMPORTED)
#配置Open CV include头文件
include_directories(include/)

#引入libopencv_java3.so文件
set_target_properties(lib_opencv
                      PROPERTIES IMPORTED_LOCATION
                      E:/OpenGL_OpenCV/MyOpenGLES/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

# add lib dependencies
target_link_libraries(gl-native-lib
                      android
                      log
                      EGL
                      GLESv2
                      lib_opencv)