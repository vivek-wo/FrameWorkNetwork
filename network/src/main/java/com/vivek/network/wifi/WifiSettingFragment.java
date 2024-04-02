package com.vivek.network.wifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class WifiSettingFragment extends Fragment {
    private WifiSFOperation mWifiSFOperation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiSFOperation = new WifiSFOperation(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mWifiSFOperation.onCreateView(inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiSFOperation.onActivityCreated();
    }


    @Override
    public void onResume() {
        super.onResume();
        mWifiSFOperation.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWifiSFOperation.onPause();
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
