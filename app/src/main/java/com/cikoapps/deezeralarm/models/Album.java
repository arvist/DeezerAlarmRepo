package com.cikoapps.deezeralarm.models;


public class Album {
    public final long id;
    public final String title;
    public final String artist;
    public final String coverURL;
    public final String duration;
    public final String imageUrlSmall;
    private final String imageUrlLarge;
    public String imageUrlMedium;
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
