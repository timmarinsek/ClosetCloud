package com.example.cloudcloset;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {

    private List<SlotSelection> slots;

    public SlotAdapter(List<SlotSelection> slots) {
        this.slots = slots;
    }

    @NonNull
    @Override
    public SlotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate a small layout with just 1 ImageView
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotAdapter.ViewHolder holder, int position) {
        SlotSelection slot = slots.get(position);

        // Load the image
        Glide.with(holder.imageView.getContext())
                .load(slot.getFilePath())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSlot);
        }
    }
}

