package com.cikoapps.deezeralarm.models;


public class Album {
    public long id;
    public String title;
    public String artist;
    String coverURL;
    String duration;
    String imageUrlSmall;
    public String imageUrlMedium;
    String imageUrlLarge;
    public boolean selected;

    public Album(long id, String title, String artist, String coverURL, String duration, String imageUrlSmall, String imageUrlMedium, String imageUrlLarge) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverURL = coverURL;
        this.duration = duration;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlLarge = imageUrlLarge;
    }
}
