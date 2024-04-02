package com.vivek.network.wifi;

public interface WifiInterface {

    void onWifiEnabledStateChanged(boolean enabled);

    void onWifiSwitchChecked(boolean checked);

    /**
     * Need to refresh the list manually
     */
    void onRefreshWifiDevices();

    void onWifiDeviceOperation(AccessPoint accessPoint);

    void onWifiSummaryChanged(int type);

}
