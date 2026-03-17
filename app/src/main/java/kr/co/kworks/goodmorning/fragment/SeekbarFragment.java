package kr.co.kworks.goodmorning.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.FragmentSeekbarBinding;

public class SeekbarFragment extends Fragment {
    private Handler mHandler;
    private FragmentSeekbarBinding binding;
    private int beforeProgress;

    public SeekbarFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_seekbar, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
        beforeProgress = 0;
        setSeekbar(binding.seekbar);
    }

    private void setSeekbar(SeekBar _seekbar) {
        if (_seekbar == null) {
            Log.i("this", "the argument _seekbar is null");
            return;
        }

        _seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (getContext() == null) return;
                int seekerColor = i == 0 ? ContextCompat.getColor(getContext(), R.color.blue_2563EB) : ContextCompat.getColor(getContext(), R.color.orange_c47e09);
                binding.seeker.setActiveColor(seekerColor);
                if (beforeProgress + 20 < i) binding.seekbar.setProgress(0);
                else {
//                    printSeekBarDragAndSeeker(seekBar);
                    beforeProgress = i;
                    printDraggedSeeker(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                beforeProgress = seekBar.getProgress();
                mHandler.post(() -> {
                    binding.seeker.setActiveColor(ContextCompat.getColor(getContext(), R.color.orange_c47e09));
                });
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress < 70) {
                    setSeekerToZero();
                } else {
                    setSeekerToMax();
                }
            }
        });
    }

    private void printDraggedSeeker(int progress) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.seeker.getLayoutParams();
        int width = binding.seekbar.getWidth();
        params.horizontalBias = width * progress * 0.01f / width;
        mHandler.post(() -> {
            binding.seeker.setLayoutParams(params);
        });
    }

    private void setSeekerToZero() {
        int progress = binding.seekbar.getProgress();
        while (progress > 0) {
            progress -= 1;
            progress = Math.max(progress, 0);
            int finalProgress = progress;
            mHandler.post(() -> {
                binding.seekbar.setProgress(finalProgress);
            });
        }
    }

    private void setSeekerToMax() {
        int progress = binding.seekbar.getProgress();
        while (progress < 100) {
            progress += 1;
            progress = Math.min(progress, 100);
            int finalProgress = progress;
            mHandler.post(() -> {
                binding.seekbar.setProgress(finalProgress);
            });
        }
    }

}
