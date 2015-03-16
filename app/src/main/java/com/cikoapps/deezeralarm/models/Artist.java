package com.cikoapps.deezeralarm.models;

public class Artist {
    public final long id;
    public final String name;
    private final String pictureUrl;
    private final String imageUrlSmall;
    private final String imageUrlLarge;
    public String imageUrlMedium;
    public boolean selected;
    private boolean hasRadio;

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
