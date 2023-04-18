package com.example.groupcycling.marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.groupcycling.model.ModelParticipant;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private String image;
    private ModelParticipant participant;

    public ClusterMarker(LatLng position, String title, String snippet, String image, ModelParticipant participant) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.image = image;
        this.participant = participant;
    }

    public ClusterMarker() {

    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public ModelParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(ModelParticipant participant) {
        this.participant = participant;
    }
}
