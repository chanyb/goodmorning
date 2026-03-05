package kr.co.kworks.goodmorning.view_holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ItemAiDetectBinding;

public class AiDetectInfoViewHolder extends RecyclerView.ViewHolder {
    public ItemAiDetectBinding binding;


    public AiDetectInfoViewHolder(ItemAiDetectBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static AiDetectInfoViewHolder from(@NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemAiDetectBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_ai_detect, parent, false);
        return new AiDetectInfoViewHolder(binding);
    }

    public void bind() {
        binding.executePendingBindings();
    }
}
