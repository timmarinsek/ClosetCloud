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
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.cloudcloset.Outfit;
import com.example.cloudcloset.OutfitListAdapter;
import com.example.cloudcloset.OutfitRepository;
import com.example.cloudcloset.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerOutfitsForDay;
    private OutfitListAdapter adapter;

    private long selectedDateMillis; // store the currently selected date

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        recyclerOutfitsForDay = view.findViewById(R.id.recyclerOutfitsForDay);

        // Default selected date is "today"
        selectedDateMillis = calendarView.getDate();

        // Setup RecyclerView
        recyclerOutfitsForDay.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OutfitListAdapter(new ArrayList<>());
        recyclerOutfitsForDay.setAdapter(adapter);

        // When user changes date on CalendarView
        calendarView.setOnDateChangeListener((viewCalendar, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            selectedDateMillis = c.getTimeInMillis();

            // Update UI
            loadOutfitsForDate(selectedDateMillis);
        });

        // Load today's outfits
        loadOutfitsForDate(selectedDateMillis);

        return view;
    }

    private void loadOutfitsForDate(long dateMillis) {
        List<Outfit> outfitsForDay = OutfitRepository.getOutfitsForDate(dateMillis);
        adapter.setData(outfitsForDay);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH)+1;
        int y = cal.get(Calendar.YEAR);
        tvSelectedDate.setText("Selected date: " + d + "/" + m + "/" + y);
    }
}
