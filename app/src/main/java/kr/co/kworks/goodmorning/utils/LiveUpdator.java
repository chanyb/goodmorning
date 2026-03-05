package kr.co.kworks.goodmorning.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.co.kworks.goodmorning.viewmodel.IntroViewModel;

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
    private IntroViewModel introViewModel;
    private Handler mHandler;
    private ExecutorService executorService;

    public static final String APK_DOWNLOAD_PATH = "http://apk-link14.kworks.co.kr/apk/forestvehicleapp/";
    public static final int HTTP_CONNECTION_TIMEOUT = 3; //3초 > 10초
    public static final String APK_VERSION_CHECK_FILE = "forestvehicleapp.txt";
    public static final int LIVE_UPDATE_CHECK_TIMEOUT_SEC = 30;


    public LiveUpdator(Context mContext, IntroViewModel introViewModel) {
        this.mContext = mContext;
        this.introViewModel = introViewModel;
        ApkDown = "N";
        bStoreVer = true;
        please_wait = "잠시만기다려주세요. 통신중에있습니다.";
        please_download_wait = "다운로드 중입니다.\n잠시만 기다려 주시기 바랍니다.";
        ServerVersion = ""; sLocalVersion = ""; sApkFileName = "";
        mHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
    }

    private String VersionCheck() {
        String FileDownLoad = "";

        // 다운받은 txt화일에 읽어가며 버전 확인
        for (int inx = 0; inx < ServerUpdateFile.size(); inx++) {
            FileDownLoad = "Y";
            double iServerVersion = 0.0;
            double iLocalVersion = 0.0;

            String currentLine = ServerUpdateFile.get(inx);
            try {
                if (inx == 0) {
                    sApkFileName = currentLine.substring(0, currentLine.indexOf(","));
                    ServerVersion = currentLine.substring(currentLine.indexOf(",") + 1, currentLine.length());
                    try {
                        PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                        sLocalVersion = String.valueOf(pInfo.versionName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    String[] serverVersionSplit = ServerVersion.split("\\.");
                    if (serverVersionSplit.length == 3) {
                        // 유의적 버전 명세
                        int serverMajor = Integer.parseInt(serverVersionSplit[0]);
                        int serverMinor = Integer.parseInt(serverVersionSplit[1]);
                        int serverPatch = Integer.parseInt(serverVersionSplit[2]);
                        String[] localVersionSplit = sLocalVersion.split("\\.");
                        int localMajor = Integer.parseInt(localVersionSplit[0]);
                        int localMinor = Integer.parseInt(localVersionSplit[1]);
                        int localPatch = Integer.parseInt(localVersionSplit[2]);

                        if (serverMajor < localMajor) {
                            FileDownLoad = "N";
                            break;
                        }
                        if (serverMajor == localMajor && serverMinor < localMinor) {
                            FileDownLoad = "N";
                            break;
                        }
                        if (serverMajor == localMajor && serverMinor == localMinor && serverPatch <= localPatch) {
                            FileDownLoad = "N";
                            break;
                        }

                    } else {
                        iServerVersion = Double.parseDouble(ServerVersion);
                        iLocalVersion = Double.parseDouble(sLocalVersion);
                        // 소수점 버전체크
                        if (iServerVersion <= iLocalVersion) {
                            FileDownLoad = "N";
                            break;
                        }
                    }

                } else {
                    ServerUpdateHistory += currentLine + "\r\n";
                }
            } catch (Exception e) {
                Logger.getInstance().error("VersionCheck()", e);
                FileDownLoad = "N";
                break;
            }

        }
        return FileDownLoad;
    }



    // GetFile CLASS
    public class GetFile {

        public GetFile() {
        }

        // http 프로토콜 화일 다운(text)
        public boolean TextDownload(String Url, String FileName, String sSaveFile) {
            // 다운 받을 화일이 위치한 서버 경로
            URL Downloadurl;
            boolean isComplete = false;
            try {
                Downloadurl = new URL(Url + FileName);

                // http 프로토콜 연결
                HttpURLConnection conn = (HttpURLConnection) Downloadurl.openConnection();
                conn.setConnectTimeout(HTTP_CONNECTION_TIMEOUT * 1000);
                conn.setReadTimeout(HTTP_CONNECTION_TIMEOUT * 1000);
                BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) conn.getInputStream(), "UTF-8"));
                // 임시 폴더에 다운받음
                FileOutputStream fos = new FileOutputStream(sSaveFile + "/" + FileName);

                byte[] contentInBytes;
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String data = inputLine + "\r\n";
                    contentInBytes = data.getBytes();
                    fos.write(contentInBytes);
                }
                fos.flush();
                fos.close();
                isComplete = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                isComplete = false;
                introViewModel.updateNotNeed.postValue(true);
            }
            return isComplete;
        }

        // http 프로토콜 화일 다운(content)
        public boolean ContentDownload(String Url, String FileName, String sSaveFile) {
            // 다운 받을 화일이 위치한 서버 경로
            URL Downloadurl;
            boolean isComplete = false;
            try {
                Downloadurl = new URL(Url + FileName);

                // http 프로토콜 연결
                HttpURLConnection conn = (HttpURLConnection) Downloadurl.openConnection();

                byte[] buffer = new byte[1024];
                int length = 0;
                long fileSize = conn.getContentLength();

                InputStream is = conn.getInputStream();
                // 임시 폴더에 다운받음
                FileOutputStream fos = new FileOutputStream(sSaveFile + "/" + FileName);

                long downloadedFileSize = 0;
                while ((length = is.read(buffer)) >= 0) {
                    downloadedFileSize += length;
                    long progress = (downloadedFileSize * 100L) / fileSize;
                    mHandler.post(() -> introViewModel.downloadPercent.postValue((int) progress));
                    fos.write(buffer, 0, length);
                }

                fos.flush();
                fos.close();
                is.close();
                isComplete = true;
            } catch (Exception ex) {
                Logger.getInstance().error("LiveUpdater-ContentDownload", ex);
                isComplete = false;
                introViewModel.updateNotNeed.postValue(true);
            }
            return isComplete;
        }
    }


    private class DownloadTextFileTask extends AsyncTask<Object, Object, Boolean> {

        private ProgressDialog mProgressDialog = new ProgressDialog(mContext);

        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Object... arg0) {
            //파일 다운받기
            return gf.TextDownload(APK_DOWNLOAD_PATH, APK_VERSION_CHECK_FILE, sDownloadDir);
        }

        protected void onProgressUpdate(Object... progress) {
            super.onProgressUpdate(progress);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result) {
                try {
                    //서버 업데이트 파일 ArrayList에 저장
                    ServerUpdateFile = Utils.get().getUpdateFile(sDownloadDir + File.separator + APK_VERSION_CHECK_FILE);
                    if (ServerUpdateFile != null) {
                        File txtfile = new File(sDownloadDir + File.separator + APK_VERSION_CHECK_FILE);
                        if (txtfile.exists()) {
                            txtfile.delete();
                        }
                        //텍스트파일의 버전과 버전 비교
                        String updateNeeded = VersionCheck();
                        if (updateNeeded.equals("Y")) {
                            String message;
                            if (Utils.get().isEmptyString(ServerUpdateHistory)) {
                                message = "새로운 버전 (" + ServerVersion + ") 이 등록 되어있습니다. 업데이트 하시겠습니까?";
                            } else {
                                message = "새로운 버전 (" + ServerVersion + ") 이 등록 되어있습니다. 업데이트 하시겠습니까?" + "\r\n" + ServerUpdateHistory;
                            }
                            introViewModel.updateString.postValue(message);
                            introViewModel.updateNeeded.postValue(true);
                        } else {
                            introViewModel.updateNotNeed.postValue(true);
                            executorService.shutdown();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                introViewModel.updateNotNeed.postValue(true);
            }
        }
    }

    public void downloadApk() {
        if(sApkFileName == null || sApkFileName.isEmpty()) return;

        File file = new File(sDownloadDir + File.separator + sApkFileName);
        if (file.exists()) file.delete();


        executorService.execute(() -> {
            gf.ContentDownload(APK_DOWNLOAD_PATH, sApkFileName, sDownloadDir);
        });
    }

    public void downloadApk(String apkFileName, String savePath) {
        if(apkFileName == null || apkFileName.isEmpty()) return;

        File file = new File(sDownloadDir + File.separator + apkFileName);
        if (file.exists()) file.delete();

        executorService.execute(() -> {
            gf.ContentDownload(APK_DOWNLOAD_PATH, apkFileName, savePath);
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

    public void checkFileVersion() {
        sDownloadDir = Utils.get().getAbsolutePath(mContext);
        new DownloadTextFileTask().execute();
    }

    public interface Listener {
        void onProgressUpdate(int progress);
    }

    public void setListener(Listener _listener) {
        this.listener = _listener;
    }
}