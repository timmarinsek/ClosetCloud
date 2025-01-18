package com.example.cloudcloset.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
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

    // A list of "slot holders"
    private final List<SlotViewHolder> slotHolders = new ArrayList<>();

    // Example categories
    private final String[] categories = {"shirts", "pants", "boots", "hoodies"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_wardrobe, container, false);

        slotsContainer = view.findViewById(R.id.slotsContainer);
        btnAddSlot = view.findViewById(R.id.btnAddSlot);
        btnSaveOutfit = view.findViewById(R.id.btnSaveOutfit);

        // By default, add 2 slots (one for top, one for bottom).
        addSlot("shirts");
        addSlot("pants");

        btnAddSlot.setOnClickListener(v -> {
            // Add a new slot with some default category, e.g. "boots"
            addSlot("boots");
        });

        btnSaveOutfit.setOnClickListener(v -> {
            // Gather all selections, ensure each has a selected file
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
            // Now show date picker or do your outfit saving
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

        // Add the slotView to the container
        slotsContainer.addView(slotView);

        // Keep track of this slot
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
                    // store outfit
                    OutfitRepository.addOutfit(outfit);
                    Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // We'll define an inner class to represent each slot's UI and state
    private class SlotViewHolder {
        View rootView;
        Spinner spinnerCategory;
        RecyclerView recyclerView;
        Button btnRemoveSlot;

        String currentCategory;
        String selectedFilePath; // updated when user selects a file

        public SlotViewHolder(View slotView) {
            this.rootView = slotView;
            spinnerCategory = slotView.findViewById(R.id.spinnerCategory);
            recyclerView = slotView.findViewById(R.id.recyclerSlotImages);
            btnRemoveSlot = slotView.findViewById(R.id.btnRemoveSlot);

            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

            btnRemoveSlot.setOnClickListener(v -> {
                // remove from parent
                slotsContainer.removeView(rootView);
                // remove from our list
                slotHolders.remove(this);
            });
        }

        public void setupSpinner(String[] allCategories, String defaultCat) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, allCategories);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(spinnerAdapter);

            // set default selection if found
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
                    String cat = allCategories[position];
                    currentCategory = cat;
                    loadCategoryImages(cat);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // set initial
            currentCategory = defaultCat;
        }

        public void loadCategoryImages(String category) {
            // just like we did with getPathsFromCategory(...)
            List<String> imagePaths = getPathsFromCategory(category);
            // create adapter
            ImageAdapter adapter = new ImageAdapter(imagePaths, path -> {
                selectedFilePath = path;
            });
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Copy the same logic from earlier to load file paths from a category subfolder.
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
}

