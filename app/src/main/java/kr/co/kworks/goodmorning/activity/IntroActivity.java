package kr.co.kworks.goodmorning.activity;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ActivityIntroBinding;
import kr.co.kworks.goodmorning.model.business_logic.Wise;
import kr.co.kworks.goodmorning.service.GoodmorningService;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.FCMManager;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.Utils;


/**
 * 채널생성 -> 1차 권한 요청 -> 2차 권한요청 -> RequestOverlayPermission -> nextPage
 */
public class IntroActivity extends AppCompatActivity {
    private AtomicBoolean out;
    private Context mContext;
    private Handler mHandler;
    private ActivityIntroBinding binding;
    private ActivityResultLauncher<Intent> mManageAppAllFiles;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> nextPageScheduled;
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
        executor = Executors.newScheduledThreadPool(1);
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
            db.insert(Column.wise, new Wise("두려움에 사로잡힌 사람은 항상 계산하고, 계획하고, 준비하고, 보호하려 든다. 이런 식으로 살다 보면 인생 전체를 망치게 된다.", "오쇼 라즈니쉬").getContentValues());
            db.insert(Column.wise, new Wise("치즈 없이 지내는 것보다는 미로 속을 헤매는 것이 더 안전하다.", "스펜서 존슨").getContentValues());
            db.insert(Column.wise, new Wise("가장 중요한 싸움은 자기 자신을 이기는 싸움이다.","야니").getContentValues());
            db.insert(Column.wise, new Wise("친절이 타인에게 미치는 가장 큰 영향은 그들 스스로도 친절해지게 만든다는 점이다.","어밀리아 에어하트").getContentValues());
            db.insert(Column.wise, new Wise("천천히 성장하는 것을 두려워마라. 제자리에 머무르는 것을 두려워하라.","중국 속담").getContentValues());
            db.insert(Column.wise, new Wise("자신의 행복은 스스로 책임져야 하며, 절대 다른 사람에게 맡기지 마라.","로이 T. 베넷").getContentValues());
            db.insert(Column.wise, new Wise("위대한 재능은 위대한 의지 없이는 존재할 수 없다.","오노레 드 발자크").getContentValues());
            db.insert(Column.wise, new Wise("우리가 목표에 도달하지 못하게 막는 것은 장애물이 아니라, 더 낮은 목표로 향하는 명확한 길이다.","바가바드 기타").getContentValues());
            db.insert(Column.wise, new Wise("인생은 아껴두는 것이 아니라 써내려가는 것이다.","D. H. 로렌스").getContentValues());
            db.insert(Column.wise, new Wise("다수가 잘못에 동참한다고 해서 그 잘못이 잘못이 아니게 되는 것은 아니다.","레오 톨스토이").getContentValues());
            db.insert(Column.wise, new Wise("분노, 후회, 걱정, 원한에 시간을 낭비하지 말라. 인생은 불행하게 보내기에는 너무 짧다.","로이 T. 베넷").getContentValues());
            db.insert(Column.wise, new Wise("매일 매 순간이 형언할 수 없이 완벽한 기적이다.","월트 휘트먼").getContentValues());
            db.insert(Column.wise, new Wise("말하지 못한 이야기를 마음속에 간직하는 것보다 더 큰 고통은 없다.","마야 앤젤루").getContentValues());
            db.insert(Column.wise, new Wise("어리석은 자는 자신을 남으로 보지만, 지혜로운 자는 남을 자신으로 본다.","도겐").getContentValues());
            db.insert(Column.wise, new Wise("고통은 오래가지 않고 사라지면 성장이라는 결과물이 남는다.","카말 라비칸트").getContentValues());
            db.insert(Column.wise, new Wise("나는 인생에서 뒤로 걸어가지 않을 것이다.","J. R. R. 톨킨").getContentValues());
            db.insert(Column.wise, new Wise("호랑이는 양의 의견 때문에 잠을 설치지 않는다.","샤히르 자그").getContentValues());
            db.insert(Column.wise, new Wise("과거와 현재에 대한 책임을 더 많이 질수록, 원하는 미래를 더 잘 만들어낼 수 있다.","셀레스틴 추아").getContentValues());
            db.insert(Column.wise, new Wise("당신이 하게 될 가장 창의적인 행위는 자신을 만들어가는 행위이다.","디팍 초프라").getContentValues());
            db.insert(Column.wise, new Wise("자신 외에는 그 무엇도 평화를 가져다줄 수 없다.","데일 카네기").getContentValues());
            db.insert(Column.wise, new Wise("성공으로 가는 길과 실패로 가는 길은 거의 똑같다.","콜린 R. 데이비스").getContentValues());
            db.insert(Column.wise, new Wise("최고의 싸움꾼은 절대 화를 내지 않는다.","노자").getContentValues());
            db.insert(Column.wise, new Wise("쾌락에만 몰두하는 삶보다 더 불쾌한 삶은 생각할 수 없다.","존 D. 록펠러").getContentValues());
            db.insert(Column.wise, new Wise("실수를 전혀 하지 않는 사람은 아무것도 하지 않는 사람뿐이다.","시어도어 루스벨트").getContentValues());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        createChannels();
//        requestPermissions();
        startNextPageSchedule();
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
        return neededPermissions;
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createNotificationChannel(FCMManager.CHANNEL_ID, NotificationManager.IMPORTANCE_MAX);
            createNotificationChannel(Utils.GOODMORNING_SERVICE_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            createNotificationChannel(FCMManager.CHANNEL_ID, 1);
            createNotificationChannel(Utils.GOODMORNING_SERVICE_CHANNEL_ID, 1);
        }
//        requestPermissions();
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
            requestOverlayPermission();
            return;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissions, 2425);
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivity(intent);
            } else {
                // 이미 허용됨
                startOverlayWork();
            }
        } else {
            // Android 6 미만은 별도 체크 없이 진행
            startOverlayWork();
        }
    }

    private void startOverlayWork() {
        // 오버레이 관련 작업 시작
        startNextPageSchedule();
    }

    private void stopNextPageSchedule() {
        if (nextPageScheduled != null && !nextPageScheduled.isCancelled()) nextPageScheduled.cancel(true);
    }

    private void startNextPageSchedule() {
        stopNextPageSchedule();
        nextPageScheduled = executor.schedule(() -> {
            mHandler.post(this::nextPage);
        }, 2_000, TimeUnit.MILLISECONDS);

    }
}



