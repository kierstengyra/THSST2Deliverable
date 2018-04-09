package com.thsst2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.thsst2.R;
import com.thsst2.processes.FieldDetector;
import com.thsst2.processes.FormDetector;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by gyra on 02/09/2018.
 */
public class ChooseModule extends AppCompatActivity {
    ImageView background;
    Button btnPSC;
    Button btnDAP;

    int studentID;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri fileURI;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_module);

        if(!OpenCVLoader.initDebug()) {
            OpenCVLoader.initDebug();
//            Log.e("OpenCV", "Should work now lol");
        }

        Intent intent = getIntent();
        this.studentID = intent.getIntExtra("StudentID", -1);

        this.initComponents();
    }

    public void initComponents() {
        this.background = (ImageView) findViewById(R.id.imgBGModule);
        this.background.setScaleType(ImageView.ScaleType.FIT_XY);

        this.btnPSC = (Button) findViewById(R.id.btnPSC);
        this.btnDAP = (Button) findViewById(R.id.btnDAP);
    }

    public void loadPSC(View view) {
        Intent intent = new Intent(this, PSCQuestions.class);
        intent.putExtra("StudentID", this.studentID);
        startActivity(intent);
    }

    public void loadCamera(View view) {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Projectives";
        File imagesFolder = new File(file_path);
        imagesFolder.mkdirs();

        File image = new File(imagesFolder, "test.jpg");
        this.fileURI = Uri.fromFile(image);

        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileURI);
        if(takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap image = (Bitmap) extras.get("data");

            try {
                final InputStream imageStream = getContentResolver().openInputStream(this.fileURI);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                FormDetector fd = new FormDetector();
                Bitmap dest = fd.extract(selectedImage);
                FieldDetector field = new FieldDetector();
                Bitmap dest2 = field.analyze(dest);

                Toast.makeText(this, "Score: "+field.getScore(), Toast.LENGTH_SHORT);

                this.saveToFile(dest2);
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }

//            FieldDetector field = new FieldDetector();
//            Bitmap dest2 = field.analyze(dest);

        }
    }

    private void saveToFile(Bitmap img) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);

        String fname = "Image-"+n+".jpg";
        File file = new File(myDir, fname);

        if(file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();

            Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
