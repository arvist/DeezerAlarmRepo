package com.cikoapps.deezeralarm.models;

/**
 * Created by arvis.taurenis on 2/16/2015.
 */
public class Album implements java.io.Serializable {
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
