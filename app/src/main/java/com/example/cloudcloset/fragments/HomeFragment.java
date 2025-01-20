package com.example.cloudcloset.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cloudcloset.Outfit;
import com.example.cloudcloset.OutfitListAdapter;
import com.example.cloudcloset.OutfitRepository;
import com.example.cloudcloset.R;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerTodaysOutfits;
    private OutfitListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerTodaysOutfits = view.findViewById(R.id.recyclerTodaysOutfits);
        recyclerTodaysOutfits.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get today's date in millis
        Calendar cal = Calendar.getInstance();
        long todayMillis = cal.getTimeInMillis();

        // Fetch outfits for today
        List<Outfit> outfitsForToday = OutfitRepository.getOutfitsForDate(todayMillis);

        // Create & set adapter
        adapter = new OutfitListAdapter(outfitsForToday);
        recyclerTodaysOutfits.setAdapter(adapter);

        return view;
    }
}
