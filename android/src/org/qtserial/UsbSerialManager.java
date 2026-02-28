package org.qtserial;

import android.app.PendingIntent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.qtproject.qt.android.QtNative;

import java.io.IOException;
import java.util.List;

public class UsbSerialManager {

    private static UsbManager usbManager;
    private static UsbSerialPort serialPort;
    private static UsbDevice currentDevice;
    private static UsbPermissionListener usbPermissionListener;
    private static UsbSerialReaderThread readerThread;
    private static boolean isReceiverRegistered = false;
    private static int lastReadChannel = -1;  // Добавляем переменную для хранения последнего канала
    private static String lastReadData = "";  // Buffer for storing last read data
    private static final Object dataLock = new Object();  // Lock for thread-safe data access

    private static final String ACTION_USB_PERMISSION = "org.qtserial.USB_PERMISSION";
    private static final int PERMISSION_REQUEST_CODE = 1;

    // Инициализация USB Manager и запрос разрешений
    public static void init(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        // Проверяем разрешение на запись в хранилище
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                showToast(context, "Cannot request permission, context is not an Activity");
            }
        } else {
            showToast(context, "Permission already granted");
        }

        // Регистрация BroadcastReceiver для обработки разрешения на USB
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
        }
    }

    // Интерфейс для обработки разрешений на USB
    public interface UsbPermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    // Метод для подключения к USB-устройству (public for JNI access)
    public static boolean connectToDevice(Context context) {
        if (usbManager == null) {
            init(context);
            showToast(context, "UsbManager was not initialized. Initialized now.");
        }

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            showToast(context, "No USB devices found.");
            return false;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        currentDevice = driver.getDevice();

        // Запрос разрешения на использование устройства
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        usbManager.requestPermission(currentDevice, permissionIntent);

        // После получения разрешения, проверяем, открыт ли порт
        if (usbManager.hasPermission(currentDevice)) {
            try {
                serialPort = driver.getPorts().get(0);
                openSerialConnection(serialPort, context);
                if (serialPort != null && serialPort.isOpen()) {
                    showToast(context, "Successfully connected to USB device and port is open.");
                    return true;
                } else {
                    showToast(context, "Failed to open serial port.");
                    return false;
                }
            } catch (Exception e) {
                showToast(context, "Error opening serial port: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            showToast(context, "Permission denied for USB device.");
            return false;
        }
    }

    // Метод для установки слушателя на разрешение USB
    public static void setUsbPermissionListener(UsbPermissionListener listener) {
        usbPermissionListener = listener;
    }

    // BroadcastReceiver для обработки запроса разрешения на использование USB-устройства
    private static final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null && device.equals(currentDevice)) {
                            // Разрешение получено, открываем порт
                            try {
                                UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
                                if (driver != null && !driver.getPorts().isEmpty()) {
                                    serialPort = driver.getPorts().get(0);
                                    openSerialConnection(serialPort, context);
                                    if (usbPermissionListener != null) {
                                        usbPermissionListener.onPermissionGranted();
                                    }
                                } else {
                                    showToast(context, "No available serial ports on the device.");
                                }
                            } catch (Exception e) {
                                showToast(context, "Error opening serial connection: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            showToast(context, "Device is null or doesn't match current device.");
                        }
                    } else {
                        // Разрешение отклонено
                        showToast(context, "Permission denied for USB device.");
                        if (usbPermissionListener != null) {
                            usbPermissionListener.onPermissionDenied();
                        }
                    }
                }
            }
        }
    };

    // Метод для открытия последовательного соединения (public for JNI access)
    public static void openSerialConnection(UsbSerialPort port, Context context) {
        try {
            if (port != null) {
                serialPort.open(usbManager.openDevice(port.getDriver().getDevice()));
                serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                showToast(context, "Serial connection opened successfully.");

                // Запускаем поток для постоянного чтения данных
                readerThread = new UsbSerialReaderThread(context);
                readerThread.start();
            } else {
                showToast(context, "Serial port is null. Cannot open connection.");
            }
        } catch (IOException e) {
            showToast(context, "Error opening serial connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для отправки данных через последовательный порт
    public static boolean sendData(String data, Context context) {
        if (serialPort != null) {
            if (serialPort.isOpen()) {
                if (!data.isEmpty()) {
                    data += "\n"; // Добавляем символ новой строки
                    try {
                        serialPort.write(data.getBytes(), 1000); // Отправка данных
                        showToast(context, "Data sent: " + data);
                        return true;
                    } catch (IOException e) {
                        showToast(context, "Error sending data: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    showToast(context, "Data is empty.");
                    return false;
                }
            } else {
                showToast(context, "Serial port is not open.");
            }
        } else {
            showToast(context, "Serial port is not connected.");
        }
        return false; // Порт не подключен или данные пусты
    }

    // Метод для безопасного отображения Toast сообщения
    public static void showToast(final Context context, final String message) {
        // Проверка, если мы не на основном потоке, переключаемся на главный поток
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            // Переключаемся на главный поток через Handler
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Kласс для постоянного чтения данных с устройства
    private static class UsbSerialReaderThread extends Thread {
        private Context context;

        UsbSerialReaderThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[64];
            while (!isInterrupted() && serialPort != null && serialPort.isOpen()) {
                try {
                    int numBytesRead = serialPort.read(buffer, 1000);
                    if (numBytesRead > 0) {
                        // Формирование строки в шестнадцатеричном формате
                        StringBuilder hexString = new StringBuilder();
                        for (int i = 0; i < numBytesRead; i++) {
                            hexString.append(String.format("%02X ", buffer[i]));
                        }

                        // Преобразование данных в строку ASCII
                        String asciiString = new String(buffer, 0, numBytesRead);

                        // Отображение данных как в шестнадцатеричном формате, так и в ASCII
                        showToast(context, "Data received (hex): " + hexString.toString());
                        showToast(context, "\nData received (ASCII): " + asciiString);

                        // Store the data in the buffer for JNI access
                        synchronized (dataLock) {
                            lastReadData = asciiString;
                        }

                        // Проверяем, если пришло значение канала
                        if (numBytesRead >= 4 && buffer[0] == (byte) 0xC1 && buffer[1] == 0x05 && buffer[2] == 0x01) {
                            // Преобразуем четвертый байт в десятичное значение канала
                            int channel = buffer[3] & 0xFF;
                            lastReadChannel = channel; // Сохраняем канал
                            showToast(context, "Channel received: " + channel);
                        }
                    }
                } catch (IOException e) {
                    showToast(context, "Error reading data: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }



    // Метод для чтения данных с устройства (JNI access)
    public static String readData() {
        synchronized (dataLock) {
            if (!lastReadData.isEmpty()) {
                String data = lastReadData;
                lastReadData = "";  // Clear after reading
                return data;
            }
        }
        return "";
    }

    // Method to send command to read
    public static void sendReadChannelCommand(Context context) {
        showToast(context, "Sending command to read channel...");
        if (serialPort != null && serialPort.isOpen()) {
            byte[] command = new byte[]{(byte) 0xC1, 0x05, 0x01};  // Command to request channel
            try {
                serialPort.write(command, 2000);  // Send command with a 2000ms timeout
                showToast(context, "Command to read channel sent. Awaiting response...");
            } catch (IOException e) {
                showToast(context, "Error sending read channel command: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showToast(context, "Port is not connected or is closed.");
        }
    }

    // Adjusted readChannel method to only read the response after command is sent
    public static int readChannel(Context context) {
        showToast(context, "Reading channel...");
        if (serialPort != null && serialPort.isOpen()) {
            byte[] command = new byte[]{(byte) 0xC1, 0x05, 0x01};
            try {
                serialPort.write(command, 2000);  // Отправляем команду на чтение канала

                // Теперь ждем ответ с данными о канале
                byte[] response = new byte[64];
                int numBytesRead = serialPort.read(response, 5000);

                if (numBytesRead >= 4 && response[0] == (byte) 0xC1 && response[1] == 0x05 && response[2] == 0x01) {
                    // Преобразуем четвертый байт в десятичное значение канала
                    int channel = response[3] & 0xFF;
                    lastReadChannel = channel;  // Сохраняем канал

                    showToast(context, "Channel read: " + channel);
                    return channel;  // Возвращаем значение канала
                } else {
                    showToast(context, "Failed to read complete response or unexpected format");
                    return -1;  // Ошибка чтения канала
                }
            } catch (IOException e) {
                showToast(context, "Error reading channel: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } else {
            showToast(context, "Port is not connected or is closed.");
            return -1;
        }
    }


    // Метод для установки канала
    // Метод для установки канала
    public static boolean setChannel(Context context, int channel) {
        if (serialPort != null && serialPort.isOpen() && channel >= 0 && channel <= 255) {
            byte[] command = new byte[]{(byte) 0xC0, 0x05, 0x01, (byte) channel};
            try {
                serialPort.write(command, 1000); // Отправляем команду на установку канала

                // Опционально читаем ответ для подтверждения
                byte[] response = new byte[64];
                int numBytesRead = serialPort.read(response, 5000);

                if (numBytesRead >= 4) {
                    StringBuilder hexString = new StringBuilder();
                    for (int i = 0; i < numBytesRead; i++) {
                        hexString.append(String.format("%02X ", response[i]));
                    }
                    showToast(context, "Set channel response (hex): " + hexString.toString());
                    return true;
                } else {
                    showToast(context, "Failed to receive confirmation of channel set");
                    return false;
                }
            } catch (IOException e) {
                showToast(context, "Error setting channel: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            showToast(context, "Port is not connected or is closed, or channel is invalid.");
            return false;
        }
    }


    // Метод для проверки подключения порта
    public static boolean isPortConnected(Context context) {
        return serialPort != null && serialPort.isOpen();
    }

    // Метод для остановки потока чтения данных
    public static void stopReaderThread() {
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
            readerThread = null;

        }
    }

    // JNI method: Request USB permissions (called from C++)
    public static void requestUsbPermissions(Context context) {
        if (usbManager == null) {
            init(context);
        }

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (!availableDrivers.isEmpty()) {
            UsbSerialDriver driver = availableDrivers.get(0);
            currentDevice = driver.getDevice();

            if (!usbManager.hasPermission(currentDevice)) {
                PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0,
                        new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(currentDevice, permissionIntent);
                showToast(context, "USB permission requested");
            } else {
                showToast(context, "USB permission already granted");
            }
        } else {
            showToast(context, "No USB devices found to request permission");
        }
    }

    // JNI method: Set channel without Context parameter (wrapper for C++)
    public static boolean setChannel(int channel) {
        // Get context from Qt Native
        try {
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);
            if (activity != null) {
                return setChannel(activity, channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // JNI method: Read channel without Context parameter (wrapper for C++)
    public static int readChannel() {
        // Get context from Qt Native
        try {
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);
            if (activity != null) {
                return readChannel(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // JNI method: Send read channel command without Context parameter (wrapper for C++)
    public static void sendReadChannelCommand() {
        // Get context from Qt Native
        try {
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);
            if (activity != null) {
                sendReadChannelCommand(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JNI method: Send data without Context parameter (wrapper for C++)
    public static boolean sendData(String data) {
        // Get context from Qt Native
        try {
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);
            if (activity != null) {
                return sendData(data, activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // JNI method: Show toast without Context parameter (wrapper for C++)
    public static void showToast(String message) {
        // Get context from Qt Native
        try {
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);
            if (activity != null) {
                showToast(activity, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JNI method: Check if port is connected without Context parameter (wrapper for C++)
    public static boolean isPortConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    // JNI method: Get the last read channel value
    public static int getLastReadChannel() {
        return lastReadChannel;
    }
}
