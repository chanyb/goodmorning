package kr.co.kworks.goodmorning.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivitySinglePageBinding;
import kr.co.kworks.goodmorning.fragment.WebviewFragment;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.Confirm;
import kr.co.kworks.goodmorning.model.network.NetworkBroadcastReceiver;
import kr.co.kworks.goodmorning.service.GoodmorningService;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.viewmodel.Event;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;

@AndroidEntryPoint
public class SinglePageActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private GlobalViewModel globalViewModel;
    private Handler mHandler;
    private ActivitySinglePageBinding binding;

    private WebviewFragment webViewFragment;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> startServiceScheduled, progressSyncScheduled;

    private CalendarHandler calendarHandler;

    private NetworkBroadcastReceiver networkBroadcastReceiver;

    private ActivityResultLauncher<Intent> getContactLauncher;

    public interface onBackPressedListener {
        public void onBack();
    }

    private onBackPressedListener mOnBackPressedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_single_page);
        init();
        observerInit();
        initClickListener();
        initProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        startAllScheduled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
        release();
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
        webViewFragment = new WebviewFragment(ApiConstants.MAIN_URL, null);

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

        getContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i("this", "result");
                if (result.getResultCode() == Activity.RESULT_OK) {
                    handleContact(result.getData());
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                }
            }
        );

    }

    private void launchContactLauncher() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        getContactLauncher.launch(intent);
    }

    private void handleContact(Intent data) {
        if(data == null || data.getData() == null) return;

        try (Cursor cursor = getContentResolver().query(data.getData(), new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null)) {
            if (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String phone = cursor.getString(1);
                phone = phone.replace("-", "");
                String finalPhone = phone;

                mHandler.post(() -> {
                    globalViewModel._callFunction.setValue(new Event<>(
                        String.format(Locale.KOREA, "%s(%s, %s)", globalViewModel._callbackForContact, name, finalPhone)
                    ));
                });
            }
        }
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

        globalViewModel._confirm.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) {
            } else {
                if (isHandled.equals("visible")) {
                    binding.confirmDialog.loDialog.setVisibility(View.VISIBLE);
                }
            }
        });

        globalViewModel._alert.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) {
            } else {
                if (isHandled.equals("visible")) {
                    binding.alertDialog.loDialog.setVisibility(View.VISIBLE);
                }
            }
        });

        globalViewModel._progress.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) {
            } else {
                if (isHandled.equals("visible")) {
                    binding.progressDialog.loDialog.setVisibility(View.VISIBLE);
                } else if (isHandled.equals("gone")) {
                    binding.progressDialog.loDialog.setVisibility(View.GONE);
                    globalViewModel.progressDialog.progress = 0;
                }
            }
        });

        globalViewModel.confirmContent.observe(this, o -> {
            if (o == null) return;
            setConfirmContent(o);
        });

        globalViewModel.alertContent.observe(this, o -> {
            if (o == null) return;
            setAlertContent(o);
        });

        globalViewModel._progressText1.observe(this, o -> {
            if (o == null) return;
            binding.progressDialog.text1.setText(o);
        });

        globalViewModel._progressText2.observe(this, o -> {
            if (o == null) return;
            binding.progressDialog.text2.setText(o);
        });

        globalViewModel._launchGetContact.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) return;
            launchContactLauncher();
        });
    }

    private void popAllBackStack() {
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void setAlertContent(@NonNull Alert alertContent) {
        binding.alertDialog.txtTitle.setText(alertContent.title);
        binding.alertDialog.txtBody.setText(alertContent.body);
    }

    private void setConfirmContent(@NonNull Confirm confirmContent) {
        binding.confirmDialog.txtTitle.setText(confirmContent.title);
        binding.confirmDialog.txtBody.setText(confirmContent.body);
        binding.confirmDialog.btnLeft.setText(confirmContent.leftBtnName);
        binding.confirmDialog.btnRight.setText(confirmContent.rightBtnName);
    }

    private void initClickListener() {
        binding.confirmDialog.btnLeft.setOnClickListener(v -> {
            if (globalViewModel.jsResult != null) {
                globalViewModel.jsResult.cancel();
                globalViewModel.jsResult = null;
            }
            binding.confirmDialog.loDialog.setVisibility(View.GONE);
        });
        binding.confirmDialog.btnRight.setOnClickListener(v -> {
            if (globalViewModel.jsResult != null) {
                globalViewModel.jsResult.confirm();
                globalViewModel.jsResult = null;
            }
            binding.confirmDialog.loDialog.setVisibility(View.GONE);
        });
        binding.alertDialog.btn.setOnClickListener(v -> {
            if (globalViewModel.jsResult != null) {
                globalViewModel.jsResult.confirm();
                globalViewModel.jsResult = null;
            }
            binding.alertDialog.loDialog.setVisibility(View.GONE);
        });
        binding.confirmDialog.loDialog.setOnClickListener(v -> {});
        binding.alertDialog.loDialog.setOnClickListener(v -> {});
        binding.progressDialog.loDialog.setOnClickListener(v -> {});
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

    private void startAllScheduled() {
    }

    private void release() {
    }

    private void initProgressDialog() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .into(new CustomTarget<GifDrawable>() {
                @Override
                public void onResourceReady(@NonNull GifDrawable resource, Transition<? super GifDrawable> transition) {
                    binding.progressDialog.gif.setImageDrawable(resource);
                    resource.setLoopCount(GifDrawable.LOOP_FOREVER);
                    resource.start();
                }

                @Override
                public void onLoadCleared(Drawable placeholder) {}
            });

    }
}