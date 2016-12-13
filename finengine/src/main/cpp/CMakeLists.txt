#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin engine lib
add_library(fin-engine-lib
            SHARED
            #==========utils blew
            utils.cpp
            #==========engine blew
            finengine/main.cpp
            finengine/shaders.cpp
            finengine/GLContextHolder.cpp
            #==========recorder blew
            finrecorder/finrecorder.cpp
            finrecorder/FinRecorderHolder.cpp
            #==========render blew
            finrender/finrender.cpp
            finrender/FinRenderHolder.cpp
            )
target_link_libraries(fin-engine-lib
                      EGL
                      GLESv2
                      log
                      android
                      )