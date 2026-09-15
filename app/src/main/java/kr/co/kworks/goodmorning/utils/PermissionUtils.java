package kr.co.kworks.goodmorning.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public final class PermissionUtils {

    public static final int REQUEST_CODE_RUNTIME_PERMISSIONS = 2424;
    public static final int REQUEST_CODE_BACKGROUND_PERMISSION = 2425;

    private PermissionUtils() {
    }

    @NonNull
    public static ArrayList<String> getNeededPermissions(@NonNull Context context) {
        ArrayList<String> neededPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }



        return neededPermissions;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean isOverlayPermissionGranted(@NonNull Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(context);
    }

    public static boolean isNotificationListenerEnabled(@NonNull Context context) {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.getPackageName());
    }

    /**
     * Requests any missing runtime permissions.
     * @return true when a permission dialog was requested, false when none was needed
     */
    public static boolean requestRuntimePermissions(@NonNull Activity activity) {
        ArrayList<String> neededPermissions = getNeededPermissions(activity);
        if (neededPermissions.isEmpty()) {
            return false;
        }

        ActivityCompat.requestPermissions(
            activity,
            neededPermissions.toArray(new String[0]),
            REQUEST_CODE_RUNTIME_PERMISSIONS
        );
        return true;
    }

    @NonNull
    public static Intent createOverlayPermissionIntent(@NonNull Context context) {
        return new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.getPackageName())
        );
    }

    /**
     * Opens the overlay permission settings when the permission is missing.
     * @return true when the settings screen was opened, false when already granted
     */
    public static boolean requestOverlayPermission(@NonNull Activity activity) {
        if (isOverlayPermissionGranted(activity)) {
            return false;
        }

        activity.startActivity(createOverlayPermissionIntent(activity));
        return true;
    }

    @NonNull
    public static Intent createNotificationListenerPermissionIntent() {
        return new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
    }

    /**
     * Opens notification-listener settings when access is missing.
     * @return true when the settings screen was opened, false when already granted
     */
    public static boolean requestNotificationListenerPermission(@NonNull Activity activity) {
        if (isNotificationListenerEnabled(activity)) {
            return false;
        }

        activity.startActivity(createNotificationListenerPermissionIntent());
        return true;
    }

    @NonNull
    public static int[] getPermissionStatus(@NonNull Context context) {
        // [0] POST_NOTIFICATIONS, [1] READ_PHONE_STATE,
        // [2] overlay, [3] notification listener
        int[] permissionStatus = {1, 1, 1, 1, 1};
        ArrayList<String> neededPermissions = getNeededPermissions(context);

        if (neededPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            permissionStatus[0] = 0;
        }
        if (neededPermissions.contains(Manifest.permission.READ_PHONE_STATE)) {
            permissionStatus[1] = 0;
        }
        if (!isOverlayPermissionGranted(context)) {
            permissionStatus[2] = 0;
        }
        if (!isNotificationListenerEnabled(context)) {
            permissionStatus[3] = 0;
        }
        if (neededPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionStatus[4] = 0;
        }
        if (neededPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionStatus[4] = 0;
        }
        return permissionStatus;
    }

    private static ArrayList<String> getNeededDetailPermission(Context context) {
        ArrayList<String> neededDetailPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                neededDetailPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        return neededDetailPermissions;
    }

    public static boolean requestBackgroundLocation(Activity activity) {
        ArrayList<String> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                list.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
        if(list.isEmpty()) {
            // 권한 요청 끝남
            return false;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_BACKGROUND_PERMISSION);
        return true;
    }
}
