package com.cikoapps.deezeralarm.models;


public class DeezerAlbum {
    public final long id;
    public final String title;
    public final String artist;
    public final String imageUrlMedium;
    public boolean selected;

    public DeezerAlbum(long id, String title, String artist, String imageUrlMedium) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.imageUrlMedium = imageUrlMedium;
    }
}
