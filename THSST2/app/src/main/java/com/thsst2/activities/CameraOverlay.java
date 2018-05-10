package com.thsst2.activities;

/**
 * Type: Activity
 * CameraOverlay is the custom camera made solely
 * for the purpose of this project. It has an overlay
 * which guides the user on how to properly capture
 * the paper form.
 */

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.thsst2.R;
import com.thsst2.processes.FieldDetector;
import com.thsst2.processes.FormDetector;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraOverlay extends AppCompatActivity implements SurfaceHolder.Callback {

    //TODO: Get school ID intent

    //Properties
    Bitmap croppedBmp;
    Uri photoUri;
    String path;

    SurfaceView  cameraView,transparentView;
    SurfaceHolder holder,holderTransparent;
    Camera camera;
    int  deviceHeight,deviceWidth;
    LayoutInflater controlInflater = null;

    int startRectX,startRectY, endRectX, endRectY, rectWidth, rectHeight;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (SurfaceView)findViewById(R.id.CameraView);

        holder = cameraView.getHolder();
        holder.addCallback((SurfaceHolder.Callback) this);
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraView.setSecure(true);
        // Create second surface with another holder (holderTransparent)
        transparentView = (SurfaceView)findViewById(R.id.TransparentView);
        holderTransparent = transparentView.getHolder();
        holderTransparent.addCallback((SurfaceHolder.Callback) this);
        holderTransparent.setFormat(PixelFormat.TRANSLUCENT);
        transparentView.setZOrderMediaOverlay(true);
        //getting the device heigth and width
        deviceWidth=getScreenWidth();
        deviceHeight=getScreenHeight();

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        Button captureButton = (Button) findViewById(R.id.takepicture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, picture);
            }
        });
    }

    //This method gets the screen width of the device in use.
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    //This method gets the screen height of the device in use.
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    //This method draws the guide over the camera preview.
    private void Draw() {
        Canvas canvas = holderTransparent.lockCanvas(null);
        Paint  paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        int x0 = canvas.getWidth()/2;
        int y0 = canvas.getHeight()/2;
        double dx = canvas.getWidth()/2.5;
        double dy = canvas.getHeight()/2.55;

        //this.topLeft = (int) (x0-dx);
        //this.bottomRight
        int left = (int) (x0-dx);
        int right = (int) (x0+dx);
        int top = (int) (y0-dy);
        int bottom = (int) (y0+dy);

        Rect rec = new Rect(left,top,right,bottom);
        //rec.
        this.startRectX = left;
        this.startRectY = top;
        this.endRectX = right;
        this.endRectY = bottom;
        this.rectHeight = rec.height();
        this.rectWidth = rec.width();  //rec.width();
        canvas.drawRect(rec,paint);
        holderTransparent.unlockCanvasAndPost(canvas);
    }

    //This method handles the action performed after taking a picture.
    Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null)
                return;
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix mat = new Matrix();
                mat.postRotate(90);  // angle is the desired angle you wish to rotate
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);

                int bitWidth =  bitmap.getWidth();
                int bitHeight = bitmap.getHeight();

                double preWidth = cameraView.getWidth();
                double preHeight = cameraView.getHeight();
                int startX = (int)(startRectX * bitWidth / preWidth);
                int startY = (int)(startRectY * bitHeight / preHeight);
                int endX = (int)(endRectX * bitWidth / (preWidth*1.16));
                int endY =(int)(endRectY * bitHeight / (preHeight *1.125));

                croppedBmp = Bitmap.createBitmap(bitmap, (int)startX, (int)startY, (int)endX, (int)endY);

                FormDetector fd = new FormDetector();
                Mat dest = fd.extract(croppedBmp);
                FieldDetector field = new FieldDetector(0);
                Bitmap dest2 = field.analyze(dest);
                //PaperFormManager.getInstance().summarize();

                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                dest2.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
                byte[] bitmapdata = blob.toByteArray();

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();

                Toast.makeText(CameraOverlay.this, "Done", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

   /* public void cropImage(){
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+
                "/MyCameraApp"+"/Test1.jpg");
        Log.e("Bitmap: ", " " + bitmap.getHeight());

        // 2.2 Rotate the bitmap to be the same as display, if need.
        Matrix mat = new Matrix();
        mat.postRotate(90);  // angle is the desired angle you wish to rotate
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);

        // 2.3 Size of rotated bitmap
        int bitWidth = bitmap.getWidth();
        int bitHeight = bitmap.getHeight();

        // 3. Size of camera preview on screen
        int preWidth = cameraView.getWidth();
        int preHeight = cameraView.getHeight();

        // 4. Scale it.
        // Assume you draw Rect as "canvas.drawRect(60, 50, 210, 297, paint);" command
        int startx = this.startRectX * bitWidth / preWidth;
        int starty = this.startRectY * bitHeight / preHeight;
        int endx = this.endRectX * bitWidth / preWidth;
        int endy = this.endRectY * bitHeight / preHeight;

        // 5. Crop image
        Bitmap greenArea = Bitmap.createBitmap(bitmap, startx, starty, endx, endy);
        //Bitmap greenArea = Bitmap.createBitmap(bitmap, startx, starty, rectWidth,rectHeight);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        greenArea.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs , blob);
        byte[] bitmapdata = blob.toByteArray();


        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
           // return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bitmapdata);
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }

    }*/

    //This method gets the media file created.
    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
       /* mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "Test1.jpg");*/

        return mediaFile;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            synchronized (holder)
            {Draw();}   //call a draw method
            camera = Camera.open(); //open a camera
        }

        catch (Exception e) {
            Log.i("Exception", e.toString());
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(90);
        }

        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }

        catch (Exception e) {
            return;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(); //call method for refresh camera
    }

    //This method refreshes the camera preview.
    public void refreshCamera() {
        if (holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        }
        catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release(); //for release a camera
    }
}
