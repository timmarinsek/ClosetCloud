package com.example.cloudcloset.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Initialize GridView
        gridView = view.findViewById(R.id.gridView);

        // Load photos
        List<File> photos = getPhotos();
        if (photos.isEmpty()) {
            Toast.makeText(requireContext(), "Ni oblačil za prikaz.", Toast.LENGTH_SHORT).show();
        } else {
            photoAdapter = new PhotoAdapter(requireContext(), photos);
            gridView.setAdapter(photoAdapter);
        }

        return view;
    }

    private List<File> getPhotos() {
        File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] files = directory != null ? directory.listFiles() : null;
        List<File> photos = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("")) {
                    photos.add(file);
                }
            }
        }
        return photos;
    }
}