#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin render lib
add_library(bad-render-lib
            SHARED
            finrender/main.cpp
            finrender/utils.cpp
            finrender/shaders.cpp
            finrender/GLContextHolder.cpp
            )
target_link_libraries(bad-render-lib
                      EGL
                      GLESv2
                      log
                      android
                      )