package com.example.test1;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialog extends Dialog<Map<String, Object>> {

    private final TextField deviceLabelField = new TextField();
    private final TextField vendorField = new TextField();
    private final TextField typeSWField = new TextField();
    private final TextField versionSWField = new TextField();
    private final TextField typeHWField = new TextField();
    private final TextField versionHWField = new TextField();
    private final TextField partNumberField = new TextField();
    private final TextField serialNumberField = new TextField();
    private final TextField placementField = new TextField();
    private final ComboBox<String> flagIsFirewallCombo = new ComboBox<>();
    private final ComboBox<NetworkDevice.networkDeviceType> deviceType = new ComboBox<>();

    private final ObservableList<Map<String, Control>> networkInterfaces = FXCollections.observableArrayList();
    private final VBox networkInterfacesContainer = new VBox(5);

    public CustomDialog() {
        setTitle("Добавление устройства");
        setHeaderText("Введите данные об устройстве");

        // Настройка кнопок
        ButtonType submitButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Основной GridPane для фиксированных полей
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Добавляем фиксированные поля
        grid.add(new Label("Метка устройства:"), 0, 0);
        grid.add(deviceLabelField, 1, 0);

        grid.add(new Label("Производитель:"), 0, 1);
        grid.add(vendorField, 1, 1);

        grid.add(new Label("Тип ПО:"), 0, 2);
        grid.add(typeSWField, 1, 2);

        grid.add(new Label("Версия ПО:"), 0, 3);
        grid.add(versionSWField, 1, 3);

        grid.add(new Label("Тип Железа:"), 0, 4);
        grid.add(typeHWField, 1, 4);

        grid.add(new Label("Версия Железа:"), 0, 5);
        grid.add(versionHWField, 1, 5);

        grid.add(new Label("Парт номер:"), 0, 6);
        grid.add(partNumberField, 1, 6);

        grid.add(new Label("Серийный номер:"), 0, 7);
        grid.add(serialNumberField, 1, 7);

        grid.add(new Label("Размещение:"), 0, 8);
        grid.add(placementField, 1, 8);

        grid.add(new Label("Это фаервол?"), 0, 9);
        flagIsFirewallCombo.getItems().addAll("Да", "Нет");
        flagIsFirewallCombo.setValue("Нет");
        grid.add(flagIsFirewallCombo, 1, 9);

        grid.add(new Label("Тип девайса:"), 0, 10);
        deviceType.getItems().setAll(NetworkDevice.networkDeviceType.values());
        deviceType.setValue(NetworkDevice.networkDeviceType.UNKNOWN);
        grid.add(deviceType, 1, 10);

        // Секция для сетевых интерфейсов
        Label interfacesLabel = new Label("Сетевые интерфейсы:");
        Button addInterfaceBtn = new Button("Добавить интерфейс");
        addInterfaceBtn.setOnAction(e -> addNetworkInterfaceFields());

        grid.add(interfacesLabel, 0, 11);
        grid.add(addInterfaceBtn, 1, 11);
        grid.add(networkInterfacesContainer, 0, 12, 2, 1);

        // Создаем ScrollPane и добавляем в него наш grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Настраиваем растягивание содержимого
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(grid, Priority.ALWAYS);

        // Устанавливаем ScrollPane как содержимое диалога
        getDialogPane().setContent(scrollPane);

        // Увеличиваем размер диалогового окна
        getDialogPane().setPrefSize(600, 600);

        // Преобразование результата при нажатии OK
        setResultConverter(buttonType -> {
            if (buttonType == submitButtonType) {
                Map<String, Object> result = new HashMap<>();

                // Основные поля
                result.put("deviceLabel", deviceLabelField.getText());
                result.put("vendor", vendorField.getText());
                result.put("typeSW", typeSWField.getText());
                result.put("versionSW", versionSWField.getText());
                result.put("typeHW", typeHWField.getText());
                result.put("versionHW", versionHWField.getText());
                result.put("partNumber", partNumberField.getText());
                result.put("serialNumber", serialNumberField.getText());
                result.put("placement", placementField.getText());
                result.put("flagIsFirewall", flagIsFirewallCombo.getValue().equals("Да"));
                result.put("devicetype", deviceType.getValue());

                // Собираем сетевые интерфейсы
                List<Map<String, Object>> interfaces = new ArrayList<>();
                for (Map<String, Control> interfaceFields : networkInterfaces) {
                    Map<String, Object> interfaceData = new HashMap<>();

                    interfaceData.put("speed", Integer.parseInt(((TextField)interfaceFields.get("speed")).getText()));
                    int[] ipAddress = new int[4];
                    ipAddress[0] = Integer.parseInt(((TextField)interfaceFields.get("ipaddress_byte1")).getText());
                    ipAddress[1] = Integer.parseInt(((TextField)interfaceFields.get("ipaddress_byte2")).getText());
                    ipAddress[2] = Integer.parseInt(((TextField)interfaceFields.get("ipaddress_byte3")).getText());
                    ipAddress[3] = Integer.parseInt(((TextField)interfaceFields.get("ipaddress_byte4")).getText());
                    interfaceData.put("ipAddress", ipAddress);

                    int[] ipMask = new int[4];
                    ipMask[0] = Integer.parseInt(((TextField)interfaceFields.get("ipmask_byte1")).getText());
                    ipMask[1] = Integer.parseInt(((TextField)interfaceFields.get("ipmask_byte2")).getText());
                    ipMask[2] = Integer.parseInt(((TextField)interfaceFields.get("ipmask_byte3")).getText());
                    ipMask[3] = Integer.parseInt(((TextField)interfaceFields.get("ipmask_byte4")).getText());
                    interfaceData.put("ipMask", ipMask);

                    int[] gateway = new int[4];
                    gateway[0] = Integer.parseInt(((TextField)interfaceFields.get("gateway1")).getText());
                    gateway[1] = Integer.parseInt(((TextField)interfaceFields.get("gateway2")).getText());
                    gateway[2] = Integer.parseInt(((TextField)interfaceFields.get("gateway3")).getText());
                    gateway[3] = Integer.parseInt(((TextField)interfaceFields.get("gateway4")).getText());
                    interfaceData.put("defaultGateway", gateway);

                    interfaceData.put("interfaceLabel", ((TextField)interfaceFields.get("interfaceLabel")).getText());
                    interfaceData.put("flagIsRoutable", ((ComboBox<String>)interfaceFields.get("flagIsRoutable")).getValue().equals("Да"));

                    interfaces.add(interfaceData);
                }
                result.put("networkInterfaces", interfaces);

                return result;
            }
            return null;
        });
    }

    private void addNetworkInterfaceFields() {
        // Создаем поля для интерфейса
        TextField speedField = new TextField();
        speedField.setPromptText("Скорость (Mbps)");

        TextField ipAddress1Field = new TextField();
        ipAddress1Field.setPromptText("IP адрес 1 байт");
        TextField ipAddress2Field = new TextField();
        ipAddress2Field.setPromptText("IP адрес 2 байт");
        TextField ipAddress3Field = new TextField();
        ipAddress3Field.setPromptText("IP адрес 3 байт");
        TextField ipAddress4Field = new TextField();
        ipAddress4Field.setPromptText("IP адрес 4 байт");

        TextField ipMask1Field = new TextField();
        ipMask1Field.setPromptText("IP маска 1 байт");
        TextField ipMask2Field = new TextField();
        ipMask2Field.setPromptText("IP маска 2 байт");
        TextField ipMask3Field = new TextField();
        ipMask3Field.setPromptText("IP маска 3 байт");
        TextField ipMask4Field = new TextField();
        ipMask4Field.setPromptText("IP маска 4 байт");

        TextField gateway1Field = new TextField();
        gateway1Field.setPromptText("Шлюз 1");
        TextField gateway2Field = new TextField();
        gateway2Field.setPromptText("Шлюз 2");
        TextField gateway3Field = new TextField();
        gateway3Field.setPromptText("Шлюз 3");
        TextField gateway4Field = new TextField();
        gateway4Field.setPromptText("Шлюз 4");

        TextField interfaceLabelField = new TextField();
        interfaceLabelField.setPromptText("Метка интерфейса");

        ComboBox<String> flagIsRoutableCombo = new ComboBox<>();
        flagIsRoutableCombo.getItems().addAll("Да", "Нет");
        flagIsRoutableCombo.setValue("Да");

        // Собираем все поля в мапу для удобного доступа
        Map<String, Control> interfaceFields = new HashMap<>();
        interfaceFields.put("speed", speedField);
        interfaceFields.put("ipaddress_byte1", ipAddress1Field);
        interfaceFields.put("ipaddress_byte2", ipAddress2Field);
        interfaceFields.put("ipaddress_byte3", ipAddress3Field);
        interfaceFields.put("ipaddress_byte4", ipAddress4Field);
        interfaceFields.put("ipmask_byte1", ipMask1Field);
        interfaceFields.put("ipmask_byte2", ipMask2Field);
        interfaceFields.put("ipmask_byte3", ipMask3Field);
        interfaceFields.put("ipmask_byte4", ipMask4Field);
        interfaceFields.put("gateway1", gateway1Field);
        interfaceFields.put("gateway2", gateway2Field);
        interfaceFields.put("gateway3", gateway3Field);
        interfaceFields.put("gateway4", gateway4Field);
        interfaceFields.put("interfaceLabel", interfaceLabelField);
        interfaceFields.put("flagIsRoutable", flagIsRoutableCombo);

        // Создаем панель для полей интерфейса
        GridPane interfacePane = new GridPane();
        interfacePane.setHgap(5);
        interfacePane.setVgap(5);
        interfacePane.setPadding(new Insets(10));
        interfacePane.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-padding: 5;");

        // Добавляем поля на панель
        interfacePane.add(new Label("Скорость:"), 0, 0);
        interfacePane.add(speedField, 1, 0);

        interfacePane.add(new Label("IP адрес:"), 0, 1);
        GridPane ipAddressPane = new GridPane();
        ipAddressPane.setHgap(5);
        ipAddressPane.add(ipAddress1Field, 0, 0);
        ipAddressPane.add(new Label("."), 1, 0);
        ipAddressPane.add(ipAddress2Field, 2, 0);
        ipAddressPane.add(new Label("."), 3, 0);
        ipAddressPane.add(ipAddress3Field, 4, 0);
        ipAddressPane.add(new Label("."), 5, 0);
        ipAddressPane.add(ipAddress4Field, 6, 0);
        interfacePane.add(ipAddressPane, 1, 1);

        interfacePane.add(new Label("IP маска:"), 0, 2);
        GridPane ipMaskPane = new GridPane();
        ipMaskPane.setHgap(5);
        ipMaskPane.add(ipMask1Field, 0, 0);
        ipMaskPane.add(new Label("."), 1, 0);
        ipMaskPane.add(ipMask2Field, 2, 0);
        ipMaskPane.add(new Label("."), 3, 0);
        ipMaskPane.add(ipMask3Field, 4, 0);
        ipMaskPane.add(new Label("."), 5, 0);
        ipMaskPane.add(ipMask4Field, 6, 0);
        interfacePane.add(ipMaskPane, 1, 2);

        interfacePane.add(new Label("Шлюз по умолчанию:"), 0, 3);
        GridPane gatewayPane = new GridPane();
        gatewayPane.setHgap(5);
        gatewayPane.add(gateway1Field, 0, 0);
        gatewayPane.add(new Label("."), 1, 0);
        gatewayPane.add(gateway2Field, 2, 0);
        gatewayPane.add(new Label("."), 3, 0);
        gatewayPane.add(gateway3Field, 4, 0);
        gatewayPane.add(new Label("."), 5, 0);
        gatewayPane.add(gateway4Field, 6, 0);
        interfacePane.add(gatewayPane, 1, 3);

        interfacePane.add(new Label("Метка интерфейса:"), 0, 4);
        interfacePane.add(interfaceLabelField, 1, 4);

        interfacePane.add(new Label("Маршрутизируемый:"), 0, 5);
        interfacePane.add(flagIsRoutableCombo, 1, 5);

        // Кнопка удаления
        Button removeBtn = new Button("Удалить интерфейс");
        removeBtn.setOnAction(e -> {
            networkInterfaces.remove(interfaceFields);
            networkInterfacesContainer.getChildren().remove(interfacePane);
        });
        interfacePane.add(removeBtn, 0, 6, 2, 1);

        // Добавляем интерфейс в списки
        networkInterfaces.add(interfaceFields);
        networkInterfacesContainer.getChildren().add(interfacePane);
    }
}