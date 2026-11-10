cmake_minimum_required(VERSION 3.22.1)
project(snapdragon_ai_lab)

add_library(ai_lab_native SHARED accelerator_probe.cpp)

find_library(log-lib log)
find_library(android-lib android)
find_library(vulkan-lib vulkan)

target_link_libraries(ai_lab_native
    ${log-lib}
    ${android-lib}
    ${vulkan-lib}
    dl
)
