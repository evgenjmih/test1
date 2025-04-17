package com.example.test1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ArrayList;


public class HelloApplication extends Application {
    public static Stage primaryStage;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("graph_editor.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        System.out.println(GlobalVariables.net.getNetworkDeviceList());

    }

    public static void main(String[] args) {
            ArrayList<NetworkDevice> devList = new ArrayList<>();
            NetworkDevice dev = new NetworkDevice();
            ImportNetworkDevice importND = new ImportNetworkDevice();
            int i;
            SyslogGenerator syslogTrace = new SyslogGenerator("packet_tracer.log");

            dev = importND.importNetworkDeviceConfig("ADMIN01.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            System.out.println(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("ELITE01.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("ELITE02.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("ELITE03.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("MARKET01.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("MARKET02.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("MARKET03.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("SRV1.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("SRV2.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("SRV3.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("SRVDMZ.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("FWINT.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            dev = new NetworkDevice();
            dev = importND.importNetworkDeviceConfig("FWBORD.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
            devList.add(dev);
            GlobalVariables.net.setNetworkDeviceList(devList);
            GlobalVariables.net.connectInterfaces();

//        net.print();

            PacketTracer pt = new PacketTracer();
            GlobalVariables.net.getNetworkDeviceList().get(1).getNetworkInterfaceList().get(0).addNetworkApplication(0, 80, NetworkPacket.NetworkProtocol.TCP, "Web-client");
            GlobalVariables.net.getNetworkDeviceList().get(8).getNetworkInterfaceList().get(0).addNetworkApplication(80, 0, NetworkPacket.NetworkProtocol.TCP, "Web-server");
            dev.print();

            pt.trace(GlobalVariables.net.getNetworkDeviceList().get(1).getNetworkInterfaceList().get(0).getNetworkApplicationList().get(0), GlobalVariables.net.getNetworkDeviceList().get(8).getNetworkInterfaceList().get(0).getNetworkApplicationList().get(0), GlobalVariables.net, 0);

            System.out.println("IPv4 конвертор, IP: " + IPv4.convertIPToString(GlobalVariables.net.getNetworkDeviceList().get(1).getNetworkInterfaceList().get(0).getAddressIP()));
            System.out.println(GlobalVariables.net.getNetworkDeviceList());
/*
        dev1 = importND.importNetworkDeviceConfig("ADMIN01.txt", ImportNetworkDevice.networkDeviceSystemType.WINDOWS_RUS);
        dev1.print();
        dev2 = importND.importNetworkDeviceConfig("dev.txt", ImportNetworkDevice.networkDeviceSystemType.LINUX);
        dev2.print();
        int i;
        System.out.println(dev2.getFirewallRuleList().size());
        for (i=0; i<dev2.getFirewallRuleList().size();i++) {
            System.out.println(dev2.getFirewallRuleList().get(i).toIptablesRule());
        }

        String rule = "iptables -A FORWARD -s 192.168.9.0/24 -d 192.168.11.0/24 -p tcp --sport 60365 --dport 143 -j ACCEPT  # IMAP";
        FirewallRule parsedRule = new FirewallRule();
        parsedRule = parsedRule.parseRule(rule);
        parsedRule.print();
        System.out.println(parsedRule.toIptablesRule());*/
        launch();
        }



    }
