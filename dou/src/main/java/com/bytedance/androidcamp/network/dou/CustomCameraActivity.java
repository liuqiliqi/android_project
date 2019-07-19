package com.bytedance.androidcamp.network.dou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.bytedance.androidcamp.network.dou.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static com.bytedance.androidcamp.network.dou.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.androidcamp.network.dou.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.androidcamp.network.dou.Utils.getOutputMediaFile;

public class CustomCameraActivity extends Activity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = CAMERA_FACING_BACK;
    private boolean isRecording = false;
    private File videoFile;
    private  int isflash;

    private int rotationDegree = 0;
    private Camera.PictureCallback mPictureCallback;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mSurfaceView = findViewById(R.id.img);
//        mCamera = getCamera(0);
//        mCamera.setDisplayOrientation(getCameraDisplayOrientation(0));
        mCamera = getCamera(CAMERA_TYPE);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {


                startPreview(surfaceHolder);
                //change  注释了原来的部分,startPreview可以用统一下沉

//                try {
//                    mCamera.setPreviewDisplay(surfaceHolder);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                //change  surface改变的时候,也可以用getCamera
                mCamera = getCamera(CAMERA_TYPE);
                startPreview(surfaceHolder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseMediaRecorder();
                releaseCameraAndPreview();
//                change 下面注释的代码,这个可以移到release camera中,surface destoryed同时,也需要释放mediaRecord
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
            }
        });
        findViewById(R.id.zoom).setOnClickListener(v -> {
            //todo 调焦，需要判断手机是否支持
            Camera.AutoFocusCallback mAutoFocusCallback;
            mAutoFocusCallback = new Camera.AutoFocusCallback() {

                public void onAutoFocus(boolean success, Camera camera) {
                    mCamera.setOneShotPreviewCallback(null);
                    Toast.makeText(CustomCameraActivity.this,"聚焦成功" , Toast.LENGTH_SHORT).show();

                }
            };
            mCamera.autoFocus(mAutoFocusCallback);
        });
        findViewById(R.id.flash).setOnClickListener(v -> {
            if(CAMERA_TYPE == 1){
                Toast.makeText(CustomCameraActivity.this,
                        "前置摄像头不能设置闪光" , Toast.LENGTH_SHORT).show();
            }
            else{
                if(isflash == 0){
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(parameters);
                    isflash = 1;
                    Toast.makeText(CustomCameraActivity.this,
                            "闪光灯已开启" , Toast.LENGTH_SHORT).show();
                }
                else{
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    Toast.makeText(CustomCameraActivity.this,
                            "闪光灯已关闭" , Toast.LENGTH_SHORT).show();
                    isflash = 0;
                }
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            mCamera.takePicture(null, null, mPicture);
            TimerTask task = new TimerTask() {
                public void run() {
                    mCamera.startPreview();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 2000);
            Toast.makeText(CustomCameraActivity.this, "got and saved!", Toast.LENGTH_LONG).show();

        });

        findViewById(R.id.btn_record).setOnClickListener(v -> {

            if (isRecording) {
                //todo 停止录制
                isRecording = false;
                releaseMediaRecorder();
                //changed 下沉至  releaseMediaRecorder();
//                mMediaRecorder.stop();
//                mMediaRecorder.reset();
//                mMediaRecorder.release();
//                mMediaRecorder=null;
//                mCamera.lock();
            } else {
                //todo 录制
                prepareVideoRecorder();
                isRecording = true;

            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            releaseMediaRecorder();
            releaseCameraAndPreview();
            if (CAMERA_TYPE == CAMERA_FACING_FRONT) {
                CAMERA_TYPE = CAMERA_FACING_BACK;
            } else {
                CAMERA_TYPE = CAMERA_FACING_FRONT;
            }
            mCamera = getCamera(CAMERA_TYPE);
            startPreview(surfaceHolder);

            //changed mediaRecorder只有在用的时候才需要setCamera,如果正在录制的话,也需要stop recorder
//            mCamera.stopPreview();
//            mCamera.release();
//            if (CAMERA_TYPE == CAMERA_FACING_FRONT) {
//                CAMERA_TYPE = CAMERA_FACING_BACK;
//                mCamera = Camera.open(CAMERA_FACING_BACK);
//                if (mMediaRecorder != null) {
//                    mMediaRecorder.setCamera(mCamera);
//                }
//            } else {
//                CAMERA_TYPE = CAMERA_FACING_FRONT;
//                mCamera = Camera.open(CAMERA_FACING_FRONT);
//                if (mMediaRecorder != null) {
//                    mMediaRecorder.setCamera(mCamera);
//                }
//            }
        });


    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        //change  补全camera 对previewsize的设定
        Camera.Parameters parameters = cam.getParameters();
        size = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(),mSurfaceView.getWidth(),mSurfaceView.getHeight());
        parameters.setPreviewSize(size.width,size.height);
        cam.setParameters(parameters);  rotationDegree = getCameraDisplayOrientation(position);
        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        mCamera.release();

    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setDisplayOrientation(getCameraDisplayOrientation(0));
        mCamera.startPreview();
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mCamera.unlock();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        //tochanged QUALITY_HIGH 中,可能有些摄像头的VideoSize是不支持的,需要用params 设置的预览分辨率,作为size.width 和height
        mMediaRecorder.setVideoSize(size.width,size.height);
        mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }


    private void releaseMediaRecorder() {
        //changed  下沉到这里
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        Log.i("Permission", "hel");
        if (pictureFile == null) {
            Log.i("Permission", "hello!!!!!!!");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Log.i("Permission", "hello!!");
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
