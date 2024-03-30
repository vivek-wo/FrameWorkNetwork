package com.vivek.network.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;


public class WifiConfigureDialogFragment extends DialogFragment {
    private WifiCDFOperation mWifiCDFOperation;

    public WifiConfigureDialogFragment() {
        mWifiCDFOperation = new WifiCDFOperation(this);
    }

    public void init(AccessPoint accessPoint, View.OnClickListener onClickListener) {
        mWifiCDFOperation.init(accessPoint, onClickListener);
    }

    public void init(int resId, AccessPoint accessPoint, View.OnClickListener onClickListener) {
        mWifiCDFOperation.init(resId, accessPoint, onClickListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.NetworkDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mWifiCDFOperation.onCreateView(inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiCDFOperation.onActivityCreated();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* package */ WifiConfiguration getConfig() {
        return mWifiCDFOperation.getConfig();
    }

}
