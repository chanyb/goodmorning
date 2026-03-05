package kr.co.kworks.goodmorning.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ai.instavision.ffmpegkit.FFmpegKit;
import ai.instavision.ffmpegkit.FFmpegKitConfig;
import ai.instavision.ffmpegkit.ReturnCode;
import ai.instavision.ffmpegkit.Session;
import ai.instavision.ffmpegkit.SessionState;
import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivitySinglePageBinding;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.network.NetworkBroadcastReceiver;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.LocationRepository;
import kr.co.kworks.goodmorning.service.LocationService;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.LiveUpdator;
import kr.co.kworks.goodmorning.utils.LocationManagerHandler;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.viewmodel.Event;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import kr.co.kworks.goodmorning.viewmodel.IntroViewModel;

@AndroidEntryPoint
public class SinglePageActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private IntroViewModel introViewModel;
    private GlobalViewModel globalViewModel;
    private Handler mHandler;
    private long ffmpegSessionId;
    private ActivitySinglePageBinding binding;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> rtmpRelayScheduled, startServiceScheduled, appUpdateCheckScheduled;
    private LocationManagerHandler locationManagerHandler;

    private CalendarHandler calendarHandler;

    private NetworkBroadcastReceiver networkBroadcastReceiver;
    private LiveUpdator liveUpdator;

    @Inject
    LocationRepository locationRepository;

    @Inject
    DeviceInfoRepository deviceInfoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_single_page);
        init();
        observerInit();
        initClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rtmpRelaySchedule();
        registerBroadcastReceiver();
        setLocationListener();
        startAppUpdateCheckScheduled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRtmpRelay();
        stopRtmpRelayScheduled();
        unregisterBroadcastReceiver();
        removeLocationListener();
        stopAppUpdateCheckScheduled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.getInstance().info("SinglePageActivity - onDestroy");
    }

    private void init() {
        calendarHandler = new CalendarHandler();
        mHandler = new Handler(Looper.getMainLooper());
        globalViewModel = new ViewModelProvider(this).get(GlobalViewModel.class);
        fragmentManager = getSupportFragmentManager();
        executor = Executors.newSingleThreadScheduledExecutor();
        introViewModel = new ViewModelProvider(this).get(IntroViewModel.class);
        liveUpdator = new LiveUpdator(this, introViewModel);


        locationManagerHandler = new LocationManagerHandler(this);


        networkBroadcastReceiver = new NetworkBroadcastReceiver(bool -> {
            globalViewModel._networkConnected.postValue(bool);
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep screen on
        setFirstFragment();


        getOnBackPressedDispatcher().addCallback(
            this,
            new OnBackPressedCallback(true) {   // 항상 활성(true) ← 매우 중요
                @Override
                public void handleOnBackPressed() {

                }
            }
        );

    }

    public static void hideKeyboard(View anyView) {
        InputMethodManager imm = (InputMethodManager) anyView.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = anyView;
        if (v == null) return;

        IBinder token = v.getWindowToken();
        if (token == null && v.getRootView() != null) {
            token = v.getRootView().getWindowToken();
        }
        if (imm != null && token != null) {
            imm.hideSoftInputFromWindow(token, 0);
        }
    }

    private void replaceFragment(Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(binding.fragmentMain.getId(), fragment);
        fragmentTransaction.addToBackStack(backStackName);
        fragmentTransaction.commit();
    }

    private void replaceFragment(int id, Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.addToBackStack(backStackName);
        fragmentTransaction.commit();
    }

    private void observerInit() {
        globalViewModel._popBackStack.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) {
                Logger.getInstance().info("handled");
            } else {
                if (isHandled.equals("back")) {
                    if(fragmentManager.getBackStackEntryCount() == 1) {
                        finish();
                    }
                    fragmentManager.popBackStack();

                }
            }
        });

    }

    private void popAllBackStack() {
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void initClickListener() {
        binding.appUpdateConfirm.loDialog.setOnClickListener(v-> {});

        binding.appUpdateConfirm.btnCancel.setOnClickListener(v -> {
            binding.appUpdateConfirm.loDialog.setVisibility(View.GONE);
        });

        binding.appUpdateConfirm.btnUpdate.setOnClickListener(v -> {
            binding.appUpdateConfirm.txtAlertBody.setText("업데이트 파일을 다운로드 합니다...");
            liveUpdator.downloadApk("forestvehicleapp.apk", getFilesDir().getAbsolutePath());
            binding.appUpdateConfirm.loProgress.setVisibility(View.VISIBLE);
            binding.appUpdateConfirm.btnUpdate.setVisibility(View.GONE);
            binding.appUpdateConfirm.btnCancel.setVisibility(View.GONE);
        });

        binding.videoRelayConfirm.loDialog.setOnClickListener(v -> {});
        binding.videoRelayConfirm.btnCancel.setOnClickListener(v -> {
            binding.videoRelayConfirm.loDialog.setVisibility(View.GONE);
        });

        binding.videoRelayConfirm.btnStart.setOnClickListener(v -> {
            globalViewModel.headerFragment_switch.postValue(new Event<>("on"));
            binding.videoRelayConfirm.btnCancel.callOnClick();
        });

    }

    /**
     * 26-01-15 POC 이후 SinglePageActivity에서 사용 하지 않음
     */
    private void startService() {
        if(GlobalApplication.getContext().isServiceRunningCheck(LocationService.class)) return;
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void startRtmpRelay() {
        Logger.getInstance().info("startRtmpRelay");
        DeviceInfo deviceInfo = globalViewModel.getDeviceInfo();
        if(deviceInfo == null) return;

        mHandler.postDelayed(() -> {
            String cmd = String.format(Locale.KOREA,
                "-i %s " +
                    "-c:v copy " +
                    "-an " +
                    "-f flv rtmps://%s:%s@%s:8443/live/%s"
                , ApiConstants.FFMPEG_RELAY_URL, "vehicle_pub", "c4adb642ccb91327", ApiConstants.STREAM_DOMAIN, deviceInfo.vehicleCode);
//
//            String cmd = String.format(Locale.KOREA,
//                "-rtsp_transport tcp " +
//                    "-fflags +genpts -use_wallclock_as_timestamps 1 " +
//                    "-avoid_negative_ts make_zero " +
//                    "-i %s " +
//                    "-vf \"scale=848:480,fps=30,format=yuv420p\" " +
//                    "-c:v h264_mediacodec " +
//                    "-g 30 -keyint_min 30 " +
//                    "-force_key_frames \"expr:gte(t,n_forced*1)\" " +
//                    "-b:v 1000k -maxrate 1000k -bufsize 2000k " +
//                    "-an " +
//                    "-f flv rtmps://%s:%s@%s:8443/live/%s"
//                , globalViewModel.currentPlayUrl, "vehicle_pub", "c4adb642ccb91327", ApiConstants.STREAM_DOMAIN, deviceInfo.vehicleCode);


            FFmpegKit.cancel(ffmpegSessionId);
            FFmpegKitConfig.enableStatisticsCallback(null);

            FFmpegKit.executeAsync(cmd, session -> {
                SessionState state = session.getState();
                ReturnCode rc = session.getReturnCode();
                String output = session.getOutput();
                if (ReturnCode.isSuccess(rc)) {
                    Logger.getInstance().info("✅ 중계 성공------------------------------------------------");
                } else if (ReturnCode.isCancel(rc)) {
                    Logger.getInstance().info("⛔ 중계 취소됨");
                } else {
                    Logger.getInstance().error("❌ 중계 실패. state=" + state + ", rc=" + rc + "\n" + output, null);
                }
                FFmpegKitConfig.clearSessions();
            });

            Session session3 = FFmpegKitConfig.getLastSession();
            if (session3 != null) ffmpegSessionId = session3.getSessionId();

        }, 3000);
    }

    private void stopRtmpRelay() {
        if(ffmpegSessionId != -1L) {
            FFmpegKit.execute("q");
            FFmpegKit.cancel(ffmpegSessionId);
            Logger.getInstance().info("stopRtmpRelay()");
        }
    }

    private void rtmpRelaySchedule() {
        stopRtmpRelayScheduled();
        rtmpRelayScheduled = executor.scheduleWithFixedDelay(() -> {
            Logger.getInstance().info("rtmpRelaySchedule()");

            if (deviceInfoRepository.getDeviceInfoFromDB().videoRelayYn.equalsIgnoreCase("N")) {
                stopRtmpRelay();
                return;
            }

            try {
                Thread.sleep(15_000);
            } catch (InterruptedException e) {
            }

            if(FFmpegKitConfig.getLastSession() != null && FFmpegKitConfig.getLastSession().getState() != null) {
                if (FFmpegKitConfig.getLastSession().getState() == SessionState.RUNNING) {
                    Logger.getInstance().info("FFmpegKit is RUNNGING return");
                    return;
                }

                if (FFmpegKitConfig.getLastSession().getState() == SessionState.CREATED) {
                    Logger.getInstance().info("FFmpegKit is CREATED return");
                    return;
                }

                if (FFmpegKitConfig.getLastSession().getState() == SessionState.COMPLETED) {
                    Logger.getInstance().info("FFmpegKit is COMPLETED, not return");
                }

                if (FFmpegKitConfig.getLastSession().getState() == SessionState.FAILED) {
                    Logger.getInstance().info("FFmpegKit is FAILED return");
                    return;
                }
            }

            if (GlobalApplication.getContext().isForeground()) startRtmpRelay();
        }, 2000, 5_000, TimeUnit.MILLISECONDS);
    }

    private void stopRtmpRelayScheduled() {
        if (rtmpRelayScheduled != null && !rtmpRelayScheduled.isCancelled()) rtmpRelayScheduled.cancel(true);
    }

    private void startStartServiceScheduled() {
        stopStartServiceScheduled();
        startServiceScheduled = executor.scheduleWithFixedDelay(() -> {
            DeviceInfo deviceInfo = globalViewModel.getDeviceInfo();
            if(deviceInfo == null || deviceInfo.vehicleCode == null || deviceInfo.vehicleCode.isEmpty()) return;
            startService();
        }, 2000, 5000, TimeUnit.MILLISECONDS);
    }

    private void stopStartServiceScheduled() {
        if (startServiceScheduled != null && !startServiceScheduled.isCancelled()) startServiceScheduled.cancel(true);
    }

    private void whenLocationObserved(Location location) {
        if (location == null) return;
        globalViewModel._location.postValue(location);
    }

    private boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(networkBroadcastReceiver);
    }

    private void setLocationListener() {
//        if (fusedLocationProvider.canUse()) {
//            fusedLocationProvider.liveLocation.observe(this,this::whenLocationObserved);
//            fusedLocationProvider.start();
//        } else {
        locationManagerHandler.liveLocation.observe(this, this::whenLocationObserved);
        locationManagerHandler.start();
//        }
    }

    private void removeLocationListener() {
//        if (fusedLocationProvider.canUse()) {
//            fusedLocationProvider.liveLocation.removeObservers(this);
//            fusedLocationProvider.stop();
//        } else {
        locationManagerHandler.liveLocation.removeObservers(this);
        locationManagerHandler.stop();
//        }
    }

    private void setFirstFragment() {
        globalViewModel._prepareFragment.postValue(new Event<>("visible"));
//        if (globalViewModel.deviceInfoRepository.getDeviceInfoFromDB() == null) {
//            return;
//        }
//
//        if (globalViewModel.deviceInfoRepository.getDeviceInfoFromDB().vehicleNumber != null && !globalViewModel.deviceInfoRepository.getDeviceInfoFromDB().vehicleNumber.isEmpty()) {
//            globalViewModel._mapFragment.postValue(new Event<>("visible"));
//        }
    }

    private void startAppUpdateCheckScheduled() {
        stopAppUpdateCheckScheduled();
        appUpdateCheckScheduled = executor.scheduleWithFixedDelay(() -> {
            globalViewModel.appUpdateCheck(this);
        }, 15_000, 600_000, TimeUnit.MILLISECONDS);
    }

    private void stopAppUpdateCheckScheduled() {
        if (appUpdateCheckScheduled != null && !appUpdateCheckScheduled.isCancelled()) appUpdateCheckScheduled.cancel(true);
    }
}