#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)



add_library(arc_soft_spotlight SHARED IMPORTED)
include_directories(${PROJECT_SOURCE_DIR}/cpp/arcsoft/inc/)
set_target_properties(arc_soft_spotlight
                      PROPERTIES IMPORTED_LOCATION
                      ${PROJECT_SOURCE_DIR}/jniLibs/armeabi-v7a/libArcSoftSpotlight.so)

add_library(face-detect-lib
          SHARED
          ${PROJECT_SOURCE_DIR}/cpp/facedetector.cpp
          ${PROJECT_SOURCE_DIR}/cpp/arcsoft/arcsoft.cpp)

target_link_libraries(face-detect-lib
                    arc_soft_spotlight
                    log
                    android)