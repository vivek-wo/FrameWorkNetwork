package com.vivek.network.wifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class WifiSettingFragment extends Fragment {
    private WifiSFOperetion mWifiSFOperetion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiSFOperetion = new WifiSFOperetion(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mWifiSFOperetion.onCreateView(inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiSFOperetion.onActivityCreated();
    }


    @Override
    public void onResume() {
        super.onResume();
        mWifiSFOperetion.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWifiSFOperetion.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
