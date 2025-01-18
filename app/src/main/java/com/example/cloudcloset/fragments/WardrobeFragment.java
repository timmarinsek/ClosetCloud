package com.example.cloudcloset.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudcloset.ImageAdapter;
import com.example.cloudcloset.Outfit;
import com.example.cloudcloset.OutfitRepository;
import com.example.cloudcloset.R;
import com.example.cloudcloset.SlotSelection;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WardrobeFragment extends Fragment {

    private LinearLayout slotsContainer;
    private Button btnAddSlot, btnSaveOutfit;

    // Keep track of each slot's view holder
    private final List<SlotViewHolder> slotHolders = new ArrayList<>();

    // Example categories = subfolder names
    private final String[] categories = {"shirts", "pants", "boots", "hoodies"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_wardrobe, container, false);

        slotsContainer = view.findViewById(R.id.slotsContainer);
        btnAddSlot = view.findViewById(R.id.btnAddSlot);
        btnSaveOutfit = view.findViewById(R.id.btnSaveOutfit);

        // By default, add 2 slots
        addSlot("shirts");


        // Add extra slot on click
        btnAddSlot.setOnClickListener(v -> {
            addSlot("boots");
        });

        // Save outfit -> gather selected items, show date picker
        btnSaveOutfit.setOnClickListener(v -> {
            List<SlotSelection> allSlots = new ArrayList<>();
            for (SlotViewHolder holder : slotHolders) {
                if (holder.selectedFilePath == null) {
                    Toast.makeText(requireContext(),
                            "Please select an item in each slot.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                allSlots.add(new SlotSelection(holder.currentCategory, holder.selectedFilePath));
            }
            showDatePicker(allSlots);
        });

        return view;
    }

    /**
     * Dynamically add a new slot row for a given category (or default).
     */
    private void addSlot(String initialCategory) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View slotView = inflater.inflate(R.layout.layout_slot, slotsContainer, false);

        SlotViewHolder holder = new SlotViewHolder(slotView);
        holder.setupSpinner(categories, initialCategory);
        holder.loadCategoryImages(initialCategory);

        // Attach the slot's view
        slotsContainer.addView(slotView);
        slotHolders.add(holder);
    }

    /**
     * Show a date picker, then create an Outfit with the selected slots for that date.
     */
    private void showDatePicker(List<SlotSelection> slots) {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, dayOfMonth) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth);
                    long dateMillis = chosen.getTimeInMillis();

                    Outfit outfit = new Outfit(slots, dateMillis);
                    OutfitRepository.addOutfit(outfit);
                    Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * Helper to load file paths from a subfolder in Pictures/ (like "pants", "shirts", etc.)
     */
    private List<String> getPathsFromCategory(String categoryName) {
        List<String> results = new ArrayList<>();
        File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) return results;

        File categoryDir = new File(picturesDir, categoryName);
        if (!categoryDir.exists()) {
            categoryDir.mkdirs();
        }

        File[] files = categoryDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    results.add(f.getAbsolutePath());
                }
            }
        }
        return results;
    }

    /**
     * Represents one "slot" with:
     * - Spinner for category
     * - Remove button
     * - < / > arrow buttons
     * - RecyclerView (horizontal)
     */
    private class SlotViewHolder {
        View rootView;
        Spinner spinnerCategory;
        RecyclerView recyclerView;
        Button btnRemoveSlot;
        Button btnPreviousImage, btnNextImage;

        String currentCategory;
        String selectedFilePath;

        private LinearLayoutManager layoutManager;
        private ImageAdapter imageAdapter;

        public SlotViewHolder(View slotView) {
            this.rootView = slotView;

            spinnerCategory = slotView.findViewById(R.id.spinnerCategory);
            recyclerView = slotView.findViewById(R.id.recyclerSlotImages);
            btnRemoveSlot = slotView.findViewById(R.id.btnRemoveSlot);

            btnPreviousImage = slotView.findViewById(R.id.btnPreviousImage);
            btnNextImage = slotView.findViewById(R.id.btnNextImage);

            // Horizontal layout
            layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);

            // Remove slot
            btnRemoveSlot.setOnClickListener(v -> {
                slotsContainer.removeView(rootView);
                slotHolders.remove(this);
            });

            // Left arrow
            btnPreviousImage.setOnClickListener(v -> {
                Log.d("WardrobeFragment", "Left arrow clicked in category=" + currentCategory);
                scrollPrevious();
            });

            // Right arrow
            btnNextImage.setOnClickListener(v -> {
                Log.d("WardrobeFragment", "Right arrow clicked in category=" + currentCategory);
                scrollNext();
            });
        }

        public void setupSpinner(String[] allCategories, String defaultCat) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, allCategories);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(spinnerAdapter);

            // set default selection
            int index = 0;
            for (int i = 0; i < allCategories.length; i++) {
                if (allCategories[i].equals(defaultCat)) {
                    index = i;
                    break;
                }
            }
            spinnerCategory.setSelection(index);

            // Listen for changes
            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentCategory = allCategories[position];
                    // Clear old selection
                    selectedFilePath = null;
                    // Reload images
                    loadCategoryImages(currentCategory);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            currentCategory = defaultCat;
        }

        public void loadCategoryImages(String category) {
            List<String> imagePaths = getPathsFromCategory(category);
            imageAdapter = new ImageAdapter(imagePaths, path -> {
                selectedFilePath = path;
            });
            recyclerView.setAdapter(imageAdapter);

            Log.d("WardrobeFragment", "Loaded " + imagePaths.size() + " images for category=" + category);
        }

        private void scrollPrevious() {
            int currentPos = layoutManager.findFirstVisibleItemPosition();
            int newPos = currentPos - 1;
            if (newPos >= 0) {
                recyclerView.smoothScrollToPosition(newPos);
            } else {
                Log.d("WardrobeFragment", "No more images to the left (currentPos=" + currentPos + ")");
            }
        }

        private void scrollNext() {
            int currentPos = layoutManager.findLastVisibleItemPosition();
            int newPos = currentPos + 1;
            if (newPos < recyclerView.getAdapter().getItemCount()) {
                recyclerView.smoothScrollToPosition(newPos);
            } else {
                Log.d("WardrobeFragment", "No more images to the right (currentPos=" + currentPos + ")");
            }
        }
    }
}
