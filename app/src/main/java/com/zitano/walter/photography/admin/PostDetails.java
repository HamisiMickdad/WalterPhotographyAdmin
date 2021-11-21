package com.zitano.walter.photography.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetails extends AppCompatActivity {

    MaterialTextView mTitleTv, mDetailTv;
    AppCompatImageView mImageIv;
    Bitmap bitmap;

    Button mSaveBtn, mShareBtn, mWallBtn;

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //initialize views
        mDetailTv = findViewById(R.id.descriptionTv);
        mTitleTv = findViewById(R.id.titleTv);
        mImageIv = findViewById(R.id.imageView);
        mSaveBtn = findViewById(R.id.saveBtn);
        mShareBtn = findViewById(R.id.shareBtn);
        mWallBtn = findViewById(R.id.wallBtn);

        // Get Data from intent
        String image = getIntent().getStringExtra("image");
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("description");

        //set data to views
        mTitleTv.setText(title);
        //mDetailTv.setText(desc);
        Picasso.get().load(image).into(mImageIv);

        PicassoImageGetter imageGetter = new PicassoImageGetter(mDetailTv);
        Spannable html;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html = (Spannable) Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            html = (Spannable) Html.fromHtml(desc, imageGetter, null);
        }
        mDetailTv.setMovementMethod(LinkMovementMethod.getInstance());
        mDetailTv.setText(html);

        //Save button click handle
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if os is >= marshmallow we need runtime permission to save image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //Show pop up to grant permission
                        requestPermissions(permission, WRITE_EXTERNAL_STORAGE_CODE);
                    }
                    else {
                        //permission already granted, save image
                        saveImage();
                    }
                }
                else {
                    //System os is < marshmallow, save image
                    saveImage();
                }

            }
        });

        //Share button click handle
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();

            }
        });

        //share btn click handle
        mWallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImgWallpaper();

            }
        });
    }

    private void setImgWallpaper() {
        //get image from Image View as  bitmap
        bitmap = ((BitmapDrawable)mImageIv.getDrawable()).getBitmap();
        WallpaperManager myWallManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallManager.setBitmap(bitmap);
            Toast.makeText(this, "Wallpaper set...", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void shareImage() {
        try {
            //get image from Image View as  bitmap
            bitmap = ((BitmapDrawable)mImageIv.getDrawable()).getBitmap();
            //get title and description and save to string s
            String s = mTitleTv.getText().toString() + "\n" + mDetailTv.getText().toString();

            File file = new File(getExternalCacheDir(), "sample.jpg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            //intent to share image & text
           /* Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, s);//put the text
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/jpg");
            startActivity(Intent.createChooser(intent, "Share via"));*/
            Intent share = new Intent("android.intent.action.SEND");
            share.putExtra(Intent.EXTRA_TEXT, s);//put the text
            //share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.setType("image/jpeg");
            Uri uri = FileProvider.getUriForFile(PostDetails.this, BuildConfig.APPLICATION_ID+ ".provider", (file));
            share.putExtra("android.intent.extra.STREAM", uri);

            startActivity(Intent.createChooser(share, "Share via"));


        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void saveImage() {
        //get image from Image View as  bitmap
        bitmap = ((BitmapDrawable)mImageIv.getDrawable()).getBitmap();
        //time stamp for image
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        //path to external storage
        //File path = Environment.getExternalStorageDirectory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            //Create a folder named "Mickdad"
            File dir = new File(path+"/Mickdad/");
            //dir.mkdirs();
            if (!dir.exists()){
                dir.mkdirs();
            } else {

            }
            //image name
            String imageName = timestamp + ".JPG";
            File file = new File(dir, imageName);
            OutputStream out;
            try {
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Toast.makeText(this, imageName+"Saved to"+ dir, Toast.LENGTH_SHORT).show();

            }
            catch (Exception e) {
                //failed to save image
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        } else {
            File path = Environment.getExternalStorageDirectory();
            //Create a folder named "Mickdad"
            File dir = new File(path+"/Mickdad/");
            //dir.mkdirs();
            if (!dir.exists()){
                dir.mkdirs();
            } else {

            }
            //image name
            String imageName = timestamp + ".JPG";
            File file = new File(dir, imageName);
            OutputStream out;
            try {
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Toast.makeText(this, imageName+"Saved to"+ dir, Toast.LENGTH_SHORT).show();

            }
            catch (Exception e) {
                //failed to save image
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }


    }
    // Handle back press go back to the previous activity

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_CODE:{
                //if request is cancelled the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //permission is granted, save image
                    saveImage();
                }
                else {
                    //Permission denied
                    Toast.makeText(this, "Allow Permission to save the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}