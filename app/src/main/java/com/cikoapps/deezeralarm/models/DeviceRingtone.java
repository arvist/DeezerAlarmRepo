package com.cikoapps.deezeralarm.models;


public class DeviceRingtone {

    public String uri;
    public String title;
    public boolean selected;

    DeviceRingtone(String uri, String title, boolean selected) {
        this.uri = uri;
        this.title = title;
        this.selected = selected;
    }
}
