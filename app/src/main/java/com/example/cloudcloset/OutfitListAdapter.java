package com.example.cloudcloset;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

public class OutfitListAdapter extends RecyclerView.Adapter<OutfitListAdapter.ViewHolder> {

    private List<Outfit> outfits;

    public OutfitListAdapter(List<Outfit> outfits) {
        this.outfits = outfits;
    }

    public void setData(List<Outfit> newList) {
        this.outfits = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return outfits.size();
    }

    @NonNull
    @Override
    public OutfitListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_for_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitListAdapter.ViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);

        // Each outfit has multiple slots, displayed in a nested recycler
        List<SlotSelection> slotList = outfit.getSlots();

        // Nested RecyclerView for the slots
        SlotAdapter slotAdapter = new SlotAdapter(slotList);
        holder.rvSlots.setLayoutManager(
                new LinearLayoutManager(
                        holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );
        holder.rvSlots.setAdapter(slotAdapter);

        // DELETE: remove from repository & adapter
        holder.btnDeleteOutfit.setOnClickListener(v -> {
            // 1) Remove from repository (so it won't come back on reloading date)
            OutfitRepository.removeOutfit(outfit);

            // 2) Remove from local adapter list (UI)
            outfits.remove(position);
            notifyItemRemoved(position);
        });

        // MOVE: change date, remove from current date's list
        holder.btnMoveOutfit.setOnClickListener(v -> {
            // Show a DatePicker to pick new date
            Context context = holder.itemView.getContext();
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(context,
                    (datePicker, year, month, dayOfMonth) -> {
                        // 1) Build new date
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);
                        long newDateMillis = chosen.getTimeInMillis();

                        // 2) Update outfit's date
                        outfit.setDateMillis(newDateMillis);

                        // 3) Because the outfit no longer belongs to this date, remove from adapter
                        outfits.remove(position);
                        notifyItemRemoved(position);

                        Toast.makeText(context,
                                "Outfit moved to " + (month+1) + "/" + dayOfMonth,
                                Toast.LENGTH_SHORT).show();
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvSlots;
        Button btnMoveOutfit, btnDeleteOutfit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rvSlots = itemView.findViewById(R.id.rvSlots);
            btnMoveOutfit = itemView.findViewById(R.id.btnMoveOutfit);
            btnDeleteOutfit = itemView.findViewById(R.id.btnDeleteOutfit);
        }
    }
}



