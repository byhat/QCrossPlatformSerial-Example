import QtQuick 2.15
import QtQuick.Controls 2.15

ApplicationWindow {
    visible: true
    width: 640
    height: 480
    title: "USB Communication Example"

    // Store the selected port name
    property string selectedPortName: ""

    Column {
        spacing: 20
        padding: 20

        // Row with refresh button and device list
        Row {
            spacing: 10
            
            Button {
                id: refreshButton
                text: "Обновить"
                onClicked: {
                    serialPortManager.refreshPorts()
                }
            }
            
            // Device list display
            ScrollView {
                width: 400
                height: 150
                clip: true
                
                ListView {
                    id: deviceListView
                    model: serialPortManager.availablePorts
                    spacing: 5
                    
                    delegate: Rectangle {
                        width: deviceListView.width
                        height: column.height + 10
                        color: selectedPortName === modelData.portName ? "#d0e0ff" : "#f0f0f0"
                        border.color: selectedPortName === modelData.portName ? "#0066cc" : "#cccccc"
                        border.width: selectedPortName === modelData.portName ? 2 : 1
                        radius: 4
                        
                        MouseArea {
                            anchors.fill: parent
                            onClicked: {
                                selectedPortName = modelData.portName
                                serialPortCommunicator.setPortName(selectedPortName)
                                console.log("Selected port:", selectedPortName)
                            }
                        }
                        
                        Column {
                            id: column
                            anchors.left: parent.left
                            anchors.right: parent.right
                            anchors.top: parent.top
                            anchors.margins: 5
                            spacing: 2
                            
                            Text {
                                text: "Порт: " + modelData.portName
                                font.bold: true
                            }
                            
                            Text {
                                text: "Описание: " + modelData.description
                            }
                            
                            Text {
                                text: "Производитель: " + (modelData.manufacturer || "N/A")
                                visible: modelData.manufacturer !== ""
                            }
                            
                            Text {
                                text: "Серийный номер: " + (modelData.serialNumber || "N/A")
                                visible: modelData.serialNumber !== ""
                            }
                        }
                    }
                    
                    // Update list when availablePortsChanged signal is emitted
                    Connections {
                        target: serialPortManager
                        function onAvailablePortsChanged() {
                            // List will automatically update due to binding
                        }
                    }
                }
            }
        }

        Button {
            text: selectedPortName ? "Подключиться к: " + selectedPortName : "Подключиться к устройству"
            enabled: selectedPortName !== ""
            onClicked: {
                console.log("Attempting to connect to port:", selectedPortName)
                if (serialPortCommunicator.connectToDevice()) {
                    console.log("Устройство подключено")
                } else {
                    console.log("Ошибка подключения")
                }
            }
        }
        
        Button {
            text: "Отключить устройство"
            enabled: serialPortCommunicator.isPortConnected()
            onClicked: {
                serialPortCommunicator.closePort()
                console.log("Устройство отключено")
            }
        }

        Button {
            text: "Прочитать данные"
            onClicked: {
                serialPortCommunicator.fetchData()
            }
        }

        Button {
            text: "Отправить команду"
            onClicked: {
                serialPortCommunicator.sendData("Команда")
            }
        }

        Button {
            text: "Отправить объект (JSON)"
            onClicked: {
                // Пример: отправляем объект со своими данными
                serialPortCommunicator.sendObject("TestObj", 55.7558, 37.6173, 123)
            }
        }

        Text {
            id: dataText
            text: "Данные устройства: "
        }

        // Подписка на сигналы из C++
        Connections {
            target: serialPortCommunicator

            onDataRead: {
                dataText.text = "Данные устройства: " + data
            }

            onMessageDisplay: {
                console.log("Message:", message)
            }
        }
    }
}
