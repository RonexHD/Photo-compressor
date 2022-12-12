package com.photocompressor.ronexhd;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.imagecompressor.ImageCompressUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class CompressActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 1;
    ImageView originalImg;
    TextView originalSize, compressedSize, qualityText, default_quality;
    SeekBar seekBar;
    Button btnSelect, btnCompress;
    File originalImage, compressedImage, renameFile;

    private static String filePath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myCompressed");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        originalImg=findViewById(R.id.imageView);
        originalSize=findViewById(R.id.original_size_txt);
        default_quality=findViewById(R.id.default_quality);
        compressedSize=findViewById(R.id.compressed_size_txt);
        qualityText=findViewById(R.id.quality_txt);
        seekBar=findViewById(R.id.seekbar);
        btnSelect=findViewById(R.id.select_btn);
        btnCompress=findViewById(R.id.compress_btn);




        filePath = path.getAbsolutePath();
        if(!path.delete()){
            path.mkdirs();
        }

//        Random generator = new Random();
//        int n = 1000;
//        n = generator.nextInt(n);
//        String fileName = "Compressed-IMG_" + n + ".jpg";
//
//        renameFile = new File(filePath, fileName);
//        Log.i(TAG, "" + renameFile);
//        if (renameFile.exists())
//            renameFile.delete();



        askPermission();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qualityText.setText("Quality: " + progress + "%" );
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


               int quality = seekBar.getProgress();

                if (seekBar.getProgress() == 0 ){
                    quality = 50;
                }


                try {
                    compressedImage = new Compressor(CompressActivity.this)
                            .setMaxWidth(900)
                            .setMaxHeight(1500)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filePath)
                            .compressToFile(new File(originalImage.getAbsolutePath()));
//                            .compressToFile(new File(compressedImage.getAbsolutePath()));

                    compressedSize.setText( MessageFormat.format("Size after Compress: {0}", Formatter.formatShortFileSize(CompressActivity.this, compressedImage.length())));
                    Toast.makeText(CompressActivity.this, "Compressed! Check your Gallery.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CompressActivity.this, "Error: " + e.getMessage() , Toast.LENGTH_SHORT).show();
                }

            }
        });



    }


//    public void compress(){
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "Image_Compressor");
//        myDir.mkdirs();
//        Random generator = new Random();
//        int n = 1000;
//        n = generator.nextInt(n);
//        String fileName = "Compressed_" + n + ".jpg";
//
//        File imageFile = new File(myDir, fileName);
//        Log.i(TAG, "" + imageFile);
//        if (imageFile.exists())
//            imageFile.delete();
//        try {
//            int quality = seekBar.getProgress();
//            InputStream inputStream = getContentResolver().openInputStream();
//            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
//            FileOutputStream out = new FileOutputStream(imageFile);
//            imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out );
//            out.flush();
//            out.close();
//
//        }catch (Exception e ){
//            e.printStackTrace();
//        }
//
//    }



    private void selectImage(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        if (getIntent().resolveActivity(getPackageManager())!= null) {
            startActivityForResult(gallery, RESULT_IMAGE );
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            btnCompress.setVisibility(View.VISIBLE );
            default_quality.setVisibility(View.VISIBLE);
            compressedSize.setVisibility(View.VISIBLE);
            qualityText.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            originalSize.setVisibility(View.VISIBLE);

            if (data != null){
                final Uri selectImageUri = data.getData();
                if (selectImageUri != null ){
                    try {

                        final InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        originalImg.setImageBitmap(bitmap);
                        originalImage = new File(getPathFromUri(selectImageUri));
                        originalSize.setText(MessageFormat.format(" Original Size: {0}", Formatter.formatShortFileSize(this, originalImage.length())));

                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                }
            }

        }else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }

    }

    private String getPathFromUri(Uri contentUri){
        String filepath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null){
            filepath = contentUri.getPath();

        }else {
            cursor.moveToFirst();
            int index =cursor.getColumnIndex("_data");
            filepath =cursor.getString(index);
            cursor.close();
        }
        return filepath;
    }

    private void askPermission() {

        Dexter.withContext(this)
                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
}