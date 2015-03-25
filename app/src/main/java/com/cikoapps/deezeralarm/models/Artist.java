package com.cikoapps.deezeralarm.models;

public class Artist {
    public final long id;
    public final String name;
    public final String pictureUrl;
    public final String imageUrlSmall;
    public final String imageUrlLarge;
    public final String imageUrlMedium;
    public boolean selected;
    public final boolean hasRadio;

    public Artist(long id, String name, String pictureUrl, String imageUrlSmall, String imageUrlMedium, String imageUrlLarge, boolean hasRadio) {
        this.id = id;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlLarge = imageUrlLarge;
        this.hasRadio = hasRadio;
    }
}
