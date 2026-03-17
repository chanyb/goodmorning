package kr.co.kworks.goodmorning.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import kr.co.kworks.goodmorning.model.business_logic.Wise;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.FCMManager;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.Logger;
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
    private CalendarHandler calendarHandler;
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
            if (Boolean.FALSE.equals(getNeededDetailPermission().isEmpty())) {
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
        initObserver();
        initDatabase();
        FCMManager fcmManager = new FCMManager();
        fcmManager.getToken();
    }

    private void initFragment() {
    }

    private void initObserver() {
    }

    private void initDatabase() {
        Database db = new Database();
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + Column.wise , null);
        cursor.moveToNext();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            db.insert(Column.wise, new Wise("삶이 있는 한 희망은 있다.", "키케로").getContentValues());
            db.insert(Column.wise, new Wise("산다는것 그것은 치열한 전투이다.", "로망로랑").getContentValues());
            db.insert(Column.wise, new Wise("하루에 3시간을 걸으면 7년 후에 지구를 한바퀴 돌 수 있다.", "사무엘존슨").getContentValues());
            db.insert(Column.wise, new Wise("언제나 현재에 집중할수 있다면 행복할것이다", "파울로 코엘료").getContentValues());
            db.insert(Column.wise, new Wise("고통이 남기고 간 뒤를 보라! 고난이 지나면 반드시 기쁨이 스며든다. ", "괴테").getContentValues());
            db.insert(Column.wise, new Wise("꿈을 계속 간직하고 있으면 반드시 실현할 때가 온다", "괴테").getContentValues());
            db.insert(Column.wise, new Wise("진짜 문제는 사람들의 마음이다. 그것은 절대로 물리학이나 윤리학의 문제가 아니다.", "아인슈타인").getContentValues());
            db.insert(Column.wise, new Wise("해야 할 것을 하라. 모든 것은 타인의 행복을 위해서, 동시에 특히 나의 행복을 위해서이다.", "톨스토이").getContentValues());
            db.insert(Column.wise, new Wise("사람이 여행을 하는 것은 도착하기 위해서가 아니라 여행하기 위해서이다.", "괴테").getContentValues());
            db.insert(Column.wise, new Wise("재산을 잃은 사람은 많이 잃은 것이고, 친구를 잃은 사람은 더많이 잃은 것이며, 용기를 잃은 사람은 모든것을 잃은 것이다.", "세르반테스").getContentValues());
            db.insert(Column.wise, new Wise("돈이란 바닷물과도 같다. 그것은 마시면 마실수록 목이 말라진다.", "쇼펜하우어").getContentValues());
            db.insert(Column.wise, new Wise("이룰수 없는 꿈을 꾸고 이길수 없는 적과 싸우며, 이룰수 없는 사랑을 하고 견딜 수 없는 고통을 견디고, 잡을수 없는 저 하늘의 별도 잡자.", "세르반테스").getContentValues());
            db.insert(Column.wise, new Wise("당신이 할수 있다고 믿든 할수 없다고 믿든 믿는 대로 될것이다.", "포드").getContentValues());
            db.insert(Column.wise, new Wise("삶을 사는 데는 단 두가지 방법이 있다. 하나는 기적이 전혀 없다고 여기는 것이고 또 다른 하나는 모든 것이 기적이라고 여기는방식이다.", "아인슈타인").getContentValues());
            db.insert(Column.wise, new Wise("이미 끝나버린 일을 후회하기 보다는 하고 싶었던 일들을 하지못한 것을 후회하라.", "탈무드").getContentValues());
            db.insert(Column.wise, new Wise("인생은 자전거를 타는 것과 같다. 균형을 잡으려면 움직여야 한다.", "아인슈타인").getContentValues());
            db.insert(Column.wise, new Wise("그 어떤 것도 우리가 의미를 부여하기 전에는 아무 의미도 없다는 사실을 기억하라.", "앤서니 라빈스").getContentValues());
            db.insert(Column.wise, new Wise("배움이 없는 자유는 언제나 위험하며, 자유가 없는 배움은 언제나 헛된 일이다.", "존F.케네디").getContentValues());
            db.insert(Column.wise, new Wise("자신은 2위로 만족한다고 말하면, 인생은 그렇게 되기 마련이라는 것을 깨달았다.", "존F.케네디").getContentValues());
            db.insert(Column.wise, new Wise("무례한 사람의 행위는 내 행실을 바로 잡게 해주는 스승이다.", "공자").getContentValues());
            db.insert(Column.wise, new Wise("절대 어제를 후회하지 마라. 인생은 오늘의 나 안에 있고 내일은 스스로 만드는 것이다.", "L.론허바드").getContentValues());
            db.insert(Column.wise, new Wise("인간의 삶 전체는 단지 한 순간에 불과하다. 인생을 즐기자.", "플루타르코스").getContentValues());
            db.insert(Column.wise, new Wise("세상에게 당신은 그저 한 사람일 뿐이지만, 누군가에게는 당신이 세상 전부일 수도 있다.", "닥터 수스").getContentValues());
            db.insert(Column.wise, new Wise("모르는 것을 두려워하지 말라. 그것에 대해 배우지 못하는 것을 두려워 하라.", "불교 명언").getContentValues());
            db.insert(Column.wise, new Wise("무지가 행동으로 나타나는 것보다 더 무서운 것은 없다.", "괴테").getContentValues());
            db.insert(Column.wise, new Wise("친구란 무엇인가? 두 몸에 깃든 하나의 영혼이다.", "아리스토텔레스").getContentValues());
            db.insert(Column.wise, new Wise("누구나 그림을 그릴 수 있지만, 그 그림을 팔 수 있는 사람은 현명한 사람이다.", "사무엘 버틀러").getContentValues());
            db.insert(Column.wise, new Wise("진실이란 없다. 오직 인식만이 존재할 뿐이다.", "구스타브 플로베르").getContentValues());
            db.insert(Column.wise, new Wise("세상에 확실한 건 단 하나, 시도조차 하지 않으면 절대 목표를 달성할 수 없다는 것이다.", "웨인 그레츠키").getContentValues());
        }
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
        Intent intent = new Intent(mContext, LockScreenActivity.class);
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
        return neededPermissions;
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createNotificationChannel(FCMManager.CHANNEL_ID, NotificationManager.IMPORTANCE_MAX);
        } else {
            createNotificationChannel(FCMManager.CHANNEL_ID, 1);
        }

//        if (!GlobalApplication.getContext().isNotificationChannelEnabled(Utils.LOCATION_SERVICE_CHANNEL_ID)) {
//            Toast.makeText(this, "채널을 생성하지 못했습니다.\n앱을 종료합니다.", Toast.LENGTH_SHORT).show();
//            mHandler.postDelayed(this::finish, 2000);
//        }

        requestPermissions();

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



