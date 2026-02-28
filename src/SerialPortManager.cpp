#include "SerialPortManager.hpp"
#include <QDebug>

SerialPortManager::SerialPortManager(QObject *parent)
    : QObject(parent)
{
    // Initialize with current available ports
    refreshPorts();
}

SerialPortManager::~SerialPortManager()
{
}

QVariantList SerialPortManager::availablePorts() const
{
    QVariantList result;

    // Convert QCrossPlatformSerialPortInfo objects to QVariantMap for QML
    for (const QCrossPlatformSerialPortInfo &port : m_availablePorts) {
        QVariantMap portMap;
        portMap["portName"] = port.portName();
        portMap["description"] = port.description();
        portMap["manufacturer"] = port.manufacturer();
        portMap["serialNumber"] = port.serialNumber();
        portMap["productIdentifier"] = port.productIdentifier();
        portMap["vendorIdentifier"] = port.vendorIdentifier();
        portMap["systemLocation"] = port.systemLocation();
        portMap["standardBaudRates"] = QVariant::fromValue(port.standardBaudRates());
        result.append(portMap);
    }

    return result;
}

void SerialPortManager::refreshPorts()
{
    // Clear existing ports
    m_availablePorts.clear();

    // Get new list of available ports using QCrossPlatformSerialPortInfo
    m_availablePorts = QCrossPlatformSerialPortInfo::availablePorts();

    // Emit signal to notify QML of the change
    emit availablePortsChanged();
}
