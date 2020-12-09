package com.shadhin.experiment.custom_camera;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.shadhin.experiment.ExifUtils;
import com.shadhin.experiment.R;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import jp.wasabeef.picasso.transformations.CropTransformation;

import static android.Manifest.permission.CAMERA;

public class CustomCameraActivity extends AppCompatActivity {

    private String[] neededPermissions = new String[]{CAMERA};
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private SurfaceHolder surfaceHolder;
    private FaceDetector detector;
    private ImageView iv_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);

        surfaceView = findViewById(R.id.surfaceView);
        iv_picture = findViewById(R.id.iv_picture);
        //surfaceView.setBackgroundResource(R.drawable.middle);
        detector = new FaceDetector.Builder(this)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

        if (!detector.isOperational()) {
            Log.w("MainActivity", "Detector Dependencies are not yet available");
        } else {
            Log.w("MainActivity", "Detector Dependencies are available");
            if (surfaceView != null) {
                boolean result = checkPermission();
                if (result) {
                    setViewVisibility(R.id.tv_capture);
                    setViewVisibility(R.id.surfaceView);
                    setupSurfaceHolder();
                }
            }
        }

        findViewById(R.id.tv_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickImage();
            }
        });
    }

    private boolean checkPermission() {
        ArrayList<String> permissionsNotGranted = new ArrayList<>();
        for (String permission : neededPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (!permissionsNotGranted.isEmpty()) {
            boolean shouldShowAlert = false;
            for (String permission : permissionsNotGranted) {
                shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            }
            if (shouldShowAlert) {
                showPermissionAlert(permissionsNotGranted.toArray(new String[0]));
            } else {
                requestPermissions(permissionsNotGranted.toArray(new String[0]));
            }
            return false;
        }
        return true;
    }

    private void showPermissionAlert(final String[] permissions) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission Required");
        alertBuilder.setMessage("Camea permission is required to move forward.");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(CustomCameraActivity.this, permissions, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(CustomCameraActivity.this, "This permission is required", Toast.LENGTH_LONG).show();
                    checkPermission();
                    return;
                }
            }
            setViewVisibility(R.id.tv_capture);
            setViewVisibility(R.id.surfaceView);
            setupSurfaceHolder();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setViewVisibility(int id) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void setupSurfaceHolder() {
        cameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(CustomCameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(surfaceHolder);
                    detector.setProcessor(new LargestFaceFocusingProcessor(detector,
                            new Tracker<Face>()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    private void clickImage() {
        if (cameraSource != null) {
            cameraSource.takePicture(/*shutterCallback*/null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                   /* Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ((ImageView) findViewById(R.id.iv_picture)).setImageBitmap(bitmap);
                    setViewVisibility(R.id.iv_picture);
                    findViewById(R.id.surfaceView).setVisibility(View.GONE);
                    findViewById(R.id.tv_capture).setVisibility(View.GONE);*/
                    int orientation = ExifUtils.getOrientation(bytes);
                    Bitmap   bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    switch(orientation) {
                        case 90:
                            bitmap= rotateImage(bitmap, 90);

                            break;
                        case 180:
                            bitmap= rotateImage(bitmap, 180);

                            break;
                        case 270:
                            bitmap= rotateImage(bitmap, 270);

                            break;
                        case 0:
                            // if orientation is zero we don't need to rotate this

                        default:
                            break;
                    }
                    ((ImageView) findViewById(R.id.iv_picture)).setImageBitmap(bitmap);
                    setViewVisibility(R.id.iv_picture);
                    findViewById(R.id.surfaceView).setVisibility(View.GONE);
                    findViewById(R.id.tv_capture).setVisibility(View.GONE);

                    //((ImageView) findViewById(R.id.iv_picture)).setImageBitmap(bitmap);

                    if (bitmap.getWidth() >= bitmap.getHeight()){

                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                bitmap.getWidth()/2 - bitmap.getHeight()/2,
                                0,
                                bitmap.getHeight(),
                                bitmap.getHeight()
                        );

                    }else{

                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                bitmap.getHeight()/2 - bitmap.getWidth()/2,
                                bitmap.getWidth(),
                                bitmap.getWidth()
                        );
                    }
                    getImageUri(CustomCameraActivity.this,bitmap);

                    Uri selectedImageURI = getImageUri(CustomCameraActivity.this,bitmap);;
                    //   Picasso.with(MainActivity.this).load(selectedImageURI).noPlaceholder().centerCrop().fit()
                    //         .into((ImageView) findViewById(R.id.image_display));
                    Picasso.get().load(selectedImageURI).transform(new CropTransformation(2400,1550))
                            //.transform(new CropCircleTransformation())
                            .into(iv_picture);

                    //((ImageView) findViewById(R.id.iv_picture)).setImageBitmap(bitmap);

                }
            });
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        //String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + Calendar.getInstance().getTime(), null);
        return Uri.parse(path);
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),   source.getHeight(), matrix,
                true);
    }
}