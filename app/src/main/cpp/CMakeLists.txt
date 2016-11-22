#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

#        #支持-std=gnu++11
#        set(CMAKE_VERBOSE_MAKEFILE on)
#        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#
#        #加载opencv库
#        add_library(lib_opencv SHARED IMPORTED)
#        #配置Open CV include头文件
#        include_directories(include/)
#
#        #引入libopencv_java3.so文件
#        set_target_properties(lib_opencv
#                             PROPERTIES IMPORTED_LOCATION
#                              E:/WorkSpace/OpenGLES_CV/MyOpenGLES/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)


#加载本项目自己的库
add_library(gl-native-lib
            SHARED
            Test.cpp
            gl/GLContextHolder.cpp
            gl/shaders.cpp
            gl/main.cpp
            gl/utils.cpp)

target_link_libraries(gl-native-lib
                      android
                      log
                      EGL
#                     lib_opencv
                      GLESv2)