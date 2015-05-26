package com.cikoapps.deezeralarm.models;

public class DeezerRadio {
    public final long id;
    public final String title;
    private final String imageUrlSmall;
    public final String imageUrlMedium;
    public boolean selected;


    public DeezerRadio(long id, String title, String imageUrlSmall, String imageUrlMedium) {
        this.id = id;
        this.title = title;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlMedium = imageUrlMedium;

    }

}
