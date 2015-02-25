package com.cikoapps.deezeralarm.models;

public class Radio implements java.io.Serializable {
    public long id;
    public String title;
    String pictureUrl;
    String imageUrlSmall;
    public String imageUrlMedium;
    String imageUrlLarge;
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
