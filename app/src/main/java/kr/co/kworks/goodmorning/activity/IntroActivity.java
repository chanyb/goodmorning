package kr.co.kworks.goodmorning.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivityIntroBinding;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.PreferenceHandler;
import kr.co.kworks.goodmorning.utils.Utils;
import kr.co.kworks.goodmorning.viewmodel.IntroViewModel;


/**
 * 채널생성 -> 1차 권한 요청 -> 2차 권한요청 -> nextPage
 */
public class IntroActivity extends AppCompatActivity {
    private AtomicBoolean out;
    private Context mContext;
    private Handler mHandler;
    private PreferenceHandler preferenceHandler;
    private ActivityIntroBinding binding;
    private ActivityResultLauncher<Intent> mManageAppAllFiles;
    private ScheduledExecutorService loadingFailureExecutor;
    private ScheduledFuture<?> loadingFailureScheduled;
    private String sPermission;
    private Drawable downLoading;
    private CalendarHandler calendarHandler;
    private IntroViewModel introViewModel;
    private int downloadPercent;
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro);
        GlobalApplication.currentActivity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep screen on
        init();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2424) {
            if (Boolean.FALSE.equals(getNeededPermissions().isEmpty())) {
                Toast.makeText(this, "허용되지 않은 권한이 있습니다.\n앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(this::finish, 2000);
            } else {
                // (1차) 권한 요청 끝난 경우
                requestDetailPermission();
            }
        }

        if (requestCode == 2425) {
            if (Boolean.FALSE.equals(getNeededPermissions().isEmpty())) {
                Toast.makeText(this, "허용되지 않은 권한이 있습니다.\n앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(this::finish, 2000);
            } else {
                // (2차) 권한 요청 끝난 경우
                nextPage();
            }
        }


    }

    private void init() {
        this.mContext = this;
        mHandler = new Handler(Looper.getMainLooper());
        loadingFailureExecutor = Executors.newScheduledThreadPool(1);
        out = new AtomicBoolean(false);
        calendarHandler = new CalendarHandler();
        introViewModel = new ViewModelProvider(this).get(IntroViewModel.class);
        fragmentManager = getSupportFragmentManager();

        getOnBackPressedDispatcher().addCallback(
            this,
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {

                }
            }
        );

        initFragment();
    }

    private void initFragment() {
        initObserver();
    }

    private void initObserver() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        createChannels();
//        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
                // 브로드캐스트 등록
    }

    private void nextPage() {
        Intent intent = new Intent(mContext, SinglePageActivity.class);
        mHandler.post(() -> startActivity(intent));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private ArrayList<String> getNeededPermissions() {
        ArrayList<String> neededPermissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        return neededPermissions;
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createNotificationChannel(Utils.LOCATION_SERVICE_CHANNEL_ID, NotificationManager.IMPORTANCE_MAX);
        } else {
            createNotificationChannel(Utils.LOCATION_SERVICE_CHANNEL_ID, 1);
        }

        if (GlobalApplication.getContext().isNotificationChannelEnabled(Utils.LOCATION_SERVICE_CHANNEL_ID)) {
            requestPermissions();
        } else {
            Toast.makeText(this, "채널을 생성하지 못했습니다.\n앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(this::finish, 2000);
        }

    }
    private void createNotificationChannel(String channelId, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!GlobalApplication.getContext().isNotificationChannelEnabled(channelId)) ;
            GlobalApplication.getContext().createNotificationChannel(channelId, importance);
        }
    }



    private void requestPermissions() {
        ArrayList<String> list = getNeededPermissions();
        if(list.isEmpty()) {
            // 권한 요청 끝남
            requestDetailPermission();
            return;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissions, 2424);
    }

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return super.getOnBackInvokedDispatcher();
    }

    private boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void replaceFragment(Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(binding.loMain.getId(), fragment);
        fragmentTransaction.addToBackStack(backStackName);
        fragmentTransaction.commit();
    }

    private void replaceFragment(ConstraintLayout layout, Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
        fragmentTransaction.replace(layout.getId(), fragment);
        fragmentTransaction.addToBackStack(backStackName);
        fragmentTransaction.commit();
    }

    private ArrayList<String> getNeededDetailPermission() {
        ArrayList<String> neededDetailPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                neededDetailPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        return neededDetailPermissions;
    }
    private void requestDetailPermission() {
        ArrayList<String> list = getNeededDetailPermission();
        if(list.isEmpty()) {
            // 권한 요청 끝남
            nextPage();
            return;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissions, 2425);
    }
}



