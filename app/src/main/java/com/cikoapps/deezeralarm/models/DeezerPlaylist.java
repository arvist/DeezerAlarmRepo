package com.cikoapps.deezeralarm.models;

public class DeezerPlaylist {
    public final long id;
    public final String title;
    public final String info;
    public final String imageUrlMedium;
    public boolean selected;

    public DeezerPlaylist(long id, String title, String info,  String imageUrlM ) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.imageUrlMedium = imageUrlM;


    }


}
