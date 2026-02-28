# QCrossPlatformSerial-Example

Cross-platform example of using the QCrossPlatformSerial library for serial port communication (Serial Port Communication).

![License: LGPL-2.1](https://img.shields.io/badge/License-LGPL--2.1-blue.svg)
![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20Android-lightgrey.svg)
![Qt](https://img.shields.io/badge/Qt-5%20%7C%206-41CD52.svg)
![C++](https://img.shields.io/badge/C++-17-00599C.svg)

## Project Description

**QCrossPlatformSerial-Example** is a demonstration application that shows how to use the QCrossPlatformSerial library for organizing communication through a serial port on various platforms.

The project demonstrates:
- Searching and enumerating available serial ports
- Connecting to a selected device
- Sending and receiving data
- Using Qt Quick/QML for the user interface
- Cross-platform support (Windows and Android)

## Features

- 🔍 **Device Discovery** — automatic detection of available serial ports
- 📡 **Serial Port Communication** — sending and receiving data through a serial port
- 📱 **Android Support** — working with USB-serial adapters on Android devices
- 🖥️ **Windows Support** — working with COM ports on Windows
- 🎨 **QML Interface** — modern user interface on Qt Quick
- 📊 **Device Information** — displaying description, manufacturer, and serial number

## System Requirements

### General Requirements

- **CMake** version 3.16 or higher
- **C++17** compatible compiler
- **Qt** version 5.15 or 6.x with the following modules:
  - Qt Core
  - Qt Qml
  - Qt Quick
  - Qt SerialPort
  - Qt Widgets

### For Windows

- Windows 10 or higher
- Qt with MinGW or MSVC support
- CMake

### For Android

- **Android SDK** (version 34 recommended)
- **Android NDK** (version 26.1.10909125 recommended)
- **Qt for Android** (arm64-v8a, armeabi-v7a, x86_64)
- **JDK** (Java Development Kit)
- **Gradle** (included in Android SDK)

## Project Structure

```
QCrossPlatformSerial-Example/
├── CMakeLists.txt              # Main build configuration file
├── README.md                   # This file
├── LICENSE                     # LGPL-2.1 license
├── .gitignore                  # Git exclusions
├── .gitmodules                 # Git submodules
│
├── src/                        # C++ source code
│   ├── main.cpp                # Application entry point
│   ├── SerialPortManager.hpp   # Port manager header
│   ├── SerialPortManager.cpp   # Port manager implementation
│   ├── SerialPortCommunicator.hpp  # Communicator header
│   └── SerialPortCommunicator.cpp  # Communicator implementation
│
├── qml/                        # QML interface files
│   ├── main.qml                # Main application screen
│   └── qml.qrc                 # QML resources
│
├── android/                    # Android configuration
│   ├── AndroidManifest.xml     # Application manifest
│   ├── build.gradle            # Gradle configuration
│   ├── gradle.properties       # Gradle properties
│   ├── gradlew                 # Gradle wrapper (Unix)
│   ├── gradlew.bat             # Gradle wrapper (Windows)
│   ├── res/                    # Android resources
│   │   ├── drawable/           # Drawable resources
│   │   │   ├── splashscreen.xml         # Splash screen
│   │   │   ├── splashscreen_port.xml    # Splash screen (portrait)
│   │   │   └── splashscreen_land.xml    # Splash screen (landscape)
│   │   ├── drawable-*/         # Drawables for different densities
│   │   │   ├── icon.png        # Application icon
│   │   │   ├── logo.png        # Logo
│   │   │   ├── logo_port.png   # Logo (portrait)
│   │   │   └── logo_land.png   # Logo (landscape)
│   │   ├── values/             # String resources
│   │   │   ├── strings.xml     # Application strings
│   │   │   └── libs.xml        # Libraries
│   │   └── xml/                # XML resources
│   │       ├── device_filter.xml        # USB device filter
│   │       └── qtprovider_paths.xml    # FileProvider paths
│   └── src/                    # Java source code
│       └── org/
│           ├── qtcrossplatformserial/  # JNI classes
│           └── qtserial/               # JNI classes (compatibility)
│
├── tools/                      # Build and deployment scripts
│   ├── build.bat               # Build script for Android
│   └── phone_deploy.bat        # Deployment script for device
│
└── thirdparty/                 # Third-party libraries
    └── QCrossPlatformSerial/   # QCrossPlatformSerial library
        ├── src/                # Library source code
        ├── android/            # Android JNI implementations
        ├── CMakeLists.txt      # Build configuration
        └── README.md           # Library documentation
```

## Build Instructions

### Building for Windows

1. **Clone the repository with submodules:**

```bash
git clone --recursive <repository-url>
cd QCrossPlatformSerial-Example
```

2. **Create a build directory:**

```bash
mkdir build
cd build
```

3. **Configure the project with CMake:**

```bash
cmake -G "Ninja" -DCMAKE_PREFIX_PATH=C:/Qt/6.9.0/mingw_64 -DCMAKE_BUILD_TYPE=Debug ..
```

Replace `C:/Qt/6.9.0/mingw_64` with the path to your Qt installation.

4. **Build the project:**

```bash
cmake --build .
```

5. **Run the application:**

```bash
./lora_app.exe
```

### Building for Android

#### Preliminary Setup

Make sure the following environment variables are set or specify the paths in the build script:

- `ANDROID_SDK_ROOT` — path to Android SDK
- `ANDROID_NDK_ROOT` — path to Android NDK
- `Qt_DIR` — path to Qt for Android

#### Using the Build Script

The project includes a ready-to-use build script [`tools/build.bat`](tools/build.bat). Before running, make sure the paths to Qt, Android SDK, and NDK match your system.

1. **Run the build script:**

```bash
cd tools
build.bat
```

The script will perform the following actions:
- Create a `build` directory
- Configure the project with CMake for Android
- Build the APK file
- Copy the finished APK to `build/android-build-debug.apk`

#### Manual Build

If you want to build the project manually:

1. **Create a build directory:**

```bash
mkdir build
cd build
```

2. **Configure the project for Android:**

```bash
cmake -G Ninja ^
    -DCMAKE_TOOLCHAIN_FILE=%ANDROID_NDK_ROOT%/build/cmake/android.toolchain.cmake ^
    -DANDROID_ABI=arm64-v8a ^
    -DANDROID_PLATFORM=android-34 ^
    -DCMAKE_PREFIX_PATH=C:/Qt/6.9.0/android_arm64_v8a ^
    -DQT_DIR=C:/Qt/6.9.0/android_arm64_v8a/lib/cmake/Qt6 ^
    -DANDROID_SDK_ROOT=%ANDROID_SDK_ROOT% ^
    -DCMAKE_BUILD_TYPE=Debug ..
```

3. **Build the project:**

```bash
cmake --build .
```

4. **Create the APK:**

```bash
C:/Qt/6.9.0/mingw_64/bin/androiddeployqt ^
    --input ./android-lora_app-deployment-settings.json ^
    --output ./android-build ^
    --apk ./android-build/lora.apk ^
    --android-platform android-34
```

## Installation on Android Device

### Using the Deployment Script

The project includes a script [`tools/phone_deploy.bat`](tools/phone_deploy.bat) for automatic installation and launching of the application on a connected device.

1. **Connect an Android device via USB**

2. **Enable developer mode and USB debugging**

3. **Run the deployment script:**

```bash
cd tools
phone_deploy.bat
```

### Manual Installation

1. **Install the APK on the device:**

```bash
adb install -r build/android-build-debug.apk
```

2. **Launch the application:**

```bash
adb shell am start -n org.qtproject.example.lora_app/org.qtproject.qt.android.bindings.QtActivity
```

## Using the Application

### Running on Windows

After building, run the executable file:
- Windows: `build/lora_app.exe`

### Running on Android

After installing the APK, launch the application from the app menu on your Android device.

### Application Functionality

1. **Refreshing the device list**
   - Click the "Refresh" button to search for available serial ports
   - The list will display all found devices with information:
     - Port name
     - Description
     - Manufacturer
     - Serial number

2. **Connecting to a device**
   - Select a device from the list
   - Click the "Connect to: [port name]" button
   - A message will appear in the console upon successful connection

3. **Sending data**
   - Click the "Send command" button to send a test command
   - Click the "Send object (JSON)" button to send a JSON object

4. **Reading data**
   - Click the "Read data" button to receive data from the device
   - Received data will be displayed in the text field

5. **Disconnecting**
   - Click the "Disconnect device" button to close the connection

## API Usage

### SerialPortCommunicator

The [`SerialPortCommunicator`](src/SerialPortCommunicator.hpp) class provides an interface for working with a serial port from QML.

**Main Methods:**

- `init()` — initialize the communicator
- `connectToDevice()` — connect to a device
- `closePort()` — close the port
- `isPortConnected()` — check connection status
- `sendData(QString data)` — send data
- `sendObject(QString name, double latitude, double longitude, int code)` — send a JSON object
- `readData()` — read data
- `fetchData()` — asynchronously fetch data
- `setPortName(QString portName)` — set the port name

**Signals:**

- `dataRead(QString data)` — emitted when data is received
- `messageDisplay(QString message)` — emitted for displaying messages

### SerialPortManager

The [`SerialPortManager`](src/SerialPortManager.hpp) class manages information about available ports.

**Main Methods:**

- `refreshPorts()` — refresh the list of available ports

**Properties:**

- `availablePorts` — list of available ports (QVariantList)

**Signals:**

- `availablePortsChanged()` — emitted when the port list changes

## Troubleshooting

### Android Build Issues

#### Error: Missing drawable resources

**Problem:** Build errors occur about missing drawable resources when building the APK.

**Solution:** Ensure all necessary drawable resources are present in the directories:
- `android/res/drawable/splashscreen.xml`
- `android/res/drawable/splashscreen_port.xml`
- `android/res/drawable/splashscreen_land.xml`
- `android/res/drawable-*/icon.png` (for all densities)
- `android/res/drawable-*/logo*.png` (for all densities)

#### Error: App name not displaying correctly

**Problem:** The application name is displayed as the package name instead of the configured name.

**Solution:** Check the file [`android/res/values/strings.xml`](android/res/values/strings.xml):

```xml
<resources>
    <string name="app_name">QCrossPlatform</string>
    <string name="QCrossPlatformSerialExample">QCrossPlatform</string>
</resources>
```

Make sure that [`AndroidManifest.xml`](android/AndroidManifest.xml:9) contains a reference to the string resource:

```xml
android:label="@string/app_name"
```

#### Error: Splash screen not working

**Problem:** The splash screen is not displayed when the application starts.

**Solution:** Check the configuration in [`AndroidManifest.xml`](android/AndroidManifest.xml:20-22):

```xml
<meta-data android:name="android.app.splash_screen_drawable" android:resource="@drawable/splashscreen"/>
<meta-data android:name="android.app.splash_screen_drawable_portrait" android:resource="@drawable/splashscreen_port"/>
<meta-data android:name="android.app.splash_screen_drawable_landscape" android:resource="@drawable/splashscreen_land"/>
```

Also ensure that the splash screen theme is defined in [`android/res/values/splashscreentheme.xml`](android/res/values/splashscreentheme.xml).

### Device Connection Issues

#### Device not detected on Android

**Problem:** The device list is empty when clicking the "Refresh" button.

**Solutions:**

1. **Check USB connection:**
   - Ensure the device is connected via an OTG cable
   - Verify that USB debugging is enabled

2. **Check permissions:**
   - On first connection, the device will request USB access permission
   - Ensure the permission is granted

3. **Check device filter:**
   - The file [`android/res/xml/device_filter.xml`](android/res/xml/device_filter.xml) defines supported USB devices
   - If necessary, add the VID/PID of your device

#### Error opening port

**Problem:** Unable to open the port on Windows.

**Solutions:**

1. **Check that the port is not occupied by another application**
2. **Run the application as administrator**
3. **Ensure the USB-serial adapter driver is installed**

### CMake Issues

#### Error: Qt not found

**Problem:** CMake cannot find Qt.

**Solution:** Specify the path to Qt using the `-DCMAKE_PREFIX_PATH` parameter:

```bash
cmake -DCMAKE_PREFIX_PATH=C:/Qt/6.9.0/mingw_64 ..
```

#### Error: Android toolchain not found

**Problem:** CMake cannot find the Android NDK.

**Solution:** Specify the path to the toolchain file:

```bash
cmake -DCMAKE_TOOLCHAIN_FILE=%ANDROID_NDK_ROOT%/build/cmake/android.toolchain.cmake ..
```

## License

This project is distributed under the **GNU Lesser General Public License v2.1** (LGPL-2.1).

Detailed license information is available in the [`LICENSE`](LICENSE) file.

### Used Libraries

- **QCrossPlatformSerial** — library for cross-platform serial port communication (LGPL-2.1)
- **Qt Framework** — framework for cross-platform application development (LGPL-2.1/GPL-3.0)

## Additional Resources

- [Qt Documentation](https://doc.qt.io/)
- [QCrossPlatformSerial README](thirdparty/QCrossPlatformSerial/README.md)
- [Qt for Android](https://doc.qt.io/qt-6/android.html)

## Contributing

Contributions are welcome! If you found a bug or want to suggest an improvement, please create an issue or pull request.

## Contact

For questions and suggestions regarding the use of the QCrossPlatformSerial library, please refer to the library documentation in the [`thirdparty/QCrossPlatformSerial/`](thirdparty/QCrossPlatformSerial/) directory.
