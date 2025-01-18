package com.example.cloudcloset.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cloudcloset.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TryOutFragment extends Fragment {

    private ImageView imageView, imageViewResult;
    private ViewFlipper viewFlipper;
    private Button btnNext, btnPrevious, btnTryOn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_try_out, container, false);

        imageView = view.findViewById(R.id.imageViewYou);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        viewFlipper = view.findViewById(R.id.viewFlipper);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnTryOn = view.findViewById(R.id.btnTryOn);

        // Load the image from the "you" folder
        loadImageFromYouFolder();

        // Load the slider images from the "shirts" folder
        loadShirtImagesToFlipper();

        // Set up button listeners for navigation
        btnNext.setOnClickListener(v -> viewFlipper.showNext());
        btnPrevious.setOnClickListener(v -> viewFlipper.showPrevious());

        // Set up "Try On" button to call the API
        btnTryOn.setOnClickListener(v -> tryOnClothesAPI());

        return view;
    }

    private void loadImageFromYouFolder() {
        File youFolder = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "you");
        if (!youFolder.exists()) {
            youFolder.mkdirs();
        }

        File[] files = youFolder.listFiles();
        if (files != null && files.length > 0) {
            // Load the first image in the folder
            Glide.with(this).load(files[0]).into(imageView);
        } else {
            Toast.makeText(requireContext(), "No images found in 'you' folder.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadShirtImagesToFlipper() {
        List<String> shirtImages = getPathsFromCategory("shirts");

        if (shirtImages.isEmpty()) {
            Toast.makeText(requireContext(), "No shirts found.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String path : shirtImages) {
            ImageView imageView = new ImageView(requireContext());
            Glide.with(this).load(path).into(imageView);
            imageView.setTag(path);  // Save the path as a tag on each ImageView
            viewFlipper.addView(imageView);
        }
    }

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

    private void tryOnClothesAPI() {
        String humanImagePath = getFirstImagePath("you");
        String clothingImagePath = getCurrentShirtImagePath();

        if (humanImagePath == null || clothingImagePath == null) {
            Toast.makeText(requireContext(), "Please ensure images are available in both 'you' and 'shirts' folders.", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("task_type", "async")
                .addFormDataPart("person_image", new File(humanImagePath).getName(), RequestBody.create(new File(humanImagePath), mediaType))
                .addFormDataPart("clothes_image", new File(clothingImagePath).getName(), RequestBody.create(new File(clothingImagePath), mediaType))
                .addFormDataPart("clothes_type", "upper_body")
                .build();

        Request request = new Request.Builder()
                .url("https://www.ailabapi.com/api/portrait/editing/try-on-clothes")
                .post(body)
                .addHeader("ailabapi-api-key", "6QK1YpC7Qu2W0GXOrb4ZycICASNh8o0faEo32DeFsWxn8TK6EThXcJHrdFjMLl5e")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "API call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    String taskId = parseTaskIdFromResponse(responseBody);
                    queryTaskResults(taskId);
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "API call unsuccessful: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private String parseTaskIdFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            // Extract task_id from the response
            return jsonResponse.getString("task_id"); // Adjust according to the actual response structure
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void queryTaskResults(String taskId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.ailabapi.com/api/common/query-async-task-result?task_id=" + taskId) // Query using task_id
                .get()
                .addHeader("ailabapi-api-key", "6QK1YpC7Qu2W0GXOrb4ZycICASNh8o0faEo32DeFsWxn8TK6EThXcJHrdFjMLl5e")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to get task results: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if(jsonResponse.getInt("task_status")==2)
                        {
                            String imageUrl = parseImageUrlFromResponse(responseBody);
                            requireActivity().runOnUiThread(() -> {
                                displayResultImage(imageUrl); // Ta klic zdaj teče na glavnem Thread-u
                            });
                        }else {
                            Thread.sleep(500);
                            queryTaskResults(taskId);
                        }

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to get results: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String parseImageUrlFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Extract image URL from the response
            if (jsonResponse.getInt("task_status") == 2) { // Check if the task is completed
                JSONObject data = jsonResponse.getJSONObject("data");
                return data.getString("image"); // Return the image URL
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void displayResultImage(String imageUrl) {
        if (imageUrl != null) {
            ImageView resultImageView = getView().findViewById(R.id.imageViewYou);
            Glide.with(this).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(resultImageView);
        } else {
            Toast.makeText(requireContext(), "No image found in the response.", Toast.LENGTH_SHORT).show();
        }
    }


    private String getFirstImagePath(String folderName) {
        File folder = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            return files[0].getAbsolutePath();
        }
        return null;
    }

    private String getCurrentShirtImagePath() {
        // Get the currently displayed shirt image in the ViewFlipper
        int currentIndex = viewFlipper.getDisplayedChild();
        if (currentIndex != -1) {
            ImageView currentImageView = (ImageView) viewFlipper.getChildAt(currentIndex);
            String imagePath = (String) currentImageView.getTag(); // Retrieve the path stored in the tag
            return imagePath;
        }
        return null;
    }
}
