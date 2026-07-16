package kr.co.kworks.goodmorning.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;

public class LiveUpdator {
    private String Version = "1.0";
    private String StoreVersion = "1.0";
    private boolean bPush = false;
    private GetFile gf = new GetFile();
    //Apk파일 다운로드 디렉토리
    private String sDownloadDir;
    //Apk파일이름
    private String sApkFileName;
    private ArrayList<String> ServerUpdateFile = new ArrayList<>();
    private String ServerVersion, sLocalVersion;
    private String ServerUpdateHistory = "";   //app
    private String ApkDown;
    Context mContext;
    private String please_wait;
    private boolean bStoreVer;
    private String please_download_wait;
    private Listener listener;
    private GlobalViewModel globalViewModel;
    private Handler mHandler;
    private ExecutorService executorService;

    public static final String APK_DOWNLOAD_PATH = "http://apk-link14.kworks.co.kr/apk/gm/";
    public static final int HTTP_CONNECTION_TIMEOUT = 3; //3초 > 10초
    public static final int LIVE_UPDATE_CHECK_TIMEOUT_SEC = 30;


    public LiveUpdator(Context mContext, GlobalViewModel globalViewModel) {
        this.mContext = mContext;
        this.globalViewModel = globalViewModel;
        ApkDown = "N";
        bStoreVer = true;
        please_wait = "잠시만기다려주세요. 통신중에있습니다.";
        please_download_wait = "다운로드 중입니다.\n잠시만 기다려 주시기 바랍니다.";
        ServerVersion = ""; sLocalVersion = ""; sApkFileName = "";
        mHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
    }

    public void downloadApk(String apkFileName, String savePath) {
        if(apkFileName == null || apkFileName.isEmpty()) return;

        File file = new File(sDownloadDir + File.separator + apkFileName);
        if (file.exists()) file.delete();

        executorService.execute(() -> {
            boolean downloaded = gf.contentDownload(
                APK_DOWNLOAD_PATH,
                apkFileName,
                savePath,
                progress -> mHandler.post(() ->
                    globalViewModel._downloadPercent.postValue(
                        Math.toIntExact(progress)
                    )
                )
            );

            if (downloaded) {
                mHandler.post(() ->
                    globalViewModel._downloadPercent.postValue(100)
                );
            }
        });
    }

    // 처음 받는 경우 셋업후 실행
    public void executeApk() {
        File apkFile = new File(sDownloadDir + File.separator + sApkFileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);

                Uri apkURI = FileProvider.getUriForFile(mContext, Utils.PACKAGE_NAME + ".provider", apkFile);
                intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Uri apkUri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void executeApk(String apkFileName, String savePath) {
        File apkFile = new File(savePath + File.separator + apkFileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);

                Uri apkURI = FileProvider.getUriForFile(mContext, Utils.PACKAGE_NAME + ".provider", apkFile);
                intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Uri apkUri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface Listener {
        void onProgressUpdate(int progress);
    }

    public void setListener(Listener _listener) {
        this.listener = _listener;
    }
}