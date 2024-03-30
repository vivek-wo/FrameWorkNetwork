package com.vivek.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiPresenter {
    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    private Context mContext;
    private WifiInterface mWifiInterface;

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;

    private WifiManager mWifiManager;

    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;
    private List<AccessPoint> mAccessPointLists = new ArrayList<AccessPoint>();

    private NetworkInfo.DetailedState mLastState;
    private WifiInfo mLastInfo;

    private final AtomicBoolean mConnected = new AtomicBoolean(false);

    public WifiPresenter(Context context, WifiInterface wifiInterface) {
        mContext = context;
        mWifiInterface = wifiInterface;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");//WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");//WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };

        mScanner = new Scanner();
    }

    public void registerReceiver() {
        mContext.registerReceiver(mReceiver, mFilter);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    //onActivityCreate
    public void init() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    }

    public void onWifiEnabledChecked(boolean isChecked) {
        // Show toast message if Wi-Fi is not allowed in airplane mode --hide
        // Disable tethering if enabling Wifi
        if (mWifiInterface != null) {
            mWifiInterface.onWifiEnabledStateChanged(false);
        }
        if (!mWifiManager.setWifiEnabled(isChecked)) {
            // Error
            if (mWifiInterface != null) {
                mWifiInterface.onWifiEnabledStateChanged(true);
            }
            Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
        }
    }

    //Wifi Enable Changed
    private void handleWifiStateChanged(int state) {
        System.out.println("-------------handleWifiStateChanged " + state);
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
//                mSwitch.setEnabled(false);
                if (mWifiInterface != null) {
                    mWifiInterface.onWifiEnabledStateChanged(false);
                }
                break;
            case WifiManager.WIFI_STATE_ENABLED:
//                setSwitchChecked(true);
//                mSwitch.setEnabled(true);
                if (mWifiInterface != null) {
                    mWifiInterface.onWifiSwitchChecked(true);
                    mWifiInterface.onWifiEnabledStateChanged(true);
                }
                break;
            case WifiManager.WIFI_STATE_DISABLING:
//                mSwitch.setEnabled(false);
                if (mWifiInterface != null) {
                    mWifiInterface.onWifiEnabledStateChanged(false);
                }
                break;
            case WifiManager.WIFI_STATE_DISABLED:
//                setSwitchChecked(false);
//                mSwitch.setEnabled(true);
                if (mWifiInterface != null) {
                    mWifiInterface.onWifiSwitchChecked(false);
                    mWifiInterface.onWifiEnabledStateChanged(true);
                }
                break;
            default:
//                setSwitchChecked(false);
//                mSwitch.setEnabled(true);
                if (mWifiInterface != null) {
                    mWifiInterface.onWifiSwitchChecked(false);
                    mWifiInterface.onWifiEnabledStateChanged(true);

                }
                break;
        }
    }

    private void updateWifiState(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(WifiManager.WIFI_STATE_ENABLING);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    /**
     * Shows the latest access points available with supplimental information like
     * the strength of network and the security for it.
     */
    public void updateAccessPoints() {
        // Safeguard from some delayed event handling
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                final Collection<AccessPoint> accessPoints = constructAccessPoints();
//                getPreferenceScreen().removeAll();
                mAccessPointLists.clear();
                mAccessPointLists.addAll(accessPoints);
                if (mWifiInterface != null) {
                    mWifiInterface.onRefreshWifiDevices();
                }
//                if (accessPoints.size() == 0) {
//                    addMessagePreference(R.string.wifi_empty_list_wifi_on);
//                }
//                for (AccessPoint accessPoint : accessPoints) {
//                    getPreferenceScreen().addPreference(accessPoint);
//                }

                break;

            case WifiManager.WIFI_STATE_ENABLING:
//                getPreferenceScreen().removeAll();
                mAccessPointLists.clear();
                if (mWifiInterface != null) {
                    mWifiInterface.onRefreshWifiDevices();
                }
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(WifiManager.WIFI_STATE_DISABLING);
                if (mWifiInterface != null) {
                    mWifiInterface.onRefreshWifiDevices();
                }
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                break;
        }
    }

    private void setOffMessage() {
//        if (mEmptyView != null) {
//            mEmptyView.setText(R.string.wifi_empty_list_wifi_off);
//            if (Settings.Global.getInt(getActivity().getContentResolver(),
//                    Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1) {
//                mEmptyView.append("\n\n");
//                int resId;
//                if (Settings.Secure.isLocationProviderEnabled(getActivity().getContentResolver(),
//                        LocationManager.NETWORK_PROVIDER)) {
//                    resId = R.string.wifi_scan_notify_text_location_on;
//                } else {
//                    resId = R.string.wifi_scan_notify_text_location_off;
//                }
//                CharSequence charSeq = getText(resId);
//                mEmptyView.append(charSeq);
//            }
//        }
//        getPreferenceScreen().removeAll();
        if (mWifiInterface != null) {
            mWifiInterface.onRefreshWifiDevices();
        }
    }

    private void addMessagePreference(int type) {
//        if (mEmptyView != null) mEmptyView.setText(messageId);
//        getPreferenceScreen().removeAll();
        mAccessPointLists.clear();
        if (mWifiInterface != null) {
            mWifiInterface.onWifiSummaryChanged(type);
            mWifiInterface.onRefreshWifiDevices();
        }
    }

    /**
     * Returns sorted list of access points
     */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(mContext, config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(mContext, result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }

    /**
     * A restricted multimap for use in constructAccessPoints
     */
    private class Multimap<K, V> {
        private final HashMap<K, List<V>> store = new HashMap<K, List<V>>();

        /**
         * retrieve a non-null list of values with key K
         */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            //Wifi Eanble Changed
            handleWifiStateChanged(intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
                || "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)
                || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            //Ignore supplicant state changes when network is connected
            //we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            SupplicantState state = intent.getParcelableExtra(
                    WifiManager.EXTRA_NEW_STATE);
            if (!mConnected.get() && isHandshakeState(state)) {
//            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf(state));
            } else {
                // During a connect, we may have the supplicant
                // state change affect the detailed network state.
                // Make sure a lost connection is updated as well.
                updateConnectionState(null);
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
//            changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
//            if (mAutoFinishOnConnection && info.isConnected()) {
//                Activity activity = getActivity();
//                if (activity != null) {
//                    activity.setResult(Activity.RESULT_OK);
//                    activity.finish();
//                }
//                return;
//            }
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }


    /**
     * SupplicantState inner class ,is hide
     *
     * @param state
     * @return
     */
    private static boolean isHandshakeState(SupplicantState state) {
        switch (state) {
            case AUTHENTICATING:
            case ASSOCIATING:
            case ASSOCIATED:
            case FOUR_WAY_HANDSHAKE:
            case GROUP_HANDSHAKE:
                return true;
            case COMPLETED:
            case DISCONNECTED:
            case INTERFACE_DISABLED:
            case INACTIVE:
            case SCANNING:
            case DORMANT:
            case UNINITIALIZED:
            case INVALID:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    private void updateConnectionState(NetworkInfo.DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

//        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
//            // Maybe there's a WifiConfigPreference
//            Preference preference = getPreferenceScreen().getPreference(i);
//            if (preference instanceof AccessPoint) {
//                final AccessPoint accessPoint = (AccessPoint) preference;
//                accessPoint.update(mLastInfo, mLastState);
//            }
//        }
    }

    public void pauseScanner() {
        mScanner.pause();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
//                Activity activity = getActivity();
//                if (activity != null) {
//                    Toast.makeText(activity, R.string.wifi_fail_to_scan,
//                            Toast.LENGTH_LONG).show();
//                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }


    public void submit(WifiConfiguration config) {

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.networkId != -1) {
//                mWifiManager.connect(mSelectedAccessPoint.networkId,
//                        mConnectListener);
                try {
                    Class<?> cls = Class.forName(WifiManager.class.getName());
                    Method method = cls.getMethod("connect", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
                    method.invoke(mWifiManager, mSelectedAccessPoint.networkId, null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else if (config.networkId != -1) {
            if (mSelectedAccessPoint != null) {
//                mWifiManager.save(config, mSaveListener);
                try {
                    Class<?> cls = Class.forName(WifiManager.class.getName());
                    Method method = cls.getMethod("save", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
                    method.invoke(mWifiManager, config, null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
//            if (configController.isEdit()) {
//                mWifiManager.save(config, mSaveListener);
//            } else {
//                mWifiManager.connect(config, mConnectListener);
            //            }
            try {
                Class<?> cls = Class.forName(WifiManager.class.getName());
                Method method = cls.getMethod("connect", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
                method.invoke(mWifiManager, config, null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
    }

    public void forget() {
        if (mSelectedAccessPoint.networkId == -1) {
            // Should not happen, but a monkey seems to triger it
            return;
        }

//        mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
        try {
            Class<?> cls = Class.forName(WifiManager.class.getName());
            Method method = cls.getMethod("forget", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            method.invoke(mWifiManager, mSelectedAccessPoint.networkId, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
        // We need to rename/replace "Next" button in wifi setup context.
    }

//    /**
//     * Refreshes acccess points and ask Wifi module to scan networks again.
//     */
//    /* package */ void refreshAccessPoints() {
//        if (mWifiManager.isWifiEnabled()) {
//            mScanner.resume();
//        }
//
//        getPreferenceScreen().removeAll();
//    }

//    /**
//     * Called when "add network" button is pressed.
//     */
//    /* package */ void onAddNetworkPressed() {
//        // No exact access point is selected.
//        mSelectedAccessPoint = null;
//        showDialog(null, true);
//    }

//    /* package */ int getAccessPointsCount() {
//        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
//        if (wifiIsEnabled) {
//            return getPreferenceScreen().getPreferenceCount();
//        } else {
//            return 0;
//        }
//    }

    /**
     * Requests wifi module to pause wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void pauseWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.pause();
        }
    }

    /**
     * Requests wifi module to resume wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void resumeWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
    }

    public int getAdapterCount() {
        return mAccessPointLists.size();
    }

    public AccessPoint getAdapterItem(int position) {
        return mAccessPointLists.get(position);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mWifiManager.isWifiEnabled()) {
            return;
        }
        mSelectedAccessPoint = mAccessPointLists.get(position);
        /** Bypass dialog for unsecured, unsaved networks */
        if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE &&
                mSelectedAccessPoint.networkId == -1) {
            mSelectedAccessPoint.generateOpenNetworkConfig();
//            mWifiManager.connect(mSelectedAccessPoint.getConfig(), mConnectListener);
            try {
                Class<?> cls = Class.forName(WifiManager.class.getName());
                Method method = cls.getMethod("connect", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
                method.invoke(mWifiManager, mSelectedAccessPoint.getConfig(), null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
//            showDialog(mSelectedAccessPoint, false);
            if (mWifiInterface != null) {
                mWifiInterface.onWifiDeviceOperation(mSelectedAccessPoint);
            }
        }
    }

}
