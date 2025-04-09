package com.example.test1;

import java.util.ArrayList;
import java.util.Arrays;

//Класс объектов сетевое приложение, существует только в привязке к сетевому интерфейсу
class NetworkRoute {
    // Linux 192.168.12.0/24 dev eth0  proto kernel  scope link  src 192.168.12.101 default via 192.168.12.1 dev eth0
// Windows Сетевой адрес           Маска сети      Адрес шлюза       Интерфейс  Метрика
//          0.0.0.0          0.0.0.0   192.168.50.100   192.168.50.190     25
    private Boolean flagIsLocal;
    private Boolean flagIsConnected;
    private Boolean flagIsStatic;
    private Boolean flagIsRIP;
    private Boolean flagIsMobile;
    private Boolean flagIsBGP;
    private int[] netIP = new int[4];
    private int[] maskIP = new int[4];
    private int[] gatewayIP = new int[4];
    private int interfaceID;
    private int metric;
    NetworkRoute(Boolean fIsLocal, Boolean fIsConnected, Boolean fIsStatic, Boolean fIsRIP, Boolean fIsMobile, Boolean fIsBGP, int[] nIP, int[] mIP, int[] gIP, int iID, int metr) {
        flagIsLocal = fIsConnected;
        flagIsConnected = fIsConnected;
        flagIsStatic = fIsStatic;
        flagIsRIP = fIsRIP;
        flagIsMobile = fIsMobile;
        flagIsBGP = fIsBGP;
        netIP = nIP;
        maskIP = mIP;
        gatewayIP = gIP;
        interfaceID = iID;
        metric = metr;
    }
}
class NetworkPacket {
    NetworkPacket(){}
}
class NetworkApplication {
    private int applicationID;
    private int portIn;
    private int portOut;
    // Приложение может использовать несколько протоколов
    private int networkProtocol;
    private String applicationLabel;
    NetworkApplication() {
        applicationID=1;
        portIn=80;
        portOut=80;
        networkProtocol=17;
        applicationLabel="Web-сервер";
    }
    NetworkApplication(int pIn, int pOut, int nProtocol, String aLabel) {
        applicationID = 1;
        portIn = pIn;
        portOut = pOut;
        networkProtocol = nProtocol;
        applicationLabel = aLabel;
    }
    int getApplicationID() {return applicationID;}
    int getPortIn() {return portIn;}
    int getPortOut() {return portOut;}
    int getNetworkProtocol() {return networkProtocol;}
    String getApplicationLabel() {return applicationLabel;}
    Boolean setApplicationID(int aID) {applicationID = aID; return true;}
    Boolean setPortIn(int pIn) {portIn = pIn; return true;}
    Boolean setPortOut(int pOut) {portOut = pOut; return true;}
    Boolean setNetworkProtocol(int nProtocol) {networkProtocol = nProtocol; return true;}
    Boolean setApplicationLabel(String aLabel) {applicationLabel=aLabel; return true;}
}
//Класс объектов сетевой интерфейс, существует только в привязке к сетевому устройству
class NetworkInterface {
    private int interfaceID;
    // Здесь скорость логического интерфейса в Мб/с, если в физическом несколько логических, они должны делить ее между собой
    private int speed;
    // Здесь логический интерфейс, на одном физическом интерфейсе могут быть несколько логических
    private int[] addressIP = new int[4];
    private int[] maskIP = new int[4];
    private int[] defaultGateway = new int[4];
    private String interfaceLabel;
    private ArrayList <NetworkApplication> networkApplicationList = new ArrayList<>();
    private Boolean flagIsRoutable;
    // Хранит deviceID и interfaceId
    private ArrayList<ArrayList<Integer>> connectedDevicesInterfaces = new ArrayList<>();
    NetworkInterface() {
        interfaceID=1;
        speed=1000;
        for (int i = 0; i <= 2; i++) {addressIP[i] = 1;}
        addressIP[3] = 2;
        for (int i = 0; i <= 2; i++) {maskIP[i] = 255;}
        maskIP[3] = 0;
        for (int i = 0; i <= 3; i++) {defaultGateway[i] = 1;}
        interfaceLabel="Ge0/1";
        // А если нет приложений?
        networkApplicationList.add(new NetworkApplication());
    }
    NetworkInterface(int sp, String iLabel, int[] aIP, int[] mIP, int[] dGateway, ArrayList <NetworkApplication> nApplicationList, Boolean flIsRoutable, ArrayList<ArrayList<Integer>> cDevicesInterfaces) {
        interfaceID=1;
        speed=sp;
        addressIP = aIP;
        maskIP = mIP;
        defaultGateway = dGateway;
        interfaceLabel = iLabel;
        // А если нет приложений?
        networkApplicationList = nApplicationList;
        flagIsRoutable=flIsRoutable;
        connectedDevicesInterfaces = cDevicesInterfaces;
    }
    // Не использовать (временный)
    NetworkInterface(int iID, int sp, String iLabel, int[] aIP, int[] mIP, int[] dGateway, ArrayList <NetworkApplication> nApplicationList, Boolean flIsRoutable, ArrayList<ArrayList<Integer>> cDevicesInterfaces) {
        interfaceID=iID;
        speed=sp;
        addressIP = aIP;
        maskIP = mIP;
        defaultGateway = dGateway;
        interfaceLabel = iLabel;
        // А если нет приложений?
        networkApplicationList = nApplicationList;
        flagIsRoutable=flIsRoutable;
        connectedDevicesInterfaces = cDevicesInterfaces;
    }
    int getInterfaceID() {return interfaceID;}
    int getSpeed() {return speed;}
    int[] getAddressIP() {return addressIP;}
    int[] getMaskIP() {return maskIP;}
    int[] getDefaultGateway() {return defaultGateway;}
    String getInterfaceLabel() {return interfaceLabel;}
    ArrayList <NetworkApplication> getNetworkApplicationList() {return networkApplicationList;}
    Boolean getFlagIsRoutable() {return flagIsRoutable;}
    ArrayList<ArrayList<Integer>> getConnectedDevicesInterfaces() {return connectedDevicesInterfaces;}

    Boolean setSpeed(int sp) {speed=sp; return true;}
    Boolean setAddressIP(int[] aIP) {addressIP=aIP; return true;}
    Boolean setMaskIP(int[] mIP) {maskIP=mIP; return true;}
    Boolean setDefaultGateway(int[] dGateway) {defaultGateway=dGateway; return true;}
    Boolean setInterfaceLabel(String iLabel) {interfaceLabel=iLabel; return true;}
    Boolean setNetworkApplicationList(ArrayList <NetworkApplication> nApplicationList) {networkApplicationList=nApplicationList; return true;}
    Boolean setFlagIsRoutable(Boolean fIsRoutable) {flagIsRoutable=fIsRoutable; return true;}
    // Добавить проверку на существование deice и interface, автоматически добавлять обратную связь
    Boolean setConnectedDevicesInterfaces(ArrayList<ArrayList<Integer>> cDevicesInterfaces) {connectedDevicesInterfaces = cDevicesInterfaces; return true;}
}
//Класс объектов сетевое устройство
class NetworkDevice {
    public enum networkDeviceType {
        UNKNOWN("Неизвестное устройство"),
        SERVER("Сервер"),
        PC("АРМ"),
        ROUTER("Маршрутизатор"),
        L3SWITCH("L3 коммутатор"),
        FIREWALL("МСЭ");

        private String label;

        networkDeviceType(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }


    }
    private int deviceID;
    private String deviceLabel;
    private String vendor;
    private String typeSW;
    private String versionSW;
    private String typeHW;
    private String versionHW;
    private String partNumber;
    private String serialNumber;
    private String placement;
    private ArrayList <NetworkInterface> networkInterfaceList = new ArrayList<>();
    private Boolean flagIsFirewall;
    networkDeviceType deviceType;
    private ArrayList <NetworkRoute> RoutingList = new ArrayList<>();

    NetworkDevice()
    {
        deviceID=0;
        deviceLabel="";
        vendor="";
        typeSW="";
        versionSW="";
        typeHW="";
        versionHW="";
        partNumber="";
        serialNumber="";
        placement="";

        networkInterfaceList.add(new NetworkInterface());
        flagIsFirewall = false;
        deviceType = networkDeviceType.UNKNOWN;
    }
    NetworkDevice(String dLabel, String ven, String tSW, String vSW, String tHW, String vHW, String pNumber, String sNumber, String pl, ArrayList <NetworkInterface> nInterfaceList, Boolean fFirewall, networkDeviceType dType)
    {
        deviceID=1;
        deviceLabel=dLabel;
        vendor=ven;
        typeSW=tSW;
        versionSW=vSW;
        typeHW=tHW;
        versionHW=vHW;
        partNumber=pNumber;
        serialNumber=sNumber;
        placement=pl;
        networkInterfaceList = nInterfaceList;
        flagIsFirewall = fFirewall;
        deviceType=dType;
    }
    // Не использовать (временный)
    NetworkDevice(int dID, String dLabel, String ven, String tSW, String vSW, String tHW, String vHW, String pNumber, String sNumber, String pl, ArrayList <NetworkInterface> nInterfaceList, Boolean fFirewall, networkDeviceType dType)
    {
        deviceID=dID;
        deviceLabel=dLabel;
        vendor=ven;
        typeSW=tSW;
        versionSW=vSW;
        typeHW=tHW;
        versionHW=vHW;
        partNumber=pNumber;
        serialNumber=sNumber;
        placement=pl;
        networkInterfaceList = nInterfaceList;
        flagIsFirewall = fFirewall;
        deviceType=dType;
    }
    Boolean loadFromDB(int ID){
        try {
            deviceID =ID;
            deviceLabel = "PT NGFW node 1";
            vendor = "Positive Technologies";
            typeSW = "PT NGFW";
            versionSW = "1.0.0";
            typeHW = "1010";
            versionHW = "1.0.0";
            partNumber = "001-000001";
            serialNumber = "00000001";
            placement = "ЦОД 1, шкаф 1, 1U";
            System.out.println("networkInterfaceList size: " + networkInterfaceList.size() + "\n");
            networkInterfaceList.set(0, new NetworkInterface());
            System.out.println("networkInterfaceList size: " + networkInterfaceList.size() + "\n");
            networkInterfaceList.add(new NetworkInterface());
            System.out.println("networkInterfaceList size: " + networkInterfaceList.size() + "\n");
            flagIsFirewall = false;
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean loadFromFile(String fileName){
        try{
            deviceID=1;
            deviceLabel="PT NGFW node 1";
            vendor="Positive Technologies";
            typeSW="PT NGFW";
            versionSW="1.0.0";
            typeHW="1010";
            versionHW="1.0.0";
            partNumber="001-000001";
            serialNumber="00000001";
            placement="ЦОД 1, шкаф 1, 1U";
            networkInterfaceList.set(0, new NetworkInterface());;
            networkInterfaceList.add(new NetworkInterface());
            flagIsFirewall = false;
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean saveToDB(){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean saveToFile(String fileName){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    int getDeviceID() {return deviceID;}
    String getVendor() {return vendor;}
    String getDeviceLabel(){return deviceLabel;}
    String getTypeSW() {return typeSW;}
    String getVersionSW() {return versionSW;}
    String getTypeHW() {return typeHW;}
    String getVersionHW() {return versionHW;}
    String getPartNumber() {return partNumber;}
    String getSerialNumber() {return serialNumber;}
    String getPlacement() {return placement;}
    ArrayList <NetworkInterface> getNetworkInterfaceList() {return networkInterfaceList;}
    Boolean getFlagIsFirewall() {return flagIsFirewall;}
    ArrayList <NetworkRoute> getRoutingList(){return RoutingList;}

    Boolean setVendor(String ven) {vendor = ven; return true;}
    Boolean setDeviceLabel(String dLabel){deviceLabel = dLabel; return true;}
    Boolean setTypeSW(String tSW) {typeSW = tSW; return true;}
    Boolean setVersionSW(String vSW) {versionSW = vSW; return true;}
    Boolean setTypeHW(String tHW) {typeHW = tHW; return true;}
    Boolean setVersionHW(String vHW) {versionHW = vHW; return true;}
    Boolean setPartNumber(String pNumber) {partNumber = pNumber;return true;}
    Boolean setSerialNumber(String sNumber) {serialNumber = sNumber; return true;}
    Boolean setPlacement(String pl) {placement = pl; return true;}
    Boolean setNetworkInterfaceList(ArrayList <NetworkInterface> nInterfaceList) {networkInterfaceList = nInterfaceList; return true;}
    Boolean setFlagIsFirewall(Boolean fIsFirewall) {flagIsFirewall = fIsFirewall; return true;}
    Boolean setRoutingList(ArrayList <NetworkRoute> rList) {RoutingList = rList; return true;}
    Boolean addRoutingList(NetworkRoute rt) {RoutingList.add(rt); return true;}
}
//class IPNetwork {
//    int[] netIP;
//    int[] maskIP;
//}
// Класс объектов сеть, предполагается существование одного только объекта
class InterNetwork {
    private int networkID;
    private String networkLabel;
    private ArrayList <NetworkDevice> NetworkDeviceList = new ArrayList<>();

    InterNetwork() {
        networkID = 1;
        networkLabel = "Единственная и неповторимая тестовая сеть";
        NetworkDeviceList.add(0, new NetworkDevice());
    }
    Boolean loadFromDB(int ID){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean loadFromFile(String fileName){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean saveToDB(){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }
    Boolean saveToFile(String fileName){
        try{
            return (true);
        }
        catch(Exception e) {return (false);}
    }

    int getNetworkID() {return networkID;}
    String getNetworkLabel() {return networkLabel;}
    ArrayList <NetworkDevice> getNetworkDeviceList() {return NetworkDeviceList;}
    Boolean setNetworkLabel(String nLabel) {networkLabel = nLabel; return true;}
    Boolean setNetworkDeviceList(ArrayList <NetworkDevice> nDeviceList) {NetworkDeviceList = nDeviceList; return true;}
    Boolean addNetworkDevice(NetworkDevice nDevice) {NetworkDeviceList.add(nDevice); return true;}
    Boolean print() {
        System.out.println("network ID: " + networkID);
        System.out.println("network Label: " + networkLabel);
        System.out.println("network Device list: " + NetworkDeviceList);
        return true;
    }
}
public class Main {
    public static void main(String[] args) {
        ArrayList <NetworkDevice> devList = new ArrayList<>();
        InterNetwork net = new InterNetwork();
/*      ArrayList <NetworkApplication> applicationList = new ArrayList<>();
        ArrayList <NetworkInterface> interfaceList = new ArrayList<>();
        ArrayList <NetworkDevice> devList = new ArrayList<>();
        NetworkDevice dev1, dev2;
        InterNetwork net = new InterNetwork();

        applicationList.add(0, new NetworkApplication(80, 80, 17, "Web-сервер (http)"));
        applicationList.add(1, new NetworkApplication(443, 443, 17, "Web-сервер (https)"));
        interfaceList.add(0,new NetworkInterface());
        interfaceList.add(1,new NetworkInterface());
        dev1 = new NetworkDevice();
        dev1.loadFromDB(1);
        dev2 = new NetworkDevice("Устройство №2", "ENIAC", "1.0.0", "1.0.0", "1.0.0", "1.0.0", "001", "001", "The U.S. Army Ballistic Research Laboratory", interfaceList, false);
        devList.add(0, dev1);
        devList.add(1, dev2);
        net.setNetworkDeviceList(devList);

        System.out.printf("Program Started!" + "\n");

        System.out.println("network ID: " + net.getNetworkID());
        System.out.println("network Label: " + net.getNetworkLabel());

        devList=net.getNetworkDeviceList();
        dev1=devList.get(0);
        dev2=devList.get(1);
        System.out.println("device 1 Label: " + net.getNetworkDeviceList().get(0).getDeviceLabel());
        System.out.println("device 2 Label: " + dev2.getDeviceLabel() + "\n");
        dev2.setDeviceLabel("Хрень какая-то, крайне несовременная");
        System.out.println("device 2 Label: " + dev2.getDeviceLabel());*/
// Создаем сетевые хосты (Пользовательский сегмент)
        for (int i = 1; i <= 5; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 1, 1 + i};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {192, 168, 1, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            nApplicationList.add(0, new NetworkApplication(0, 80, 17, "Web-клиент (http)"));
            nApplicationList.add(1, new NetworkApplication(0, 443, 17, "Web-клиент (https)"));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 11)));
            iList.add(0, new NetworkInterface(i, 1000, "Ethernet 1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "АРМ пользователя " + i, "Lenovo", "MS Windows", "10.0.19045.5608", "X1", "1.0.12", "21KF0014LM", "SN000"+i, "Опенспейс", iList, false, NetworkDevice.networkDeviceType.PC);
            int[] nIP = {192, 168, 1, 0};
            int[] emptyIP = {0, 0, 0, 0};
            NetworkRoute route = new NetworkRoute(true, true, false, false, false, false, nIP, mIP, emptyIP, i, 100);
            dev.addRoutingList(route);
            route = new NetworkRoute(false, true, false, false, false, false, emptyIP, emptyIP, dGateway, i, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
// Создаем сетевые хосты (Сегмент управления)
        for (int i = 6; i <= 6; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 9, 254};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {192, 168, 9, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            nApplicationList.add(0, new NetworkApplication(0, 22, 17, "Терминал клиент (ssh)"));
            nApplicationList.add(1, new NetworkApplication(0, 443, 17, "Web-клиент (https)"));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 13)));
            iList.add(0, new NetworkInterface(i, 1000, "Ethernet 1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "АРМ администратора", "Lenovo", "MS Windows", "10.0.19045.5608", "X1", "1.0.12", "21KF0014LM", "SN000"+i, "Операторная", iList, false, NetworkDevice.networkDeviceType.PC);
            int[] nIP = {192, 168, 9, 0};
            int[] emptyIP = {0, 0, 0, 0};
            NetworkRoute route = new NetworkRoute(true, true, false, false, false, false, nIP, mIP, emptyIP, i, 100);
            dev.addRoutingList(route);
            route = new NetworkRoute(false, true, false, false, false, false, emptyIP, emptyIP, dGateway, i, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
// Создаем сетевые хосты (Серверный сегмент)
        for (int i = 7; i <= 9; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 10, i-5};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {192, 168, 10, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            nApplicationList.add(0, new NetworkApplication(80, 0, 6, "Web-сервер (http)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 6, "Web-сервер (https)"));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 12)));
            iList.add(0, new NetworkInterface(i, 1000, "eth1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  9; aIP[3] = i-5;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 9; dGateway[3] = 1;
            nApplicationList.clear();
            nApplicationList.add(0, new NetworkApplication(22, 0, 6, "Терминал сервер (ssh)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 6, "Web-сервер (https)"));
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 13)));
            iList.add(1, new NetworkInterface(i+11, 1000, "ilo1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "Сервер " + (i-6), "XFusion", "Astra Linux", "1.6", "1288H V6", "1.0.12", "21KF0014LM", "XF000"+i, "Шкаф 12/6 " + i +"U", iList, false, NetworkDevice.networkDeviceType.SERVER);
            int[] nIP = {192, 168, 10, 0};
            int[] emptyIP = {0, 0, 0, 0};
            NetworkRoute route = new NetworkRoute(true, true, false, false, false, false, nIP, mIP, emptyIP, i, 100);
            dev.addRoutingList(route);
            route = new NetworkRoute(false, true, false, false, false, false, emptyIP, emptyIP, dGateway, i, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
// Создаем сетевые хосты (сегмент DMZ)
        for (int i = 10; i <= 10; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 11, 2};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {192, 168, 11, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            nApplicationList.add(0, new NetworkApplication(80, 0, 6, "Web-сервер (http)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 6, "Web-сервер (https)"));
            nApplicationList.add(2, new NetworkApplication(53, 0, 17, "DNS-сервер (dns)"));
            nApplicationList.add(3, new NetworkApplication(123, 0, 17, "NTP-сервер (ntp)"));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(12, 16)));
            iList.add(0, new NetworkInterface(i, 1000, "eth1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  9; aIP[3] = 5;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 9; dGateway[3] = 1;
            nApplicationList.clear();
            nApplicationList.add(0, new NetworkApplication(22, 0, 17, "Терминал сервер (ssh)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 17, "Web-сервер (https)"));
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 13)));
            iList.add(1, new NetworkInterface(23, 1000, "ilo1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "Сервер DMZ", "XFusion", "Astra Linux", "1.6", "1288H V6", "1.0.12", "21KF0014LM", "XF000"+i, "Шкаф 12/6 " + i +"U", iList, false, NetworkDevice.networkDeviceType.SERVER);
            int[] nIP = {192, 168, 11, 0};
            NetworkRoute route = new NetworkRoute(false, true, false, false, false, false, nIP, mIP, dGateway, i, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
// Создаем внутренний FW
        for (int i = 11; i <= 11; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 1, 1};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {192, 168, 254, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(1, 1)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(2, 2)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(3, 3)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(4, 6)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(5, 5)));
            iList.add(0, new NetworkInterface(11, 1000, "ge0/0", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  9; aIP[3] = 1;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 254; dGateway[3] = 1;
            nApplicationList.clear();
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(6, 6)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(7, 18)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(8, 19)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(9, 20)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(10, 23)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 21)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(12, 22)));

            iList.add(1, new NetworkInterface(13, 1000, "ge0/1", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  10; aIP[3] = 1;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 254; dGateway[3] = 1;
            nApplicationList.clear();
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(7, 7)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(8, 8)));
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(9, 9)));
            iList.add(1, new NetworkInterface(12, 1000, "ge0/2", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  254; aIP[3] = 2;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 254; dGateway[3] = 1;
            nApplicationList.clear();
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(12, 15)));
            iList.add(1, new NetworkInterface(14, 1000, "ge0/3", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  9; aIP[3] = 10;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 192; dGateway[1] = 168; dGateway[2] = 9; dGateway[3] = 1;
            nApplicationList.clear();
            nApplicationList.add(0, new NetworkApplication(22, 0, 17, "Терминал сервер (ssh)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 17, "Web-сервер (https)"));
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 13)));
            iList.add(1, new NetworkInterface(21, 1000, "ilo1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "МСЭ внутренней сегментации", "Usergate", "Usergate 7", "7.0", "E1000", "1.0.0", "", "UG000"+i, "Шкаф 12/6 " + i +"U", iList, true, NetworkDevice.networkDeviceType.FIREWALL);
            int[] emptyIP = {0, 0, 0, 0};
            int[] nIP1 = {192, 168, 1, 0};
            NetworkRoute route = new NetworkRoute(true, true, false, false, false, false, nIP1, mIP, emptyIP, 11, 100);
            dev.addRoutingList(route);
            int[] nIP2 = {192, 168, 9, 0};
            route = new NetworkRoute(true, true, false, false, false, false, nIP2, mIP, emptyIP, 12, 100);
            dev.addRoutingList(route);
            int[] nIP3 = {192, 168, 10, 0};
            route = new NetworkRoute(true, true, false, false, false, false, nIP3, mIP, emptyIP, 13, 100);
            dev.addRoutingList(route);
            int[] nIP4 = {192, 168, 254, 0};
            int[] mIP4 = {255, 255, 255, 252};
            route = new NetworkRoute(true, true, false, false, false, false, nIP4, mIP4, emptyIP, 14, 100);
            dev.addRoutingList(route);
            route = new NetworkRoute(false, true, false, false, false, false, emptyIP, emptyIP, dGateway, 14, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
// Создаем пограничный FW
        for (int i = 12; i <= 12; i++) {
            ArrayList <NetworkInterface> iList = new ArrayList<>();
            int[] aIP = {192, 168, 254, 1};
            int[] mIP = {255, 255, 255, 0};
            int[] dGateway = {202, 132, 16, 1};
            ArrayList <NetworkApplication> nApplicationList = new ArrayList<>();
            ArrayList<ArrayList<Integer>> cDevicesInterfaces = new ArrayList<ArrayList<Integer>>();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 14)));
            iList.add(0, new NetworkInterface(15, 1000, "ge0/0", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  11; aIP[3] = 1;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 202; dGateway[1] = 132; dGateway[2] = 16; dGateway[3] = 1;
            nApplicationList.clear();
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(10, 10)));
            iList.add(1, new NetworkInterface(16, 1000, "ge0/1", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 202; aIP[1] = 132; aIP[2] =  16; aIP[3] = 4;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 202; dGateway[1] = 132; dGateway[2] = 16; dGateway[3] = 1;
            nApplicationList.clear();
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(0, 0)));
            iList.add(1, new NetworkInterface(17, 1000, "ge0/3", aIP, mIP, dGateway, nApplicationList, true, cDevicesInterfaces));

            aIP[0] = 192; aIP[1] = 168; aIP[2] =  9; aIP[3] = 11;
            mIP[0] = 255; mIP[1] = 255; mIP[2] =255; mIP[3] =0;
            dGateway[0] = 202; dGateway[1] = 132; dGateway[2] = 16; dGateway[3] = 1;
            nApplicationList.clear();
            nApplicationList.add(0, new NetworkApplication(22, 0, 17, "Терминал сервер (ssh)"));
            nApplicationList.add(1, new NetworkApplication(443, 0, 17, "Web-сервер (https)"));
            cDevicesInterfaces.clear();
            cDevicesInterfaces.add(new ArrayList<Integer>(Arrays.asList(11, 13)));
            iList.add(1, new NetworkInterface(22, 1000, "ilo1", aIP, mIP, dGateway, nApplicationList, false, cDevicesInterfaces));

            NetworkDevice dev = new NetworkDevice(i, "Пограничный МСЭ", "Usergate", "Usergate 7", "7.0", "E1000", "1.0.0", "", "UG000"+i, "Шкаф 12/6 " + i +"U", iList, true, NetworkDevice.networkDeviceType.FIREWALL);
            int[] emptyIP = {0, 0, 0, 0};
            int[] nIP1 = {192, 168, 11, 0};
            NetworkRoute route = new NetworkRoute(true, true, false, false, false, false, nIP1, mIP, emptyIP, 16, 100);
            dev.addRoutingList(route);
            int[] nIP2 = {192, 168, 1, 0};
            int[] gIP2 = {192, 168, 254, 2};
            route = new NetworkRoute(false, true, false, false, false, false, nIP2, mIP, gIP2, 15, 100);
            dev.addRoutingList(route);
            int[] nIP3 = {192, 168, 9, 0};
            route = new NetworkRoute(false, true, false, false, false, false, nIP3, mIP, gIP2, 15, 100);
            dev.addRoutingList(route);
            int[] nIP4 = {192, 168, 10, 0};
            route = new NetworkRoute(false, true, false, false, false, false, nIP4, mIP, gIP2, 15, 100);
            dev.addRoutingList(route);
            int[] nIP5 = {192, 168, 254, 0};
            int[] mIP5 = {255, 255, 255, 252};
            route = new NetworkRoute(true, true, false, false, false, false, nIP3, mIP5, emptyIP, 15, 100);
            dev.addRoutingList(route);
            route = new NetworkRoute(false, true, false, false, false, false, emptyIP, emptyIP, dGateway, 17, 100);
            dev.addRoutingList(route);
            devList.add(dev);
        }
        net.setNetworkDeviceList(devList);
        net.saveToDB();
        net.print();
    }
}