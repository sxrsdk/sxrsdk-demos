package com.samsungxr.arpet.mainview;

import android.view.View;

import com.samsungxr.arpet.view.IView;

public interface ICleanView extends IView {
    void setOnCancelClickListener(View.OnClickListener listener);

    void setOnConfirmClickListener(View.OnClickListener listener);
}
