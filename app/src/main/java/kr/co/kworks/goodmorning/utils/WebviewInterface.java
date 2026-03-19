package kr.co.kworks.goodmorning.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.Locale;

import kr.co.kworks.goodmorning.viewmodel.Event;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import kr.co.kworks.goodmorning.viewmodel.WebviewCommunicationViewModel;


public class WebviewInterface {
    private final Activity mActivity;
    private final GlobalViewModel global;
    private final Database db;


    public WebviewInterface(Activity activity) {
        mActivity = activity;
        db = new Database();
        global = new ViewModelProvider((ViewModelStoreOwner) activity).get(GlobalViewModel.class);
    }

    // 1. Open Progress Dialog P
    @JavascriptInterface
    public void doOpenProgress(String msg) {
        global._progress.setValue(new Event<>("visible"));
        global._progressText1.setValue("");
        global._progressText2.setValue(msg);
    }

    // 2. Close Progress Dialog P
    @JavascriptInterface
    public void doCloseProgress() {
        global._progress.setValue(new Event<>("gone"));
    }

    // 3. 앱 버전정보 가져오기 P
    @JavascriptInterface
    public void doGetAppVersion(String callback) {
        Logger.getInstance().info("doGetAppVersion()");
        PackageInfo i = null;
        try {
            i = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            global._callFunction.setValue(new Event<>(
                String.format(Locale.KOREA, "%s(%s)", callback, i.versionName)
            ));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 4. 외부 브라우저 사용 P
    @JavascriptInterface
    public void doNoticeUrl(String url) {
        Intent siteLaunch = new Intent(Intent.ACTION_VIEW);
        siteLaunch.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        siteLaunch.setData(Uri.parse(url));
        mActivity.startActivity(siteLaunch);
    }

    // 5. toast 메시지 P
    @JavascriptInterface
    public void doOpenToast(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    // 6. Firebase Token 값 가져오기 P
    @JavascriptInterface
    public void doGetPushToken(String callback) {
        Logger.getInstance().info("doGetPushToken()");
        global._callFunction.setValue(new Event<>(
            String.format(Locale.KOREA, "%s(%s)", callback, db.getFcmToken())
        ));
    }

    // 7. 주소록 가져오기 (연락처에서 선택한 사람의 저장 명칭과 번호) P
    @JavascriptInterface
    public void doGetContact(String callback) {
        global._callbackForContact = callback;
        Logger.getInstance().info("doGetContact()");
        global._launchGetContact.setValue(new Event<>("launch"));
    }


}