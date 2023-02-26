package com.example.mexpense.fragments.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mexpense.databinding.FragmentCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private FragmentCameraBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private final String TAG = "CameraFragment";
    private final int REQUEST_CODE_PERMISSIONS = 10;
    private String[] REQUIRED_PERMISSIONS;
    private String imgName;
    private Bitmap imgBitmap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        app.getSupportActionBar().hide();
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }else {
            REQUIRED_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
        }

        if(allPermissionsGranted()) {
            startCamera();
        }else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        binding.btnImageCapture.setOnClickListener(view -> takePhoto());
        binding.btnBack.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(requireView());
            if(imgBitmap != null) {
                saveImage();
                navController.getPreviousBackStackEntry().getSavedStateHandle().set("img_name", imgName);
            }
            navController.navigateUp();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
        return binding.getRoot();
    }

    private void takePhoto() {
        if(imageCapture == null) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Log.i(TAG, "Image Proxy Format: " + image.getFormat());
                imgBitmap = convertImageProxyToBitmap(image);
                rotateImage();
                binding.imgPreview.setImageBitmap(imgBitmap);
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Error capturing image => " + exception);
            }
        });
    }

    private void rotateImage() {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imgBitmap = Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);
        }catch (Exception e) {
            Log.e(TAG, "Error rotating image => " + e);
        }
    }

    private void saveImage() {
        try {
            File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis());
            imgName = name + ".jpg";
            File file = new File(storageDir.getPath(), imgName);
            FileOutputStream fOut = new FileOutputStream(file);
            boolean isSuccess = imgBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            Log.i(TAG, isSuccess ? "Image saved at " + file.getPath() : "Error compressing bitmap to image file");
            fOut.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image => " + e);
        }
    }

    private Bitmap convertImageProxyToBitmap(ImageProxy image) {
        try {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byteBuffer.rewind();
            byte[] bytes = new byte[byteBuffer.capacity()];
            byteBuffer.get(bytes);
            byte[] clonedBytes = bytes.clone();
            return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
        }catch (Exception e) {
            Log.e(TAG, "Error converting image proxy to bitmap => " + e);
            return null;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private boolean allPermissionsGranted() {
        for(String p : REQUIRED_PERMISSIONS) {
            boolean isGranted = requireActivity().checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED;
            if(!isGranted) return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.i(TAG, "Permission Granted!");
                startCamera();
            } else {
                Log.i(TAG, "Permission Denied!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}