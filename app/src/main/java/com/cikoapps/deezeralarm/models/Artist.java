package com.cikoapps.deezeralarm.models;


/*
(artist.getId(), artist.getName()
                            , artist.getPictureUrl(), artist.getImageUrl(AImageOwner.ImageSize.small), artist.getImageUrl(AImageOwner.ImageSize.medium),
                            artist.getImageUrl(AImageOwner.ImageSize.big), artist.hasRadio());
 */
public class Artist implements java.io.Serializable {
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
