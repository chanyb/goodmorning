package kr.co.kworks.goodmorning.view_holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.databinding.ItemFireListBinding;

public class FireInfoViewHolder extends RecyclerView.ViewHolder {
    public ItemFireListBinding binding;


    public FireInfoViewHolder(ItemFireListBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static FireInfoViewHolder from(@NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemFireListBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_fire_list, parent, false);
        return new FireInfoViewHolder(binding);
    }

    public void bind() {
        binding.executePendingBindings();
    }
}
