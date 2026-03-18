package kr.co.kworks.goodmorning.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivityLockScreenBinding;
import kr.co.kworks.goodmorning.fragment.SeekbarFragment;
import kr.co.kworks.goodmorning.service.GoodmorningService;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.GoodmorningBroadcastReceiver;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;

@AndroidEntryPoint
public class LockScreenActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private GlobalViewModel globalViewModel;
    private Handler mHandler;
    private ActivityLockScreenBinding binding;

    private SeekbarFragment seekbarFragment;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> startServiceScheduled, datetimeScheduled;

    private CalendarHandler calendarHandler;

    private KeyguardManager km;

    public interface onBackPressedListener {
        public void onBack();
    }

    private onBackPressedListener mOnBackPressedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lock_screen);
        init();
        observerInit();
        initClickListener();
        initSeekbar();

        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAllScheduled();
        setRandomWise();
    }

    @Override
    protected void onPause() {
        super.onPause();
        release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(0, 0);
        Logger.getInstance().info("SinglePageActivity - onDestroy");
    }

    private void init() {
        calendarHandler = new CalendarHandler();
        mHandler = new Handler(Looper.getMainLooper());
        globalViewModel = new ViewModelProvider(this).get(GlobalViewModel.class);
        fragmentManager = getSupportFragmentManager();
        executor = Executors.newSingleThreadScheduledExecutor();
        seekbarFragment = new SeekbarFragment();

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

    private void initSeekbar() {
        replaceFragment(binding.loSeekbar.getId(), seekbarFragment, "seekbar");
        seekbarFragment.setListener(new SeekbarFragment.Listener() {
            @Override
            public void onComplete() {
                requestKeyguardDismiss();
            }
        });
    }


    private void replaceFragment(int id, Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.addToBackStack(backStackName);
        fragmentTransaction.commit();
    }

    private void observerInit() {
    }

    private void popAllBackStack() {
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void initClickListener() {
    }


    private boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void setOnKeyBackPressedListener(onBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    private void startAllScheduled() {
        startDatetimeScheduled();
    }

    private void release() {
        stopDatetimeScheduled();
    }

    private void stopDatetimeScheduled() {
        if (datetimeScheduled != null && !datetimeScheduled.isCancelled()) {
            datetimeScheduled.cancel(true);
        }
    }

    private void startDatetimeScheduled() {
        stopDatetimeScheduled();
        datetimeScheduled = executor.scheduleWithFixedDelay(this::setCurrentDatetime, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void setCurrentDatetime() {
        Calendar current = Calendar.getInstance();
        mHandler.post(() -> {
            binding.txtTime.setText(String.format(Locale.KOREA, "%s%d:%s%d",
                calendarHandler.getHourOf24(current) < 10 ? "0":"",
                calendarHandler.getHourOf24(current),
                calendarHandler.getMinute(current) < 10 ? "0":"",
                calendarHandler.getMinute(current)
            ));

            binding.txtDate.setText(String.format(Locale.KOREA, "%d년 %d월 %d일 %s요일",
                calendarHandler.getYear(current),
                calendarHandler.getMonth(current),
                calendarHandler.getDay(current),
                calendarHandler.getDayOfWeekKorean(current)
            ));
        });
    }

    private void setRandomWise() {
        Database db = new Database();
        String sql = "SELECT * FROM " + Column.wise +" ORDER BY RANDOM() LIMIT 1";
        Cursor cursor  = db.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String text = cursor.getString(cursor.getColumnIndexOrThrow(Column.wise_column_text));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Column.wise_column_name));
            mHandler.post(() -> {
                binding.txtWise.setText(text);
                binding.txtName.setText(String.format(Locale.KOREA, "- %s -", name));
            });
        }
        cursor.close();
    }

    private void requestKeyguardDismiss() {
        if (km != null && km.isKeyguardLocked()) {
            km.requestDismissKeyguard(
                this,
                new KeyguardManager.KeyguardDismissCallback() {
                    @Override
                    public void onDismissSucceeded() {
                        super.onDismissSucceeded();
                        finish();
                    }

                    @Override
                    public void onDismissCancelled() {
                        super.onDismissCancelled();
                    }

                    @Override
                    public void onDismissError() {
                        super.onDismissError();
                    }
                }
            );
        } else {
            finish();
        }
    }
}