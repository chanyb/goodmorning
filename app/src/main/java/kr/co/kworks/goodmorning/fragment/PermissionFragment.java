package kr.co.kworks.goodmorning.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.FragmentPermissionBinding;
import kr.co.kworks.goodmorning.viewmodel.Event;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;

/**
 * 채널생성 -> 1차 권한 요청 -> 2차 권한요청(Detail) -> RequestOverlayPermission -> end
 */

public class PermissionFragment extends Fragment {

    private FragmentPermissionBinding binding;
    private GlobalViewModel globalViewModel;
    private Handler mHandler;

    public PermissionFragment() {}

    public interface Listener{
        void onComplete();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permission, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void init() {
        mHandler = new Handler(Looper.getMainLooper());
        globalViewModel = new ViewModelProvider(requireActivity()).get(GlobalViewModel.class);
    }

    private ArrayList<String> getNeededPermissions() {
        ArrayList<String> neededPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // POST_NOTIFICATION
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        return neededPermissions;
    }

    public void requestPermissions() {
        ArrayList<String> list = getNeededPermissions();
        if(list.isEmpty()) {
            // 권한 요청 끝남
            return;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(requireActivity(), permissions, 2424);

    }

    public void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireActivity())) {
                Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireActivity().getPackageName())
                );
                startActivity(intent);
            } else {
                // 이미 허용됨
            }
        } else {
            // Android 6 미만은 별도 체크 없이 진행
        }
    }



}
