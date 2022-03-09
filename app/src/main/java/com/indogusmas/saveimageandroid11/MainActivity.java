package com.indogusmas.saveimageandroid11;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gzeinnumer.gzndirectory.helper.FGPermission;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import com.indogusmas.saveimageandroid11.databinding.ActivityMainBinding;
import com.indogusmas.saveimageandroid11.helper.FileCompressor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

public class MainActivity extends AppCompatActivity implements PickiTCallbacks  {


    private static final int REQUEST_TAKE_PHOTO = 20;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 21;
    private static final int REQUEST_TAKE_PHOTO_CAMERA = 23;
    private ActivityMainBinding binding;
    private FileCompressor fileCompressor;
    private PickiT pickiT;
    private String TAG = getClass().getSimpleName();
    File mPhotoAsset;

    private final PermissionEnum[] permissions = new PermissionEnum[]{
            PermissionEnum.WRITE_EXTERNAL_STORAGE,
            PermissionEnum.READ_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pickiT = new PickiT(this, this, this);
        fileCompressor = new FileCompressor(this);
        /**
         * Untuk path bisa dicustom, tpi supaya dibisa diakses dari applikasi sendiri.
         * Saran dimasukkan ke path folder public contoh Download, DCIM,PICTURE
         * Atau juga bisa dimasukan ke path internal App
         * com.package.
         * com.indogusmas.saveimageandroid11
         */

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        Log.d(TAG, "onCreate: "+ path);
        fileCompressor.setDestinationDirectoryPath(path);
        onClick();
        FGPermission.checkPermissions(this, permissions);
        checkPermissions();
    }
    private void checkPermissions() {
        boolean isAllGranted = FGPermission.getPermissionResult(this, permissions);
        if (isAllGranted) {
            Toast.makeText(this, "Permission All Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
        }
    }

    private void onClick() {
        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        binding.btnGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);

                mPhotoAsset = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        /**
         * File temporary janga lupa dihpus wkkwkw
         */
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }


    private void openGallery() {
        /**
         * Ketika open gallery Logic yang dilakukan
         * Pilih file yang akan diambil
         * Dapatkan Real File
         * Copy ke folder internal
         * Jangan lupa dihpus setelah selesai digunakan wkwkwkkw
         * referensi library
         * implementation 'com.github.HBiSoft:PickiT:2.0.2'
         */
        if (checkSelfPermission()) {
            Intent intent;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
            }
            //  In this example we will set the type to video
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra("return-data", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
        }
        if (requestCode == REQUEST_TAKE_PHOTO_CAMERA) {
            try {
                mPhotoAsset = fileCompressor.compressToFile(mPhotoAsset);
                Glide.with(MainActivity.this)
                        .load(mPhotoAsset)
                        .into(binding.image);
                binding.tvPath.setText(mPhotoAsset.getPath());
            } catch (IOException e) {
                Log.e(TAG, "onActivityResult: "+ e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    @Override
    public void PickiTonUriReturned() {
        
    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        Log.d(TAG, "PickiTonCompleteListener: "+ path);
        binding.tvPath.setText(path);
        Glide.with(getApplicationContext())
                .load(path)
                .into(binding.image);
    }

    @Override
    public void PickiTonMultipleCompleteListener(ArrayList<String> paths, boolean wasSuccessful, String Reason) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(this, requestCode, permissions, grantResults);
        checkPermissions();
    }
}