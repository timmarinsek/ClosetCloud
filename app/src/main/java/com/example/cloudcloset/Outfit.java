package com.example.cloudcloset;

import java.util.List;

public class Outfit {
    private List<SlotSelection> slots;
    private long dateMillis;   // date in milliseconds since epoch

    public Outfit(List<SlotSelection> slots, long dateMillis) {
        this.slots = slots;
        this.dateMillis = dateMillis;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public List<SlotSelection> getSlots() {
        return slots;
    }

    public void setDateMillis(long timeInMillis) {
        this.dateMillis = timeInMillis;
    }



    //setters

}

