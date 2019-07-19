package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bytedance.androidcamp.network.dou.model.Video;

import java.util.List;
import java.util.Random;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class VideoActivity extends AppCompatActivity {
    private static final int UPDATE_INTERVAL = 100;
    private long mLastUpdateTime;
    private  List<Video> myVideos;
    private float mLastX, mLastY, mLastZ;
    //摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感
    private int shakeThreshold = 1200;
    static public String nexturl;
    private String[] permissions = new String[] {

    };
    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("url", url);
        intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //指定监听的传感器类型//all为全部，ACCELEROMETER为加速度，ORIENTATION为方向
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    long currentTime = System.currentTimeMillis();
                    long diffTime = currentTime - mLastUpdateTime;
                    if (diffTime < UPDATE_INTERVAL) {
                        return;
                    }
                    mLastUpdateTime = currentTime;
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    float deltaX = x - mLastX;
                    float deltaY = y - mLastY;
                    float deltaZ = z - mLastZ;
                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;
                    float delta = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / diffTime * 10000);
                    int min=0;
                    myVideos=FragmentB.mVideos.size()>FragmentC.mVideos.size()?FragmentB.mVideos:FragmentC.mVideos;
                    int max =myVideos.size();
                    Random random = new Random();
                    int num ;
                    num = random.nextInt(max)%(max-min+1) + min;
                    if (delta > shakeThreshold) {
                        VideoActivity.launch(VideoActivity.this,myVideos.get(num).getVideoUrl());
                        finish();
                    }
                }
            }
            @Override
            public void onAccuracyChanged (Sensor sensor,int accuracy){
            }

        }, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        setContentView(R.layout.activity_video);
        String url = getIntent().getStringExtra("url");
        VideoView videoView = findViewById(R.id.video_container);
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }
}
