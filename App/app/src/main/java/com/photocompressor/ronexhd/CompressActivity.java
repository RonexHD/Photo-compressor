package com.photocompressor.ronexhd;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

public class CompressActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 1;
    ImageView originalImg;
    TextView originalSize, compressedSize, qualityText;
    SeekBar seekBar;
    Button btnSelect, btnCompress;
    File originalImage;
    private static String filePath;
    File path= new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "myCompressed");
    private Object Manifest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        originalImg=findViewById(R.id.imageView);
        originalSize=findViewById(R.id.original_size_txt);
        compressedSize=findViewById(R.id.compressed_size_txt);
        qualityText=findViewById(R.id.quality_txt);
        seekBar=findViewById(R.id.seekbar);
        btnSelect=findViewById(R.id.select_btn);
        btnCompress=findViewById(R.id.compress_btn);

        filePath=path.getAbsolutePath();

        if(!path.delete()){
            path.mkdirs();
        }

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
                openGallery();
            }
        });



    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_IMAGE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            btnCompress.setVisibility(View.VISIBLE );
            compressedSize.setVisibility(View.VISIBLE);
            qualityText.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            originalSize.setVisibility(View.VISIBLE);

            final Uri imageUri = data.getData();
            try {

                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                originalImg.setImageBitmap(selectedImage);
                originalImage = new File(imageUri.getPath());
                originalSize.setText(MessageFormat.format("Size: {0}", Formatter.formatShortFileSize(this, originalImage.length())));


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }

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