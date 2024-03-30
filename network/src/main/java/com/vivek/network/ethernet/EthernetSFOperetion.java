package com.vivek.network.ethernet;

import androidx.fragment.app.Fragment;;
import android.net.ethernet.EthernetDevInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

public class EthernetSFOperetion implements EthernetInterface {
    private EthernetPresenter mPresenter;
    private EthernetConfigureDialogFragment mConfigureDialogFragment;
    private Fragment mFragment;
    private int mDialogStyleDrawable;

    private CheckBox mCHkEnable;
    private TextView mTxtConnectContent;
    private TextView mTxtIpAddressContent;
    private TextView mTxtMacContent;
    private LinearLayout mLLayoutConfigure;
    private LinearLayout mLLayoutBody;

    public EthernetSFOperetion(Fragment fragment) {
        mFragment = fragment;
        mPresenter = new EthernetPresenter(mFragment.getActivity(), this);
    }

    public void onResume() {
        mPresenter.registerReceiver();
    }

    public void onPause() {
        mPresenter.unregisterReceiver();
    }

    public void onActivityCreated() {
        find();
        mPresenter.init();
        listener();
    }

    public EthernetPresenter getEthernetPresenter() {
        return mPresenter;
    }

    public void toggleToEthernet() {
        if (mCHkEnable.isEnabled()) {
            mCHkEnable.toggle();
        }
    }

    public void intentToConfigure() {
        mLLayoutConfigure.performClick();
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.network_fragment_network_ethernet, null);
    }

    private void find() {
        mCHkEnable = (CheckBox) mFragment.getView().findViewById(R.id.network_eth_enable_toggle);

        mTxtConnectContent = (TextView) mFragment.getView().findViewById(R.id.network_eth_connect_content);
        mTxtIpAddressContent = (TextView) mFragment.getView().findViewById(R.id.network_eth_ip_content);
        mTxtMacContent = (TextView) mFragment.getView().findViewById(R.id.network_eth_mac_content);
        mLLayoutConfigure = (LinearLayout) mFragment.getView().findViewById(R.id.network_eth_configure_llayout);

        mLLayoutBody = (LinearLayout) mFragment.getView().findViewById(R.id.network_eth_body_layout);
    }

    private void listener() {
        mCHkEnable.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mLLayoutConfigure.setOnClickListener(mOnClickListener);
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.network_eth_enable_toggle) {
                changeCHKEnableStatus(isChecked);
                mPresenter.setEthEnabled(isChecked);
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_confirm) {
                mConfigureDialogFragment.set();
                mConfigureDialogFragment.dismiss();
                mPresenter.update();
            } else if (v.getId() == R.id.btn_cancel) {
                mConfigureDialogFragment.dismiss();
            } else if (v.getId() == R.id.network_eth_configure_llayout) {
                intentToConfigureDialogFragment();
            }
        }
    };

    protected void intentToConfigureDialogFragment() {
        mConfigureDialogFragment = new EthernetConfigureDialogFragment();
        mConfigureDialogFragment.init(mDialogStyleDrawable, mOnClickListener, mPresenter);
        mFragment.getChildFragmentManager().beginTransaction()
                .add(mConfigureDialogFragment, mConfigureDialogFragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }

    public void setDialogStyleDrawable(int resId) {
        mDialogStyleDrawable = resId;
    }

    @Override
    public void enable(boolean enabled) {
        mCHkEnable.setChecked(enabled);
        changeCHKEnableStatus(enabled);
    }

    private void changeCHKEnableStatus(boolean enabled) {
        if (enabled) {
            mTxtConnectContent.setVisibility(View.VISIBLE);
        } else {
            mTxtConnectContent.setText(mFragment.getString(R.string.eth_unconnect_content));
            mTxtConnectContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void connected(boolean connected) {
        if (connected) {
            mTxtConnectContent.setText(mFragment.getString(R.string.eth_connect_content));
        } else {
            mTxtConnectContent.setText(mFragment.getString(R.string.eth_unconnect_content));
        }
    }

    @Override
    public void onEnableSetInPreExecute(boolean enabled) {
        mCHkEnable.setEnabled(false);
        mLLayoutConfigure.setEnabled(false);
    }

    @Override
    public void onEnableSetInProgressUpdate() {

    }

    @Override
    public void onEnableSetInPostExecute(boolean enabled) {
        mCHkEnable.setEnabled(true);
        mLLayoutConfigure.setEnabled(true);
    }

    @Override
    public void onUpdateSetInInPreExecute() {
        mCHkEnable.setEnabled(false);
        mLLayoutConfigure.setEnabled(false);
    }

    @Override
    public void onUpdateSetInPostExecute() {
        mCHkEnable.setEnabled(true);
        mLLayoutConfigure.setEnabled(true);
    }

    @Override
    public void updateEthInfo(EthernetDevInfo devInfo, List<EthernetDevInfo> devInfoList) {
        if (devInfo != null && devInfo.getHwaddr() != null) {
            mTxtMacContent.setText(devInfo.getHwaddr().toUpperCase());
        }
    }

    @Override
    public void updateDeviceNotFound() {
        mTxtIpAddressContent.setText(mFragment.getString(R.string.eth_ip_default_text));
        mTxtMacContent.setText(mFragment.getString(R.string.eth_mac_default_text));
    }

    @Override
    public void onConfigurationSucceeded(String connectedIpAddress, boolean dhcp) {
        String ipAddressMode = mFragment.getString(R.string.eth_ip_content_mode)
                + (dhcp ? mFragment.getString(R.string.eth_ip_content_mode_dhcp)
                : mFragment.getString(R.string.eth_ip_content_mode_manual));
        mTxtIpAddressContent.setText(connectedIpAddress + ipAddressMode);
    }

    @Override
    public void onConfigurationConnected(EthernetDevInfo devInfo) {
        mTxtConnectContent.setText(mFragment.getString(R.string.eth_connect_content) + devInfo.getIfName());
    }

    @Override
    public void onConfigurationFailured() {
        mTxtIpAddressContent.setText(mFragment.getString(R.string.eth_ip_default_text));
    }

    @Override
    public void onDisconnected() {
        mTxtConnectContent.setText(mFragment.getString(R.string.eth_unconnect_content));
    }
}
