package com.example.test1;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDilogForEdges extends Dialog<Map<String, Object>>{

    private final ComboBox<String> sInterfaceField = new ComboBox<>();
    private final TextField portIn = new TextField();
    private final TextField portOut = new TextField();
    private final ComboBox<NetworkPacket.NetworkProtocol> networkProtocol =  new ComboBox<>();
    private final TextField applicationLabel = new TextField();
    private final ComboBox<String> dInterfaceField = new ComboBox<>();


    public CustomDilogForEdges(List<NetworkInterface> sitems, List<NetworkInterface> ditems){

        setTitle("Добавление связи");
        setHeaderText("Введите данные о связи");

        // Настройка кнопок
        ButtonType submitButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Основной GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        ArrayList<String> nsitems = new ArrayList();
        for (NetworkInterface devint : sitems){
            nsitems.add(devint.getInterfaceLabel());
        }
        ArrayList<String> nditems = new ArrayList();
        for (NetworkInterface devint : ditems){
            nditems.add(devint.getInterfaceLabel());
        }

        sInterfaceField.getItems().setAll(nsitems);
        dInterfaceField.getItems().setAll(nditems);

        grid.add(new Label("Source Interface:"), 0, 0);
        grid.add(sInterfaceField, 1, 0);

        grid.add(new Label("Source Port:"), 0, 1);
        grid.add(portIn, 1, 1);

        grid.add(new Label("Destination Interface:"), 0, 2);
        grid.add(dInterfaceField, 1, 2);

        grid.add(new Label("Destination Port:"), 0, 3);
        grid.add(portOut, 1, 3);

        grid.add(new Label("Протокол"), 0, 4);
        networkProtocol.getItems().setAll(NetworkPacket.NetworkProtocol.values());
        networkProtocol.setValue(NetworkPacket.NetworkProtocol.UDP);
        grid.add(networkProtocol, 1, 4);

        grid.add(new Label("Название подключения"), 0, 5);
        grid.add(applicationLabel, 1, 5);
        getDialogPane().setPrefSize(600, 600);


        getDialogPane().setContent(grid);
        setResultConverter(buttonType ->
        {
            if (buttonType == submitButtonType)
            {
                Map<String, Object> result = new HashMap<>();

                result.put("sInterface", sInterfaceField.getValue());
                result.put("dInterface", dInterfaceField.getValue());
                result.put("sPort",  Integer.parseInt(portIn.getText()));
                System.out.println(portIn.getText());
                result.put("dPort", Integer.parseInt(portOut.getText()));
                result.put("networkProtocol", networkProtocol.getValue());
                result.put("applicationLabel", applicationLabel.getText());


                return result;
            }
            return null;
        });

    }
}
