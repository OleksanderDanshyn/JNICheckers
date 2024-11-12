cmake_minimum_required(VERSION 3.10)
project(CheckersBack)

set(CMAKE_CXX_STANDARD 11)

# Specify the architecture if necessary (64-bit)
set(CMAKE_GENERATOR_PLATFORM x64)

# Add your source files and create a shared library
add_library(checkers SHARED library.cpp)

# Specify the output name without the "lib" prefix
set_target_properties(checkers PROPERTIES OUTPUT_NAME "checkers")
set_target_properties(checkers PROPERTIES PREFIX "")

# Include directories for JNI
target_include_directories(checkers PRIVATE "C:/Program Files/Java/jdk-23/include")
target_include_directories(checkers PRIVATE "C:/Program Files/Java/jdk-23/include/win32")

target_link_options(checkers PRIVATE -static)