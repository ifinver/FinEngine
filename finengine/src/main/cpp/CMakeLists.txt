#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin engine lib
add_library(fin-engine-lib
            SHARED
            #==========engine blew
            finengine/main.cpp
            finengine/utils.cpp
            finengine/shaders.cpp
            finengine/GLContextHolder.cpp
            #==========recorder blew
            finrecorder/finrecorder.cpp
            finrecorder/utils.cpp
            #==========render blew
            finrender/finrender.cpp
            finrender/FinRenderHolder.cpp
            finrender/utils.cpp
            )
target_link_libraries(fin-engine-lib
                      EGL
                      GLESv2
                      log
                      android
                      )