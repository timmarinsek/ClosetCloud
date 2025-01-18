package com.example.cloudcloset.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cloudcloset.PhotoAdapter;
import com.example.cloudcloset.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private Spinner spinnerCategory;
    private GridView gridView;
    private PhotoAdapter photoAdapter;

    private Button btnSelect, btnDeleteSelected, btnCategorizeSelected;
    private LinearLayout selectActions;

    // Toggle for multi-select
    private boolean selectMode = false;

    // Example categories (same as subfolders)
    private String[] categories = {
            "pants", "shirts", "skirts", "suits", "hoodies", "boots", "shoes", "heels", "undefined"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Find Views
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        gridView = view.findViewById(R.id.gridView);
        btnSelect = view.findViewById(R.id.btnSelect);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        btnCategorizeSelected = view.findViewById(R.id.btnCategorizeSelected);
        selectActions = view.findViewById(R.id.selectActions);

        // Set up Spinner with the categories
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // When user picks a category from the Spinner
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                // Load photos from the chosen category
                String selectedCat = categories[position];
                loadPhotos(selectedCat);

                // Whenever we switch categories, also turn off select mode
                if (selectMode) {
                    toggleSelectMode(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        // "Select" button toggles selectMode
        btnSelect.setOnClickListener(v -> {
            // Toggle
            toggleSelectMode(!selectMode);
        });

        // "Delete Selected" button
        btnDeleteSelected.setOnClickListener(v -> {
            if (photoAdapter != null) {
                photoAdapter.deleteSelectedPhotos();
            }
        });

        // "Categorize Selected" button
        btnCategorizeSelected.setOnClickListener(v -> {
            if (photoAdapter != null) {
                showCategoryDialogForMultiple();
            }
        });

        return view;
    }

    /**
     * Enable or disable multi-select mode in the UI and in the adapter.
     */
    private void toggleSelectMode(boolean enable) {
        selectMode = enable;
        if (selectMode) {
            // Enter select mode
            btnSelect.setText("Cancel");
            selectActions.setVisibility(View.VISIBLE);
            if (photoAdapter != null) {
                photoAdapter.setSelectMode(true);
            }
        } else {
            // Exit select mode
            btnSelect.setText("Select");
            selectActions.setVisibility(View.GONE);
            if (photoAdapter != null) {
                photoAdapter.setSelectMode(false);
            }
        }
    }

    /**
     * Shows a dialog to pick a category for the selected photos.
     */
    private void showCategoryDialogForMultiple() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select category")
                .setItems(categories, (dialog, which) -> {
                    String selectedCategory = categories[which];
                    if (photoAdapter != null) {
                        photoAdapter.categorizeSelectedPhotos(selectedCategory);
                    }
                })
                .show();
    }

    /**
     * Loads all files from the chosen subfolder (category) and shows them in the GridView.
     */
    private void loadPhotos(String category) {
        File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            Toast.makeText(requireContext(), "Pictures directory not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If category is NOT "undefined", just load from that subfolder
        if (!"undefined".equals(category)) {
            File categoryDir = new File(picturesDir, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }
            List<File> photoFiles = getPhotoFiles(categoryDir);

            if (photoFiles.isEmpty()) {
                Toast.makeText(requireContext(), "No items in " + category, Toast.LENGTH_SHORT).show();
                clearOrSetEmptyAdapter();
            } else {
                updateAdapterData(photoFiles);
            }
        }
        // If category IS "undefined", find files that do not belong to known categories
        else {
            List<File> undefinedFiles = new ArrayList<>();

            // (1) Grab all files in the top-level of picturesDir that are not directories
            File[] topLevelFiles = picturesDir.listFiles();
            if (topLevelFiles != null) {
                for (File f : topLevelFiles) {
                    if (f.isFile()) {
                        // This is a file directly under picturesDir, so it's undefined
                        undefinedFiles.add(f);
                    }
                }
            }

            // (2) Scan subfolders. If a folder name is NOT one of the known categories,
            //     we treat its contents as undefined.
            File[] allFiles = picturesDir.listFiles();
            if (allFiles != null) {
                for (File folder : allFiles) {
                    if (folder.isDirectory()) {
                        // Check if this directory is a known category
                        if (!isKnownCategory(folder.getName())) {
                            // It's not known, so everything in it is 'undefined'
                            undefinedFiles.addAll(getPhotoFiles(folder));
                        }
                    }
                }
            }

            // Show or update the adapter
            if (undefinedFiles.isEmpty()) {
                Toast.makeText(requireContext(), "No undefined items found.", Toast.LENGTH_SHORT).show();
                clearOrSetEmptyAdapter();
            } else {
                updateAdapterData(undefinedFiles);
            }
        }
    }

    /**
     * Helper to check if a folder name is in our known categories (including 'undefined' itself).
     */
    private boolean isKnownCategory(String folderName) {
        for (String cat : categories) {
            if (cat.equals(folderName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to get all files from a given folder (optionally filter by extension).
     */
    private List<File> getPhotoFiles(File directory) {
        List<File> photoFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    // Optionally check extension:
                    // if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png")) ...
                    photoFiles.add(f);
                }
            }
        }
        return photoFiles;
    }

    /**
     * Clears or sets an empty adapter if no photos exist.
     */
    private void clearOrSetEmptyAdapter() {
        if (photoAdapter != null) {
            photoAdapter.clear();
        } else {
            // If adapter was never created, create one with empty list
            photoAdapter = new PhotoAdapter(requireContext(), new ArrayList<>());
            gridView.setAdapter(photoAdapter);
        }
    }

    /**
     * Updates the existing adapter or creates a new one with the given list of files.
     */
    private void updateAdapterData(List<File> photos) {
        if (photoAdapter == null) {
            photoAdapter = new PhotoAdapter(requireContext(), photos);
            gridView.setAdapter(photoAdapter);
        } else {
            photoAdapter.setPhotos(photos);
        }
    }

}


