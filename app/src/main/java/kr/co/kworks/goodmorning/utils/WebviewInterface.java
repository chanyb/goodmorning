package kr.co.kworks.goodmorning.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.webkit.JavascriptInterface;

import androidx.lifecycle.ViewModelProvider;

import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import kr.co.kworks.goodmorning.viewmodel.WebviewCommunicationViewModel;


public class WebviewInterface {
    private ProgressDialog mProgressDialog;
    private Activity mActivity;
    private WebviewCommunicationViewModel webviewCommunicationViewModel;
    private GlobalViewModel global;
    private PreferenceHandler preferenceHandler;

    public WebviewInterface(Activity activity, ProgressDialog progressDialog) {
        mActivity = activity;
        mProgressDialog = progressDialog;
        webviewCommunicationViewModel = new ViewModelProvider(GlobalApplication.getContext()).get(WebviewCommunicationViewModel.class);
//        global = new ViewModelProvider(GlobalApplication.getContext()).get(GlobalViewModel.class);
        preferenceHandler = new PreferenceHandler(mActivity);
    }

    // 3. 앱 버전정보 가져오기
    @JavascriptInterface
    public void doGetAppVersion() {
        PackageInfo i = null;
        try {
            i = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        ((MainActivity)mActivity).getWebviewFragment().callFunction("setAppVersion", i.versionName);
    }

    // 4. SIM 번호 가져오기
    @JavascriptInterface
    public void doGetPhoneNumbers() {
    }

    // 5. Firebase Token 값 가져오기
    @JavascriptInterface
    public void doGetToken() {
    }
}