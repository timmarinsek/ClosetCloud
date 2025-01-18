package com.example.cloudcloset;

public class SlotSelection {
    private String category;   // e.g. "pants", "shirts"
    private String filePath;   // the chosen image path

    public SlotSelection(String category, String filePath) {
        this.category = category;
        this.filePath = filePath;
    }

    public String getCategory() { return category; }
    public String getFilePath() { return filePath; }

    public void setFilePath(String path) {
        this.filePath = path;
    }
}

