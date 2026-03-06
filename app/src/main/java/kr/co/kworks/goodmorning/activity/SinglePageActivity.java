package kr.co.kworks.goodmorning.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivitySinglePageBinding;
import kr.co.kworks.goodmorning.fragment.WebviewFragment;
import kr.co.kworks.goodmorning.model.network.NetworkBroadcastReceiver;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.LocationRepository;
import kr.co.kworks.goodmorning.service.LocationService;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
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

    private WebviewFragment webViewFragment;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> startServiceScheduled;

    private CalendarHandler calendarHandler;

    private NetworkBroadcastReceiver networkBroadcastReceiver;

    public interface onBackPressedListener {
        public void onBack();
    }

    private onBackPressedListener mOnBackPressedListener;


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
//        initClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
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
        webViewFragment = new WebviewFragment("https://kworks.co.kr", null);

        networkBroadcastReceiver = new NetworkBroadcastReceiver(bool -> {
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep screen on
        setFirstFragment();


        getOnBackPressedDispatcher().addCallback(
            this,
            new OnBackPressedCallback(true) {   // 항상 활성(true) ← 매우 중요
                @Override
                public void handleOnBackPressed() {
                    if (mOnBackPressedListener != null) {
                        mOnBackPressedListener.onBack();
                    } else {

                    }
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

        globalViewModel._webViewFragment.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) {
            } else {
                if (isHandled.equals("visible")) {
                    replaceFragment(webViewFragment, "webview");
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

    private void startStartServiceScheduled() {
        stopStartServiceScheduled();
        startServiceScheduled = executor.scheduleWithFixedDelay(() -> {
            startService();
        }, 2000, 5000, TimeUnit.MILLISECONDS);
    }

    private void stopStartServiceScheduled() {
        if (startServiceScheduled != null && !startServiceScheduled.isCancelled()) startServiceScheduled.cancel(true);
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

    private void setFirstFragment() {
        globalViewModel._webViewFragment.postValue(new Event<>("visible"));
    }

    public void setOnKeyBackPressedListener(onBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }
}