mkdir build
cd build
cmake -G Ninja -DCMAKE_MAKE_PROGRAM=C:/Qt/Tools/Ninja/ninja.exe -DCMAKE_TOOLCHAIN_FILE=C:/Users/User/AppData/Local/Android/Sdk/ndk/26.1.10909125/build/cmake/android.toolchain.cmake -DANDROID_ABI=armeabi-v7a -DANDROID_PLATFORM=android-34 -DCMAKE_ANDROID_API=34 -DCMAKE_ANDROID_BUILD_TOOLS_VERSION=34.0.0 -DCMAKE_PREFIX_PATH=C:/Qt/6.9.0/android_armv7 -DQT_DIR=C:/Qt/6.9.0/android_armv7/lib/cmake/Qt6 -DCMAKE_FIND_ROOT_PATH=C:/Users/User/AppData/Local/Android/Sdk/ndk/26.1.10909125;C:/Qt/6.9.0/android_armv7 -DANDROID_SDK_ROOT=C:/Users/User/AppData/Local/Android/Sdk -DCMAKE_BUILD_TYPE=Debug ..
powershell -Command "(Get-Content android-build\gradle.properties) -replace 'qtTargetSdkVersion=35', 'qtTargetSdkVersion=34' -replace 'androidBuildToolsVersion=35.0.0', 'androidBuildToolsVersion=34.0.0' -replace 'androidCompileSdkVersion=android-35', 'androidCompileSdkVersion=android-34' | Set-Content android-build\gradle.properties"
cmake --build .
C:\Qt\6.9.0\mingw_64\bin\androiddeployqt --input ./android-lora_app-deployment-settings.json --output ./android-build --apk ./android-build/lora.apk --android-platform android-34
copy android-build\build\outputs\apk\debug\android-build-debug.apk android-build-debug.apk

