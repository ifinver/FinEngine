#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin render lib
add_library(fin-engine-lib
            SHARED
            finengine/main.cpp
            finengine/utils.cpp
            finengine/shaders.cpp
            finengine/GLContextHolder.cpp
            )
target_link_libraries(fin-engine-lib
                      EGL
                      GLESv2
                      log
                      android
                      )