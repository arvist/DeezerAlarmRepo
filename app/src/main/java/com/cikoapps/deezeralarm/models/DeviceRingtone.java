package com.cikoapps.deezeralarm.models;


@SuppressWarnings({"CanBeFinal", "SameParameterValue"})
public class DeviceRingtone {

    public String uri;
    public String title;
    public boolean selected;

    public DeviceRingtone(String uri, String title, boolean selected) {
        this.uri = uri;
        this.title = title;
        this.selected = selected;
    }
}
