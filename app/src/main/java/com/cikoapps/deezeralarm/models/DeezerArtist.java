package com.cikoapps.deezeralarm.models;

public class DeezerArtist {
    public final long id;
    public final String name;

    public final String imageUrlMedium;
    public boolean selected;
    private final boolean hasRadio;

    public DeezerArtist(long id, String name, String imageUrlMedium, boolean hasRadio) {
        this.id = id;
        this.name = name;
        this.imageUrlMedium = imageUrlMedium;
        this.hasRadio = hasRadio;
    }
}
