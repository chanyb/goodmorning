package kr.co.kworks.goodmorning.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.activity.IntroActivity;

public class FCMManager extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "fcm_channel";
    Context context;

    private Database database;

    public FCMManager() {
        database = new Database();
    }

    public FCMManager(Context context) {
        this.context = context;
        subscribeToTopic("smart_agri_mach");
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Logger.getInstance().error("Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        database.setPushToken(token);
                        Logger.getInstance().info(token);
                    }
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Logger.getInstance().info("onMessageReceived");
        Logger.getInstance().info("From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("message");

            sendNotification(remoteMessage);
        }
    }


    private void sendNotification(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("message");
            try {
                Logger.getInstance().info("sendNotification.title: " + title);
                Logger.getInstance().info("sendNotification.body: " + body);

                Logger.getInstance().info("sendNotification.noti.title: " + remoteMessage.getNotification().getTitle());
                Logger.getInstance().info("sendNotification.noti.body: " + remoteMessage.getNotification().getBody());
                Logger.getInstance().info("sendNotification.messageId: " + remoteMessage.getMessageId());
            } catch (Exception e) {
                Logger.getInstance().error("sendNotification", e);
            }

            if (title == null) {
                title = "타이틀";
                body = "바디";
            }

            Intent intent = new Intent(this, IntroActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String channelId  = CHANNEL_ID;
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.icon)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9, notificationBuilder.build());
        }

    }

    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

}
