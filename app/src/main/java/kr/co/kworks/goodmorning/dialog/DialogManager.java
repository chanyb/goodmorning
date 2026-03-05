package kr.co.kworks.goodmorning.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;


import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.utils.GlobalApplication;

public class DialogManager {
    private static DialogManager instance;
    private DialogManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }
    private CustomConfirmDialog customConfirmDialog;
    private CustomAlertDialog customAlertDialog;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;


    public static int ICON_NULL = -1;

    public static DialogManager getInstance() {
        if(instance == null) instance = new DialogManager();
        return instance;
    }


    public void showAlertDialog(Context context, String msg, View.OnClickListener clickListener, int header_icon_id, String header_title_string) {
        if (GlobalApplication.getContext().isBackground()) return;
        if (customAlertDialog != null && customAlertDialog.isShowing()) return;

        Drawable icon = null;
        if (header_icon_id != ICON_NULL) icon = ContextCompat.getDrawable(GlobalApplication.getContext(), header_icon_id);
        customAlertDialog = new CustomAlertDialog(context, msg, GlobalApplication.getContext().getString(R.string.str_confirm), clickListener, icon, header_title_string);

        mHandler.post(() -> {
            try{
                customAlertDialog.show();
            } catch(Exception e) {
                Log.e("this", "showAlertDialog - Exception:", e);
            }
        });
    }

    public void dismissAlertDialog(Activity activity) {
        if(activity == null) return ;
        try {
            if (GlobalApplication.getContext().isForeground() && customAlertDialog != null) if (customAlertDialog.isShowing()) activity.runOnUiThread(() -> customAlertDialog.dismiss());
        } catch (Exception e) {
            customAlertDialog = null;
            Log.e("this", "dismissDialog", e);
        }
    }

    public void showConfirmDialog(Context context, String msg, View.OnClickListener cancelListener, View.OnClickListener confirmListener, int header_icon_id, String header_title_string) {
        if (GlobalApplication.getContext().isBackground()) return;
        if (customConfirmDialog != null && customConfirmDialog.isShowing()) {
            try {
                customConfirmDialog.dismiss();
            } catch (Exception e) {
                Log.e("this", "error", e);
            }
            return;
        }

        Drawable icon = null;
        if (header_icon_id != ICON_NULL) icon = ContextCompat.getDrawable(GlobalApplication.getContext(), header_icon_id);
        customConfirmDialog = new CustomConfirmDialog(context, msg, GlobalApplication.getContext().getString(R.string.str_cancel),GlobalApplication.getContext().getString(R.string.str_confirm), cancelListener, confirmListener, icon, header_title_string);

        customConfirmDialog.show();
    }

    public void showConfirmDialog(Context context, String msg, String cancelName, String confirmName, View.OnClickListener cancelListener, View.OnClickListener confirmListener, int header_icon_id, String header_title_string) {
        if (GlobalApplication.getContext().isBackground()) return;
        if (customConfirmDialog != null && customConfirmDialog.isShowing()) {
            try {
                customConfirmDialog.dismiss();
            } catch (Exception e) {
                Log.e("this", "error", e);
            }
            return;
        }

        Drawable icon = null;
        if (header_icon_id != ICON_NULL) icon = ContextCompat.getDrawable(GlobalApplication.getContext(), header_icon_id);
        customConfirmDialog = new CustomConfirmDialog(context, msg, cancelName, confirmName, cancelListener, confirmListener, icon, header_title_string);

        customConfirmDialog.show();
    }

    public void dismissConfirmDialog(Activity activity) {
        if (activity == null) return;

        try{
            if (GlobalApplication.getContext().isForeground() && customConfirmDialog != null) if (customConfirmDialog.isShowing()) activity.runOnUiThread(() -> customConfirmDialog.dismiss());
        } catch (Exception e) {
            customConfirmDialog = null;
            Log.e("this", "dismissDialog", e);
        }
    }

    public void showProgressDialog(Context mContext, String message) {
        if (GlobalApplication.getContext().isBackground()) return;
        try {
            if (mContext != null) {
                if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        } catch(Exception e) {
            Log.e("this", "showProgressDialog", e);
        }

    }

    public void dismissProgressDialog(Activity activity) {
        if(activity == null) return;
        try {
            if(GlobalApplication.getContext().isForeground() && mProgressDialog != null && mProgressDialog.isShowing()) activity.runOnUiThread(() -> mProgressDialog.dismiss());
        } catch (Exception e) {
            mProgressDialog = null;
            Log.e("this", "dismissDialog", e);
        }
    }

    public void dismissAllDialog(Activity _context) {
        dismissProgressDialog(_context);
        dismissAlertDialog(_context);
        dismissConfirmDialog(_context);
    }
}
