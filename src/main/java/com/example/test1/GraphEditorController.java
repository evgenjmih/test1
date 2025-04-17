package com.example.test1;

import javafx.beans.property.MapProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.shape.*;

import java.util.*;

public class GraphEditorController {
    @FXML
    private Pane graphPane;

    private Map<Integer, Circle> vertices = new HashMap<>();
    private Map<Integer, Text> verticesText = new HashMap<>();
    private Map<String, Line> edges = new HashMap<>();
    private Map<Line, Text> edgeWeights = new HashMap<>();
    private ArrayList<Circle> applicationVertexes = new ArrayList<>();
    private Map<ArrayList<Integer>, Map<String, Path>> paths = new HashMap<>();

    private Circle selectedVertex = null;
    private boolean edgeCreationMode = false;
    private boolean applicationCreationMode = false;
    private boolean vertexDragging = false;
    private InterNetwork net = GlobalVariables.net;


    private int vertexIdCounter = 0;

    @FXML
    private void handleAddApplication(){
        applicationCreationMode = true;
        for (Map.Entry<Integer, Circle> entry : vertices.entrySet()){
            entry.getValue().setStroke(Color.BROWN);

        }
        Button finishApplication = new Button("Добавить взаимодействие");
        finishApplication.setOnAction(event -> {
            applicationCreationMode = false;
            graphPane.getChildren().remove(finishApplication);
            DrawApplication();

            for (Map.Entry<Integer, Circle> entry : vertices.entrySet()){
                entry.getValue().setStroke(Color.BLACK);

            }
            applicationVertexes.clear();
        });
        graphPane.getChildren().add(finishApplication);

    }


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
    private void loadSheme(){
        //совпадение адресов сети по адр+маска
        //если да + эдже
        System.out.println(net.getNetworkDeviceList());

        for (NetworkDevice curDev : net.getNetworkDeviceList()) {
            int id = vertexIdCounter++;
            double x = 100 + Math.random() * 400;
            double y = 100 + Math.random() * 200;


            Circle vertex = new Circle(x, y, 20, Color.LIGHTBLUE);
            vertex.setStroke(Color.BLACK);
            vertex.setStrokeWidth(2);

            Text label = new Text(x - 30, y + 30, String.valueOf(curDev.getDeviceLabel()));
            vertex.setOnMousePressed(this::handleVertexPressed);
            vertex.setOnMouseDragged(this::handleVertexDragged);
            vertex.setOnMouseReleased(this::handleVertexReleased);
            System.out.println(label);
            System.out.println(curDev.getDeviceLabel());
            System.out.println(net.getNetworkDeviceList().get(0).getDeviceLabel());

            verticesText.put(curDev.getDeviceID(), label);
            vertices.put(curDev.getDeviceID(), vertex);
            graphPane.getChildren().addAll(vertex, label);
        }
        for (NetworkDevice curDev : net.getNetworkDeviceList()){
            for (NetworkInterface curInt : curDev.getNetworkInterfaceList()){
                System.out.println(curInt.getConnectedDevicesInterfaces());
                for (ArrayList<Integer> curCon : curInt.getConnectedDevicesInterfaces())
                {
                    int startId = curDev.getDeviceID();
                    int startIdInt = curInt.getInterfaceID();
                    int endId = curCon.get(0);
                    int endIdInt = curCon.get(1);
                    String edgeKey = startId < endId ? startId + "-" + startIdInt + "-" + endId + "-" + endIdInt : endId  + "-" + endIdInt+ "-" + startId+ "-" + startIdInt;
                    System.out.println(edgeKey);
                    System.out.println(vertices);
                    if (!edges.containsKey(edgeKey)) {
                        Line edge = new Line(
                                vertices.get(startId).getCenterX(), vertices.get(startId).getCenterY(),
                                vertices.get(endId).getCenterX(), vertices.get(endId).getCenterY()
                        );
                        edge.setStrokeWidth(2);

                        edges.put(edgeKey, edge);
                        graphPane.getChildren().addAll(edge);

                        edge.toBack();
                    }
                }

            }
        }}

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
        ArrayList <NetworkInterface.NetworkApplication> nApplicationList = new ArrayList<>();
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

        net.addNetworkDevice(new NetworkDevice(vertexIdCounter-1, deviceLabel, vendor, typeSW, versionSW, typeHW, versionHW, partNumber, serialNumber, placement, iList, flagIsFirewall, deviceType));

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

            if (edgeCreationMode && selectedVertex != null && selectedVertex != vertex && !applicationCreationMode) {
                // Создаем ребро между selectedVertex и текущей вершиной
                addEdge(selectedVertex, vertex);
                selectedVertex.setStroke(Color.BLUE);
                selectedVertex = vertex;
                edgeCreationMode = false;
            } else if (!applicationCreationMode){
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
            } else{
                if (applicationVertexes.contains(vertex)){
                    applicationVertexes.remove(vertex);
                    vertex.setStroke(Color.BROWN);

                }
                else{
                    applicationVertexes.add(vertex);
                    vertex.setStroke(Color.YELLOW);

                }

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
            for (Map.Entry<ArrayList<Integer>, Map<String, Path>> entry : paths.entrySet()) {
                System.out.println(entry.getKey());
                if (entry.getKey().contains(getVertexId(vertex))) {
                    ArrayList<double[]> points = new ArrayList<>();
                    for (Integer curId: entry.getKey()){

                        points.add(new double[] {vertices.get(curId).getCenterX(), vertices.get(curId).getCenterY()});
                    }
                    for(String k: entry.getValue().keySet()){
                        updatePath(points, entry.getValue().get(k));

                    }
                }
            }
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
                System.out.println(entry.getKey());
                String[] parts = entry.getKey().split("-");
                int v1 = Integer.parseInt(parts[0]);
                int i1 = Integer.parseInt(parts[1]);
                int i2 = Integer.parseInt(parts[3]);
                int v2 = Integer.parseInt(parts[2]);

                if (v1 == idToRemove || v2 == idToRemove) {
                    graphPane.getChildren().remove(entry.getValue());
                    ArrayList<ArrayList<Integer>> devsStart = net.getNetworkDeviceList().get(v1).getNetworkInterfaceList().get(i1).getConnectedDevicesInterfaces();
                    ArrayList<ArrayList<Integer>> devsEnd = net.getNetworkDeviceList().get(v2).getNetworkInterfaceList().get(i2).getConnectedDevicesInterfaces();
                    ArrayList<Integer> rem1 = new ArrayList<>();
                    ArrayList<Integer> rem2 = new ArrayList<>();
                    rem1.add(v2);
                    rem1.add(i2);
                    rem2.add(v1);
                    rem2.add(i1);
                    devsStart.remove(rem1);
                    devsEnd.add(rem2);
                    net.getNetworkDeviceList().get(v1).getNetworkInterfaceList().get(i1).setConnectedDevicesInterfaces(devsStart);
                    net.getNetworkDeviceList().get(v2).getNetworkInterfaceList().get(i2).setConnectedDevicesInterfaces(devsEnd);




                    return true;
                }
                return false;
            });

            // Удаляем вершину
            vertices.remove(idToRemove);
            Text nodej = verticesText.get(idToRemove);
            ArrayList<ArrayList<Integer>> keysToRemove = new ArrayList<>();
            for (NetworkDevice curDev : net.getNetworkDeviceList()){
                if (curDev.getDeviceID() == idToRemove){
                    for (Map.Entry<ArrayList<Integer>, Map<String, Path>> entry : paths.entrySet()){
                        System.out.println(entry.getKey());
                        if (entry.getKey().contains(idToRemove)){
                            System.out.println("fffff");


                        }

                    }

                    for (ArrayList<Integer> curKey : keysToRemove){
                        paths.remove(curKey);
                    }


                    ArrayList<NetworkDevice> curDevs = net.getNetworkDeviceList();
                    curDevs.remove(curDev);
                    net.setNetworkDeviceList(curDevs);
                    break;
                }
            }

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
                CustomDialogForEdges1 dialog = new CustomDialogForEdges1(getDevId(startId).getNetworkInterfaceList(), getDevId(endId).getNetworkInterfaceList());

                dialog.showAndWait().ifPresent(results -> {
                    // Получение данных из results Map

                    String sInt = (String) results.get("sInterface");
                    String dInt = (String) results.get("dInterface");
                    int endIntId = -1;
                    int startIntId = -1;

                    for (int i=0; i < getDevId(startId).getNetworkInterfaceList().size(); i++){
                        if (getDevId(startId).getNetworkInterfaceList().get(i).getInterfaceLabel() == sInt){
                            startIntId = i;
                            break;
                        }
                    }

                    for (int i=0; i < getDevId(endId).getNetworkInterfaceList().size(); i++){
                        if (getDevId(endId).getNetworkInterfaceList().get(i).getInterfaceLabel() == dInt){
                            endIntId = i;
                            break;
                        }
                    }

                    ArrayList<Integer> curConForStart = new ArrayList<>();
                    ArrayList<Integer> curConForEnd = new ArrayList<>();
                    curConForStart.add(endId);
                    curConForStart.add(endIntId);
                    curConForEnd.add(startId);
                    curConForEnd.add(startIntId);
                    ArrayList<ArrayList<Integer>> devsStart = getDevId(startId).getNetworkInterfaceList().get(startIntId).getConnectedDevicesInterfaces();
                    ArrayList<ArrayList<Integer>> devsEnd = getDevId(endId).getNetworkInterfaceList().get(endIntId).getConnectedDevicesInterfaces();
                    devsStart.add(curConForStart);
                    devsEnd.add(curConForEnd);
                    getDevId(startId).getNetworkInterfaceList().get(startIntId).setConnectedDevicesInterfaces(devsStart);
                    getDevId(endId).getNetworkInterfaceList().get(endIntId).setConnectedDevicesInterfaces(devsEnd);

                });
            int startIdInt = -1;
            int endIdInt = -1;
            for (NetworkInterface devint :getDevId(startId).getNetworkInterfaceList()){
                for (ArrayList<Integer> cur : devint.getConnectedDevicesInterfaces()){
                    if (cur.get(0) == endId){
                        startIdInt = cur.get(1);
                    }
                }
            }
            for (NetworkInterface devint : getDevId(endId).getNetworkInterfaceList()){
                for (ArrayList<Integer> cur : devint.getConnectedDevicesInterfaces()){
                    if (cur.get(0) == startId){
                        endIdInt = cur.get(1);
                    }
                }
            }
            String edgeKey = startId < endId ? startId + "-" + startIdInt + "-" + endId + "-" + endIdInt : endId  + "-" + endIdInt+ "-" + startId+ "-" + startIdInt;
            System.out.println(edgeKey);

            if (!edges.containsKey(edgeKey)) {
                Line edge = new Line(
                        startVertex.getCenterX(), startVertex.getCenterY(),
                        endVertex.getCenterX(), endVertex.getCenterY()
                );
                edge.setStrokeWidth(2);

                // Обработчики событий для ребра
                edge.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        //редактирование натроек связи

                    }
                });

                edges.put(edgeKey, edge);
                graphPane.getChildren().addAll(edge);
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

    private NetworkDevice getDevId(int netdevId){
        for (NetworkDevice curDev : net.getNetworkDeviceList()){
            if (curDev.getDeviceID() == netdevId){
                return curDev;
            }
        }
        return null;
    }

    private void DrawApplication(){
        ArrayList<double[]> points = new ArrayList<>();
        ArrayList<Integer> way = new ArrayList<>();
        for (Circle curVer : applicationVertexes){
            way.add(getVertexId(curVer));
            points.add(new double[] {curVer.getCenterX(), curVer.getCenterY()});
        }

        Path path = new Path();
        updatePath(points, path);
        System.out.println(way);

        CustomDilogForEdges dialog = new CustomDilogForEdges(getDevId(way.get(0)).getNetworkInterfaceList(), getDevId(way.get(way.size()-1)).getNetworkInterfaceList());

        dialog.showAndWait().ifPresent(results -> {
            // Получение данных из results Map

            String sInt = (String) results.get("sInterface");
            String dInt = (String) results.get("dInterface");
            int sPort = (int) results.get("sPort");
            int dPort = (int) results.get("dPort");
            NetworkPacket.NetworkProtocol netProt = (NetworkPacket.NetworkProtocol) results.get("networkProtocol");
            String appLabel = (String) results.get("applicationLabel");

            for (int i=0; i<getDevId(way.get(0)).getNetworkInterfaceList().size();i++) {
                if (getDevId(way.get(0)).getNetworkInterfaceList().get(i).getInterfaceLabel() == sInt){
                    getDevId(way.get(0)).getNetworkInterfaceList().get(i).addNetworkApplication(sPort, dPort, netProt, appLabel);
                    System.out.println(getDevId(way.get(0)).getNetworkInterfaceList().get(i).getNetworkApplicationList());

                    break;
                }
            }

            for (int i=0; i<getDevId(way.get(way.size()-1)).getNetworkInterfaceList().size();i++) {
                if (getDevId(way.get(way.size()-1)).getNetworkInterfaceList().get(i).getInterfaceLabel() == dInt){
                    getDevId(way.get(way.size()-1)).getNetworkInterfaceList().get(i).addNetworkApplication(dPort, sPort, netProt, appLabel);
                    System.out.println(getDevId(way.get(way.size()-1)).getNetworkInterfaceList().get(i).getNetworkApplicationList());

                    break;
                }
            }

            Map<String, Path> curPath = new HashMap<>();
            curPath.put(appLabel, path);
            System.out.println(way);
            System.out.println(points);
            paths.put(way, curPath);
            graphPane.getChildren().add(path);
        });

    }

    private void updatePath(ArrayList<double[]> points, Path path) {
        path.getElements().clear();

        if (points.isEmpty()) return;

        // Начинаем с первой точки
        double[] firstPoint = points.get(0);
        path.getElements().add(new MoveTo(firstPoint[0], firstPoint[1]));

        // Строим Catmull-Rom сплайн
        for (int i = 1; i < points.size(); i++) {
            double[] prevPrev = (i == 1) ? points.get(0) : points.get(i - 2);
            double[] prev = points.get(i - 1);
            double[] current = points.get(i);
            double[] next = (i == points.size() - 1) ? current : points.get(i + 1);

            // Контрольные точки (можно настроить коэффициенты)
            double tension = 0.5; // Натяжение кривой (0.0 - 1.0)

            double cp1x = prev[0] + (current[0] - prevPrev[0]) * tension;
            double cp1y = prev[1] + (current[1] - prevPrev[1]) * tension;
            double cp2x = current[0] - (next[0] - prev[0]) * tension;
            double cp2y = current[1] - (next[1] - prev[1]) * tension;

            path.getElements().add(new CubicCurveTo(cp1x, cp1y, cp2x, cp2y, current[0], current[1]));
        }

        // Стиль кривой
        path.setStroke(Color.BLUE);
        path.setStrokeWidth(3);
        path.setFill(null);

    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}