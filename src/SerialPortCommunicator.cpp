#include "SerialPortCommunicator.hpp"
#include <QDebug>
#include <QJsonObject>
#include <QJsonDocument>
#include <QIODevice>

SerialPortCommunicator::SerialPortCommunicator(QObject *parent)
    : QObject(parent)
    , m_serialPort(new QCrossPlatformSerialPort(this))
{
    connect(m_serialPort, &QCrossPlatformSerialPort::readyRead,
            this, &SerialPortCommunicator::onReadyRead);
}

SerialPortCommunicator::~SerialPortCommunicator()
{
    if (m_serialPort && m_serialPort->isOpen()) {
        m_serialPort->close();
    }
}

void SerialPortCommunicator::init()
{
    qDebug() << "SerialPortCommunicator initialized";
}

bool SerialPortCommunicator::connectToDevice()
{
    if (m_portName.isEmpty()) {
        qWarning() << "Port name not set. Call setPortName() first.";
        return false;
    }

    m_serialPort->setPortName(m_portName);
    return m_serialPort->open(QIODevice::ReadWrite);
}

QString SerialPortCommunicator::readData()
{
    QByteArray data = m_serialPort->readAll();
    return QString::fromUtf8(data);
}

bool SerialPortCommunicator::isPortConnected()
{
    return m_serialPort->isOpen();
}

bool SerialPortCommunicator::sendData(const QString &data)
{
    return m_serialPort->write(data.toUtf8()) >= 0;
}

bool SerialPortCommunicator::sendObject(const QString &name,
                                         double latitude,
                                         double longitude,
                                         int code)
{
    QJsonObject jsonObject;
    jsonObject["name"] = name;
    jsonObject["latitude"] = latitude;
    jsonObject["longitude"] = longitude;
    jsonObject["code"] = code;

    QJsonDocument jsonDoc(jsonObject);
    QString jsonString = jsonDoc.toJson(QJsonDocument::Compact);

    return sendData(jsonString);
}

void SerialPortCommunicator::setPortName(const QString &portName)
{
    m_portName = portName;
}

QString SerialPortCommunicator::portName() const
{
    return m_portName;
}

bool SerialPortCommunicator::openPort()
{
    if (m_portName.isEmpty()) {
        qWarning() << "Port name not set. Call setPortName() first.";
        return false;
    }

    m_serialPort->setPortName(m_portName);
    return m_serialPort->open(QIODevice::ReadWrite);
}

void SerialPortCommunicator::closePort()
{
    m_serialPort->close();
}

void SerialPortCommunicator::onReadyRead()
{
    QString data = readData();
    if (!data.isEmpty()) {
        emit dataRead(data);
    }
}

void SerialPortCommunicator::fetchData()
{
    QString data = readData();
    emit dataRead(data);
}
