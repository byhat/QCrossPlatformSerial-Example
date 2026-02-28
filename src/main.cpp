#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include "SerialPortCommunicator.hpp"  // Подключаем SerialPortCommunicator для QML интеграции
#include "SerialPortManager.hpp"  // Подключаем SerialPortManager для QML интеграции

int main(int argc, char *argv[]) {
    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    SerialPortCommunicator serialPortCommunicator;
    SerialPortManager serialPortManager;

    // Инициализация SerialPortCommunicator и запрос разрешений в Java-коде (Android)
    serialPortCommunicator.init();
    engine.rootContext()->setContextProperty("serialPortCommunicator", &serialPortCommunicator);
    engine.rootContext()->setContextProperty("serialPortManager", &serialPortManager);
    
    // Загружаем начальный список устройств
    serialPortManager.refreshPorts();

    const QUrl url("qrc:/main.qml");
    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
                         if (!obj && url == objUrl)
                             QCoreApplication::exit(-1);
                     }, Qt::QueuedConnection);
    engine.load(url);

    return app.exec();
}
