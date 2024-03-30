package com.vivek.network.wifi;

public interface WifiInterface {

    void onWifiEnabledStateChanged(boolean enabled);

    void onWifiSwitchChecked(boolean checked);

    void onRefreshWifiDevices();

    void onWifiDeviceOperation(AccessPoint accessPoint);

    void onWifiSummaryChanged(int type);

}
