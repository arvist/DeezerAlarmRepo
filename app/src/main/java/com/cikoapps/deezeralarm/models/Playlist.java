package com.cikoapps.deezeralarm.models;

/**
 * Created by arvis.taurenis on 2/15/2015.
 */
public class Playlist implements java.io.Serializable {
    public long id;
    public String title;
    public String info;
    String imageUrlSmall;
    public String imageUrlMedium;
    String imageUrlLarge;
    public boolean selected;

    public Playlist(long id, String title, String info, String imageUrl, String imageUrlM, String imageUrlL) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.imageUrlSmall = imageUrl;
        this.imageUrlMedium = imageUrlM;
        this.imageUrlLarge = imageUrlL;

    }


}
