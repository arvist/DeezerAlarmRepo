package com.cikoapps.deezeralarm.models;

public class Radio {
    public final long id;
    public final String title;
    private final String pictureUrl;
    private final String imageUrlSmall;
    private final String imageUrlLarge;
    public final String imageUrlMedium;
    public boolean selected;


    public Radio(long id, String title, String pictureUrl, String imageUrlSmall, String imageUrlMedium, String imageUrlLarge) {
        this.id = id;
        this.title = title;
        this.pictureUrl = pictureUrl;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlLarge = imageUrlLarge;

    }

}
