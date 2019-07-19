package com.bytedance.androidcamp.network.dou.api;

import com.bytedance.androidcamp.network.dou.model.Video;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PostVideoResponse {
    @SerializedName("url") private String url;
    @SerializedName("success") private boolean success;

    public String toString(){

        return "PostVideosResponse{ }";
    }
}
