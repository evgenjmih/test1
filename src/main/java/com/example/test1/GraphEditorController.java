package com.example.test1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.*;

public class GraphEditorController {
    @FXML
    private Pane graphPane;

    private Map<Integer, Circle> vertices = new HashMap<>();
    private Map<Integer, Text> verticesText = new HashMap<>();
    private Map<String, Line> edges = new HashMap<>();
    private Map<Line, Text> edgeWeights = new HashMap<>();

    private Circle selectedVertex = null;
    private boolean edgeCreationMode = false;
    private boolean vertexDragging = false;

    public InterNetwork net = new InterNetwork();
    private int vertexIdCounter = net.getNetworkDeviceList().size();


    @FXML
    private void handleAddVertex() {

        System.out.println(net.getNetworkDeviceList());
        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait().ifPresent(results -> {
            // Получение данных из results Map

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> networkInterfaceList = (List<Map<String, Object>>) results.get("networkInterfaces");

            String deviceLabel = (String) results.get("deviceLabel");
            String vendor = (String) results.get("vendor");
            String typeSW= (String) results.get("typeSW");
            String versionSW= (String) results.get("versionSW");
            String typeHW= (String) results.get("typeHW");
            String versionHW= (String) results.get("versionHW");
            String partNumber= (String) results.get("partNumber");
            String serialNumber= (String) results.get("serialNumber");
            String placement = (String) results.get("placement");
            Boolean flagIsFirewall = (Boolean) results.get("flagIsFirewall");
            NetworkDevice.networkDeviceType devicetype = (NetworkDevice.networkDeviceType) results.get("devicetype");

            addVertex(100 + Math.random() * 400, 100 + Math.random() * 200, deviceLabel, vendor,
                    typeSW, versionSW, typeHW, versionHW, partNumber, serialNumber, placement, flagIsFirewall,
                    networkInterfaceList, devicetype);
        });


    }

    @FXML
    private void handleDeleteVertex() {
        if (selectedVertex != null) {
            deleteVertex(selectedVertex);
            selectedVertex = null;
        } else {
            showAlert("Не выбрана вершина", "Пожалуйста, выберите вершину для удаления");
        }
    }

    @FXML
    private void handleAddEdge() {
        if (selectedVertex != null && !edgeCreationMode) {
            edgeCreationMode = true;
            selectedVertex.setStroke(Color.RED);
        } else {
            edgeCreationMode = false;
            if (selectedVertex != null) {
                selectedVertex.setStroke(Color.BLUE);
            }
        }
    }


    private void addVertex(double x, double y, String  deviceLabel, String vendor,
                           String typeSW, String versionSW, String typeHW, String versionHW,
                           String partNumber, String serialNumber, String placement, Boolean flagIsFirewall,
                           List<Map<String, Object>> networkInterfaceList, NetworkDevice.networkDeviceType deviceType) {
        int id = vertexIdCounter++;

        System.out.println(net.getNetworkDeviceList());
        int iid = 0;
        ArrayList<NetworkInterface> iList = new ArrayList<>();
        ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();

        networkInterfaceList.forEach(item -> {
            int speed = (int) item.get("speed");
            String inlabel = (String) item.get("interfaceLabel");
            int[] ipAddress = (int[]) item.get("ipAddress");
            int[] ipMask = (int[]) item.get("ipMask");
            int[] dGateway = (int[]) item.get("defaultGateway");
            Boolean flagIsR = (Boolean) item.get("flagIsRoutable");

            iList.add(iid, new NetworkInterface(iid,  speed, inlabel, ipAddress, ipMask, dGateway, nApplicationList, flagIsR, cDevicesInterfaces));


        });

        net.addNetworkDevice(new NetworkDevice(vertexIdCounter, deviceLabel, vendor, typeSW, versionSW, typeHW, versionHW, partNumber, serialNumber, placement, iList, flagIsFirewall, deviceType));

        System.out.println("yes");

        Circle vertex = new Circle(x, y, 20, Color.LIGHTBLUE);
        vertex.setStroke(Color.BLACK);
        vertex.setStrokeWidth(2);

        Text label = new Text(x -30, y+30, String.valueOf(deviceLabel));

        //net.addNetworkDeviceList(new NetworkDevice(id, deviceLabel, vendor, typeSW,
        //        versionSW, typeHW, versionHW, partNumber, serialNumber, placement, networkInterfaceList, flagIsFirewall));
        // Обработчики событий для вершины
        vertex.setOnMousePressed(this::handleVertexPressed);
        vertex.setOnMouseDragged(this::handleVertexDragged);
        vertex.setOnMouseReleased(this::handleVertexReleased);

        verticesText.put(id, label);
        vertices.put(id, vertex);
        graphPane.getChildren().addAll(vertex, label);
        System.out.println(net.getNetworkDeviceList());
        System.out.println("devkist");

    }

    private void handleVertexPressed(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            Circle vertex = (Circle) event.getSource();

            if (edgeCreationMode && selectedVertex != null && selectedVertex != vertex) {
                // Создаем ребро между selectedVertex и текущей вершиной
                addEdge(selectedVertex, vertex);
                selectedVertex.setStroke(Color.BLUE);
                selectedVertex = vertex;
                edgeCreationMode = false;
            } else {
                // Выбираем вершину
                if (selectedVertex != null) {
                    selectedVertex.setStroke(Color.BLACK);
                }
                selectedVertex = vertex;
                vertex.setStroke(Color.BLUE);
                vertex.toFront();

                // Начинаем перетаскивание
                vertexDragging = true;
                event.consume();
            }
        }
    }

    private void handleVertexDragged(MouseEvent event) {
        if (vertexDragging && event.getButton() == MouseButton.PRIMARY) {
            Circle vertex = (Circle) event.getSource();

            // Обновляем позицию вершины
            vertex.setCenterX(event.getX());
            vertex.setCenterY(event.getY());

            Text node = verticesText.get(getVertexId(vertex));
            ((Text) node).setX(event.getX() - 30);
            ((Text) node).setY(event.getY() + 30);

            // Обновляем позиции связанных рёбер
            updateConnectedEdges(vertex);
            event.consume();
        }
    }

    private void handleVertexReleased(MouseEvent event) {
        vertexDragging = false;
        event.consume();
    }

    private void deleteVertex(Circle vertex) {
        // Находим ID вершины
        int idToRemove = getVertexId(vertex);
        //ребра, из общей, текст

        if (idToRemove != -1) {
            // Удаляем все связанные рёбра
            edges.entrySet().removeIf(entry -> {
                String[] parts = entry.getKey().split("-");
                int v1 = Integer.parseInt(parts[0]);
                int v2 = Integer.parseInt(parts[1]);

                if (v1 == idToRemove || v2 == idToRemove) {
                    graphPane.getChildren().remove(entry.getValue());
                    Text weightText = edgeWeights.get(entry.getValue());
                    if (weightText != null) {
                        graphPane.getChildren().remove(weightText);
                    }
                    return true;
                }
                return false;
            });

            // Удаляем вершину
            vertices.remove(idToRemove);
            Text nodej = verticesText.get(idToRemove);

            verticesText.remove(idToRemove);

            graphPane.getChildren().removeIf(node ->
                    node == vertex || node == nodej);
        }
    }

    private void addEdge(Circle startVertex, Circle endVertex) {
        // Находим ID вершин
        int startId = getVertexId(startVertex);
        int endId = getVertexId(endVertex);

        if (startId != -1 && endId != -1) {


                // Запрос веса ребра у пользователя
                CustomDilogForEdges dialog = new CustomDilogForEdges(net.getNetworkDeviceList().get(startId).getNetworkInterfaceList(), net.getNetworkDeviceList().get(endId).getNetworkInterfaceList());

                dialog.showAndWait().ifPresent(results -> {
                    // Получение данных из results Map

                    String sInt = (String) results.get("sInterface");
                    String dInt = (String) results.get("dInterface");
                    System.out.println(results.get("sPort"));
                    int pIn= (Integer) results.get("sPort");
                    int pOut= (Integer) results.get("dPort");
                    int nProt= (Integer) results.get("networkProtocol");
                    String aLab= (String) results.get("applicationLabel");

                    for (NetworkInterface devint : net.getNetworkDeviceList().get(startId).getNetworkInterfaceList()) {
                        if (devint.getInterfaceLabel() == sInt) {
                            ArrayList<NetworkApplication> nConn = devint.getNetworkApplicationList();
                            ArrayList<ArrayList<Integer>> nConnId = devint.getConnectedDevicesInterfaces();
                            ArrayList<Integer> nC = new ArrayList<>();
                            nC.add(endId);
                            nC.add(devint.getInterfaceID());
                            nConnId.add(nC);
                            nConn.add(new NetworkApplication(pIn, pOut, nProt, aLab));
                            devint.setConnectedDevicesInterfaces(nConnId);
                            devint.setNetworkApplicationList(nConn);
                            ArrayList<NetworkInterface> nnetIntL = net.getNetworkDeviceList().get(startId).getNetworkInterfaceList();
                            nnetIntL.add(devint);
                            net.getNetworkDeviceList().get(startId-1).setNetworkInterfaceList(nnetIntL);
                            break;
                        }


                    }
                    for (NetworkInterface devint : net.getNetworkDeviceList().get(endId).getNetworkInterfaceList()) {
                        if (devint.getInterfaceLabel() == dInt) {
                            ArrayList<NetworkApplication> nConn = devint.getNetworkApplicationList();
                            ArrayList<ArrayList<Integer>> nConnId = devint.getConnectedDevicesInterfaces();
                            ArrayList<Integer> nC = new ArrayList<>();
                            nC.add(startId);
                            nC.add(devint.getInterfaceID());
                            nConnId.add(nC);
                            nConn.add(new NetworkApplication(pOut, pIn, nProt, aLab));
                            devint.setConnectedDevicesInterfaces(nConnId);
                            devint.setNetworkApplicationList(nConn);
                            ArrayList<NetworkInterface> nnetIntL = net.getNetworkDeviceList().get(endId).getNetworkInterfaceList();
                            nnetIntL.add(devint);
                            net.getNetworkDeviceList().get(endId - 1).setNetworkInterfaceList(nnetIntL);
                            break;
                        }
                    }

                });
            int startIdInt = -1;
            int endIdInt = -1;
            for (NetworkInterface devint : net.getNetworkDeviceList().get(startId).getNetworkInterfaceList()){
                for (ArrayList<Integer> cur : devint.getConnectedDevicesInterfaces()){
                    if (cur.get(0) == startId){
                        endIdInt = cur.get(1);
                    }
                    if (cur.get(0) == endId){
                        startIdInt = cur.get(1);
                    }
                }
            }
            String edgeKey = startId < endId ? startId + "-" + startIdInt + "-" + endId + "-" + endIdInt : endId  + "-" + endIdInt+ "-" + startId+ "-" + startIdInt;

            if (!edges.containsKey(edgeKey)) {
                Line edge = new Line(
                        startVertex.getCenterX(), startVertex.getCenterY(),
                        endVertex.getCenterX(), endVertex.getCenterY()
                );
                edge.setStrokeWidth(2);

                // Обработчики событий для ребра
                edge.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        deleteEdge(edge);
                        // УДАЛИТЬ ИЗ СЕТИ И ВСЕ ПОДКЛЮЧЕННННИИИИ ЯЯЯЯЯЯЯЯЯ

                    } else if (e.getClickCount() == 2) {
                        // Редактирование веса по двойному клику
                    }
                });

                edges.put(edgeKey, edge);
                graphPane.getChildren().addAll(edge);
                System.out.println(net.getNetworkDeviceList().get(0).getNetworkInterfaceList().get(0).getNetworkApplicationList());
                System.out.println("netapplist");

                edge.toBack();
            } else {
                showAlert("Ребро уже существует", "Между этими вершинами уже есть ребро");
            }
        }
    }

    private void deleteInterface(NetworkInterface nint, int id){
        ArrayList<NetworkDevice> nnetdevs = net.getNetworkDeviceList();
        ArrayList<Integer> lToRemove = new ArrayList<>();
        lToRemove.add(id);
        lToRemove.add(nnetdevs.get(id).getNetworkInterfaceList().indexOf(nint));
        for (ArrayList<Integer> conn : nint.getConnectedDevicesInterfaces())
        {
            NetworkDevice curDev =  nnetdevs.get(conn.get(0));
            curDev.getNetworkInterfaceList().get(conn.get(1)).getConnectedDevicesInterfaces().remove(lToRemove);
            nnetdevs.get(conn.get(0)).setNetworkInterfaceList(curDev.getNetworkInterfaceList());
        }
        nnetdevs.get(id).getNetworkInterfaceList().remove(nint);
        net.setNetworkDeviceList(nnetdevs);
    }

    private void deleteEdge(Line edge) {
        String edgeKeyToRemove = null;
        for (Map.Entry<String, Line> entry : edges.entrySet()) {
            if (entry.getValue() == edge) {
                edgeKeyToRemove = entry.getKey();
                break;
            }
        }

        if (edgeKeyToRemove != null) {
            edges.remove(edgeKeyToRemove);
            graphPane.getChildren().removeAll(edge);
            String[] parts = edgeKeyToRemove.split("-");
            int dev1 = Integer.parseInt(parts[0]);
            int int1 = Integer.parseInt(parts[1]);
            int dev2 = Integer.parseInt(parts[2]);
            int int2 = Integer.parseInt(parts[3]);
            ArrayList<NetworkDevice> nDevs = net.getNetworkDeviceList();

            nDevs.get(dev1).getNetworkInterfaceList().get(int1).getNetworkApplicationList().remove(nDevs.get(dev1).getNetworkInterfaceList().get(int1).getConnectedDevicesInterfaces().indexOf(Arrays.asList(new int[] {dev2, int2})));
            nDevs.get(dev2).getNetworkInterfaceList().get(int2).getNetworkApplicationList().remove(nDevs.get(dev2).getNetworkInterfaceList().get(int2).getConnectedDevicesInterfaces().indexOf(Arrays.asList(new int[] {dev1, int1})));
            nDevs.get(dev1).getNetworkInterfaceList().get(int1).getConnectedDevicesInterfaces().remove(Arrays.asList(new int[] {dev2, int2}));
            nDevs.get(dev2).getNetworkInterfaceList().get(int2).getConnectedDevicesInterfaces().remove(Arrays.asList(new int[] {dev1, int1}));
            for (NetworkDevice nd : net.getNetworkDeviceList()){

                for (NetworkInterface ni : nd.getNetworkInterfaceList()) {
                    System.out.println(ni.getConnectedDevicesInterfaces());

                }
            }
            net.setNetworkDeviceList(nDevs);
            for (NetworkDevice nd : net.getNetworkDeviceList()){

                for (NetworkInterface ni : nd.getNetworkInterfaceList()) {
                    System.out.println(ni.getConnectedDevicesInterfaces());

                }
            }
        }
    }

    private void updateConnectedEdges(Circle vertex) {
        int vertexId = getVertexId(vertex);

        if (vertexId != -1) {
            // Обновляем все рёбра, связанные с этой вершиной
            for (Map.Entry<String, Line> entry : edges.entrySet()) {
                String[] parts = entry.getKey().split("-");
                int v1 = Integer.parseInt(parts[0]);
                int v2 = Integer.parseInt(parts[2]);

                if (v1 == vertexId || v2 == vertexId) {
                    Line edge = entry.getValue();
                    Circle otherVertex = v1 == vertexId ? vertices.get(v2) : vertices.get(v1);

                    edge.setStartX(vertex.getCenterX());
                    edge.setStartY(vertex.getCenterY());
                    edge.setEndX(otherVertex.getCenterX());
                    edge.setEndY(otherVertex.getCenterY());

                    // Обновляем позицию текста с весом
                    Text weightText = edgeWeights.get(edge);
                    if (weightText != null) {
                        weightText.setX((vertex.getCenterX() + otherVertex.getCenterX()) / 2);
                        weightText.setY((vertex.getCenterY() + otherVertex.getCenterY()) / 2);
                    }
                }
            }
        }
    }

    private int getVertexId(Circle vertex) {
        for (Map.Entry<Integer, Circle> entry : vertices.entrySet()) {
            if (entry.getValue() == vertex) {
                return entry.getKey();
            }
        }
        return -1;
    }




    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}