package com.vivek.network.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class EthernetPresenter {
    private static final String IFNAME = "eth0";
    private Context mContext;
    private EthernetInterface mEthInterface;
    private ConnectivityManager mService;

    private EthernetManager mEthManager;
    private final IntentFilter mFilter;
    private final BroadcastReceiver mEthStateReceiver;
    private List<EthernetDevInfo> mListDevices = new ArrayList<EthernetDevInfo>();

    private EthernetDevInfo mEthConf;

    public EthernetPresenter(Context context, EthernetInterface ethernetInterface) {
        mContext = context;
        mEthInterface = ethernetInterface;

        mFilter = new IntentFilter();
        mFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
        mFilter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);

        mEthStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
    }

    public void registerReceiver() {
        mContext.registerReceiver(mEthStateReceiver, mFilter);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(mEthStateReceiver);
    }

    /**
     * onCreate or onActivityCreate
     */
    public void init() {
        /* first, we must get the Service. */
        mService = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mEthManager = EthernetManager.getInstance();

        /* Now, it should check the EthernetState. */
        if (mEthInterface != null) {
            mEthInterface.enable(mEthManager.getState() == EthernetManager.ETHERNET_STATE_ENABLED);
        }

        if (mService != null) {
            NetworkInfo networkinfo = mService.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (mEthInterface != null) {
                mEthInterface.connected(networkinfo.isConnected());
            }
        }

        /* Get the SaveConfig and update for Dialog. */
        EthernetDevInfo saveInfo = mEthManager.getSavedConfig();
        if (saveInfo != null && saveInfo.getIfName().endsWith(IFNAME)) {
            upDeviceList(saveInfo);
        } else {
            upDeviceList(null);
        }
    }

    /**
     * @param enable
     */
    public void setEthEnabled(final boolean enable) {

        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                //Disable button
                //mEthEnable.setSummary(R.string.eth_toggle_summary_opening);
                mEthInterface.onEnableSetInPreExecute(enable);
            }

            @Override
            protected Void doInBackground(Void... unused) {
                try {
                    if ((mEthManager.isConfigured() != true) && (enable == true)) {
                        publishProgress();
                    } else {
                        mEthManager.setEnabled(enable);
                    }
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                return null;
            }

            protected void onProgressUpdate(Void... unused) {
                if (mEthConf != null) {
                    mEthManager.updateDevInfo(mEthConf);
                    mEthManager.setEnabled(enable);
                }
            }

            protected void onPostExecute(Void unused) {
                //Enable button
                mEthInterface.onEnableSetInPostExecute(enable);
            }
        }.execute();
    }

    private void upDeviceList(EthernetDevInfo devIfo) {
        mListDevices = mEthManager.getDeviceNameList();
        if (mListDevices != null) {
            for (EthernetDevInfo deviceinfo : mListDevices) {
                if (deviceinfo.getIfName().equals(IFNAME)) {
                    if (devIfo != null) {
                        devIfo.setHwaddr(deviceinfo.getHwaddr());
                        mEthConf = devIfo;
                    } else {
                        mEthConf = deviceinfo;
                    }
                }
            }
            if (mEthInterface != null) {
                mEthInterface.updateEthInfo(mEthConf, mListDevices);
            }
        } else {
            //Mac tip "00:00:00:00:00:00"
            //ip tip "0.0.0.0"
            mEthConf = null;
            if (mEthInterface != null) {
                mEthInterface.updateDeviceNotFound();
            }
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (EthernetManager.ETHERNET_STATE_CHANGED_ACTION.equals(action)) {
            final EthernetDevInfo devinfo = intent.getParcelableExtra(EthernetManager.EXTRA_ETHERNET_INFO);
            final int event = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE,
                    EthernetManager.EVENT_NEWDEV);

            if (event == EthernetManager.EVENT_NEWDEV || event == EthernetManager.EVENT_DEVREM) {
                if (mEthConf != null) {
                    upDeviceList(mEthConf);
                } else {
                    upDeviceList(null);
                }
            }
        } else if (EthernetManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            final NetworkInfo networkInfo = intent.getParcelableExtra(EthernetManager.EXTRA_NETWORK_INFO);
            final LinkProperties linkProperties = intent.getParcelableExtra(EthernetManager.EXTRA_LINK_PROPERTIES);
            final int event = intent.getIntExtra(EthernetManager.EXTRA_ETHERNET_STATE,
                    EthernetManager.EVENT_CONFIGURATION_SUCCEEDED);
            switch (event) {
                case EthernetManager.EVENT_CONFIGURATION_SUCCEEDED:
                    for (LinkAddress l : linkProperties.getLinkAddresses()) {
                        //ip setting
                        if (mEthInterface != null) {
                            boolean dhcp = mEthConf.getConnectMode() == EthernetDevInfo.ETHERNET_CONN_MODE_DHCP;
                            mEthInterface.onConfigurationSucceeded(l.getAddress().getHostAddress(), dhcp);
                        }
                    }
                    EthernetDevInfo saveInfo = mEthManager.getSavedConfig();
                    if ((mEthConf != null) && (saveInfo != null)) {
                        upDeviceList(saveInfo);
                        //mEthEnable.setSummaryOn(context.getString(R.string.eth_dev_summaryon)
                        //+ mSelected.getConfigure().getIfName());
                        if (mEthInterface != null) {
                            mEthInterface.onConfigurationConnected(mEthConf);
                        }
                    }
                    break;
                case EthernetManager.EVENT_CONFIGURATION_FAILED:
                    //mIpPreference.setSummary("0.0.0.0");
                    if (mEthInterface != null) {
                        mEthInterface.onConfigurationFailured();
                    }
                    break;
                case EthernetManager.EVENT_DISCONNECTED:
                    //if (mEthEnable.isChecked())
                    //    mEthEnable.setSummaryOn(context.getString(R.string.eth_dev_summaryoff));
                    //else
                    //    mEthEnable.setSummaryOn(context.getString(R.string.eth_dev_summaryoff));
                    if (mEthInterface != null) {
                        mEthInterface.onDisconnected();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public EthernetDevInfo updateEthDevInfoModeManual(String ipAddress, String netMask,
                                                      String dnsAddr, String gateWay) {
        mEthConf.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
        mEthConf.setIpAddress(ipAddress);
        mEthConf.setNetMask(netMask);
        mEthConf.setDnsAddr(dnsAddr);
        mEthConf.setGateWay(gateWay);
        return mEthConf;
    }

    public EthernetDevInfo updateEthDevInfoModeDHCP() {
        mEthConf.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
        return mEthConf;
    }

    public EthernetDevInfo getEthernetDevInfo() {
        return mEthConf;
    }

    public void update() {
        new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                //Disable button
                if (mEthInterface != null) {
                    mEthInterface.onUpdateSetInInPreExecute();
                }
            }

            @Override
            protected Void doInBackground(Void... unused) {
                try {
                    mEthManager.updateDevInfo(mEthConf);
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                return null;
            }

            protected void onProgressUpdate(Void... unused) {
            }

            protected void onPostExecute(Void unused) {
                //Enable button
                if (mEthInterface != null) {
                    mEthInterface.onUpdateSetInPostExecute();
                }
            }
        }.execute();
    }

}
