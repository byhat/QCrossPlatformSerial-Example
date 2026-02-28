package org.qtserial;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;

import com.hoho.android.usbserial.driver.*;
import org.qtserial.SerialInputOutputManager;

import org.qtproject.qt.android.bindings.QtActivity;
import org.qtproject.qt.android.QtNative;

import org.json.JSONArray;
import org.json.JSONObject;

public class JniUsbSerial
{
    private static UsbManager usbManager;
    private static HashMap<String, UsbSerialPort> m_usbSerialPort;
    private static HashMap<String, SerialInputOutputManager> m_usbIoManager;

    private static native void nativeDeviceException(int classPoint, String messageA);
    private static native void nativeDeviceNewData(int classPoint, byte[] dataA);

    public JniUsbSerial()
    {
        //m_instance = this;
        m_usbIoManager = new HashMap<String, SerialInputOutputManager>();
        m_usbSerialPort = new HashMap<String, UsbSerialPort>();
    }

    private static boolean getCurrentDevices()
    {
        try {
            // Use reflection to access QtNative.activity() in Qt6
            java.lang.reflect.Method activityMethod = org.qtproject.qt.android.QtNative.class.getDeclaredMethod("activity");
            activityMethod.setAccessible(true);
            Activity activity = (Activity) activityMethod.invoke(null);

            if (activity == null)
                return false;

            if (usbManager == null)
                usbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONArray availableDevicesInfo()
    {
        //  GET THE LIST OF CURRENT DEVICES
        if (!getCurrentDevices())
            return null;

        if (usbManager.getDeviceList().size() < 1)
            return null;

        JSONArray devicesArray = new JSONArray();

        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();

        for(UsbDevice deviceL : usbManager.getDeviceList().values()) {

            UsbSerialDriver driverL = usbDefaultProber.probeDevice(deviceL);

            JSONObject deviceInfo = new JSONObject();
            try {
                // portName - String (e.g., "/dev/bus/usb/001/002")
                String portName = deviceL.getDeviceName();
                deviceInfo.put("portName", portName);

                // description - String (e.g., "USB Serial Port")
                String description = "USB Serial Port";
                if (driverL != null) {
                    if (driverL instanceof CdcAcmSerialDriver) {
                        description = "CDC ACM Serial Port";
                    } else if (driverL instanceof Ch34xSerialDriver) {
                        description = "CH34x Serial Port";
                    } else if (driverL instanceof CommonUsbSerialPort) {
                        description = "Common USB Serial Port";
                    } else if (driverL instanceof Cp21xxSerialDriver) {
                        description = "CP21xx Serial Port";
                    } else if (driverL instanceof FtdiSerialDriver) {
                        description = "FTDI Serial Port";
                    } else if (driverL instanceof ProlificSerialDriver) {
                        description = "Prolific Serial Port";
                    }
                } else {
                    description = "Unknown USB Device";
                }
                deviceInfo.put("description", description);

                // manufacturer - String
                String manufacturer = deviceL.getManufacturerName();
                if (manufacturer == null) manufacturer = "";
                deviceInfo.put("manufacturer", manufacturer);

                // serialNumber - String
                String serialNumber = deviceL.getSerialNumber();
                if (serialNumber == null) serialNumber = "";
                deviceInfo.put("serialNumber", serialNumber);

                // productId - int
                deviceInfo.put("productId", deviceL.getProductId());

                // vendorId - int
                deviceInfo.put("vendorId", deviceL.getVendorId());

                // systemLocation - String
                // Construct a system location based on device name
                String systemLocation = "/dev/bus/usb/" + portName;
                deviceInfo.put("systemLocation", systemLocation);

                devicesArray.put(deviceInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return devicesArray;
    }

    public static boolean setParameters(String portNameA, int baudRateA, int dataBitsA, int stopBitsA, int parityA)
    {
        if (m_usbSerialPort.size() <= 0)
            return false;

        if (m_usbSerialPort.get(portNameA) == null)
            return false;

        try
        {
            m_usbSerialPort.get(portNameA).setParameters(baudRateA, dataBitsA, stopBitsA, parityA);
            return true;
        }
        catch(IOException eA)
        {
            return false;
        }
    }

    public static void stopIoManager(String portNameA)
    {
        if (m_usbIoManager.get(portNameA) == null)
            return;

        m_usbIoManager.get(portNameA).stop();
        m_usbIoManager.remove(portNameA);
    }

    public static void startIoManager(String portNameA, int classPoint)
    {
        if (m_usbSerialPort.get(portNameA) == null)
            return;

        SerialInputOutputManager usbIoManager = new SerialInputOutputManager(m_usbSerialPort.get(portNameA), m_Listener, classPoint);

        m_usbIoManager.put(portNameA, usbIoManager);
        m_usbIoManager.get(portNameA).start();
    }

    public static boolean close(String portNameA)
    {
        if (m_usbSerialPort.get(portNameA) == null)
            return false;

        try
        {
            stopIoManager(portNameA);
            m_usbSerialPort.get(portNameA).close();
            m_usbSerialPort.remove(portNameA);

            return true;
        }
        catch (IOException eA)
        {
            return false;
        }
    }

    public static int open(String portNameA, int classPoint)
    {
        //  GET THE LIST OF CURRENT DEVICES
        if (!getCurrentDevices())
            return 0;

        if (usbManager.getDeviceList().size() < 1)
            return 0;

        if (m_usbSerialPort.get(portNameA) != null)
            return 0;

        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        for(UsbDevice deviceL : usbManager.getDeviceList().values()) {

            if (portNameA.equals(deviceL.getDeviceName()))
            {
            }
            else
            {
                continue;
            }

            UsbSerialDriver driverL = usbDefaultProber.probeDevice(deviceL);
            if (driverL == null)
            {
                return 0;
            }

            UsbDeviceConnection connectionL = usbManager.openDevice(driverL.getDevice());
            if (connectionL == null) {
                return 0;
            }

            UsbSerialPort usbSerialPort = driverL.getPorts().get(0);

            try{
                usbSerialPort.open(connectionL);
                m_usbSerialPort.put(portNameA ,usbSerialPort);

                startIoManager(portNameA, classPoint);

                return 1;
            }
            catch (Exception e) {
                m_usbSerialPort.remove(portNameA);
                stopIoManager(portNameA);
                return 0;
            }
        }
        return 0;
    }

    public static int write(String portNameA, byte[] sourceA, int timeoutMSecA)
    {
        if (m_usbSerialPort.get(portNameA) == null)
            return 0;

        try
        {
            System.out.println("Serial write:" + new String(sourceA, StandardCharsets.UTF_8));
            m_usbSerialPort.get(portNameA).write(sourceA, timeoutMSecA);
        }
        catch (IOException eA)
        {
            return 0;
        }

        return 1;
    }

    // SerialInputOutputManager.Listener

    private final static SerialInputOutputManager.Listener m_Listener =
    new SerialInputOutputManager.Listener()
    {
        @Override
        public void onNewData(byte[] data, int classPoint) {
            nativeDeviceNewData(classPoint, data);
            System.out.println("Serial read:" + new String(data, StandardCharsets.UTF_8));
        };

        @Override
        public void onRunError(Exception e, int classPoint) {
            nativeDeviceException(classPoint, e.getMessage());
        }
    };
    //
}
