package com.vivek.network.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class NetworkService extends Service {
    private static final int THREAD_DELAY_SLEEP = 5000;
    public static final String ACTION_NETWORK_CONNECT_INFO
            = "com.taichuan.network.utils.networkservice.action.network_connect_info";
    ConnectivityManager mService;
    WifiManager mWifiManager;
//    BroadcastReceiver mEthStateReceiver;
//    IntentFilter mFilter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        initReceiver();
        new NetworkThread().start();
    }

//    public void initReceiver() {
//        mFilter = new IntentFilter();
//        mFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
//        mFilter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
//
//        mEthStateReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                handleEvent(context, intent);
//            }
//        };
//        registerReceiver();
//    }
//
//    public void registerReceiver() {
//        registerReceiver(mEthStateReceiver, mFilter);
//    }
//
//    public void unregisterReceiver() {
//        unregisterReceiver(mEthStateReceiver);
//    }

    class NetworkThread extends Thread {
        private static final int NETWORK_CHECKED = 0x01;
        private static final int NETWORK_UPDATE = 0x02;
        private int NETWORK_UPDATE_DELAY_SLEEP = 2 * 1000 * 60;
        Intent intent = new Intent(ACTION_NETWORK_CONNECT_INFO);
        Bundle bundle = new Bundle();
        Handler mHandler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mHandler = new Handler(Looper.myLooper()) {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case NETWORK_CHECKED:
                            onNetworkChecked();
                            sendEmptyMessageDelayed(NETWORK_CHECKED, THREAD_DELAY_SLEEP);
                            break;
                        case NETWORK_UPDATE:
                            updateEthDevInfo();
                            NETWORK_UPDATE_DELAY_SLEEP += NETWORK_UPDATE_DELAY_SLEEP;
                            if (NETWORK_UPDATE_DELAY_SLEEP > 60 * 60 * 1000) {
                                NETWORK_UPDATE_DELAY_SLEEP = 60 * 60 * 1000;
                            }
                            break;
                    }
                }

            };
            mHandler.sendEmptyMessage(NETWORK_CHECKED);
            Looper.loop();
        }

        private void onNetworkChecked() {
            boolean ethernetEnabled = Network.ethernetEnabled();
            boolean wifiEnabled = Network.wifiEnabled(getApplicationContext());
            boolean ethernetConnected = false;
            boolean wifiConnected = false;
            int wifiRssi = -200;
            if (mService != null) {
                if (wifiEnabled) {
                    NetworkInfo networkinfo = mService.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    wifiConnected = networkinfo.isConnected();
                }
                if (!wifiConnected) {
                    if (ethernetEnabled) {
                        NetworkInfo networkinfo = mService.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                        ethernetConnected = networkinfo.isConnected();
                        //网络连接的定时操作处理
                        if (ethernetConnected) {
                            if (mHandler.hasMessages(NETWORK_UPDATE)) {
                                NETWORK_UPDATE_DELAY_SLEEP = 2 * 1000 * 60;
                                mHandler.removeMessages(NETWORK_UPDATE);
                            }
                        } else {
                            if (!mHandler.hasMessages(NETWORK_UPDATE)) {
                                mHandler.sendEmptyMessageDelayed(NETWORK_UPDATE, NETWORK_UPDATE_DELAY_SLEEP);
                            }
                        }
                    }
                } else {
                    if (mWifiManager == null) {
                        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    }
                    WifiInfo info = mWifiManager.getConnectionInfo();
                    wifiRssi = info.getRssi();
                }
            } else {
                mService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            }

            System.out.println("ethernetEnabled: " + ethernetEnabled + ",wifiEnabled: "
                    + wifiEnabled + ",ethernetConnected: " + ethernetConnected + ",wifiConnected: "
                    + wifiConnected + ",wifiRssi: " + wifiRssi);
            bundle.putBoolean("extra_data_ethernet", ethernetEnabled);
            bundle.putBoolean("extra_data_wifi", wifiEnabled);
            bundle.putBoolean("extra_date_ethernet_connected", ethernetConnected);
            bundle.putBoolean("extra_date_wifi_connected", wifiConnected);
            bundle.putInt("extra_date_wifi_rssi", wifiRssi);
            intent.putExtras(bundle);
            sendBroadcast(intent);
        }

        private void updateEthDevInfo() {
            EthernetManager ethernetManager = EthernetManager.getInstance();
            EthernetDevInfo devInfo = Network.getEthernetDevInfo(ethernetManager);
            if (devInfo.getConnectMode() == EthernetDevInfo.ETHERNET_CONN_MODE_DHCP) {
                System.out.println("ethernetConnect failed updateEthDevInfo DHCP " + NETWORK_UPDATE_DELAY_SLEEP);
                Network.updateEthDevInfo(ethernetManager, devInfo);
            }
        }
    }

//    private void handleEvent(Context context, Intent intent) {
//        String action = intent.getAction();
//        if (EthernetManager.ETHERNET_STATE_CHANGED_ACTION.equals(action)) {
//            final EthernetDevInfo devinfo = intent.getParcelableExtra(EthernetManager.EXTRA_ETHERNET_INFO);
//            final int event = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE,
//                    EthernetManager.EVENT_NEWDEV);
//            if (event == EthernetManager.EVENT_NEWDEV || event == EthernetManager.EVENT_DEVREM) {
//                System.out.println("NetworkService ETHERNET_STATE_CHANGED_ACTION : " + devinfo.toString());
//            }
//        } else if (EthernetManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//            final NetworkInfo networkInfo = intent.getParcelableExtra(EthernetManager.EXTRA_NETWORK_INFO);
//            final LinkProperties linkProperties = intent.getParcelableExtra(EthernetManager.EXTRA_LINK_PROPERTIES);
//            final int event = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE,
//                    EthernetManager.EVENT_CONFIGURATION_SUCCEEDED);
//            switch (event) {
//                case EthernetManager.EVENT_CONFIGURATION_SUCCEEDED:
//                    for (LinkAddress l : linkProperties.getLinkAddresses()) {
//                        //ip setting
//                        System.out.println("NetworkService EVENT_CONFIGURATION_SUCCEEDED : " + networkInfo.getTypeName() + ":" + l.getAddress().getHostAddress());
//                    }
//                    break;
//                case EthernetManager.EVENT_CONFIGURATION_FAILED:
//                    System.out.println("NetworkService EVENT_CONFIGURATION_FAILED : " + networkInfo.getTypeName());
//                    break;
//                case EthernetManager.EVENT_DISCONNECTED:
//                    System.out.println("NetworkService EVENT_DISCONNECTED : " + networkInfo.getTypeName());
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
}
