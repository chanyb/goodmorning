package kr.co.kworks.goodmorning.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.FragmentPermissionBinding;

public class PermissionFragment extends Fragment {

    private FragmentPermissionBinding binding;
    private Handler mHandler;

    public PermissionFragment() {
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
        init();
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
    }
}
