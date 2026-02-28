cmake_minimum_required(VERSION 3.16)

project(lora VERSION 0.1 LANGUAGES CXX)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(QT NAMES Qt6 Qt5 REQUIRED COMPONENTS Widgets Core Qml Quick SerialPort )
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Widgets Core Qml Quick SerialPort )

add_subdirectory(thirdparty/QCrossPlatformSerial)

set(PROJECT_SOURCES
    src/main.cpp
    src/SerialPortManager.hpp
    src/SerialPortManager.cpp
    src/SerialPortCommunicator.hpp
    src/SerialPortCommunicator.cpp
    qml/qml.qrc
)

if(${QT_VERSION_MAJOR} GREATER_EQUAL 6)
    qt_add_executable(lora_app
        MANUAL_FINALIZATION
        ${PROJECT_SOURCES}
    )
    qt_add_qml_module(lora_app
        URI lora
        VERSION 1.0
        QML_FILES qml/main.qml
    )
else()
    add_executable(lora_app
        ${PROJECT_SOURCES}
    )
endif()

if(ANDROID)
    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_SOURCE_DIR}/android")
    set(QT_ANDROID_TARGET_SDK_VERSION "34" CACHE STRING "Android target SDK version" FORCE)
    set_target_properties(lora_app PROPERTIES
        QT_ANDROID_PACKAGE_SOURCE_DIR "${ANDROID_PACKAGE_SOURCE_DIR}"
        ANDROID_GUI TRUE
        QT_ANDROID_DEPLOYQT_OPTIONS "--android-platform;android-34"
    )

    # Copy Java files from QCrossPlatformSerial library to android/src
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtcrossplatformserial/UsbSerialManager.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtcrossplatformserial/UsbSerialManager.java
        COPYONLY
    )
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtcrossplatformserial/JniUsbSerial.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtcrossplatformserial/JniUsbSerial.java
        COPYONLY
    )
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtcrossplatformserial/SerialInputOutputManager.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtcrossplatformserial/SerialInputOutputManager.java
        COPYONLY
    )
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtserial/UsbSerialManager.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtserial/UsbSerialManager.java
        COPYONLY
    )
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtserial/JniUsbSerial.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtserial/JniUsbSerial.java
        COPYONLY
    )
    configure_file(
        ${CMAKE_SOURCE_DIR}/thirdparty/QCrossPlatformSerial/android/src/org/qtserial/SerialInputOutputManager.java
        ${CMAKE_SOURCE_DIR}/android/src/org/qtserial/SerialInputOutputManager.java
        COPYONLY
    )

    list(APPEND PROJECT_SOURCES
        android/src/org/qtcrossplatformserial/UsbSerialManager.java
        android/src/org/qtcrossplatformserial/JniUsbSerial.java
        android/src/org/qtcrossplatformserial/SerialInputOutputManager.java
        android/src/org/qtserial/UsbSerialManager.java
        android/src/org/qtserial/JniUsbSerial.java
        android/src/org/qtserial/SerialInputOutputManager.java
        android/res/xml/device_filter.xml
        android/res/values/libs.xml
        android/res/xml/qtprovider_paths.xml
        android/AndroidManifest.xml
        android/build.gradle
        android/gradle/wrapper/gradle-wrapper.jar
        android/gradle/wrapper/gradle-wrapper.properties
        android/gradle.properties
        android/gradlew
        android/gradlew.bat
    )
endif()

target_link_libraries(lora_app PRIVATE
    Qt${QT_VERSION_MAJOR}::Widgets
    Qt${QT_VERSION_MAJOR}::Core
    Qt${QT_VERSION_MAJOR}::Qml
    Qt${QT_VERSION_MAJOR}::Quick
    QCrossPlatformSerial
)

install(TARGETS lora_app
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)

if(${QT_VERSION_MAJOR} GREATER_EQUAL 6 AND ANDROID)
    qt_policy(SET QTP0001 NEW)
    qt_policy(SET QTP0002 NEW)
    qt_finalize_executable(lora_app)
endif()
