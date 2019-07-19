package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.GetVideoResponse;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity
{
    private Button mBtnRefresh;
    private static final int REQUEST_EXTERNAL_CAMERA = 101;
    private static final int PAGE_COUNT = 3;
    private FloatingActionButton fab;
    private RecyclerView mRv;
    private String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager pager = (ViewPager)findViewById(R.id.vip);
        fab = findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isPermissionsReady(MainActivity.this, permissions)) {
                    //todo 打开摄像机
                    startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
                } else {
                    Utils.reuqestPermissions(MainActivity.this, permissions, REQUEST_EXTERNAL_CAMERA);
                }
            }
        });

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab);

        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch (i){
                    case 0 :return new FragmentA();
                    case 1:
                        mRv=findViewById(R.id.rv);
                        return new FragmentB();
                    case 2:


                        return new FragmentC();
                }
                return new FragmentA();
            }

            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case 0:
                        return "热搜榜";
                    case 1:
                        return "最新速览";
                    case 2:
                        return "随机推荐";
                }
                return "ZQS" + position;
            }
        });
        tabLayout.setupWithViewPager(pager);
    }
    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        // TODO 10: get videos & update recycler list
        Call<GetVideoResponse> call = getMiniDouyinService().getVideos();
        call.enqueue(new Callback<GetVideoResponse>() {
            @Override
            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response) {

                if(response.body()!=null &&response.body().getVideos()!= null) {
                    FragmentB.mVideos = response.body().getVideos();
                }
                mRv.getAdapter().notifyDataSetChanged();
                mBtnRefresh.setText("refresh");
                mBtnRefresh.setEnabled(true);
            }

            @Override
            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                mBtnRefresh.setText(R.string.refresh_feed);
                mBtnRefresh.setEnabled(true);
                Toast.makeText(MainActivity.this,throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(MainActivity.this, "TODO 10: get videos & update recycler list"+FragmentB.mVideos.size(), Toast.LENGTH_SHORT).show();
    }
    public IMiniDouyinService getMiniDouyinService() {
        if (FragmentB.retrofit == null) {
            FragmentB.retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (FragmentB.miniDouyinService == null) {
            FragmentB. miniDouyinService = FragmentB.retrofit.create(IMiniDouyinService.class);
        }
        return FragmentB.miniDouyinService;
    }
}
