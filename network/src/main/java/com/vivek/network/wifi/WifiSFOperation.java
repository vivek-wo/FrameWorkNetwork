package com.vivek.network.wifi;

import androidx.fragment.app.Fragment;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.vivek.frameworknetwork.R;

public class WifiSFOperation implements WifiInterface {
    protected WifiPresenter mWifiPresenter;
    private boolean mStateMachineEvent;
    protected WifiDevicesAdapter mAdapter;
    private WifiConfigureDialogFragment mConfigureDialogFragment;
    private Fragment mFragment;
    private int mDialogStyleDrawable;

    protected Switch mSwitch;
    private ListView mListView;

    public WifiSFOperation(Fragment fragment) {
        mFragment = fragment;
        mWifiPresenter = new WifiPresenter(mFragment.getActivity(), this);
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.network_fragment_network_wifi, null);
    }

    public View onCreateItemView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.network_item_style_wifi_devices, null);
    }

    public void onActivityCreated() {
        find();
        mWifiPresenter.init();
        listener();
    }

    private void find() {
        mSwitch = (Switch) mFragment.getView().findViewById(R.id.network_wifi_enable_toggle);
        mListView = (ListView) mFragment.getView().findViewById(R.id.network_wifi_devices_list);
    }

    private void listener() {
        mSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public void toggleToEnableWifi() {
        if (mSwitch.isEnabled()) {
            mSwitch.toggle();
        }
    }

    public void requestFocus() {
        mListView.setFocusable(true);
        mListView.setFocusableInTouchMode(true);
        mListView.requestFocus();
    }

    public void onResume() {
        mWifiPresenter.registerReceiver();
        mWifiPresenter.updateAccessPoints();
    }

    public void onPause() {
        mWifiPresenter.unregisterReceiver();
        mWifiPresenter.pauseScanner();
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mStateMachineEvent) {
                return;
            }
            //operation
            mWifiPresenter.onWifiEnabledChecked(isChecked);
        }
    };

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mWifiPresenter.onItemClick(parent, view, position, id);
        }
    };

    protected View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_confirm) {
                onClickConfirm();
            } else if (v.getId() == R.id.btn_cancel) {
                onClickCancel();
            } else if (v.getId() == R.id.btn_cancel_save) {
                onClickCancelSave();
            }
        }
    };

    protected void onClickConfirm() {
        mWifiPresenter.submit(mConfigureDialogFragment.getConfig());
        mConfigureDialogFragment.dismiss();
    }

    protected void onClickCancel() {
        mConfigureDialogFragment.dismiss();
    }

    protected void onClickCancelSave() {
        mWifiPresenter.forget();
        mConfigureDialogFragment.dismiss();
    }

    @Override
    public void onWifiEnabledStateChanged(boolean enabled) {
        mSwitch.setEnabled(enabled);
    }

    @Override
    public void onWifiSwitchChecked(boolean checked) {
        //changed wifi enable switch state
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    @Override
    public void onRefreshWifiDevices() {
        if (mAdapter == null) {
            mAdapter = new WifiDevicesAdapter();
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
//        requestFocus();
    }

    @Override
    public void onWifiDeviceOperation(AccessPoint accessPoint) {
        mConfigureDialogFragment = new WifiConfigureDialogFragment();
        mConfigureDialogFragment.init(mDialogStyleDrawable, accessPoint, mOnClickListener);
        mFragment.getChildFragmentManager().beginTransaction()
                .add(mConfigureDialogFragment, mConfigureDialogFragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }

    public void setDialogStyleDrawable(int resId) {
        mDialogStyleDrawable = resId;
    }

    @Override
    public void onWifiSummaryChanged(int type) {

    }

    class WifiDevicesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mWifiPresenter.getAdapterCount();
        }

        @Override
        public Object getItem(int position) {
            return mWifiPresenter.getAdapterItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = onCreateItemView(mFragment.getActivity().getLayoutInflater());
                holder = new ViewHolder();
                holder.mTxtTitle = (TextView) convertView.findViewById(R.id.item_wifi_devices_title);
                holder.mTxtSummary = (TextView) convertView.findViewById(R.id.item_wifi_devices_summary);
                holder.mImgSignal = (ImageView) convertView.findViewById(R.id.item_wifi_devices_iamge);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AccessPoint accessPoint = mWifiPresenter.getAdapterItem(position);
            accessPoint.onBindTitleTextView(holder.mTxtTitle);
            accessPoint.onBindSummaryTextView(holder.mTxtSummary);
            accessPoint.onBindImageView(holder.mImgSignal);
            return convertView;
        }

        class ViewHolder {
            TextView mTxtTitle;
            TextView mTxtSummary;
            ImageView mImgSignal;
        }
    }
}
