package com.vivek.network.ethernet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

public class EthernetConfigureDialogFragment extends DialogFragment {
    private EthernetCDFOperation mEthernetCDFOperation;

    public EthernetConfigureDialogFragment() {
        mEthernetCDFOperation = new EthernetCDFOperation(this);
    }

    public void init(View.OnClickListener onClickListener, EthernetPresenter presenter) {
        mEthernetCDFOperation.init(onClickListener, presenter);
    }

    public void init(int resId, View.OnClickListener onClickListener, EthernetPresenter presenter) {
        mEthernetCDFOperation.init(resId, onClickListener, presenter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.NetworkDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mEthernetCDFOperation.onCreateView(inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEthernetCDFOperation.onActivityCreated();
    }

    public boolean set() {
        return mEthernetCDFOperation.set();
    }
}
