package com.cikoapps.deezeralarm.models;

public class DeezerRadio {
    public final long id;
    public final String title;
    public final String pictureUrl;
    public final String imageUrlSmall;
    public final String imageUrlLarge;
    public final String imageUrlMedium;
    public boolean selected;


    public DeezerRadio(long id, String title, String pictureUrl, String imageUrlSmall, String imageUrlMedium, String imageUrlLarge) {
        this.id = id;
        this.title = title;
        this.pictureUrl = pictureUrl;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlLarge = imageUrlLarge;

    }

}
