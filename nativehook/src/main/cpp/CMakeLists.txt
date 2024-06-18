cmake_minimum_required(VERSION 3.22.1)
project("nativehook")

add_library(${CMAKE_PROJECT_NAME} SHARED
        nativehook.cpp
        sysprop.cpp)

find_package(bytehook REQUIRED CONFIG)

target_link_libraries(${CMAKE_PROJECT_NAME}
        bytehook::bytehook
        android
        log)