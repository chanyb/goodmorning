package kr.co.kworks.goodmorning.view_holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ItemNoticeBinding;

public class NoticeViewHolder extends RecyclerView.ViewHolder {
    public ItemNoticeBinding binding;


    public NoticeViewHolder(ItemNoticeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static NoticeViewHolder from(@NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemNoticeBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_notice, parent, false);
        return new NoticeViewHolder(binding);
    }

    public void bind() {
        binding.executePendingBindings();
    }
}
