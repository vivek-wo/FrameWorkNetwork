package com.vivek.network.ethernet;

import android.net.ethernet.EthernetDevInfo;

import java.util.List;

public interface EthernetInterface {

    void enable(boolean enabled);

    void connected(boolean connected);

    /**
     * Disable button
     *
     * @param enabled
     */
    void onEnableSetInPreExecute(boolean enabled);

    void onEnableSetInProgressUpdate();

    /**
     * Enable button
     *
     * @param enabled
     */
    void onEnableSetInPostExecute(boolean enabled);


    void onUpdateSetInInPreExecute();

    void onUpdateSetInPostExecute();

    void updateEthInfo(EthernetDevInfo devInfo, List<EthernetDevInfo> devInfoList);

    void updateDeviceNotFound();

    void onConfigurationSucceeded(String connectedIpAddress, boolean dhcp);

    void onConfigurationConnected(EthernetDevInfo devInfo);

    void onConfigurationFailured();

    void onDisconnected();

}
