package kr.co.kworks.goodmorning.viewmodel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.GetFile;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.Logger;


@HiltViewModel
public class GlobalViewModel extends ViewModel {

    private final Executor io = Executors.newSingleThreadExecutor();
    private GetFile getFile;

    public MutableLiveData<Event<String>> _mapFragment, _cctvFragment, _aiFragment, _fireListFragment, _popBackStack, _prepareFragment, _step1Fragment, _step2Fragment, _step3Fragment, _softwareUpdate, _networkCheckFragment;
    public MutableLiveData<Event<String>> _mainMenuFragment, _noticeFragment, _settingFragment, _vehicleInfoFragment, _videoFragment, _videoRelayConfirm;
    public MutableLiveData<Event<String>> headerFragment_switch;
    public MutableLiveData<Event<Alert>> _alert, _exitConfirm;
    public MutableLiveData<Object> _videoFragmentInfo;
    public DeviceInfoRepository deviceInfoRepository;
    public String wonwooUrl, ubitronUrl;
    public String currentPlayUrl;
    public String ffmpegRelayUrl;
    public MutableLiveData<Boolean> _updateNeeded, _updateNotNeeded, _sqliteUpdateNeeded, _sqliteUpdateNotNeeded, _networkConnected;

    public int fire_ing, fire_end, fire_finish;

    public MutableLiveData<Location> _location;

    private String sqliteVersionFileName;
    private String sqliteFileName;
    private String sqliteSavePath;

    private Database db;

    @Inject
    public GlobalViewModel(
        DeviceInfoRepository deviceInfoRepository
    ) {
        this.deviceInfoRepository = deviceInfoRepository;
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        getFile = new GetFile();
        _mapFragment = new MutableLiveData<>();
        _cctvFragment = new MutableLiveData<>();
        _aiFragment = new MutableLiveData<>();
        _fireListFragment = new MutableLiveData<>();
        _popBackStack = new MutableLiveData<>();
        _step1Fragment = new MutableLiveData<>();
        _step2Fragment = new MutableLiveData<>();
        _step3Fragment = new MutableLiveData<>();
        _softwareUpdate = new MutableLiveData<>();
        _networkCheckFragment = new MutableLiveData<>();
        _mainMenuFragment = new MutableLiveData<>();
        _noticeFragment = new MutableLiveData<>();
        _settingFragment = new MutableLiveData<>();
        _vehicleInfoFragment = new MutableLiveData<>();
        _updateNotNeeded = new MutableLiveData<>();
        _updateNeeded = new MutableLiveData<>();
        _sqliteUpdateNeeded = new MutableLiveData<>();
        _sqliteUpdateNotNeeded = new MutableLiveData<>();
        _networkConnected = new MutableLiveData<>();
        _alert = new MutableLiveData<>();
        _videoFragment = new MutableLiveData<>();
        _videoFragmentInfo = new MutableLiveData<>();
        _exitConfirm = new MutableLiveData<>();
        _videoRelayConfirm = new MutableLiveData<>();
        _prepareFragment = new MutableLiveData<>();

        headerFragment_switch = new MutableLiveData<>();
        db = new Database();

        wonwooUrl = ApiConstants.WONWOO_VIDEO_URL;
        ubitronUrl = ApiConstants.UBITRON_VIDEO_URL;
        ffmpegRelayUrl = wonwooUrl;
        currentPlayUrl = "";

        fire_ing = 0;
        fire_end = 0;
        fire_finish = 0;

        _location = new MutableLiveData<>();
        Location location = new Location("MANUAL");
        location.setLatitude(36.350470);
        location.setLongitude(127.384827);
        _location.postValue(location);

        sqliteFileName = "JUSO.sqlite";
        sqliteVersionFileName = "sqlite_version.txt";
        sqliteSavePath = GlobalApplication.getContext().getFilesDir().getAbsolutePath();
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfoRepository.getDeviceInfoFromDB();
    }


    public void appUpdateCheck(Context mContext) {
        getRecentVersionOfApp(mContext, recentVersion -> {
            if (isLocalVersionRecent(getCurrentVersionOfApp(mContext), recentVersion)) {
                // recent
                _updateNotNeeded.postValue(true);
            } else {
                // need update
                _updateNeeded.postValue(true);
            }
        });
    }

    private void getRecentVersionOfApp(Context mContext, Consumer<String> callback) {
        io.execute(() -> {
            if (getFile.textDownload("https://apk-link14.kworks.co.kr/apk/forestvehicleapp/", "forestvehicleapp.txt", mContext.getFilesDir().getAbsolutePath())) {
                List<String> lines = getFile.readFileToLineList(mContext.getFilesDir().getAbsolutePath() + "/" + "forestvehicleapp.txt");
                try {
                    if (lines.size() >= 2) {
                        String line = lines.get(1);
                        String serverVersion = line.split(",")[1];
                        callback.accept(serverVersion);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public String getCurrentVersionOfApp(Context mContext) {
        PackageInfo i = null;
        try {
            i = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return "100.0.0";
        }
        return i.versionName;
    }

    public boolean isLocalVersionRecent(String localVersion, String serverVersion) {
        String[] serverVersionSplit = serverVersion.split("\\.");
        if (serverVersionSplit.length == 3) {
            // 유의적 버전 명세
            int serverMajor = Integer.parseInt(serverVersionSplit[0]);
            int serverMinor = Integer.parseInt(serverVersionSplit[1]);
            int serverPatch = Integer.parseInt(serverVersionSplit[2]);
            String[] localVersionSplit = localVersion.split("\\.");
            int localMajor = Integer.parseInt(localVersionSplit[0]);
            int localMinor = Integer.parseInt(localVersionSplit[1]);
            int localPatch = Integer.parseInt(localVersionSplit[2]);

            if (serverMajor < localMajor) {
                return true;
            }
            if (serverMajor == localMajor && serverMinor < localMinor) {
                return true;
            }
            if (serverMajor == localMajor && serverMinor == localMinor && serverPatch <= localPatch) {
                return true;
            }
            return false;
        } else {
            // 소수점 버전체크
            double dCurrent = Double.parseDouble(localVersion);
            double dRecent = Double.parseDouble(serverVersion);
            if (dRecent <= dCurrent) {
                return false;
            }

            return true;
        }
    }

    private boolean isAttached(String dbName) {
        Cursor cursor = db.getReadableDatabase().rawQuery("PRAGMA database_list;", null);
        while (cursor.moveToNext()) {
            int seq = cursor.getInt(0);        // DB 번호 (0 = main, 1 = temp, 2 이상 = attach된 DB)
            String name = cursor.getString(1); // DB 이름 (main, temp, sales 등)
            String file = cursor.getString(2); // 실제 파일 경로
            if (name.equals(dbName)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public void detachDatabase(String dbName) {
        db.getWritableDatabase().execSQL(String.format(Locale.KOREA, "DETACH DATABASE %s;", dbName));
    }

    private void getRecentVersionOfAddressDB(Consumer<String> callback) {
        io.execute(() -> {
            getFile.contentDownload("https://apk-link14.kworks.co.kr/apk/forestvehicleapp/", sqliteVersionFileName, sqliteSavePath);
            List<String> lines = getFile.readFileToLineList(sqliteSavePath + "/" + sqliteVersionFileName);
            if (lines.size() >= 2) {
                String recentSqliteVersion = lines.get(1);
                Logger.getInstance().info("recentSqliteVersion: " + recentSqliteVersion);
                callback.accept(recentSqliteVersion);
            }
        });
    }

    public String getCurrentVersionOfAddressDB(Context mContext) {
        if (!isAttached("juso")) {
            db.getWritableDatabase().execSQL(String.format(Locale.KOREA, "ATTACH DATABASE '%s/JUSO.sqlite' AS juso", sqliteSavePath));
        }

        Cursor cursor = db.getReadableDatabase().rawQuery("select * from juso.info", null);
        if(cursor.moveToNext()) {
            return cursor.getString(0);
        }

        return "19990101000000";
    }

    public void addressDBUpdateCheck(Context mContext) {
        getRecentVersionOfAddressDB(recentVersion -> {
            if (Long.parseLong(recentVersion) > Long.parseLong(getCurrentVersionOfAddressDB(mContext))) {
                _sqliteUpdateNeeded.postValue(true);
                _sqliteUpdateNotNeeded.postValue(false);
            } else {
                _sqliteUpdateNotNeeded.postValue(true);
                _sqliteUpdateNeeded.postValue(false);
            }
        });
    }

    public String getCctvIp() {
        int i = currentPlayUrl.indexOf("@");
        int j = currentPlayUrl.indexOf(":554");
        String ip = currentPlayUrl.substring(i+1,j);
        return ip;
    }

    public void changeCctv() {
        if (currentPlayUrl.equals(wonwooUrl)) {
            currentPlayUrl = ubitronUrl;
        } else {
            currentPlayUrl = wonwooUrl;
        }
    }
}
