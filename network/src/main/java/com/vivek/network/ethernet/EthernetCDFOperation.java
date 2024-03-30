package com.vivek.network.ethernet;

import androidx.fragment.app.Fragment;;
import android.net.ethernet.EthernetDevInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class EthernetCDFOperation {
    private View.OnClickListener mOnClickListener;
    private CompoundButton.OnCheckedChangeListener mExtraOnCheckedChangeListener;
    private EthernetPresenter mEthernetPresenter;
    private int mDialogStyleDrawable;
    private Fragment mFragment;

    public CheckBox mChkMode;
    public EditText mEdtIp;
    public EditText mEdtNetMask;
    public EditText mEdtDnsAddr;
    public EditText mEdtGateWay;
    public EditText mEdtMac;

    public Button mBtnConfirm;
    public Button mBtnCancel;

    public EthernetCDFOperation(Fragment fragment) {
        mFragment = fragment;
    }

    public void init(View.OnClickListener onClickListener, EthernetPresenter presenter) {
        mOnClickListener = onClickListener;
        mEthernetPresenter = presenter;
    }

    public void init(CompoundButton.OnCheckedChangeListener checkedChangeListener,
                     View.OnClickListener onClickListener, EthernetPresenter presenter) {
        mExtraOnCheckedChangeListener = checkedChangeListener;
        mOnClickListener = onClickListener;
        mEthernetPresenter = presenter;
    }

    public void init(int resId, View.OnClickListener onClickListener, EthernetPresenter presenter) {
        mDialogStyleDrawable = resId;
        mOnClickListener = onClickListener;
        mEthernetPresenter = presenter;
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.network_dialog_network_ethnet_configure, null);
    }

    public void onActivityCreated() {
        find();
        init();
        listener();
    }

    private void find() {
        mChkMode = (CheckBox) mFragment.getView().findViewById(R.id.network_eth_mode_toggle);


        mEdtIp = (EditText) mFragment.getView().findViewById(R.id.network_eth_ip_edit);
        mEdtNetMask = (EditText) mFragment.getView().findViewById(R.id.network_eth_netmask_edit);
        mEdtDnsAddr = (EditText) mFragment.getView().findViewById(R.id.network_eth_dns_edit);
        mEdtGateWay = (EditText) mFragment.getView().findViewById(R.id.network_eth_gateway_edit);
        mEdtMac = (EditText) mFragment.getView().findViewById(R.id.network_eth_mac_edit);

        mBtnConfirm = (Button) mFragment.getView().findViewById(R.id.btn_confirm);
        mBtnCancel = (Button) mFragment.getView().findViewById(R.id.btn_cancel);

        if (mDialogStyleDrawable != 0) {
            mBtnConfirm.setBackgroundResource(mDialogStyleDrawable);
            mBtnCancel.setBackgroundResource(mDialogStyleDrawable);
        }
    }

    private void listener() {
        if (mOnCheckedChangeListener != null) {
            mChkMode.setOnCheckedChangeListener(mOnCheckedChangeListener);
        }

        if (mOnClickListener != null) {
            mBtnConfirm.setOnClickListener(mOnClickListener);
            mBtnCancel.setOnClickListener(mOnClickListener);
        }

    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.network_eth_mode_toggle) {
                changeCHKModeStatus(isChecked);
                if (mExtraOnCheckedChangeListener != null) {
                    mExtraOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        }
    };

    private void init() {
        EthernetDevInfo devInfo = mEthernetPresenter.getEthernetDevInfo();
        if (devInfo != null) {
            mEdtIp.setText(devInfo.getIpAddress());
            mEdtNetMask.setText(devInfo.getNetMask());
            mEdtGateWay.setText(devInfo.getGateWay());
            mEdtDnsAddr.setText(devInfo.getDnsAddr());
            if (devInfo.getHwaddr() != null) {
                mEdtMac.setText(devInfo.getHwaddr().toUpperCase());
            }
            boolean dhcp = devInfo.getConnectMode() == EthernetDevInfo.ETHERNET_CONN_MODE_DHCP;
            mChkMode.setChecked(dhcp);
            changeCHKModeStatus(dhcp);
        }
    }

    private void changeCHKModeStatus(boolean dhcp) {
        if (dhcp) {
            mEdtIp.setEnabled(false);
            mEdtNetMask.setEnabled(false);
            mEdtGateWay.setEnabled(false);
            mEdtDnsAddr.setEnabled(false);
        } else {
            mEdtIp.setEnabled(true);
            mEdtNetMask.setEnabled(true);
            mEdtGateWay.setEnabled(true);
            mEdtDnsAddr.setEnabled(true);
        }
    }

    public boolean set() {
        boolean isDhcp = mChkMode.isChecked();
        if (isDhcp) {
            mEthernetPresenter.updateEthDevInfoModeDHCP();
        } else {
            String ipAddress = mEdtIp.getText().toString().trim();
            String netMask = mEdtNetMask.getText().toString().trim();
            String dnsAddr = mEdtDnsAddr.getText().toString().trim();
            String gateWay = mEdtGateWay.getText().toString().trim();
            mEthernetPresenter.updateEthDevInfoModeManual(ipAddress, netMask, dnsAddr, gateWay);
        }
        return true;
    }

    public void update() {
        mEthernetPresenter.update();
    }
}
