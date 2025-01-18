package com.example.cloudcloset;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * ImageAdapter that supports single selection with the ability to unselect
 * when clicking the same item again.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    public interface OnItemClickListener {
        // Pass the new selection (or null if unselected)
        void onItemClick(String newSelectedPath);
    }

    private List<String> imagePaths;
    private OnItemClickListener clickListener;

    /**
     * Tracks which image path is currently selected in this slot, or null if none.
     */
    private String selectedPath = null;

    public ImageAdapter(List<String> imagePaths, OnItemClickListener clickListener) {
        this.imagePaths = imagePaths;
        this.clickListener = clickListener;
    }

    /**
     * Optionally call this if you want to explicitly clear or set the selection externally.
     */
    public void setSelectedPath(String path) {
        this.selectedPath = path;
        notifyDataSetChanged();
    }

    /**
     * Returns whichever path is currently selected, or null if none.
     */
    public String getSelectedPath() {
        return selectedPath;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_small, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = imagePaths.get(position);

        // Load the image using Glide
        Glide.with(holder.imageView.getContext())
                .load(path)
                .into(holder.imageView);

        // Highlight if this item is the selected one
        if (path.equals(selectedPath)) {
            // e.g., smaller + more transparent
            holder.itemView.setScaleX(0.95f);
            holder.itemView.setScaleY(0.95f);
            holder.itemView.setAlpha(0.7f);
        } else {
            // normal state
            holder.itemView.setScaleX(1f);
            holder.itemView.setScaleY(1f);
            holder.itemView.setAlpha(1f);
        }

        // On click: toggle if it's already selected
        holder.itemView.setOnClickListener(v -> {
            if (path.equals(selectedPath)) {
                // already selected -> unselect
                selectedPath = null;
            } else {
                // select a new item
                selectedPath = path;
            }

            // Refresh the UI
            notifyDataSetChanged();

            // Let the parent know the new selection (could be null)
            if (clickListener != null) {
                clickListener.onItemClick(selectedPath);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewSmall);
        }
    }
}
