package com.example.cloudcloset;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private List<File> photos;

    public PhotoAdapter(Context context, List<File> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageViewItem);
        File photo = photos.get(position);
        imageView.setImageURI(Uri.fromFile(photo));

        return convertView;
    }
}
