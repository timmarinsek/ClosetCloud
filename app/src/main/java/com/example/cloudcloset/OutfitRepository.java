package com.example.cloudcloset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OutfitRepository {
    private static List<Outfit> outfits = new ArrayList<>();

    public static void addOutfit(Outfit outfit) {
        outfits.add(outfit);
    }

    public static void removeOutfit(Outfit outfit) {
        outfits.remove(outfit);
    }

    // Return all outfits or filter by date
    public static List<Outfit> getAllOutfits() {
        return outfits;
    }

    public static List<Outfit> getOutfitsForDate(long dateMillis) {
        // Filter by date (same day)
        // We'll do a simple "day" match example
        List<Outfit> result = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTimeInMillis(dateMillis);
        for (Outfit outfit : outfits) {
            cal2.setTimeInMillis(outfit.getDateMillis());
            if (isSameDay(cal1, cal2)) {
                result.add(outfit);
            }
        }
        return result;
    }

    // Helper to check if two Calendar objects have the same day/month/year
    private static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}

