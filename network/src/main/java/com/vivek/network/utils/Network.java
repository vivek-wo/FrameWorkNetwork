package com.vivek.network.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Network {

    public static void onInitNetwork(Context context) {
        //ethernet enable and wifi enable
        if ((!ethernetEnabled()) && (!wifiEnabled(context))) {
            //open ethernet enable
            openEthernet();
        }
    }

    public static boolean ethernetEnabled() {
        boolean ethEnabled = EthernetManager.getInstance().getState() == EthernetManager.ETHERNET_STATE_ENABLED;
        return ethEnabled;
    }

    public static boolean wifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        return wifiEnabled;
    }

    private static void openEthernet() {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        EthernetDevInfo devInfo = getEthernetDevInfo(ethernetManager);
        if (devInfo != null) {
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static EthernetDevInfo getEthernetDevInfo(EthernetManager ethernetManager) {
        EthernetDevInfo devInfo = ethernetManager.getSavedConfig();
//        if (devInfo == null) {
        List<EthernetDevInfo> mListDevices = ethernetManager
                .getDeviceNameList();
        if (mListDevices != null) {
            for (EthernetDevInfo deviceinfo : mListDevices) {
                if (deviceinfo.getIfName().equals("eth0")) {
//                        devInfo = deviceinfo;
                    if (devInfo != null) {
                        devInfo.setHwaddr(deviceinfo.getHwaddr());
                    } else {
                        devInfo = deviceinfo;
                    }
                }
            }
        }
//        }
        return devInfo;
    }

    public static void updateEthernet() {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        ethernetManager.setEnabled(false);
        EthernetDevInfo devInfo = getEthernetDevInfo(ethernetManager);
        if (devInfo != null) {
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEthDevInfoModeManual(String ipAddress, String netMask,
                                                  String dnsAddr, String gateWay) {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        ethernetManager.setEnabled(false);
        EthernetDevInfo devInfo = getEthernetDevInfo(ethernetManager);
        if (devInfo != null) {
            devInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
            devInfo.setIpAddress(ipAddress);
            devInfo.setNetMask(netMask);
            devInfo.setDnsAddr(dnsAddr);
            devInfo.setGateWay(gateWay);
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEthDevInfoModeDHCP() {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        ethernetManager.setEnabled(false);
        EthernetDevInfo devInfo = getEthernetDevInfo(ethernetManager);
        if (devInfo != null) {
            devInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEthDevInfo(EthernetManager ethernetManager, EthernetDevInfo devInfo) {
        ethernetManager.setEnabled(false);
        if (devInfo != null) {
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEthernetMAC(String mac) {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        EthernetDevInfo devInfo = ethernetManager.getSavedConfig();
        ethernetManager.setEnabled(false);
        if (devInfo != null) {
            devInfo.setHwaddr(mac);
            try {
                ethernetManager.updateDevInfo(devInfo);
                ethernetManager.setEnabled(true);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCurrentConnectedIp(Context context) {
        String ip = "";
        ConnectivityManager service = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (wifiEnabled(context)) {
            NetworkInfo networkinfo = service.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkinfo.isConnected()) {
                ip = getWlanAddress();
                return ip;
            }
        }
        if (ethernetEnabled()) {
            NetworkInfo networkinfo = service.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (networkinfo.isConnected()) {
                ip = getEthernetAddress();
                return ip;
            }
        }
        return ip;
    }

    public static int getCurrentConnectedNetworkInfo(Context context) {
        ConnectivityManager service = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (wifiEnabled(context)) {
            NetworkInfo networkinfo = service.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkinfo.isConnected()) {
                return ConnectivityManager.TYPE_WIFI;
            }
        }
        if (ethernetEnabled()) {
            NetworkInfo networkinfo = service.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (networkinfo.isConnected()) {
                return ConnectivityManager.TYPE_ETHERNET;
            }
        }
        return -1;
    }

    public static String getEthernetMAC() {
        String mac = "";
        List<EthernetDevInfo> devInfoLists = EthernetManager.getInstance().getDeviceNameList();
        if (devInfoLists != null) {
            for (EthernetDevInfo deviceinfo : devInfoLists) {
                if (deviceinfo.getIfName().equals("eth0")) {
                    mac = deviceinfo.getHwaddr();
                }
            }
        }
        return mac;
    }

    public static String getEthernetAddress() {
        String ipv4 = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getDisplayName().equalsIgnoreCase("eth0")
                        || intf.getName().equalsIgnoreCase("eth0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            ipv4 = inetAddress.getHostAddress().toString();
                            return ipv4;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ipv4;
    }

    public static String getWlanAddress() {
        String ipv4 = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getDisplayName().equalsIgnoreCase("wlan0")
                        || intf.getName().equalsIgnoreCase("wlan0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            ipv4 = inetAddress.getHostAddress().toString();
                            return ipv4;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ipv4;
    }

    public static int getLevel(int rssi) {
        int level = -1;
        //根据获得的信号强度发送信息
        if (rssi <= 0 && rssi >= -50) {
            level = 3;
        } else if (rssi < -50 && rssi >= -70) {
            level = 2;
        } else if (rssi < -70 && rssi >= -80) {
            level = 1;
        } else if (rssi < -80 && rssi >= -100) {
            level = 0;
        }
        return level;
    }

    public static boolean isIP(String ip) {
        if (TextUtils.isEmpty(ip) || ip.length() < 7 || ip.length() > 15) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(ip);

        boolean isIP = mat.find();

        return isIP;
    }
}
