cmake_minimum_required(VERSION 3.4.1)

add_library(native-lib SHARED
    src/main/cpp/native-lib.cpp)

target_link_libraries(native-lib log)

add_library(gl2jni SHARED
    src/main/cpp/gl_code.cpp)

target_link_libraries(gl2jni log GLESv2)