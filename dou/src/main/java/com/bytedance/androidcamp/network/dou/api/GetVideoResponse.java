package com.bytedance.androidcamp.network.dou.api;

import com.bytedance.androidcamp.network.dou.model.Video;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.http.Query;

public class GetVideoResponse {

    @SerializedName("feeds") private List<Video> videos;
    @SerializedName("success") private boolean success;

    public List<Video> getVideos() {return videos;}

    public String toString(){

        return "GetVideosResponse{ "+"success="+success+","+",videos="+videos+"}";
    }



}
