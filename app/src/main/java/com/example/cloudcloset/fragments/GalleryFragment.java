package com.example.cloudcloset.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cloudcloset.PhotoAdapter;
import com.example.cloudcloset.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private GridView gridView;
    private PhotoAdapter photoAdapter;
    private Spinner spinnerCategory;

    // List of subfolder names
    private String[] categories = {"pants", "shirts", "skirts", "suits", "hoodies", "undefined"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Initialize GridView
        gridView = view.findViewById(R.id.gridView);

        // Initialize Spinner
        spinnerCategory = view.findViewById(R.id.spinnerCategory);

        // Set up Spinner with category list
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Listen for spinner item selection
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Reload photos from the selected category
                String selectedCategory = categories[position];
                loadPhotos(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });

        return view;
    }

    /**
     * Load photos from the specified category subfolder.
     */
    private void loadPhotos(String category) {
        // 1. Get the parent directory (e.g. .../Android/data/<package>/files/Pictures)
        File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            Toast.makeText(requireContext(), "Pictures directory not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. If category is NOT "undefined", proceed normally
        if (!category.equals("undefined")) {
            File categoryDir = new File(picturesDir, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }
            // Load photos from that subfolder
            List<File> photos = getPhotoFiles(categoryDir);

            if (photos.isEmpty()) {
                Toast.makeText(requireContext(), "No items in " + category, Toast.LENGTH_SHORT).show();
                if (photoAdapter != null) {
                    photoAdapter.clear();
                }
            } else {
                updateAdapter(photos);
            }
        }
        // 3. If category IS "undefined", find files that aren’t in the known subfolders
        else {
            // We'll gather any files that are directly under picturesDir (not in subfolders),
            // plus any subfolders that are not in the known categories list
            List<File> allUndefinedFiles = new ArrayList<>();

            // (a) Add all files directly in picturesDir
            //     (Make sure we skip directories)
            File[] topLevelFiles = picturesDir.listFiles();
            if (topLevelFiles != null) {
                for (File f : topLevelFiles) {
                    if (f.isFile()) {
                        // It's a file directly in the picturesDir, so "undefined"
                        allUndefinedFiles.add(f);
                    }
                }
            }

            // (b) Check every subfolder, if it's not one of the known categories,
            //     treat it as "undefined"
            File[] subFolders = picturesDir.listFiles();
            if (subFolders != null) {
                for (File folder : subFolders) {
                    if (folder.isDirectory() && !isKnownCategory(folder.getName())) {
                        // Add all files from this folder
                        allUndefinedFiles.addAll(getPhotoFiles(folder));
                    }
                }
            }

            if (allUndefinedFiles.isEmpty()) {
                Toast.makeText(requireContext(), "No undefined items found.", Toast.LENGTH_SHORT).show();
                if (photoAdapter != null) {
                    photoAdapter.clear();
                }
            } else {
                updateAdapter(allUndefinedFiles);
            }
        }
    }

    // Helper method to check if a subfolder matches a known category
    private boolean isKnownCategory(String folderName) {
        for (String cat : categories) {
            if (cat.equals(folderName)) {
                return true;
            }
        }
        return false;
    }

    // Helper to get a list of image files from a given folder
    private List<File> getPhotoFiles(File directory) {
        List<File> photos = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    // Optionally, check extension:
                    // if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png")) ...
                    photos.add(f);
                }
            }
        }
        return photos;
    }

    // Helper to handle adapter logic in one place
    private void updateAdapter(List<File> photos) {
        if (photoAdapter == null) {
            photoAdapter = new PhotoAdapter(requireContext(), photos);
            gridView.setAdapter(photoAdapter);
        } else {
            photoAdapter.setPhotos(photos);  // Replaces data and refreshes
        }
    }

}
