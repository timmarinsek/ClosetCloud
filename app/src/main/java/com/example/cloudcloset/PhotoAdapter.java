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
import java.util.List;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private List<File> photos;

    // List of categories you want to move to
    // You can also fetch this from the fragment if needed.
    private String[] categories = {
            "pants",
            "shirts",
            "skirts",
            "suits",
            "hoodies",
            "undefined"
    };

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

    /**
     * Update the photos list and refresh.
     */
    public void setPhotos(List<File> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    /**
     * Clear all photos from the adapter and refresh.
     */
    public void clear() {
        photos.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflate layout if needed
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_photo, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageViewItem);
        File photoFile = photos.get(position);

        // Use Glide to load the image from the File
        Glide.with(context)
                .load(Uri.fromFile(photoFile))
                .placeholder(R.drawable.placeholder)  // optional placeholder
                .into(imageView);

        // Long-press listener: show dialog to delete or re-categorize
        convertView.setOnLongClickListener(v -> {
            showOptionsDialog(photoFile, position);
            return true; // consume the long-click
        });

        return convertView;
    }

    /**
     * Show an options dialog on long press: Delete or Categorize
     */
    private void showOptionsDialog(File photoFile, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose an option")
                .setItems(new String[]{"Delete", "Categorize"}, (dialog, which) -> {
                    if (which == 0) {
                        // Delete
                        deletePhoto(photoFile, position);
                    } else if (which == 1) {
                        // Categorize
                        showCategoryDialog(photoFile, position);
                    }
                })
                .show();
    }

    /**
     * Delete the photo from storage and remove from adapter.
     */
    private void deletePhoto(File photoFile, int position) {
        if (photoFile.delete()) {
            photos.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Could not delete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show a dialog with category options to move the file.
     */
    private void showCategoryDialog(File photoFile, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select category")
                .setItems(categories, (dialog, which) -> {
                    String selectedCategory = categories[which];
                    movePhotoToCategory(photoFile, selectedCategory, position);
                })
                .show();
    }

    /**
     * Physically move the file to the chosen category folder.
     */
    private void movePhotoToCategory(File photoFile, String category, int position) {
        // Get the parent Pictures directory
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            Toast.makeText(context, "No Pictures directory.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create or ensure the subfolder
        File categoryDir = new File(picturesDir, category);
        if (!categoryDir.exists()) {
            categoryDir.mkdirs();
        }

        // Move the file
        File newFile = new File(categoryDir, photoFile.getName());
        boolean success = photoFile.renameTo(newFile);
        if (success) {
            // Remove from current adapter list
            photos.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Moved to " + category, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Couldn't move file.", Toast.LENGTH_SHORT).show();
        }
    }
}
