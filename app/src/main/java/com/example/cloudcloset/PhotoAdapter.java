package com.example.cloudcloset;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private List<File> photos;

    // Multi-select fields
    private boolean selectMode = false;
    private Set<File> selectedPhotos = new HashSet<>();

    public PhotoAdapter(Context context, List<File> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Replace adapter data. */
    public void setPhotos(List<File> newPhotos) {
        this.photos = newPhotos;
        // Clear selections if any
        selectedPhotos.clear();
        notifyDataSetChanged();
    }

    /** Clear all data from adapter. */
    public void clear() {
        photos.clear();
        selectedPhotos.clear();
        notifyDataSetChanged();
    }

    /** Enable/disable multi-selection mode. */
    public void setSelectMode(boolean enable) {
        this.selectMode = enable;
        if (!enable) {
            // Clear selections if turning off
            selectedPhotos.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * Delete all selected photos from storage + adapter.
     */
    public void deleteSelectedPhotos() {
        if (selectedPhotos.isEmpty()) {
            Toast.makeText(context, "No photos selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        int deleteCount = 0;
        // Copy them so we don't modify the set while iterating
        List<File> toRemove = new ArrayList<>(selectedPhotos);

        for (File photoFile : toRemove) {
            if (photoFile.delete()) {
                photos.remove(photoFile);
                deleteCount++;
            }
        }
        selectedPhotos.clear();
        notifyDataSetChanged();

        Toast.makeText(context,
                "Deleted " + deleteCount + " photos.",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Move all selected photos to a new category folder.
     */
    public void categorizeSelectedPhotos(String category) {
        if (selectedPhotos.isEmpty()) {
            Toast.makeText(context, "No photos selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            Toast.makeText(context, "No Pictures directory.", Toast.LENGTH_SHORT).show();
            return;
        }

        File categoryDir = new File(picturesDir, category);
        if (!categoryDir.exists()) {
            categoryDir.mkdirs();
        }

        int moveCount = 0;
        List<File> toRemove = new ArrayList<>(selectedPhotos);

        for (File photoFile : toRemove) {
            File newFile = new File(categoryDir, photoFile.getName());
            // renameTo() moves the file
            if (photoFile.renameTo(newFile)) {
                photos.remove(photoFile);
                moveCount++;
            }
        }
        selectedPhotos.clear();
        notifyDataSetChanged();

        Toast.makeText(context,
                "Moved " + moveCount + " photos to " + category,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate layout if needed
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_photo, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageViewItem);
        File photoFile = photos.get(position);

        // Use Glide for smoother loading (make sure you have placeholder.png or remove .placeholder())
        Glide.with(context)
                .load(Uri.fromFile(photoFile))
                .placeholder(R.drawable.placeholder)
                .into(imageView);

        // If we are in selectMode, tapping toggles selection
        convertView.setOnClickListener(v -> {
            if (selectMode) {
                toggleSelection(photoFile);
            } else {
                // Maybe open photo in full screen, or do nothing
                Toast.makeText(context, "Clicked: " + photoFile.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Highlight selected items (smaller scale, lower alpha, or custom background, etc.)
        if (selectedPhotos.contains(photoFile)) {
            convertView.setScaleX(0.95f);
            convertView.setScaleY(0.95f);
            convertView.setAlpha(0.7f);
        } else {
            convertView.setScaleX(1.0f);
            convertView.setScaleY(1.0f);
            convertView.setAlpha(1.0f);
        }

        return convertView;
    }

    /**
     * Toggle selection state for one photo.
     */
    private void toggleSelection(File photoFile) {
        if (selectedPhotos.contains(photoFile)) {
            selectedPhotos.remove(photoFile);
        } else {
            selectedPhotos.add(photoFile);
        }
        notifyDataSetChanged();
    }
}
