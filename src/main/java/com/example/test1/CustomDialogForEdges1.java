package com.example.test1;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialogForEdges1 extends Dialog<Map<String, Object>>{

    private final ComboBox<String> sInterfaceField1 = new ComboBox<>();
    private final ComboBox<String> dInterfaceField1 = new ComboBox<>();


    public CustomDialogForEdges1(List<NetworkInterface> sitems, List<NetworkInterface> ditems){

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


        sInterfaceField1.getItems().setAll(nsitems);
        dInterfaceField1.getItems().setAll(nditems);

        grid.add(new Label("Source Interface:"), 0, 0);
        grid.add(sInterfaceField1, 1, 0);

        grid.add(new Label("Destination Interface:"), 0, 1);
        grid.add(dInterfaceField1, 1, 1);


        getDialogPane().setContent(grid);
        setResultConverter(buttonType ->
        {
            if (buttonType == submitButtonType)
            {
                Map<String, Object> result = new HashMap<>();

                result.put("sInterface", sInterfaceField1.getValue());
                result.put("dInterface", dInterfaceField1.getValue());


                return result;
            }
            return null;
        });

    }
}
