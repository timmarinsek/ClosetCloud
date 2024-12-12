package com.example.cloudcloset;

import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryActivity extends AppCompatActivity {
    private GridView gridView;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        gridView = findViewById(R.id.gridView);

        List<File> photos = getPhotos();
        if (photos.isEmpty()) {
            Toast.makeText(this, "Ni oblačil za prikaz.", Toast.LENGTH_SHORT).show();
        } else {
            photoAdapter = new PhotoAdapter(this, photos);
            gridView.setAdapter(photoAdapter);
        }
    }

    private List<File> getPhotos() {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] files = directory.listFiles();
        List<File> photos = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("CloudCloset.jpg")) {
                    photos.add(file);
                }
            }
        }
        return photos;
    }
}
