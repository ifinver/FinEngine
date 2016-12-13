#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin recorder lib
add_library(fin-recorder-lib
            SHARED
            finrecorder/finrecorder.cpp
            finrecorder/utils.cpp
            )
target_link_libraries(fin-recorder-lib
                      EGL
                      GLESv2
                      log
                      android
                      )