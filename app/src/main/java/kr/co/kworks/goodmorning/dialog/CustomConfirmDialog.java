package kr.co.kworks.goodmorning.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import kr.co.kworks.goodmorning.R;

public class CustomConfirmDialog extends Dialog {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.layout_confirm_dialog);

        setLayout();
        if (mCancel != null) {
            setLeft(mCancel);
        }
        if (mConfirm != null) {
            setRight(mConfirm);
        }

        if (mContent != null)
            setContent(mContent);

        if (icon != null) {
            header_icon.setImageDrawable(icon);
        }

        if (title != null) {
            header_title.setText(title);
        }

        if (icon == null && title == null) {
            lo_icon.setVisibility(View.GONE);
        }


        setClickListener(mCancelClickListener, mConfirmClickListener);
    }

    public CustomConfirmDialog(Context context, String content, String cancel, String confirm,
                               View.OnClickListener cancelListener, View.OnClickListener confirmListener, Drawable icon, String title) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        if (content != null)
            this.mContent = content;
        this.mCancel = cancel;
        this.mConfirm = confirm;
        this.mCancelClickListener = cancelListener;
        this.mConfirmClickListener = confirmListener;
        this.icon = icon;
        this.title = title;
    }

    private void setContent(String content) {
        mContentView.setText(content);
    }

    private void setLeft(String left) {
        mCancelButton.setText(left);
    }

    private void setRight(String right) {
        mConfirmButton.setText(right);
    }

    private void setClickListener(View.OnClickListener mCancelClickListener, View.OnClickListener mConfirmClickListener) {
        mCancelButton.setOnClickListener(mCancelClickListener);
        mConfirmButton.setOnClickListener(mConfirmClickListener);
    }

    /*
     * Layout
     */
    private TextView mContentView;
    private TextView mCancelButton;
    private TextView mConfirmButton;
    private String mContent;
    private String mCancel;
    private String mConfirm;

    private ConstraintLayout lo_icon;
    private ImageView header_icon;
    private TextView header_title;
    private Drawable icon;
    private String title;

    private View.OnClickListener mCancelClickListener;
    private View.OnClickListener mConfirmClickListener;

    /*
     * Layout
     */
    private void setLayout() {
        mContentView = (TextView) findViewById(R.id.txtmsg);
        mCancelButton = (TextView) findViewById(R.id.txt_left);
        mConfirmButton = (TextView) findViewById(R.id.txt_right);
        lo_icon = findViewById(R.id.lo_icon);
        header_icon = findViewById(R.id.header_icon);
        header_title = findViewById(R.id.header_title);
    }
}









