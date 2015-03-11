package com.cikoapps.deezeralarm.models;

public class Artist {
    public long id;
    public String name;
    String pictureUrl;
    String imageUrlSmall;
    public String imageUrlMedium;
    String imageUrlLarge;
    boolean hasRadio;
    public boolean selected;

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
