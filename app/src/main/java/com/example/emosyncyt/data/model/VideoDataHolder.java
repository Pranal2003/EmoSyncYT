package com.example.emosyncyt.data.model;

import java.util.ArrayList;
import java.util.List;

public class VideoDataHolder {
    private static VideoDataHolder instance;
    private List<YouTubeResponse.Item> videoList;

    private VideoDataHolder() {
        videoList = new ArrayList<>();
    }

    public static synchronized VideoDataHolder getInstance() {
        if (instance == null) {
            instance = new VideoDataHolder();
        }
        return instance;
    }

    public List<YouTubeResponse.Item> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<YouTubeResponse.Item> videos) {
        videoList.clear();
        videoList.addAll(videos);
    }
}

