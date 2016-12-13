#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)


#加载fin engine lib
add_library(unity-transfer-lib
    SHARED
    main.cpp
    UnityTransfer.cpp
)
target_link_libraries(unity-transfer-lib
    log
    android
)