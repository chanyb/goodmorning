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


public class CustomAlertDialog extends Dialog {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.layout_alert_dialog);

        setLayout();
        if (mConfirm != null) {
            setConfirm(mConfirm);
        }

        if (mContent != null)
            setContent(mContent);

        if (icon != null) {
            header_icon.setImageDrawable(icon);
        }


        if (title != null) {
            header_title.setText(title);
        }

        if (title == null && icon == null) {
            // gone
            lo_icon.setVisibility(View.GONE);
        }


        setClickListener(mConfirmClickListener);
    }

    public CustomAlertDialog(Context context, String content, String right, View.OnClickListener confirmListener, Drawable headerIcon, String headerTitle) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContent = content;
        this.mConfirm = right;
        this.mConfirmClickListener = confirmListener;
        this.icon = headerIcon;
        this.title = headerTitle;
    }

    private void setContent(String content) {
        mContentView.setText(content);
    }

    private void setConfirm(String confirm) {
        mConfirmButton.setText(confirm);
    }

    private void setClickListener(View.OnClickListener mConfirmClickListener) {
        mConfirmButton.setOnClickListener(mConfirmClickListener);
    }

    /*
     * Layout
     */
    private ImageView header_icon;
    private Drawable icon;
    private TextView header_title;
    private String title;
    private TextView mContentView;
    private TextView mConfirmButton;
    private String mContent;
    private String mConfirm;
    private ConstraintLayout lo_icon;

    private View.OnClickListener mConfirmClickListener;

    /*
     * Layout
     */
    private void setLayout() {
        mContentView = (TextView) findViewById(R.id.txtmsg);
        mConfirmButton = (TextView) findViewById(R.id.txt_ok);
        header_icon = (ImageView) findViewById(R.id.header_icon);
        header_title = (TextView) findViewById(R.id.header_title);
        lo_icon = findViewById(R.id.lo_icon);
    }
}









