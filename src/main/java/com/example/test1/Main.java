package com.example.test1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.lang.Integer;
import static java.lang.Integer.parseInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



class HashSectionSettings  {
    private HashMap<String, String> hash = new HashMap<>();
    private String sectionName;
    private String separator;

    // Геттеры и сеттеры
    String getSectionName() {
        return sectionName;
    }
    Boolean setSectionName(String sName) {
        sectionName=sName;
        return true;
    }
    String getSeparator() {
        return separator;
    }
    Boolean setSeparator(String sep) {
        separator=sep;
        return true;
    }
    String getValue(String ha) {
        return hash.get(ha);
    }
    Boolean setValue(String ha, String val) {
        hash.put(ha, val);
        return true;
    }
    HashSectionSettings() {
        sectionName="";
        separator="";
    }
}
class IPv4 {
    private int[] intIP = new int[4];
    private String strIP = "";
    static String convertIPToString(int[] iIP) {
        String str = Integer.toString(iIP[0])+"."+Integer.toString(iIP[1])+"."+Integer.toString(iIP[2])+"."+Integer.toString(iIP[3]);
        System.out.println("IPv4 конвертор, IP: " + str);
        return(str);
    }
    int[] convertStringToIP(String sIP) {
        String[] parts = sIP.split("\\.");
        int[] iIP = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            iIP[i] = parseInt(parts[i]);
        }
        return(iIP);
    }
    int[] convertCIDRToMask(String sCidr) {
        int[] iIP = new int[4];
        int cidr = Integer.parseInt(sCidr);
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("CIDR must be between 0 and 32");
        }
        int mask = 0xffffffff << (32 - cidr);
        iIP[0] = (mask >> 24) & 0xff;
        iIP[1] = (mask >> 16) & 0xff;
        iIP[2] = (mask >> 8) & 0xff;
        iIP[3] = mask & 0xff;
        return (iIP);
    }
    int convertMaskToCidr(int[] mask) {
        int cidr = 0;
        for (int octet : mask) {
            if (octet == 255) {
                cidr += 8;
            } else {
                int m = octet & 0xFF;
                while (m != 0) {
                    cidr++;
                    m <<= 1;
                }
                break;
            }
        }
        return cidr;
    }
}
class ConnectorToSystem {
    private int deviceID;
    String deviceLabel;
    String vendor;
    String typeSW;
    String versionSW;
    String typeHW;
    String versionHW;
    String partNumber;
    String serialNumber;
    ArrayList <NetworkInterface> networkInterfaceList = new ArrayList<>();
    ArrayList <NetworkRoute> RoutingList = new ArrayList<>();
    // Роли сетевым хостам, признак FW и признак Routing интерфейсам невозможно назначить автоматически, только оператор
    private String placement;
    private NetworkDevice.networkDeviceType deviceType;
    private Boolean flagIsFirewall;
    // Ключи по которым будем искать значения
    ArrayList <HashSectionSettings> hashSSList = new ArrayList<>();
    NetworkDevice ImportFromFile(String fName) {
        return new NetworkDevice();
    }
    String findValueInSection(String fName, String section, String key, String separator, int subSection, String subSectionSeparator) {
        boolean isInCorrectSection = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fName), Charset.forName("CP1251")))) {
            String line;
            int count = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Удаляем пробелы в начале и конце строки

                // Проверяем, является ли строка разделом (например, [route])
                if (line.startsWith("[") && line.endsWith("]")) {
                    String currentSection = line.substring(1, line.length() - 1).trim();
                    isInCorrectSection = currentSection.equalsIgnoreCase(section);
                    continue;
                }

                // Если мы в нужном разделе, ищем ключ
                if (isInCorrectSection && line.contains(separator)) {
                    String[] parts = line.split(separator, 2); // Разделяем по первому сепаратору
                    String currentKey = parts[0].trim();
//                    System.out.println(", key1: " + key + " key2: " +currentKey + " in line: " + line);
                    if (currentKey.equalsIgnoreCase(key)) {
//                        System.out.println("found: " + parts[1].trim());
                        if (count++==subSection) {
                            if (parts[1].trim()=="") {
                                return null;
                            }
                            else {
                                return parts[1].trim(); // Возвращаем значение без пробелов
                            }
                        }

                    }
                }
            }
            return null;
        }
        catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return null; // Ключ не найден
    }
    ArrayList <String[]> findTableInSection(String fName, String section, String tableHeader, int skipLines, String tableEndKey, String tableSeparator, String columnSeparator, int columnNumber) {
        boolean isInCorrectSection = false;
        boolean isInCorrectTable = false;
        int linesAfterHeader = 0;
        ArrayList <String[]> foundTable = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fName), Charset.forName("CP1251")))) {
            String line;
            int skip=0;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Удаляем пробелы в начале и конце строки
                // Проверяем, является ли строка началом нужного раздела (например, [route])
                if (line.startsWith("[") && line.endsWith("]")) {
                    //System.out.println("section: " + line.substring(1, line.length() - 1).trim());
                    String currentSection = line.substring(1, line.length() - 1).trim();
                    isInCorrectSection = currentSection.equalsIgnoreCase(section);
                    continue;
                }
                // Если мы в нужном разделе, ищем начало таблицы
                if (isInCorrectSection) {
                    // Если мы внутри таблицы
                    System.out.println("in table section: " + section);
                    if (isInCorrectTable) {
                        System.out.println("in table section: " + tableHeader);
                        if (skip >= skipLines) {
                            // Проверяем не является ли строка концом таблицы
                            if (line.matches(tableSeparator + "+")) {
                                return foundTable;
                            } else {
                                String[] row = line.split(columnSeparator + "+");
                                foundTable.add(row);
                                System.out.println("line: " + line);
                            }
                        }
                        else {System.out.println("skipped line: " + line+ ", skip: " + skip); skip++;}
                    }
                    else {
                        isInCorrectTable = line.equalsIgnoreCase(tableHeader);
                        System.out.println("line: " + line + ", tableHeader: " + tableHeader);
                    }

                }
            }
        }
        catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return null; // Таблица не найдена
    }
}
//Класс объектов сетевое приложение, существует только в привязке к сетевому интерфейсу
class NetworkRoute {
    // Linux 192.168.12.0/24 dev eth0  proto kernel  scope link  src 192.168.12.101 default via 192.168.12.1 dev eth0
// Windows Сетевой адрес           Маска сети      Адрес шлюза       Интерфейс  Метрика
//          0.0.0.0          0.0.0.0   192.168.50.100   192.168.50.190     25
    private static int count = 0;
    private int routeID;
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
        routeID = count++;
        flagIsLocal = fIsLocal;
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
    Boolean print() {
        System.out.println("Объект NetworkRoute: " + routeID);
        System.out.println("Переменная interfaceID: " + interfaceID);
        System.out.println("Переменная адрес сети: " + Arrays.toString(netIP));
        System.out.println("Переменная маска подсети: " + Arrays.toString(maskIP));
        System.out.println("Переменная шлюз по умолчанию: " + Arrays.toString(gatewayIP));
        System.out.println("Переменная метрика: " + metric);
        System.out.println("Флаг Local: " + flagIsLocal);
        System.out.println("Флаг Connected: " + flagIsConnected);
        System.out.println("Флаг RIP: " + flagIsStatic);
        System.out.println("Флаг RIP: " + flagIsRIP);
        System.out.println("Флаг Mobile: " + flagIsMobile);
        System.out.println("Флаг BGP: " + flagIsBGP + "\n");
        return true;
    }
}
class NetworkInterface {
    private static int count = 0;
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
    private Boolean flagIsUp;
    // Хранит deviceID и interfaceId
    private ArrayList<ArrayList<Integer>> connectedDevicesInterfaces = new ArrayList<>();
    NetworkInterface() {
        interfaceID = count++;;
        speed=1000;
        for (int i = 0; i <= 2; i++) {addressIP[i] = 1;}
        addressIP[3] = 2;
        for (int i = 0; i <= 2; i++) {maskIP[i] = 255;}
        maskIP[3] = 0;
        for (int i = 0; i <= 3; i++) {defaultGateway[i] = 0;}
        interfaceLabel="Ge0/1";
        // А если нет приложений?
//        networkApplicationList.add(new NetworkApplication());
    }
    class NetworkApplication {
        private static int count = 0;
        private int applicationID;
        private int portIn;
        private int portOut;
        // Приложение может использовать несколько протоколов
        private NetworkPacket.NetworkProtocol networkProtocol;
        private String applicationLabel;
        NetworkApplication() {
            applicationID = count++;;
            portIn=80;
            portOut=80;
            networkProtocol=NetworkPacket.NetworkProtocol.TCP;
            applicationLabel="Web-сервер";
        }
        NetworkApplication(int pIn, int pOut, NetworkPacket.NetworkProtocol nProtocol, String aLabel) {
            applicationID = count++;;
            portIn = pIn;
            portOut = pOut;
            networkProtocol = nProtocol;
            applicationLabel = aLabel;
        }

        // Геттеры и сеттеры
        int getApplicationID() {return applicationID;}
        int getPortIn() {return portIn;}
        int getPortOut() {return portOut;}
        NetworkPacket.NetworkProtocol getNetworkProtocol() {return networkProtocol;}
        String getApplicationLabel() {return applicationLabel;}
        int getInterfaceID() { return NetworkInterface.this.interfaceID; }
        int[] getAddressIP() { return NetworkInterface.this.addressIP; }
//        int[] getNetworkDeviceID() { return NetworkDevice.this.getDeviceID(); }

        Boolean setApplicationID(int aID) {applicationID = aID; return true;}
        Boolean setPortIn(int pIn) {portIn = pIn; return true;}
        Boolean setPortOut(int pOut) {portOut = pOut; return true;}
        Boolean setNetworkProtocol(NetworkPacket.NetworkProtocol nProtocol) {networkProtocol = nProtocol; return true;}
        Boolean setApplicationLabel(String aLabel) {applicationLabel=aLabel; return true;}


        Boolean print() {
            System.out.println("Объект NetworkApplication: " + applicationID);
            System.out.println("Переменная название приложения: " + applicationLabel);
            System.out.println("Переменная входящий порт: " + portIn);
            System.out.println("Переменная исходящий порт: " + portOut);
            System.out.println("Переменная номер сетевого протокола: " + networkProtocol + "\n");
            return true;
        }
    }
    //Класс объектов сетевой интерфейс, существует только в привязке к сетевому устройству
    NetworkInterface(int sp, String iLabel, int[] aIP, int[] mIP, int[] dGateway, ArrayList <NetworkApplication> nApplicationList, Boolean flIsRoutable, ArrayList<ArrayList<Integer>> cDevicesInterfaces) {
        interfaceID = count++;
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
    Boolean getFlagIsRUp() {return flagIsUp;}
    ArrayList<ArrayList<Integer>> getConnectedDevicesInterfaces() {return connectedDevicesInterfaces;}

    Boolean setSpeed(int sp) {speed=sp; return true;}
    Boolean setAddressIP(int[] aIP) {addressIP=aIP; return true;}
    Boolean setMaskIP(int[] mIP) {maskIP=mIP; return true;}
    Boolean setDefaultGateway(int[] dGateway) {defaultGateway=dGateway; return true;}
    Boolean setInterfaceLabel(String iLabel) {interfaceLabel=iLabel; return true;}
    Boolean setNetworkApplicationList(ArrayList <NetworkApplication> nApplicationList) {networkApplicationList=nApplicationList; return true;}
    Boolean addNetworkApplication(int pIn, int pOut, NetworkPacket.NetworkProtocol nProtocol, String aLabel) {networkApplicationList.add(new NetworkApplication(pIn, pOut, nProtocol, aLabel)); return true;}
    Boolean setFlagIsRoutable(Boolean fIsRoutable) {flagIsRoutable=fIsRoutable; return true;}
    Boolean setFlagIsUp(Boolean fIsUp) {flagIsUp=fIsUp; return true;}
    // Добавить проверку на существование deice и interface, автоматически добавлять обратную связь
    Boolean setConnectedDevicesInterfaces(ArrayList<ArrayList<Integer>> cDevicesInterfaces) {connectedDevicesInterfaces = cDevicesInterfaces; return true;}
    Boolean print() {
        System.out.println("Объект NetworkInterface: " + interfaceID);
        System.out.println("Переменная название интерфейса: " + interfaceLabel);
        System.out.println("Переменная скорость соединения, Мб/с:: " + speed);
        System.out.println("Переменная IP адрес: " + Arrays.toString(addressIP));
        System.out.println("Переменная маска подсети: " + Arrays.toString(maskIP));
        System.out.println("Переменная шлюз (default gateway) по умолчанию: " + Arrays.toString(defaultGateway));
        System.out.println("Флаг Routable: " + flagIsRoutable);
        for (int i = 0; i < networkApplicationList.size(); i++) {
            networkApplicationList.get(i).print();
        }
        for (int i = 0; i < connectedDevicesInterfaces.size(); i++) {
            System.out.println("Элемент connectedDevicesInterfaces: " + connectedDevicesInterfaces.get(i));
        }
        System.out.println("\n");
        return true;
    }
}
/*class FirewallRule {
    public enum FWAction {
        ACCEPT,
        DROP,
        REJECT,
        RETURN,
        LOG
    }
    public enum FWConnectionState {
        NEW,
        ESTABLISHED,
        RELATED,
        INVALID,
        UNTRACKED,
        ANY
    }
    private String ruleLabel;
    private int[] sourceAddress = new int[4];
    private int[] destinationAddress = new int[4];
    private int[] sourceMask = new int[4];
    private int[] destinationMask = new int[4];
    private int sourcePort = 0;
    private int destinationPort = 0;
    private String protocol = "ANY";
    private FWAction action;
    private FWConnectionState state = FWConnectionState.ANY;
    FirewallRule(String rLabel, int[] sAddress, int[] dAddress, int sPort, int dPort, String proto, FWAction act, FWConnectionState st) {
        ruleLabel = rLabel;
        sourceAddress = sAddress;
        destinationAddress = dAddress;
        sourcePort = sPort;
        destinationPort = dPort;
        protocol = proto;
        action = act;
        state = st;
    }
    FirewallRule() {

    }
    @Override
    public String toString() {
        return "FirewallRule{" +
                "\n  ruleLabel='" + ruleLabel + '\'' +
                ",\n  sourceAddress=" + Arrays.toString(sourceAddress) +
                ",\n  sourceMask=" + sourceMask +
                ",\n  destinationAddress=" + Arrays.toString(destinationAddress) +
                ",\n  destinationMask=" + destinationMask +
                ",\n  sourcePort=" + sourcePort +
                ",\n  destinationPort=" + destinationPort +
                ",\n  protocol='" + protocol + '\'' +
                ",\n  action=" + action +
                ",\n  state=" + state +
                "\n}";
    }
    public String toIptablesRule(FirewallRule rule) {
        StringBuilder sb = new StringBuilder("iptables -A FORWARD");
        IPv4 adr = new IPv4();

        // Добавляем исходный адрес с маской
        if (!isZeroAddress(rule.sourceAddress)) {
            sb.append(" -s ").append(joinAddress(rule.sourceAddress));
            int srcMask = adr.convertMaskToCidr(rule.sourceMask);
            if (srcMask != 32) {
                sb.append("/").append(srcMask);
            }
        }

        // Добавляем адрес назначения с маской
        if (!isZeroAddress(rule.destinationAddress)) {
            sb.append(" -d ").append(joinAddress(rule.destinationAddress));
            int dstMask = adr.convertMaskToCidr(rule.destinationMask);
            if (dstMask != 32) {
                sb.append("/").append(dstMask);
            }
        }

        // Добавляем протокол (если не ANY)
        if (!"ANY".equalsIgnoreCase(rule.protocol)) {
            sb.append(" -p ").append(rule.protocol.toLowerCase());
        }

        // Добавляем порт источника (если указан)
        if (rule.sourcePort > 0) {
            sb.append(" --sport ").append(rule.sourcePort);
        }

        // Добавляем порт назначения (если указан)
        if (rule.destinationPort > 0) {
            sb.append(" --dport ").append(rule.destinationPort);
        }

        // Добавляем состояние соединения (если не ANY)
        if (rule.state != FWConnectionState.ANY) {
            sb.append(" -m state --state ").append(rule.state.name());
        }

        // Добавляем действие
        sb.append(" -j ").append(rule.action.name());

        // Добавляем комментарий (если есть)
        if (rule.ruleLabel != null && !rule.ruleLabel.isEmpty()) {
            sb.append("  # ").append(rule.ruleLabel);
        }

        return sb.toString();
    }

// Вспомогательные методы

    private String joinAddress(int[] address) {
        return address[0] + "." + address[1] + "." + address[2] + "." + address[3];
    }

    private boolean isZeroAddress(int[] address) {
        return address[0] == 0 && address[1] == 0 && address[2] == 0 && address[3] == 0;
    }


    private static void parseAddress(String input, String flag, int[] address,
                                     java.util.function.Consumer<String> maskConsumer) {
        Pattern pattern = Pattern.compile(flag + "\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?:/(\\d{1,2}))?");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split("\\.");
            for (int i = 0; i < 4; i++) {
                address[i] = Integer.parseInt(parts[i]);
            }
            if (matcher.group(2) != null) {
                maskConsumer.accept(matcher.group(2));
            }
        }
    }
    public FirewallRule parseRule(String ruleLine) {
        FirewallRule rule = new FirewallRule();

        // Извлечение комментария (label)
        Pattern commentPattern = Pattern.compile("#\\s*(.*)$");
        Matcher commentMatcher = commentPattern.matcher(ruleLine);
        if (commentMatcher.find()) {
            rule.ruleLabel = commentMatcher.group(1).trim();
        }

        // Извлечение действия
        Pattern actionPattern = Pattern.compile("-j\\s+(ACCEPT|DROP|REJECT|RETURN)");
        Matcher actionMatcher = actionPattern.matcher(ruleLine);
        if (actionMatcher.find()) {
            rule.action = FWAction.valueOf(actionMatcher.group(1));
        }

        // Извлечение исходного адреса
        IPv4 adr = new IPv4();
        parseAddress(ruleLine, "-s", rule.sourceAddress, val -> rule.sourceMask = adr.convertCIDRToMask(val));

        // Извлечение адреса назначения
        parseAddress(ruleLine, "-d", rule.destinationAddress, val -> rule.destinationMask = adr.convertCIDRToMask(val));

        // Извлечение протокола
        Pattern protoPattern = Pattern.compile("-p\\s+(\\w+)");
        Matcher protoMatcher = protoPattern.matcher(ruleLine);
        if (protoMatcher.find()) {
            rule.protocol = protoMatcher.group(1).toLowerCase();
        }

        // Извлечение порта источника
        Pattern sportPattern = Pattern.compile("--sport\\s+(\\d+)");
        Matcher sportMatcher = sportPattern.matcher(ruleLine);
        if (sportMatcher.find()) {
            rule.sourcePort = Integer.parseInt(sportMatcher.group(1));
        }

        // Извлечение порта назначения
        Pattern dportPattern = Pattern.compile("--dport\\s+(\\d+)");
        Matcher dportMatcher = dportPattern.matcher(ruleLine);
        if (dportMatcher.find()) {
            rule.destinationPort = Integer.parseInt(dportMatcher.group(1));
        }

        // Извлечение состояния соединения
        parseConnectionState(ruleLine, rule);

        return rule;
    }
    private static void parseConnectionState(String input, FirewallRule rule) {
        Pattern statePattern = Pattern.compile("-m\\s+state\\s+--state\\s+([\\w,]+)");
        Matcher stateMatcher = statePattern.matcher(input);
        if (stateMatcher.find()) {
            String states = stateMatcher.group(1);
            if (states.contains("NEW")) {
                rule.state = FWConnectionState.NEW;
            } else if (states.contains("ESTABLISHED")) {
                rule.state = FWConnectionState.ESTABLISHED;
            } else if (states.contains("RELATED")) {
                rule.state = FWConnectionState.RELATED;
            }
            // Добавить обработку других состояний при необходимости
        }
    }

    Boolean print() {
        System.out.println("Переменная название правила: " + ruleLabel);
        System.out.println("Переменная адрес источника: " + sourceAddress[0] + "." + sourceAddress[1] + "." + sourceAddress[2] + "." + sourceAddress[3]);
        System.out.println("Переменная адрес назначения: "  + destinationAddress[0] + "." + destinationAddress[1] + "." + destinationAddress[2] + "." + destinationAddress[3]);
        System.out.println("Переменная маска источника: " + sourceMask[0] + "." + sourceMask[1] + "." + sourceMask[2] + "." + sourceMask[3]);
        System.out.println("Переменная маска назначения: " + destinationMask[0] + "." + destinationMask[1] + "." + destinationMask[2] + "." + destinationMask[3]);
        System.out.println("Переменная порт источника: " + sourcePort);
        System.out.println("Переменная порт назначения: " + destinationPort);
        System.out.println("Переменная протокол: " + protocol);
        System.out.println("Переменная действие: " + action);
        System.out.println("Переменная состояние: " + state);
        System.out.println("\n");
        return true;
    }
}*/
/*class FirewallRule {
    public enum FWAction {
        ACCEPT,
        DROP,
        REJECT,
        RETURN,
        LOG
    }

    public enum FWConnectionState {
        NEW,
        ESTABLISHED,
        RELATED,
        INVALID,
        UNTRACKED,
        ANY
    }

    private String chain = "FORWARD"; // Новая переменная для хранения цепочки
    private String ruleLabel;
    private int[] sourceAddress = new int[4];
    private int[] destinationAddress = new int[4];
    private int[] sourceMask = new int[4];
    private int[] destinationMask = new int[4];
    private int sourcePort = 0;
    private int destinationPort = 0;
    private String protocol = "ANY";
    private FWAction action;
    private FWConnectionState state = FWConnectionState.ANY;

    // Конструкторы
    FirewallRule(String chain, String rLabel, int[] sAddress, int[] dAddress,
                 int sPort, int dPort, String proto, FWAction act, FWConnectionState st) {
        this.chain = chain;
        this.ruleLabel = rLabel;
        this.sourceAddress = sAddress;
        this.destinationAddress = dAddress;
        this.sourcePort = sPort;
        this.destinationPort = dPort;
        this.protocol = proto;
        this.action = act;
        this.state = st;
    }

    FirewallRule() {
    }

    // Геттеры и сеттеры для chain
    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "FirewallRule{" +
                "\n  chain='" + chain + '\'' +
                ",\n  ruleLabel='" + ruleLabel + '\'' +
                ",\n  sourceAddress=" + Arrays.toString(sourceAddress) +
                ",\n  sourceMask=" + Arrays.toString(sourceMask) +
                ",\n  destinationAddress=" + Arrays.toString(destinationAddress) +
                ",\n  destinationMask=" + Arrays.toString(destinationMask) +
                ",\n  sourcePort=" + sourcePort +
                ",\n  destinationPort=" + destinationPort +
                ",\n  protocol='" + protocol + '\'' +
                ",\n  action=" + action +
                ",\n  state=" + state +
                "\n}";
    }

    public String toIptablesRule() {
        StringBuilder sb = new StringBuilder("iptables -A ").append(chain);
        IPv4 adr = new IPv4();

        // Добавляем исходный адрес с маской
        if (!isZeroAddress(sourceAddress)) {
            sb.append(" -s ").append(joinAddress(sourceAddress));
            int srcMask = adr.convertMaskToCidr(sourceMask);
            if (srcMask != 32) {
                sb.append("/").append(srcMask);
            }
        }

        // Добавляем адрес назначения с маской
        if (!isZeroAddress(destinationAddress)) {
            sb.append(" -d ").append(joinAddress(destinationAddress));
            int dstMask = adr.convertMaskToCidr(destinationMask);
            if (dstMask != 32) {
                sb.append("/").append(dstMask);
            }
        }

        // Добавляем протокол (если не ANY)
        if (!"ANY".equalsIgnoreCase(protocol)) {
            sb.append(" -p ").append(protocol.toLowerCase());
        }

        // Добавляем порт источника (если указан)
        if (sourcePort > 0) {
            sb.append(" --sport ").append(sourcePort);
        }

        // Добавляем порт назначения (если указан)
        if (destinationPort > 0) {
            sb.append(" --dport ").append(destinationPort);
        }

        // Добавляем состояние соединения (если не ANY)
        if (state != FWConnectionState.ANY) {
            sb.append(" -m state --state ").append(state.name());
        }

        // Добавляем действие
        sb.append(" -j ").append(action.name());

        // Добавляем комментарий (если есть)
        if (ruleLabel != null && !ruleLabel.isEmpty()) {
            sb.append("  # ").append(ruleLabel);
        }

        return sb.toString();
    }

    // Вспомогательные методы
    private String joinAddress(int[] address) {
        return address[0] + "." + address[1] + "." + address[2] + "." + address[3];
    }

    private boolean isZeroAddress(int[] address) {
        return address[0] == 0 && address[1] == 0 && address[2] == 0 && address[3] == 0;
    }

    public static FirewallRule parseRule(String ruleLine) {
        FirewallRule rule = new FirewallRule();
        IPv4 adr = new IPv4();

        // Извлечение цепочки
        Pattern chainPattern = Pattern.compile("-A\\s+(\\w+)");
        Matcher chainMatcher = chainPattern.matcher(ruleLine);
        if (chainMatcher.find()) {
            rule.chain = chainMatcher.group(1);
        }

        // Извлечение комментария (label)
        Pattern commentPattern = Pattern.compile("#\\s*(.*)$");
        Matcher commentMatcher = commentPattern.matcher(ruleLine);
        if (commentMatcher.find()) {
            rule.ruleLabel = commentMatcher.group(1).trim();
        }

        // Извлечение действия
        Pattern actionPattern = Pattern.compile("-j\\s+(ACCEPT|DROP|REJECT|RETURN|LOG)");
        Matcher actionMatcher = actionPattern.matcher(ruleLine);
        if (actionMatcher.find()) {
            rule.action = FWAction.valueOf(actionMatcher.group(1));
        }

        // Извлечение исходного адреса
        parseAddress(ruleLine, "-s", rule.sourceAddress,
                val -> rule.sourceMask = adr.convertCIDRToMask(val));

        // Извлечение адреса назначения
        parseAddress(ruleLine, "-d", rule.destinationAddress,
                val -> rule.destinationMask = adr.convertCIDRToMask(val));

        // Извлечение протокола
        Pattern protoPattern = Pattern.compile("-p\\s+(\\w+)");
        Matcher protoMatcher = protoPattern.matcher(ruleLine);
        if (protoMatcher.find()) {
            rule.protocol = protoMatcher.group(1).toLowerCase();
        }

        // Извлечение порта источника
        Pattern sportPattern = Pattern.compile("--sport\\s+(\\d+)");
        Matcher sportMatcher = sportPattern.matcher(ruleLine);
        if (sportMatcher.find()) {
            rule.sourcePort = Integer.parseInt(sportMatcher.group(1));
        }

        // Извлечение порта назначения
        Pattern dportPattern = Pattern.compile("--dport\\s+(\\d+)");
        Matcher dportMatcher = dportPattern.matcher(ruleLine);
        if (dportMatcher.find()) {
            rule.destinationPort = Integer.parseInt(dportMatcher.group(1));
        }

        // Извлечение состояния соединения
        parseConnectionState(ruleLine, rule);

        return rule;
    }

    private static void parseAddress(String input, String flag, int[] address,
                                     java.util.function.Consumer<String> maskConsumer) {
        Pattern pattern = Pattern.compile(flag + "\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?:/(\\d{1,2}))?");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split("\\.");
            for (int i = 0; i < 4; i++) {
                address[i] = Integer.parseInt(parts[i]);
            }
            if (matcher.group(2) != null) {
                maskConsumer.accept(matcher.group(2));
            }
        }
    }

    private static void parseConnectionState(String input, FirewallRule rule) {
        Pattern statePattern = Pattern.compile("-m\\s+state\\s+--state\\s+([\\w,]+)");
        Matcher stateMatcher = statePattern.matcher(input);
        if (stateMatcher.find()) {
            String states = stateMatcher.group(1);
            if (states.contains("NEW")) {
                rule.state = FWConnectionState.NEW;
            } else if (states.contains("ESTABLISHED")) {
                rule.state = FWConnectionState.ESTABLISHED;
            } else if (states.contains("RELATED")) {
                rule.state = FWConnectionState.RELATED;
            }
        }
    }

    Boolean print() {
        System.out.println("Цепочка: " + chain);
        System.out.println("Название правила: " + ruleLabel);
        System.out.println("Адрес источника: " + joinAddress(sourceAddress));
        System.out.println("Маска источника: " + joinAddress(sourceMask));
        System.out.println("Адрес назначения: " + joinAddress(destinationAddress));
        System.out.println("Маска назначения: " + joinAddress(destinationMask));
        System.out.println("Порт источника: " + sourcePort);
        System.out.println("Порт назначения: " + destinationPort);
        System.out.println("Протокол: " + protocol);
        System.out.println("Действие: " + action);
        System.out.println("Состояние: " + state);
        System.out.println("\n");
        return true;
    }
}*/
class FirewallRule {
    public enum FWAction {
        ACCEPT,
        DROP,
        REJECT,
        RETURN,
        LOG
    }

    public enum FWConnectionState {
        NEW,
        ESTABLISHED,
        RELATED,
        INVALID,
        UNTRACKED,
        ANY
    }

    public enum RuleType {
        STANDARD,  // Обычное правило (-A)
        POLICY     // Политика по умолчанию (-P)
    }

    private RuleType ruleType = RuleType.STANDARD;
    private String chain = "FORWARD";
    private String ruleLabel;
    private int[] sourceAddress = new int[4];
    private int[] destinationAddress = new int[4];
    private int[] sourceMask = new int[4];
    private int[] destinationMask = new int[4];
    private int sourcePort = 0;
    private int destinationPort = 0;
    private String protocol = "ANY";
    private FWAction action;
    private FWConnectionState state = FWConnectionState.ANY;
    private int ruleID = 0;
    private static int count = 1;

    // Конструкторы
    FirewallRule(RuleType ruleType, String chain, String rLabel, FWAction action) {
        this.ruleType = ruleType;
        this.chain = chain;
        this.ruleLabel = rLabel;
        this.action = action;
        ruleID = count++;
    }

    FirewallRule() {
        ruleID = count++;
    }

    // Геттеры и сеттеры
    public RuleType getRuleType() { return ruleType; }
    public String getChain() { return chain; }
    public int getRuleID() { return ruleID; }


    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }
    public void setChain(String chain) { this.chain = chain; }


    @Override
    public String toString() {
        if (ruleType == RuleType.POLICY) {
            return "FirewallPolicy{" +
                    "\n  chain='" + chain + '\'' +
                    ",\n  action=" + action +
                    ",\n  ruleLabel='" + ruleLabel + '\'' +
                    "\n}";
        }

        return "FirewallRule{" +
                "\n  chain='" + chain + '\'' +
                ",\n  ruleLabel='" + ruleLabel + '\'' +
                ",\n  sourceAddress=" + Arrays.toString(sourceAddress) +
                ",\n  sourceMask=" + Arrays.toString(sourceMask) +
                ",\n  destinationAddress=" + Arrays.toString(destinationAddress) +
                ",\n  destinationMask=" + Arrays.toString(destinationMask) +
                ",\n  sourcePort=" + sourcePort +
                ",\n  destinationPort=" + destinationPort +
                ",\n  protocol='" + protocol + '\'' +
                ",\n  action=" + action +
                ",\n  state=" + state +
                "\n}";
    }

    public String toIptablesRule() {
        if (ruleType == RuleType.POLICY) {
            return String.format("iptables -P %s %s%s",
                    chain,
                    action.name(),
                    ruleLabel != null && !ruleLabel.isEmpty() ? "  # " + ruleLabel : "");
        }

        StringBuilder sb = new StringBuilder("iptables -A ").append(chain);
        IPv4 adr = new IPv4();

        // Добавляем исходный адрес с маской
        if (!isZeroAddress(sourceAddress)) {
            sb.append(" -s ").append(joinAddress(sourceAddress));
            int srcMask = adr.convertMaskToCidr(sourceMask);
            if (srcMask != 32) {
                sb.append("/").append(srcMask);
            }
        }

        // Добавляем адрес назначения с маской
        if (!isZeroAddress(destinationAddress)) {
            sb.append(" -d ").append(joinAddress(destinationAddress));
            int dstMask = adr.convertMaskToCidr(destinationMask);
            if (dstMask != 32) {
                sb.append("/").append(dstMask);
            }
        }

        // Добавляем протокол (если не ANY)
        if (!"ANY".equalsIgnoreCase(protocol)) {
            sb.append(" -p ").append(protocol.toLowerCase());
        }

        // Добавляем порт источника (если указан)
        if (sourcePort > 0) {
            sb.append(" --sport ").append(sourcePort);
        }

        // Добавляем порт назначения (если указан)
        if (destinationPort > 0) {
            sb.append(" --dport ").append(destinationPort);
        }

        // Добавляем состояние соединения (если не ANY)
        if (state != FWConnectionState.ANY) {
            sb.append(" -m state --state ").append(state.name());
        }

        // Добавляем действие
        sb.append(" -j ").append(action.name());

        // Добавляем комментарий (если есть)
        if (ruleLabel != null && !ruleLabel.isEmpty()) {
            sb.append("  # ").append(ruleLabel);
        }

        return sb.toString();
    }

    // Вспомогательные методы
    private String joinAddress(int[] address) {
        return address[0] + "." + address[1] + "." + address[2] + "." + address[3];
    }

    private boolean isZeroAddress(int[] address) {
        return address[0] == 0 && address[1] == 0 && address[2] == 0 && address[3] == 0;
    }

    public static FirewallRule parseRule(String ruleLine) {
        // Сначала проверяем, является ли правило политикой (-P)
        Pattern policyPattern = Pattern.compile("iptables\\s+-P\\s+(\\w+)\\s+(ACCEPT|DROP|REJECT|RETURN|LOG)(?:\\s+#\\s*(.*))?");
        Matcher policyMatcher = policyPattern.matcher(ruleLine);
//        System.out.println("Поиск признака политики: " + policyMatcher.find());
        if (policyMatcher.find()) {
//            System.out.println("Поиск признака политики: " + policyMatcher.find());
            FirewallRule rule = new FirewallRule();
            rule.ruleType = RuleType.POLICY;
            rule.chain = policyMatcher.group(1);
            rule.action = FWAction.valueOf(policyMatcher.group(2));
            if (policyMatcher.group(3) != null) {
                rule.ruleLabel = policyMatcher.group(3).trim();
            }
            rule.print();
            return rule;
        }

        // Обработка обычного правила (-A)
        FirewallRule rule = new FirewallRule();
        rule.ruleType = RuleType.STANDARD;
        IPv4 adr = new IPv4();

        // Извлечение цепочки
        Pattern chainPattern = Pattern.compile("-A\\s+(\\w+)");
        Matcher chainMatcher = chainPattern.matcher(ruleLine);
        if (chainMatcher.find()) {
            rule.chain = chainMatcher.group(1);
        }

        // Извлечение комментария (label)
        Pattern commentPattern = Pattern.compile("#\\s*(.*)$");
        Matcher commentMatcher = commentPattern.matcher(ruleLine);
        if (commentMatcher.find()) {
            rule.ruleLabel = commentMatcher.group(1).trim();
        }

        // Извлечение действия
        Pattern actionPattern = Pattern.compile("-j\\s+(ACCEPT|DROP|REJECT|RETURN|LOG)");
        Matcher actionMatcher = actionPattern.matcher(ruleLine);
        if (actionMatcher.find()) {
            rule.action = FWAction.valueOf(actionMatcher.group(1));
        }

        // Извлечение исходного адреса
        parseAddress(ruleLine, "-s", rule.sourceAddress,
                val -> rule.sourceMask = adr.convertCIDRToMask(val));

        // Извлечение адреса назначения
        parseAddress(ruleLine, "-d", rule.destinationAddress,
                val -> rule.destinationMask = adr.convertCIDRToMask(val));

        // Извлечение протокола
        Pattern protoPattern = Pattern.compile("-p\\s+(\\w+)");
        Matcher protoMatcher = protoPattern.matcher(ruleLine);
        if (protoMatcher.find()) {
            rule.protocol = protoMatcher.group(1).toLowerCase();
        }

        // Извлечение порта источника
        Pattern sportPattern = Pattern.compile("--sport\\s+(\\d+)");
        Matcher sportMatcher = sportPattern.matcher(ruleLine);
        if (sportMatcher.find()) {
            rule.sourcePort = Integer.parseInt(sportMatcher.group(1));
        }

        // Извлечение порта назначения
        Pattern dportPattern = Pattern.compile("--dport\\s+(\\d+)");
        Matcher dportMatcher = dportPattern.matcher(ruleLine);
        if (dportMatcher.find()) {
            rule.destinationPort = Integer.parseInt(dportMatcher.group(1));
        }

        // Извлечение состояния соединения
        parseConnectionState(ruleLine, rule);

        return rule;
    }

    private static void parseAddress(String input, String flag, int[] address,
                                     java.util.function.Consumer<String> maskConsumer) {
        Pattern pattern = Pattern.compile(flag + "\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?:/(\\d{1,2}))?");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split("\\.");
            for (int i = 0; i < 4; i++) {
                address[i] = Integer.parseInt(parts[i]);
            }
            if (matcher.group(2) != null) {
                maskConsumer.accept(matcher.group(2));
            }
        }
    }

    private static void parseConnectionState(String input, FirewallRule rule) {
        Pattern statePattern = Pattern.compile("-m\\s+state\\s+--state\\s+([\\w,]+)");
        Matcher stateMatcher = statePattern.matcher(input);
        if (stateMatcher.find()) {
            String states = stateMatcher.group(1);
            if (states.contains("NEW")) {
                rule.state = FWConnectionState.NEW;
            } else if (states.contains("ESTABLISHED")) {
                rule.state = FWConnectionState.ESTABLISHED;
            } else if (states.contains("RELATED")) {
                rule.state = FWConnectionState.RELATED;
            }
        }
    }

    Boolean print() {
        if (ruleType == RuleType.POLICY) {
            System.out.println("Номер: " + ruleID);
            System.out.println("Тип: Политика по умолчанию");
            System.out.println("Цепочка: " + chain);
            System.out.println("Действие: " + action);
            System.out.println("Описание: " + (ruleLabel != null ? ruleLabel : "нет"));
            System.out.println("\n");
            return true;
        }

        System.out.println("Номер: " + ruleID);
        System.out.println("Тип: Стандартное правило");
        System.out.println("Цепочка: " + chain);
        System.out.println("Название правила: " + ruleLabel);
        System.out.println("Адрес источника: " + joinAddress(sourceAddress));
        System.out.println("Маска источника: " + joinAddress(sourceMask));
        System.out.println("Адрес назначения: " + joinAddress(destinationAddress));
        System.out.println("Маска назначения: " + joinAddress(destinationMask));
        System.out.println("Порт источника: " + sourcePort);
        System.out.println("Порт назначения: " + destinationPort);
        System.out.println("Протокол: " + protocol);
        System.out.println("Действие: " + action);
        System.out.println("Состояние: " + state);
        System.out.println("\n");
        return true;
    }
}

//Класс объектов сетевое устройство
class NetworkDevice {
    private static int count = 0;
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
    private networkDeviceType deviceType;
    private ArrayList <NetworkRoute> RoutingList = new ArrayList<>();
    private ArrayList <FirewallRule> firewallRuleList = new ArrayList<>();


    NetworkDevice()
    {
        deviceID=count++;
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
        deviceID=count++;
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
    NetworkPacket processNetworkPacket(NetworkPacket networkPacket, InterNetwork net) {
        boolean flagIsSource = false;
        boolean flagIsDestination = false;
        int i;
        String message = SyslogGenerator.generateSyslogMessage(10, 6, IPv4.convertIPToString(networkInterfaceList.get(0).getAddressIP()) +" - "+ deviceLabel, "network driver", "Packet addressed to " + IPv4.convertIPToString(networkPacket.getDestinationAddress()) +":"+ networkPacket.getDestinationPort() + " received");
        System.out.println(message);
        //        syslogTrace.addMessageToBufferedLogCache(message);

        networkPacket.setIsReceived(false);
        networkPacket.setIsSent(false);
        networkPacket.setIsDropped(false);

        for (i=0;i<net.getNetworkDeviceList().size();i++) {
            int ii;
            for (ii=0;ii<net.getNetworkDeviceList().get(i).getNetworkInterfaceList().size();ii++) {
                if (networkPacket.getSourceAddress()  == net.getNetworkDeviceList().get(i).getNetworkInterfaceList().get(ii).getAddressIP()) {
                    flagIsSource = true;
                    System.out.println("исходное устройство идентифицировано:" + net.getNetworkDeviceList().get(i).getDeviceLabel());
                }
            }
        }

        for (i=0;i<net.getNetworkDeviceList().size();i++) {
            int ii;
            for (ii=0;ii<net.getNetworkDeviceList().get(i).getNetworkInterfaceList().size();ii++) {
                if (networkPacket.getDestinationAddress()  == net.getNetworkDeviceList().get(i).getNetworkInterfaceList().get(ii).getAddressIP()) {
                    flagIsDestination = true;
                }
            }
        }

        //Если это устройство - отправитель
        if (flagIsSource) {
            // Проверяем, что устройство является МСЭ
            if (flagIsFirewall) {
                //Применяем правила фильтрации
                if (!(applyFilterChainRules(networkPacket, firewallRuleList,"OUTPUT"))) {
                    networkPacket.setIsDropped(true);
                    message = SyslogGenerator.generateSyslogMessage(10, 6, deviceLabel, "network driver", "Packet addressed to " + IPv4.convertIPToString(networkPacket.getDestinationAddress()) +":"+ networkPacket.getDestinationPort() + " is dropped");
                    System.out.println(message);
                    //        syslogTrace.addMessageToBufferedLogCache(message);

                }
            }
            //Выполняем роутинг

        }
        else {
            //Выполняем роутинг

            //Если это устройство - отправитель
            if (flagIsDestination) {
                // Проверяем, что устройство является МСЭ
                if (flagIsFirewall) {
                    //Если это устройство - получатель, применяем правила фильтрации
                    if (!(applyFilterChainRules(networkPacket, firewallRuleList,"INPUT"))) {
                        networkPacket.setIsDropped(true);
                        message = SyslogGenerator.generateSyslogMessage(10, 6, deviceLabel, "network driver", "Packet addressed to " + networkPacket.getDestinationAddress() +":"+ networkPacket.getDestinationPort() + " is dropped");
                        System.out.println(message);
                        //        syslogTrace.addMessageToBufferedLogCache(message);
                    }
                    else {
                        networkPacket.setIsReceived(true);
                    }
                }
            }
            else {
                // Проверяем, что устройство является МСЭ
                if (flagIsFirewall) {
                    //Если это промежуточное устройство, применяем правила фильтрации
                    if (!(applyFilterChainRules(networkPacket, firewallRuleList,"FORWARD"))) {
                        networkPacket.setIsDropped(true);
                        message = SyslogGenerator.generateSyslogMessage(10, 6, deviceLabel, "network driver", "Packet addressed to " + networkPacket.getDestinationAddress() +":"+ networkPacket.getDestinationPort() + " is dropped");
                        System.out.println(message);
                        //        syslogTrace.addMessageToBufferedLogCache(message);
                    }
                    else {
                        networkPacket.setIsSent(true);
                    }
                }
            }
        }

        networkPacket.setTTL(networkPacket.getTTL()-1);
        return networkPacket;
    }
    private boolean applyFilterChainRules(NetworkPacket nPacket, ArrayList <FirewallRule> ruleTable, String chainName) {
        return true;
    }


    void getNetworkPacketState() {

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

    // Геттеры и сеттеры
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
    ArrayList <FirewallRule> getFirewallRuleList(){return firewallRuleList;}


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
    Boolean setFirewallRuleList(ArrayList <FirewallRule> rList) {firewallRuleList = rList; return true;}

    Boolean print() {
        System.out.println("Объект NetworkDevice: " + deviceID);
        System.out.println("Переменная название устройства: " + deviceLabel);
        System.out.println("Переменная производитель устройства: " + vendor);
        System.out.println("Переменная модель устройства: " + typeHW);
        System.out.println("Переменная версия устройства: " + versionHW);
        System.out.println("Переменная операционная система: " + typeSW);
        System.out.println("Переменная версия операционной системы: " + versionSW);
        System.out.println("Переменная part номер: " + partNumber);
        System.out.println("Переменная серийный номер: " + serialNumber);
        System.out.println("Переменная место размещения устройства: " + placement);
        System.out.println("Переменная тип устройства: " + deviceType);
        System.out.println("Флаг Firewall: " + flagIsFirewall);
        for (int i = 0; i < networkInterfaceList.size(); i++) {
            networkInterfaceList.get(i).print();
        }
        for (int i = 0; i < RoutingList.size(); i++) {
            RoutingList.get(i).print();
        }
        for (int i = 0; i < firewallRuleList.size(); i++) {
            firewallRuleList.get(i).print();
        }
        System.out.println("\n");
        return true;
    }
}
// Класс объектов сеть, предполагается существование одного только объекта
class InterNetwork {
    private static int count = 0;
    private int networkID;
    private String networkLabel;
    private ArrayList<NetworkDevice> NetworkDeviceList = new ArrayList<>();

    InterNetwork() {
        networkID = count++;
        networkLabel = "";
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

    // Геттеры и сеттеры
    int getNetworkID() {return networkID;}
    String getNetworkLabel() {return networkLabel;}
    ArrayList <NetworkDevice> getNetworkDeviceList() {return NetworkDeviceList;}
    Boolean setNetworkLabel(String nLabel) {networkLabel = nLabel; return true;}
    Boolean setNetworkDeviceList(ArrayList <NetworkDevice> nDeviceList) {NetworkDeviceList = nDeviceList; return true;}
    Boolean addNetworkDevice(NetworkDevice nDevice) {NetworkDeviceList.add(nDevice); return true;}

    //Устанавливает соединения между всему устройствами в NetworkDeviceList
    public void connectInterfaces() {
        // Проходим по всем парам устройств
        for (int i = 0; i < NetworkDeviceList.size(); i++) {
            NetworkDevice dev1 = NetworkDeviceList.get(i);
            System.out.println(NetworkDeviceList);
            ArrayList<NetworkInterface> interfaces1 = dev1.getNetworkInterfaceList();

            for (int j = i + 1; j < NetworkDeviceList.size(); j++) {
                NetworkDevice dev2 = NetworkDeviceList.get(j);
                ArrayList<NetworkInterface> interfaces2 = dev2.getNetworkInterfaceList();

                // Проверяем все комбинации интерфейсов между двумя устройствами
                for (NetworkInterface if1 : interfaces1) {
                    for (NetworkInterface if2 : interfaces2) {
                        // Проверяем, находятся ли интерфейсы в одной подсети
                        if (areInSameSubnet(if1, if2)) {
                            // Проверяем, является ли один из интерфейсов шлюзом по умолчанию
                            if (isDefaultGateway(if1, if2)) {
                                // Добавляем связь между интерфейсами
                                addConnection(if1, dev1.getDeviceID(), if2, dev2.getDeviceID());
                            }
                        }
                    }
                }
            }
        }
    }

    // Проверяет, является ли один интерфейс шлюзом по умолчанию для другого
    private boolean isDefaultGateway(NetworkInterface iface1, NetworkInterface iface2) {
        int[] defaultGateway1 = iface1.getDefaultGateway();
        int[] defaultGateway2 = iface2.getDefaultGateway();
        int[] addressIP1 = iface1.getAddressIP();
        int[] addressIP2 = iface2.getAddressIP();

        // Шлюз по умолчанию считается установленным, если он не нулевой, адрес, если он не нулевой и не [0,0,0,0]
        if (defaultGateway1 != null && defaultGateway2 != null && addressIP1 != null && addressIP2 != null && !(addressIP1[0] == 0 && addressIP1[1] == 0 && addressIP1[2] == 0 && addressIP1[3] == 0)&& !(addressIP2[0] == 0 && addressIP2[1] == 0 && addressIP2[2] == 0 && addressIP2[3] == 0)) {
            // Адрес одного из интерфейсов совпадает со шлюзом по умолчанию другого
            return ((defaultGateway1[0] == addressIP2[0]) && (defaultGateway1[1] == addressIP2[1]) && (defaultGateway1[2] == addressIP2[2]) && (defaultGateway1[3] == addressIP2[3])||(defaultGateway2[0] == addressIP1[0]) && (defaultGateway2[1] == addressIP1[1]) && (defaultGateway2[2] == addressIP1[2]) && (defaultGateway2[3] == addressIP1[3]));
        }
        return false;
    }

    // Проверяет, находятся ли два интерфейса в одной подсети
    private boolean areInSameSubnet(NetworkInterface if1, NetworkInterface if2) {
        int[] ip1 = if1.getAddressIP();
        int[] ip2 = if2.getAddressIP();
        int[] mask1 = if1.getMaskIP();
        int[] mask2 = if2.getMaskIP();

        // Проверяем, что маски подсети одинаковые
        if (!Arrays.equals(mask1, mask2)) {
            return false;
        }

        // Проверяем, что IP-адреса в одной подсети
        for (int i = 0; i < 4; i++) {
            if ((ip1[i] & mask1[i]) != (ip2[i] & mask1[i])) {
                return false;
            }
        }

        return true;
    }

    // Добавляет связь между двумя интерфейсами
    private void addConnection(NetworkInterface if1, int devId1, NetworkInterface if2, int devId2) {
        // Добавляем связь в первый интерфейс
        ArrayList<ArrayList<Integer>> connections1 = if1.getConnectedDevicesInterfaces();
        ArrayList<Integer> newConnection1 = new ArrayList<>();
        newConnection1.add(devId2);
        newConnection1.add(if2.getInterfaceID());
        connections1.add(newConnection1);

        // Добавляем связь во второй интерфейс
        ArrayList<ArrayList<Integer>> connections2 = if2.getConnectedDevicesInterfaces();
        ArrayList<Integer> newConnection2 = new ArrayList<>();
        newConnection2.add(devId1);
        newConnection2.add(if1.getInterfaceID());
        connections2.add(newConnection2);
    }


    Boolean print() {
        System.out.println("Объект InterNetwork: " + networkID);
        System.out.println("Переменная название сети: " + networkLabel);
        for (int i = 0; i < NetworkDeviceList.size(); i++) {
            NetworkDeviceList.get(i).print();
        }
        System.out.println("network Device list size: " + NetworkDeviceList.size() + "\n");
        return true;
    }
}
class NetworkPacket {
    public enum NetworkProtocol {
        UDP,
        TCP,
        ICMP
    }
    private NetworkProtocol networkProtocol;
    private int sourcePort;
    private int destinationPort;
    private int[] sourceAddress;
    private int[] destinationAddress;
    private int sequenceNumber;
    private String data;
    private int TTL;
    private boolean flagPacketIsDropped;
    private boolean flagPacketIsSent;
    private boolean flagPacketIsReceived;
    private NetworkDevice nextNetworkDevice;


    NetworkPacket(NetworkProtocol nProtokol, int[] sAddress, int[] dAddress, int sPort, int dPort, String d) {
        networkProtocol = nProtokol;
        sourcePort = sPort;
        destinationPort = dPort;
        sourceAddress = sAddress;
        destinationAddress = dAddress;
        data = d;
        TTL=64;
        flagPacketIsDropped = false;
        flagPacketIsSent = false;
        flagPacketIsReceived = false;
        nextNetworkDevice=null;
    }

    // Геттеры и сеттеры
    NetworkProtocol getNetworkProtocol() { return networkProtocol; }
    public int getSourcePort() { return sourcePort; }
    public int getDestinationPort() { return destinationPort; }
    public int[] getSourceAddress() { return sourceAddress; }
    public int[] getDestinationAddress() { return destinationAddress; }
    public String getData() { return data; }
    int getSequenceNumber() {return sequenceNumber;}
    int getTTL() {return TTL;}
    boolean isDropped() {return flagPacketIsDropped;}
    boolean isSent() {return flagPacketIsSent;}
    boolean isReceived() {return flagPacketIsReceived;}
    NetworkDevice getNextNetworkDevice() {return nextNetworkDevice;}

    void setSequenceNumber(int sNumber) {sequenceNumber = sNumber;}
    void setTTL(int ttl) {TTL = ttl;}
    void setIsDropped(boolean fPacketIsDropped) {flagPacketIsDropped=fPacketIsDropped;}
    void setIsSent(boolean fPacketIsSent) {flagPacketIsSent=fPacketIsSent;}
    void setIsReceived(boolean fPacketIsReceived) {flagPacketIsReceived=fPacketIsReceived;}
    void setNextNetworkDevice(NetworkDevice nNetworkDevice) {nextNetworkDevice=nNetworkDevice;}
}

class IPNetwork {
    private static int count = 1;
    int[] netIP = new int[4];
    int[] maskIP = new int[4];
    IPNetwork (int[] nIP, int[] mIP) {
        netIP = nIP;
        maskIP = mIP;
    }
    Boolean IsAddresBelongsToNetwork(int[] nIP, int[] mIP) {
        for (int i = 0; i < 4; i++) {
            // Применяем маску к IP-адресу и сравниваем с сетью
            if ((nIP[i] & mIP[i]) != (netIP[i] & maskIP[i])) {
                return false;
            }
        }
        return true;
    }
}
class ImportNetworkDevice {
    public enum networkDeviceSystemType {
        WINDOWS_RUS, LINUX;
    }
    private networkDeviceSystemType systemType;
    private ConnectorToSystemWindowsRus connectorToWindows = new ConnectorToSystemWindowsRus();
    private ConnectorToSystemLinux connectorToLinux = new ConnectorToSystemLinux();
    private NetworkDevice dev = new NetworkDevice();

    NetworkDevice importNetworkDeviceConfig(String fileName, networkDeviceSystemType sType) {

        if (Files.exists(Paths.get(fileName))) {
            switch(sType) {
                case WINDOWS_RUS:
                    dev = connectorToWindows.ImportFromFile(fileName);
                    break;
                case LINUX:
                    dev = connectorToLinux.ImportFromFile(fileName);
                    break;
            }
            return dev;
        }
        else {
            return null;
        }

    }
}
class ConnectorToSystemWindowsRus extends ConnectorToSystem {

    NetworkDevice ImportFromFile(String fName) {
        NetworkDevice dev = new NetworkDevice();
        NetworkInterface inter = new NetworkInterface();
        ArrayList <NetworkInterface> interfaceList = new ArrayList<>();
        ArrayList <NetworkRoute> routingList = new ArrayList<>();
        String value = "";
        ArrayList <String[]> valueTable = new ArrayList<>();
        Boolean flagValueNotFound = false;
        IPv4 valueIP = new IPv4();

//        System.out.println("params, file: " + fName + ", section: " + hashSSList.get(0).getSectionName() + ", value: " + hashSSList.get(0).getValue("deviceLabel") + ", separator: " + hashSSList.get(0).getSeparator());
// Вытаскивает данные о системе
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("deviceLabel"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setDeviceLabel(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("vendor"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setVendor(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("typeSW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setTypeSW(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("versionSW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setVersionSW(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("typeHW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setTypeHW(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("versionHW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setVersionHW(value);}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("vendor"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {dev.setVendor(value);}
        else {flagValueNotFound = true;}
//Вытаскивает данные о первом из рабочих интерфейсов, дописать
        value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("addressIP"), hashSSList.get(1).getSeparator(), 1, "");
        if (value != null) {inter.setAddressIP(valueIP.convertStringToIP(value));}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("maskIP"), hashSSList.get(1).getSeparator(), 1, "");
        if (value != null) {inter.setMaskIP(valueIP.convertStringToIP(value));}
        else {flagValueNotFound = true;}
        value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("defaultGateway"), hashSSList.get(1).getSeparator(), 1, "");
        if (value != null) {inter.setDefaultGateway(valueIP.convertStringToIP(value));}
        else {flagValueNotFound = true;}
        inter.setFlagIsRoutable(false);
        interfaceList.add(inter);
        dev.setNetworkInterfaceList(interfaceList);
//Вытаскивает данные о таблице маршрутизации
        valueTable = findTableInSection(fName, "route", "IPv4 таблица маршрута", 3,"=", "=", "  ", 5);
        if (valueTable != null) {
            int i;
            for (i=0;i<valueTable.size();i++) {
                String[] routeStr = new String[5];
                NetworkRoute routeNet;
                routeStr = valueTable.get(i);
                int interfaceID = 0;
                if (routeStr[2].equalsIgnoreCase("On-link")) {routeStr[2]="0.0.0.0";}
                int j;
                for (j=0;j<interfaceList.size();j++) {
                    if (valueIP.convertStringToIP(routeStr[3]) == interfaceList.get(j).getAddressIP()) {
                        interfaceID = interfaceList.get(j).getInterfaceID();
                    };
                }
                routeNet = new NetworkRoute(routeStr[2].equalsIgnoreCase("0.0.0.0"), true, false, false, false, false, valueIP.convertStringToIP(routeStr[0]), valueIP.convertStringToIP(routeStr[1]), valueIP.convertStringToIP(routeStr[2]), interfaceID, Integer.parseInt(routeStr[4]));
//                routeNet.print();
                routingList.add(routeNet);
            }
            dev.setRoutingList(routingList);
        }
        else {flagValueNotFound = true;}
        return dev;
    }
    ConnectorToSystemWindowsRus(){
        HashSectionSettings hSS = new HashSectionSettings();
        hSS.setSectionName("systeminfo");
        hSS.setSeparator(":");
        hSS.setValue("deviceLabel", "Имя узла");
        hSS.setValue("vendor", "Изготовитель системы");
        hSS.setValue("typeSW", "Название ОС");
        hSS.setValue("versionSW", "Версия ОС");
        hSS.setValue("typeHW", "Модель системы");
        hSS.setValue("versionHW", "Версия BIOS");
        hSS.setValue("partNumber", "");
        hSS.setValue("serialNumber", "");
        hashSSList.add(0, hSS);

        hSS = new HashSectionSettings();
        hSS.setSectionName("ipconfig");
        hSS.setSeparator(":");
        hSS.setValue("addressIP","IPv4-адрес. . . . . . . . . . . .");
        hSS.setValue("maskIP","Маска подсети . . . . . . . . . .");
        hSS.setValue("defaultGateway","Основной шлюз. . . . . . . . .");
// Другой разделитель - " "
        hSS.setValue("interfaceLabel","Адаптер Ethernet");
        hashSSList.add(1, hSS);

        hSS = new HashSectionSettings();
        hSS.setSectionName("route");
        hSS.setSeparator("  ");
        hSS.setValue("netIP","Сетевой адрес");
        hSS.setValue("maskIP","Маска подсети");
        hSS.setValue("gatewayIP","Адрес шлюза");
        hSS.setValue("interfaceID","Интерфейс");
        hSS.setValue("metric","Метрика");
        hashSSList.add(2, hSS);
    }
}
class ConnectorToSystemLinux extends ConnectorToSystem {
    ConnectorToSystemLinux() {
        HashSectionSettings hSS = new HashSectionSettings();
        hSS.setSectionName("systeminfo");
        hSS.setSeparator(":");
        hSS.setValue("deviceLabel", "Static hostname");
        hSS.setValue("vendor", "Hardware Vendor");
        hSS.setValue("typeSW", "Operating System");
        hSS.setValue("versionSW", "Kernel");
        hSS.setValue("typeHW", "Hardware Model");
        hSS.setValue("versionHW", "Firmware Version");
        hSS.setValue("partNumber", "");
        hSS.setValue("serialNumber", "");
        hashSSList.add(0, hSS);

        hSS = new HashSectionSettings();
        hSS.setSectionName("ipconfig");
        hSS.setSeparator(":");
        hSS.setValue("interfaceLabel", "Interface");
        hSS.setValue("interfaceState", "State");
        hSS.setValue("interfaceSpeed", "Speed");
        hSS.setValue("addressIP", "IP-address");
        hSS.setValue("maskIP", "Mask");
        hashSSList.add(1, hSS);

        hSS = new HashSectionSettings();
        hSS.setSectionName("route");
        hSS.setSeparator(" ");
        hSS.setValue("netIP", "Destination");
        hSS.setValue("maskIP", "Genmask");
        hSS.setValue("gatewayIP", "Gateway");
        hSS.setValue("interfaceID", "Iface");
        hSS.setValue("metric", "Metric");
        hashSSList.add(2, hSS);
    }
    NetworkDevice ImportFromFile(String fName) {
        NetworkDevice dev = new NetworkDevice();
        ArrayList<NetworkInterface> interfaceList = new ArrayList<>();
        ArrayList<NetworkRoute> routingList = new ArrayList<>();
        String value = "";
        ArrayList<String[]> valueTable = new ArrayList<>();
        Boolean flagValueNotFound = false;
        IPv4 valueIP = new IPv4();

//        System.out.println("params, file: " + fName + ", section: " + hashSSList.get(0).getSectionName() + ", value: " + hashSSList.get(0).getValue("deviceLabel") + ", separator: " + hashSSList.get(0).getSeparator());
// Вытаскивает данные о системе
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("deviceLabel"), hashSSList.get(0).getSeparator(), 1, "");
        System.out.println("Value: " + value + ", fname: " + fName + ", section: " + hashSSList.get(0).getSectionName() + ", hash: " + hashSSList.get(0).getValue("deviceLabel"));

        if (value != null) {
//            System.out.println("deviceLabel: " + value);
            dev.setDeviceLabel(value);
        } else {
            flagValueNotFound = true;
        }
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("vendor"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {
//            System.out.println("vendor: " + value);
            dev.setVendor(value);
        } else {
            flagValueNotFound = true;
        }
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("typeSW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {
//            System.out.println("typeSW: " + value);
            dev.setTypeSW(value);
        } else {
            flagValueNotFound = true;
        }
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("versionSW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {
//            System.out.println("versionSW: " + value);
            dev.setVersionSW(value);
        } else {
            flagValueNotFound = true;
        }
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("typeHW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {
//            System.out.println("typeHW: " + value);
            dev.setTypeHW(value);
        } else {
            flagValueNotFound = true;
        }
        value = findValueInSection(fName, hashSSList.get(0).getSectionName(), hashSSList.get(0).getValue("versionHW"), hashSSList.get(0).getSeparator(), 1, "");
        if (value != null) {
//            System.out.println("versionHW: " + value);
            dev.setVersionHW(value);
        } else {
            flagValueNotFound = true;
        }
//Вытаскивает данные о рабочих интерфейсах
        flagValueNotFound = false;
        int ii = 0;
        while (!flagValueNotFound) {
            NetworkInterface inter = new NetworkInterface();
            ii++;
            String iState = "";
            value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("interfaceLabel"), hashSSList.get(1).getSeparator(), ii, "=");
//            System.out.println("Value: " + value + ", fname: " + fName + ", section: " + hashSSList.get(1).getSectionName() + ", hash: " + hashSSList.get(1).getValue("interfaceLabel"));
            if (value != null) {
//                System.out.println("interfaceLabel: " + value);
                inter.setInterfaceLabel(value);
            }
            else {
                flagValueNotFound = true;
            }
            value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("interfaceState"), hashSSList.get(1).getSeparator(), ii, "=");
            if (value==null) {
                iState="";
            }
            else {
                iState = value;
            }
            if (value != null) {
//                System.out.println("interfaceState: " + value);

            }
            else {
                flagValueNotFound = true;
            }
            value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("interfaceSpeed"), hashSSList.get(1).getSeparator(), ii, "=");
            if (value != null) {
                //               System.out.println("interfaceSpeed: " + value);
                inter.setSpeed(Integer.parseInt(value));
            }
            else {
                flagValueNotFound = true;
            }
            value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("addressIP"), hashSSList.get(1).getSeparator(), ii, "=");

//            System.out.println("Value: " + value + ", fname: " + fName + ", section: " + hashSSList.get(1).getSectionName() + ", hash: " + hashSSList.get(1).getValue("addressIP"));
            if (value != null) {
//                System.out.println("addressIP: " + value);
                inter.setAddressIP(valueIP.convertStringToIP(value));
            }
            else {
                flagValueNotFound = true;
            }
            value = findValueInSection(fName, hashSSList.get(1).getSectionName(), hashSSList.get(1).getValue("maskIP"), hashSSList.get(1).getSeparator(), ii, "=");
//            System.out.println("Value: " + value + ", fname: " + fName + ", section: " + hashSSList.get(1).getSectionName() + ", hash: " + hashSSList.get(1).getValue("maskIP"));
            if (value != null) {
//                System.out.println("maskIP: " + value);
                inter.setMaskIP(valueIP.convertCIDRToMask(value.substring(2, value.length() - 1).trim()));
            }
            else {
                flagValueNotFound = true;
            }
            inter.setFlagIsRoutable(true);
//            System.out.println("Flag: " + (!flagValueNotFound) + " " +iState.equalsIgnoreCase("UP") +" iState= 0" + iState+"0");
            inter.setFlagIsUp(iState.equalsIgnoreCase("UP"));
            if (!flagValueNotFound) {interfaceList.add(inter);}
        }
        dev.setFlagIsFirewall(true);
        dev.setNetworkInterfaceList(interfaceList);
//Вытаскивает данные о таблице маршрутизации
        valueTable = findTableInSection(fName, "route", "Kernel IP routing table", 1,"=", "=", " ", 8);
//        System.out.println("ValueTable: " + valueTable);
        if (valueTable != null) {
            int i;
            for (i=0;i<valueTable.size();i++) {
                String[] routeStr = new String[8];
                NetworkRoute routeNet;
                routeStr = valueTable.get(i);
//                System.out.println("routeStr: " + routeStr);
                int interfaceID = 0;
                int j;
//                System.out.println("interfaceList.size(): " + interfaceList.size());
                for (j=0;j<interfaceList.size();j++) {
//                    System.out.println("routeStr[7]: " + routeStr[7]);
                    if (routeStr[7].equalsIgnoreCase(interfaceList.get(j).getInterfaceLabel())) {
                        interfaceID = interfaceList.get(j).getInterfaceID();
                    };
                }
                routeNet = new NetworkRoute(routeStr[1].equalsIgnoreCase("0.0.0.0"), routeStr[2].toUpperCase().contains("U"), false, false, false, false, valueIP.convertStringToIP(routeStr[0]), valueIP.convertStringToIP(routeStr[2]), valueIP.convertStringToIP(routeStr[1]), interfaceID, Integer.parseInt(routeStr[4]));
                routingList.add(routeNet);
                // Если это маршрут по умолчанию, дописать его в соответствующий сетевой интерфейс
                if ((routeStr[0].equalsIgnoreCase("0.0.0.0")) && (routeStr[2].equalsIgnoreCase("0.0.0.0"))) {
                    for (ii=0;ii<dev.getNetworkInterfaceList().size();ii++) {
                        if (routeStr[7].equalsIgnoreCase(dev.getNetworkInterfaceList().get(ii).getInterfaceLabel())){
                            dev.getNetworkInterfaceList().get(ii).setDefaultGateway(valueIP.convertStringToIP(routeStr[1]));
                        }

                    }
                }
//                routeNet.print();
            }
            dev.setRoutingList(routingList);

        }
        else {flagValueNotFound = true;}
//Вытаскивает данные о таблице фильтрации
        boolean isInCorrectSection = false;
        boolean isFirewall = false;
        ArrayList <FirewallRule> fRuleList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fName), Charset.forName("CP1251")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Удаляем пробелы в начале и конце строки

                // Проверяем, является ли строка разделом (например, [route])
                if (line.startsWith("[") && line.endsWith("]")) {
                    String currentSection = line.substring(1, line.length() - 1).trim();
                    isInCorrectSection = currentSection.equalsIgnoreCase("firewall");
                    continue;
                }
                // Если мы в нужном разделе, разбираем строку
                if (isInCorrectSection) {
                    FirewallRule fRule = new FirewallRule();
                    fRule=fRule.parseRule(line);
                    System.out.println("Поиск пустого: ");
                    fRule.print();
                    fRuleList.add(fRule);
                    isFirewall=true;
//                    System.out.println("i: " + line);
//                    System.out.println("o: " + fRule.toIptablesRule());
                }
            }
            if (isFirewall) {
                dev.setFirewallRuleList(fRuleList);
                dev.setFlagIsFirewall(true);
            }
        }
        catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return dev;
    }
}
class PacketTracer {

    PacketTracer() {
    }
    public boolean trace(NetworkInterface.NetworkApplication nApp1, NetworkInterface.NetworkApplication nApp2, InterNetwork net, int sNumber) {
        int currentNetworkApplicationID = nApp1.getApplicationID();
        NetworkPacket networkPacket = new NetworkPacket(nApp1.getNetworkProtocol(), nApp1.getAddressIP(), nApp2.getAddressIP(), nApp1.getPortOut(), nApp2.getPortIn(), "Я к вам пишу — чего же боле?");
        NetworkDevice currentNetworkDevice = new NetworkDevice();
        int i;
        System.out.println("трейс запущен");
        networkPacket.setSequenceNumber(sNumber);
        // Получаем устройство на котором находимся
        for (i=0;i<net.getNetworkDeviceList().size();i++) {
            int j;
            for (j=0;j<net.getNetworkDeviceList().get(i).getNetworkInterfaceList().size();j++) {
                if (nApp1.getInterfaceID() == net.getNetworkDeviceList().get(i).getNetworkInterfaceList().get(j).getInterfaceID()) {
                    currentNetworkDevice = net.getNetworkDeviceList().get(i);
                }
            }
        }
        System.out.println("устройство идентифицировано:" + currentNetworkDevice.getDeviceLabel());
        // Устанавливаем у пакета указатель на текущее сетевое устройство и статус отправлено устройству
        networkPacket.setNextNetworkDevice(currentNetworkDevice);
        networkPacket.setIsSent(true);
        // Пока пакет не дойдет или не выйдет TTL
        while (!(networkPacket.isReceived()) && (networkPacket.getTTL()>0)) {
            // Кидаем пакет в интерфейс сетевого устройства
            System.out.println("пакет ушел в устройство:" + currentNetworkDevice.getDeviceLabel());
            networkPacket = currentNetworkDevice.processNetworkPacket(networkPacket, net);
            // Анализируем ответ сетевого устройства (сброшен, отправлен дальше)
            if (networkPacket.isDropped()) {
                String message = SyslogGenerator.generateSyslogMessage(10, 4, currentNetworkDevice.getDeviceLabel(), "Packet tracer", "Packet is dropped");
                System.out.println(message);
//            syslogTrace.addMessageToBufferedLogCache(message);
                return false;
            }
            else {
                // Определить на какое следующее устройство кидаем пакет
                currentNetworkDevice = networkPacket.getNextNetworkDevice();
                String message = SyslogGenerator.generateSyslogMessage(10, 6, currentNetworkDevice.getDeviceLabel(), "Packet tracer", "Packet sent to " + currentNetworkDevice.getDeviceLabel());
                System.out.println(message);
//                syslogTrace.addMessageToBufferedLogCache(message);
            }

        }
        if (networkPacket.isReceived()) {
            if (networkPacket.getSequenceNumber()==0) {
                networkPacket.setSequenceNumber(1);
                String message = SyslogGenerator.generateSyslogMessage(10, 6, currentNetworkDevice.getDeviceLabel(), networkPacket.getDestinationAddress() +":"+ networkPacket.getDestinationPort(), "Packet successfully received by " + currentNetworkDevice.getDeviceLabel());
                System.out.println(message);
                trace(nApp2, nApp1, net, 1);
            }
            else {
                String message = SyslogGenerator.generateSyslogMessage(10, 6, currentNetworkDevice.getDeviceLabel(), networkPacket.getDestinationAddress() +":"+ networkPacket.getDestinationPort(), "Returned packet successfully received by " + currentNetworkDevice.getDeviceLabel());
                System.out.println(message);

//                syslogTrace.flushLogCache();
                return true;
            }
        }

//        syslogTrace.flushLogCache();
        return false;
    }


}

class InformationFlow {

}

class SyslogGenerator {
    private ArrayList <String> logCache = new ArrayList<>();
    private String fileNameLog = "";

    SyslogGenerator(String fileNameLog) {
        fileNameLog=fileNameLog;
    }

    public static String generateSyslogMessage(
            int facility,       // 0-23 (0=kernel, 1=user-level, 3=daemon и т.д.)
            int severity,       // 0-7 (0=Emergency, 6=Info, 7=Debug)
            String hostname,    // Имя хоста
            String appName,     // Название приложения
            String message) {   // Текст сообщения

        // Рассчитываем PRI (приоритет = facility * 8 + severity)
        int priority = facility * 8 + severity;

        // Форматируем timestamp в RFC 3339 (ISO 8601)
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .format(LocalDateTime.now())
                .replace("T", " ");

        // Формируем структурированные данные (можно кастомные)
        String structuredData = "-"; // "-" означает отсутствие структурированных данных

        // Собираем полное сообщение
        return String.format(Locale.US,
                "<%d>1 %s %s %s - - %s %s",
                priority,
                timestamp,
                hostname,
                appName,
                structuredData,
                message);
    }
    public void addMessageToBufferedLogCache(String record) {
        logCache.add(record);
    }

    public boolean flushLogCache() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileNameLog), Charset.forName("CP1251")))) {

            for (String line : logCache) {
                writer.write(line);
                writer.newLine(); // Добавляем разделитель строк
            }
            System.out.println("Лог PacketTracer успешно записан в файл: " + fileNameLog);
            return true;
        }
        catch (IOException e) {
            System.err.println("Ошибка при записи лога в файл: " + e.getMessage());
            return false;
        }
    }

    public boolean clearLogCache() {

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(fileNameLog, Charset.forName("CP1251"), false))) {
            // Параметр 'false' в FileWriter означает перезапись файла (очистка содержимого)
            // Просто создание BufferedWriter в этом режиме очистит файл

            System.out.println("Содержимое файла " + fileNameLog + " успешно очищено");
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка при очистке файла: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
