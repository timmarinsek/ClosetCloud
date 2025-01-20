package com.example.cloudcloset.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cloudcloset.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A fragment that uses CameraX to show a live preview, capture a photo,
 * and optionally remove the background (API usage).
 */
public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";

    private PreviewView viewFinder;
    private Button btnCapture;
    private ImageView imgCapturePreview;
    private LinearLayout previewControls;
    private Button btnAddToAlbum, btnDiscard;

    private boolean isPreviewVisible = false;
    private ImageCapture imageCapture;      // For taking pictures
    private File photoFile;                 // Last captured file

    // We'll keep track of the cameraProvider so we can unbind in onPause
    private ProcessCameraProvider cameraProvider;
    private boolean isCameraInitialized = false;

    private static final int REQUEST_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        viewFinder = view.findViewById(R.id.viewFinder);
        btnCapture = view.findViewById(R.id.btnCapture);
        imgCapturePreview = view.findViewById(R.id.imgCapturePreview);
        previewControls = view.findViewById(R.id.previewControls);
        btnAddToAlbum = view.findViewById(R.id.btnAddToAlbum);
        btnDiscard = view.findViewById(R.id.btnDiscard);

        btnCapture.setOnClickListener(v -> takePhoto());
        btnAddToAlbum.setOnClickListener(v -> addToAlbum());
        btnDiscard.setOnClickListener(v -> discardPhoto());

        // Check permissions
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // If permissions are granted and not initialized, start
        if (allPermissionsGranted() && !isCameraInitialized) {
            startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called. Unbinding camera.");

        // Unbind camera so it stops
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        isCameraInitialized = false;
    }

    /**
     * Check if we have all required permissions.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Initialize CameraX and bind the camera use cases: Preview + ImageCapture.
     */
    private void startCamera() {
        Log.d(TAG, "startCamera called");

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            if (!isAdded()) {
                Log.d(TAG, "Fragment no longer added, abort camera init");
                return;
            }
            try {
                cameraProvider = cameraProviderFuture.get();

                // Preview UseCase
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // ImageCapture UseCase
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind before rebinding
                cameraProvider.unbindAll();
                // Bind preview + capture
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

                isCameraInitialized = true;
                Log.d(TAG, "Camera initialized");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to start camera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (!isCameraInitialized || imageCapture == null) {
            Toast.makeText(requireContext(), "Camera not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create output file
        photoFile = createImageFile();

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Show preview
                        showCapturedPreview(photoFile);

                        File outputFile = new File(photoFile.getParent(), photoFile.getName());
                        removeBackground(photoFile, outputFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(), "Capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void removeBackground(File inputFile, File outputFile) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image_file", inputFile.getName(),
                        RequestBody.create(inputFile, MediaType.parse("image/jpg")))
                .addFormDataPart("size", "auto")
                .build();

        Request request = new Request.Builder()
                .url("https://api.remove.bg/v1.0/removebg")
                .addHeader("X-Api-Key", "9XfSRLY9WH4yu8yD3XvtH8Ca")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    byte[] imageBytes = response.body().bytes();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(imageBytes);
                    }
                    requireActivity().runOnUiThread(() -> {
                        Uri uri = Uri.fromFile(outputFile);
                        imgCapturePreview.setImageURI(uri);
                    });
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "No response body";
                    int statusCode = response.code();
                    Log.e("RemoveBG", "API error: " + response.message() + " - Status code: " + statusCode + " - " + errorMessage);

                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "API error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Create a temporary file in external pictures folder with timestamp name.
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "CloudCloset_" + timeStamp + ".jpg";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return new File(storageDir, imageFileName);
    }

    /**
     * Show the captured photo in our preview ImageView, hide the camera preview UI.
     */
    private void showCapturedPreview(File photoFile) {
        // Hide the live camera feed & capture button
        viewFinder.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);

        // Show the ImageView and preview controls
        imgCapturePreview.setVisibility(View.VISIBLE);
        previewControls.setVisibility(View.VISIBLE);

        // Display the captured image
        Uri uri = Uri.fromFile(photoFile);
        imgCapturePreview.setImageURI(uri);
    }

    /**
     * User chose to "Add to album" (i.e., keep the photo).
     * If you want them to pick a folder (pants, shirts, etc.), show a dialog or spinner here.
     */
    private void addToAlbum() {
        Toast.makeText(requireContext(), "Photo saved to " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        resetCameraPreview();
    }

    /**
     * User discarded the photo. Delete the file and go back to camera preview.
     */
    private void discardPhoto() {
        if (photoFile != null && photoFile.exists()) {
            boolean deleted = photoFile.delete();
            Log.d(TAG, "discardPhoto: file deleted=" + deleted);
        }
        resetCameraPreview();
    }

    /**
     * Hide the preview UI, show the camera feed again.
     */
    private void resetCameraPreview() {
        // Clear the ImageView
        imgCapturePreview.setImageURI(null);

        // Hide the preview and show the live camera
        imgCapturePreview.setVisibility(View.GONE);
        previewControls.setVisibility(View.GONE);

        viewFinder.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (allPermissionsGranted()) {
                if (isResumed() && !isCameraInitialized) {
                    startCamera();
                }
            } else {
                Toast.makeText(requireContext(), "Permissions not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
