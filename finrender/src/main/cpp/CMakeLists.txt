#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin engine lib
add_library(fin-render-lib
    SHARED
    finrender/finrender.cpp
    finrender/FinRenderHolder.cpp
    finrender/utils.cpp
)
target_link_libraries(fin-render-lib
    EGL
    GLESv2
    log
    android
)