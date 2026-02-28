#pragma once

#include <QObject>
#include <QVariantList>
#include <QCrossPlatformSerialPortInfo.hpp>

/**
 * @brief The SerialPortManager class manages serial port info for QML.
 *
 * This class provides a QML-accessible interface to query available serial ports.
 * It uses QCrossPlatformSerialPortInfo internally to provide cross-platform support.
 *
 * This class handles all QML integration (Q_PROPERTY, Q_INVOKABLE, signals).
 */
class SerialPortManager : public QObject
{
    Q_OBJECT
    Q_PROPERTY(QVariantList availablePorts READ availablePorts NOTIFY availablePortsChanged)

public:
    /**
     * @brief Constructs a SerialPortManager object.
     * @param parent Parent QObject
     */
    explicit SerialPortManager(QObject *parent = nullptr);

    /**
     * @brief Destroys the SerialPortManager object.
     */
    ~SerialPortManager();

    /**
     * @brief Returns the list of available serial ports as QVariantList for QML.
     * @return QVariantList of available ports
     */
    QVariantList availablePorts() const;

public slots:
    /**
     * @brief Refreshes the list of available serial ports.
     *
     * This method queries the system for available serial ports and
     * emits the availablePortsChanged signal when done.
     */
    Q_INVOKABLE void refreshPorts();

signals:
    /**
     * @brief Emitted when the list of available ports changes.
     */
    void availablePortsChanged();

private:
    QList<QCrossPlatformSerialPortInfo> m_availablePorts;
};
