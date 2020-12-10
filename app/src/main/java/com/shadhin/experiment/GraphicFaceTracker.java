package com.shadhin.experiment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class GraphicFaceTracker extends Tracker<Face> implements SensorEventListener{

    private static final float OPEN_THRESHOLD = 0.85f;
    private static final float CLOSE_THRESHOLD = 0.4f;
    private final MainActivity mainActivity;
    private int state = 0;
    private int blink=0;
    private static final int FORCE_THRESHOLD = 100;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 100;
    private static final int SHAKE_DURATION = 50;
    private static final int SHAKE_COUNT = 1;

    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;
    private Toast toastMessage;
    private boolean capture=false;
    public interface OnShakeListener {
        public void onShake();
    }

    public void ShakeListener(Context context) {

        Log.d("XXX","ShakeListener invoked---->");
        mContext = context;
        resume();
    }
    public void setOnShakeListener(OnShakeListener listener) {
        Log.d("XXX","ShakeListener setOnShakeListener invoked---->");
        mShakeListener = listener;
    }

    public void resume() {
        mSensorMgr = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = false;
        try {
            supported = mSensorMgr.registerListener(this,
                    mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        } catch (Exception e) {
           // Toast.makeText(mContext, "Shaking not supported", Toast.LENGTH_LONG).show();
        }

        if ((!supported) && (mSensorMgr != null))
            mSensorMgr.unregisterListener(this);
    }

    public void pause() {
        if (mSensorMgr != null) {

            mSensorMgr.unregisterListener(this);
            mSensorMgr = null;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            float speed = Math.abs(event.values[SensorManager.DATA_X]
                    + event.values[SensorManager.DATA_Y]
                    + event.values[SensorManager.DATA_Z] - mLastX - mLastY
                    - mLastZ)
                    / diff * 10000;
            if (speed > FORCE_THRESHOLD) {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    Log.d("XXX","ShakeListener mShakeListener---->"+mShakeListener);
                    blink=0;
                        if (toastMessage == null) {
                            if(capture) {
                            }else {
                                toastMessage= Toast.makeText(mContext, "Do not shake your phone", Toast.LENGTH_SHORT);
                                toastMessage.show();
                            }
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toastMessage=null;
                                }
                            }, 2000);
                        }
                    if (mShakeListener != null) {
                        mShakeListener.onShake();
                    }
                }
                mLastForce = now;
            }
            mLastTime = now;
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
            mLastZ = event.values[SensorManager.DATA_Z];
        }
    }
    GraphicFaceTracker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        ShakeListener(this.mainActivity);
    }

    private void blink(float value) {
        switch (state) {
            case 0:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are initially open
                    state = 1;
                }
                break;
            case 1:
                if (value < CLOSE_THRESHOLD) {
                    // Both eyes become closed
                    state = 2;
                }
                break;
            case 2:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are open again
                    Log.i("Camera Demo", "blink has occurred!");
                    state = 0;

                        blink++;
                        Log.d("XXX"+blink,blink+"");
                    if(blink>3){
                        if (toastMessage!= null) {
                            toastMessage.cancel();
                        }
                       // Log.d("XXX inside"+blink,blink+"");
                        mainActivity.captureImage();
                        capture=true;
                        toastMessage= Toast.makeText(mContext, "Capture", Toast.LENGTH_SHORT);
                        toastMessage.show();
                       // Toast.makeText(mContext, "Capture", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            default:
                break;
        }
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        float left = face.getIsLeftEyeOpenProbability();
        float right = face.getIsRightEyeOpenProbability();
        if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                (right == Face.UNCOMPUTED_PROBABILITY)) {
            // One of the eyes was not detected.
            return;
        }

        float value = Math.min(left, right);
        blink(value);
    }
}