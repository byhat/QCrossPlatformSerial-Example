#ifndef SERIALPORTCOMMUNICATOR_H
#define SERIALPORTCOMMUNICATOR_H

#include <QObject>
#include <QString>
#include <QByteArray>
#include <QCrossPlatformSerialPort.hpp>

/**
 * @brief The SerialPortCommunicator class provides a QML-accessible interface for serial port communication.
 *
 * This class wraps QCrossPlatformSerialPort and provides QML-accessible methods and signals.
 * It maintains compatibility with the previous UsbController interface while using
 * QCrossPlatformSerialPort as the underlying implementation.
 */
class SerialPortCommunicator : public QObject
{
    Q_OBJECT

public:
    /**
     * @brief Constructs a SerialPortCommunicator object.
     * @param parent Parent QObject
     */
    explicit SerialPortCommunicator(QObject *parent = nullptr);

    /**
     * @brief Destroys the SerialPortCommunicator object.
     */
    ~SerialPortCommunicator();

    /**
     * @brief Initializes the serial port communicator.
     * Platform-specific initialization is handled by QCrossPlatformSerialPort.
     */
    Q_INVOKABLE void init();

    /**
     * @brief Connects to the serial port device.
     * @return true if successful, false otherwise
     */
    Q_INVOKABLE bool connectToDevice();

    /**
     * @brief Reads data from the serial port.
     * @return The data read as a string
     */
    Q_INVOKABLE QString readData();

    /**
     * @brief Checks if the port is connected.
     * @return true if connected, false otherwise
     */
    Q_INVOKABLE bool isPortConnected();

    /**
     * @brief Sends data to the serial port.
     * @param data The data to send
     * @return true if successful, false otherwise
     */
    Q_INVOKABLE bool sendData(const QString &data);

    /**
     * @brief Sends an object as JSON to the serial port.
     * @param name The object name
     * @param latitude The latitude value
     * @param longitude The longitude value
     * @param code The code value
     * @return true if successful, false otherwise
     */
    Q_INVOKABLE bool sendObject(const QString &name,
                                double latitude,
                                double longitude,
                                int code);

    /**
     * @brief Sets the port name for connection.
     * @param portName The port name
     */
    Q_INVOKABLE void setPortName(const QString &portName);

    /**
     * @brief Returns the current port name.
     * @return The port name
     */
    Q_INVOKABLE QString portName() const;

    /**
     * @brief Opens the serial port.
     * @return true if successful, false otherwise
     */
    Q_INVOKABLE bool openPort();

    /**
     * @brief Closes the serial port.
     */
    Q_INVOKABLE void closePort();

signals:
    /**
     * @brief Emitted when data is read from the serial port.
     * @param data The data read
     */
    void dataRead(QString data);

    /**
     * @brief Emitted when a message should be displayed to the user.
     * @param message The message to display
     */
    void messageDisplay(const QString &message);

public slots:
    /**
     * @brief Fetches data from the serial port and emits dataRead signal.
     */
    void fetchData();

private slots:
    /**
     * @brief Handles the readyRead signal from QCrossPlatformSerialPort.
     */
    void onReadyRead();

private:
    QCrossPlatformSerialPort *m_serialPort;
    QString m_portName;
};

#endif // SERIALPORTCOMMUNICATOR_H
