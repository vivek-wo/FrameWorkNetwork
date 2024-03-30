package com.vivek.network.wifi;

import androidx.fragment.app.Fragment;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class WifiCDFOperation {
    private Fragment mFragment;
    private AccessPoint mAccessPoint;
    private View.OnClickListener mOnClickListener;

    // e.g. AccessPoint.SECURITY_NONE
    private int mAccessPointSecurity;

    /* These values come from "wifi_peap_phase2_entries" resource array */
    public static final int WIFI_PEAP_PHASE2_NONE = 0;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2 = 1;
    public static final int WIFI_PEAP_PHASE2_GTC = 2;

    private final Handler mTextViewChangedHandler;

    private int mDialogStyleDrawable;

    private LinearLayout mLayoutAddFields;
    private LinearLayout mLayoutSecurityFields;
    private LinearLayout mLayoutAdvancedFields;
    private LinearLayout mLayoutStaticFields;

    private TextView mTxtDialogTitle;
    public EditText mEdtSSID;
    public Spinner mSpinnerSecurity;
    public EditText mEdtPassword;
    public CheckBox mChkPassword;
    private CheckBox mChkModeStatic;
    private EditText mEdtIp;
    private EditText mEdtNetMask;
    private EditText mEdtGateWay;
    private EditText mEdtDnsAddr;
    private EditText mEdtDnsAddr2;

    public Button mBtnConfirm;
    public Button mBtnCancelSaved;
    public Button mBtnCancel;

    public WifiCDFOperation(Fragment fragment) {
        mFragment = fragment;
        mTextViewChangedHandler = new Handler();
    }

    public void init(AccessPoint accessPoint, View.OnClickListener onClickListener) {
        mAccessPoint = accessPoint;
        mOnClickListener = onClickListener;
    }

    public void init(int resId, AccessPoint accessPoint, View.OnClickListener onClickListener) {
        mDialogStyleDrawable = resId;
        mAccessPoint = accessPoint;
        mOnClickListener = onClickListener;
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.network_dialog_network_wifi_configure, null);
    }

    public void onActivityCreated() {
        find();
        listener();
        init();
    }

    private void find() {
        mLayoutAddFields = (LinearLayout) mFragment.getView().findViewById(R.id.wifi_add_layout);
        mLayoutSecurityFields = (LinearLayout) mFragment.getView().findViewById(R.id.wifi_security_fields_layout);
        mLayoutAdvancedFields = (LinearLayout) mFragment.getView().findViewById(R.id.wifi_advanced_toggle_layout);
        mLayoutStaticFields = (LinearLayout) mFragment.getView().findViewById(R.id.wifi_static_ip_layout);

        mTxtDialogTitle = (TextView) mFragment.getView().findViewById(R.id.network_dialog_title_txt);
        mEdtSSID = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_ssid);
        mSpinnerSecurity = (Spinner) mFragment.getView().findViewById(R.id.wifi_spinner_security);
        mEdtPassword = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_password);

        mChkPassword = (CheckBox) mFragment.getView().findViewById(R.id.wifi_chk_show_password);
        mChkModeStatic = (CheckBox) mFragment.getView().findViewById(R.id.wifi_chk_advanced_togglebox);

        mEdtIp = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_ip);
        mEdtNetMask = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_netmask);
        mEdtDnsAddr = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_dns1);
        mEdtGateWay = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_gateway);
        mEdtDnsAddr2 = (EditText) mFragment.getView().findViewById(R.id.wifi_edit_dns2);

        mBtnConfirm = (Button) mFragment.getView().findViewById(R.id.btn_confirm);
        mBtnCancelSaved = (Button) mFragment.getView().findViewById(R.id.btn_cancel_save);
        mBtnCancel = (Button) mFragment.getView().findViewById(R.id.btn_cancel);

        if (mDialogStyleDrawable != 0) {
            mBtnConfirm.setBackgroundResource(mDialogStyleDrawable);
            mBtnCancelSaved.setBackgroundResource(mDialogStyleDrawable);
            mBtnCancel.setBackgroundResource(mDialogStyleDrawable);
        }
    }

    private void listener() {
        mChkPassword.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mChkModeStatic.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mEdtPassword.addTextChangedListener(mTextWatcher);

        mBtnConfirm.setOnClickListener(mOnClickListener);
        mBtnCancel.setOnClickListener(mOnClickListener);
        mBtnCancelSaved.setOnClickListener(mOnClickListener);
    }

    private void init() {
        mAccessPointSecurity = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE :
                mAccessPoint.security;

        if (mAccessPoint == null) {
            //add
            onShowAddFields();
        } else {
            //设置SSID
//            getDialog().setTitle(mAccessPoint.ssid);
            mTxtDialogTitle.setText(mAccessPoint.ssid);
            NetworkInfo.DetailedState state = mAccessPoint.getState();
            int level = mAccessPoint.getLevel();
            WifiInfo info = mAccessPoint.getInfo();
            if (mAccessPoint.networkId != -1) {
                //connected saved
                if (state == null && level != -1) {
                    onShowHadSavedFields();
                } else {
                    onShowConnectedFields();
                }
            } else {
                //connect
                onShowConnectFields();
            }
        }

        if (mBtnConfirm.getVisibility() == View.VISIBLE) {
            enableSubmitIfAppropriate();
        }
    }

    public void onShowAddFields() {
        mLayoutAddFields.setVisibility(View.VISIBLE);
        mLayoutAdvancedFields.setVisibility(View.VISIBLE);
        mBtnConfirm.setText(mFragment.getString(R.string.btn_save));
    }

    public void onShowConnectFields() {
        mLayoutSecurityFields.setVisibility(View.VISIBLE);
    }

    public void onShowConnectedFields() {
        mBtnCancelSaved.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
    }

    public void onShowHadSavedFields() {
        mBtnCancelSaved.setVisibility(View.VISIBLE);
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.wifi_chk_show_password) {
                updatePasswordVisibility(isChecked);
            } else if (buttonView.getId() == R.id.wifi_chk_advanced_togglebox) {

            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mTextViewChangedHandler.post(new Runnable() {
                @Override
                public void run() {
                    enableSubmitIfAppropriate();
                }
            });
        }
    };

    /* show submit button if password, ip and proxy settings are valid */
    protected void enableSubmitIfAppropriate() {
        if (mBtnConfirm.getVisibility() == View.GONE) {
            return;
        }
        boolean enabled = false;
        boolean passwordInvalid = false;

        if ((mLayoutSecurityFields.getVisibility() == View.VISIBLE) &&
                ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mEdtPassword.length() == 0) ||
                        (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mEdtPassword.length() < 8))) {
            passwordInvalid = true;
        }

        enabled = !(((mLayoutAddFields.getVisibility() == View.VISIBLE) && mEdtSSID.length() == 0) ||
                ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
                        passwordInvalid));
        mBtnConfirm.setEnabled(enabled);
    }

    /**
     * Make the characters of the password visible if show_password is checked.
     */
    private void updatePasswordVisibility(boolean checked) {
        int pos = mEdtPassword.getSelectionEnd();
        mEdtPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (checked ?
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                        InputType.TYPE_TEXT_VARIATION_PASSWORD));
        if (pos >= 0) {
            mEdtPassword.setSelection(pos);
        }
    }

    public WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != -1) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mEdtSSID.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == -1) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (mEdtPassword.length() != 0) {
                    int length = mEdtPassword.length();
                    String password = mEdtPassword.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (mEdtPassword.length() != 0) {
                    String password = mEdtPassword.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
//                config.enterpriseConfig = new WifiEnterpriseConfig();
//                int eapMethod = mEapMethodSpinner.getSelectedItemPosition();
//                int phase2Method = mPhase2Spinner.getSelectedItemPosition();
//                config.enterpriseConfig.setEapMethod(eapMethod);
//                switch (eapMethod) {
//                    case WifiEnterpriseConfig.Eap.PEAP:
//                        // PEAP supports limited phase2 values
//                        // Map the index from the PHASE2_PEAP_ADAPTER to the one used
//                        // by the API which has the full list of PEAP methods.
//                        switch (phase2Method) {
//                            case WIFI_PEAP_PHASE2_NONE:
//                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
//                                break;
//                            case WIFI_PEAP_PHASE2_MSCHAPV2:
//                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
//                                break;
//                            case WIFI_PEAP_PHASE2_GTC:
//                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
//                                break;
//                            default:
//                                break;
//                        }
//                        break;
//                    default:
//                        // The default index from PHASE2_FULL_ADAPTER maps to the API
//                        config.enterpriseConfig.setPhase2Method(phase2Method);
//                        break;
//                }
//                String caCert = (String) mEapCaCertSpinner.getSelectedItem();
//                if (caCert.equals(unspecifiedCert)) caCert = "";
//                config.enterpriseConfig.setCaCertificateAlias(caCert);
//                String clientCert = (String) mEapUserCertSpinner.getSelectedItem();
//                if (clientCert.equals(unspecifiedCert)) clientCert = "";
//                config.enterpriseConfig.setClientCertificateAlias(clientCert);
//                config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
//                config.enterpriseConfig.setAnonymousIdentity(
//                        mEapAnonymousView.getText().toString());
//
//                if (mPasswordView.isShown()) {
//                    // For security reasons, a previous password is not displayed to user.
//                    // Update only if it has been changed.
//                    if (mPasswordView.length() > 0) {
//                        config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
//                    }
//                } else {
//                    // clear password
//                    config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
//                }
                break;
            default:
                return null;
        }

//        config.proxymIpAssignmentnkProperties(mLinkProperties);

        return config;
    }

}
