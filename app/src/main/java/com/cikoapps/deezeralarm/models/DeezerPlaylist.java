package com.cikoapps.deezeralarm.models;

public class DeezerPlaylist {
    public final long id;
    public final String title;
    public final String info;
    private final String imageUrlSmall;
    private final String imageUrlLarge;
    public final String imageUrlMedium;
    public boolean selected;

    public DeezerPlaylist(long id, String title, String info, String imageUrl, String imageUrlM, String imageUrlL) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.imageUrlSmall = imageUrl;
        this.imageUrlMedium = imageUrlM;
        this.imageUrlLarge = imageUrlL;

    }


}
