package com.bytedance.androidcamp.network.dou;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.GetVideoResponse;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.api.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.util.RecycleViewDivider;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class FragmentC extends Fragment {
    public static Retrofit retrofit;
    public static IMiniDouyinService miniDouyinService;
    private SwipeRefreshLayout swiperereshlayout ;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private RecyclerView mLv;
    public static  List<Video> mVideos = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    private static final int REQUEST_EXTERNAL_CAMERA = 101;
    public Button mBtn;
    static private TextView texx;
    private StaggeredGridLayoutManager mLayoutManager;
    private static Context context = null;
    static ShareUtil shareUtil;
    private String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // TODO 8: initialize retrofit & miniDouyinService
    private Gson gson;
    public IMiniDouyinService getMiniDouyinService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (miniDouyinService == null) {
            miniDouyinService = retrofit.create(IMiniDouyinService.class);
        }
        return miniDouyinService;
    }

    private View rootView = null;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context=getActivity();
        rootView = inflater.inflate(R.layout.pbl, container, false);
        initRecyclerView();
        initBtns();
        if (Utils.isPermissionsReady(getActivity(), permissions)) {
        } else {
            Utils.reuqestPermissions(getActivity(), permissions, REQUEST_EXTERNAL_CAMERA);
        }
        shareUtil = new ShareUtil(getContext());
        swiperereshlayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swiperereshlayout);
        swiperereshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Call <GetVideoResponse> call = getMiniDouyinService().getVideos();
                        call.enqueue(new Callback<GetVideoResponse>() {
                            @Override
                            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response) {
                                if(response.body()!=null &&response.body().getVideos()!= null) {
                                    mVideos = response.body().getVideos();
                                    Video vtemp;
                                    int min=0;
                                    int max=mVideos.size();
                                    Random random = new Random();
                                    int num ;
                                    for(int i=0;i<mVideos.size();i++){
                                        num = random.nextInt(max)%(max-min+1) + min;
                                        vtemp=mVideos.get(i);
                                        mVideos.set(i,mVideos.get(num));
                                        mVideos.set(num,vtemp);
                                    }
                                    mRv.getAdapter().notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                            }
                        });
                        swiperereshlayout.setRefreshing(false);
                    }

                });
            }
        });
        Video vtemp;
        int min=0;
        int max=mVideos.size();
        Random random = new Random();
        int num ;
        for(int i=0;i<mVideos.size();i++){
            num = random.nextInt(max)%(max-min+1) + min;
            vtemp=mVideos.get(i);
            mVideos.set(i,mVideos.get(num));
            mVideos.set(num,vtemp);
        }



        return rootView;
    }


    private void initBtns() {
        mBtn = rootView.findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = "
                                + mSelectedVideo
                                + ", mSelectedImage = "
                                + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });


    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            texx = itemView.findViewById(R.id.texx);
        }

        public void bind(final Activity activity, final Video video) {
            texx.setText("author:"+video.getUserName());
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            Uri uri = Uri.parse((String) video.getVideoUrl());
            img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    shareUtil.shareText(null, null, uri.toString(),null,null);
                    return true;
                }
            });
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.launch(activity, video.getVideoUrl());
                }
            });
        }
    }

    private void initRecyclerView() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRv=rootView.findViewById(R.id.rv);
        mRv.setItemAnimator(null);
        mRv.setLayoutManager(mLayoutManager);
        final Paint dividerPaint = new Paint();
        dividerPaint.setColor(0xFFB5C5);
        mRv.setLayoutManager(mLayoutManager);
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.video_item_view, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                ViewGroup.LayoutParams params = viewHolder.img.getLayoutParams();
                params.height=video.getImage_h()*(getResources().getDisplayMetrics().widthPixels/2 - 1)/video.getImage_w();
                viewHolder.bind(getActivity(), video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
        mRv.addItemDecoration(new RecycleViewDivider(this.getContext(), LinearLayoutManager.VERTICAL, 10,0xFF98FB98));
        mRv.addItemDecoration(new RecycleViewDivider(this.getContext(), LinearLayoutManager.HORIZONTAL, 10,0xFF98FB98));


    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(getActivity(), uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
        // TODO 9: post video & update buttons
        Call <PostVideoResponse> call = getMiniDouyinService().postvideo("134567","doo",coverImagePart,videoPart);
        miniDouyinService.postvideo("1023324","doudoud",coverImagePart,videoPart).enqueue(
                new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        if(response.body()!=null){

                        }
                        mBtn.setText("select a image");
                        mBtn.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                        mBtn.setText("select a image");
                        mBtn.setEnabled(true);
                    }
                }
        );

        Toast.makeText(getActivity(), "TODO 9: post video & update buttons", Toast.LENGTH_SHORT).show();


    }


}
