package com.example.groupcycling.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelAvatar implements Parcelable {

    int image;

    public ModelAvatar(int image) {
        this.image = image;
    }

    protected ModelAvatar(Parcel in) {
        image = in.readInt();
    }

    public static final Parcelable.Creator<ModelAvatar> CREATOR = new Parcelable.Creator<ModelAvatar>() {
        @Override
        public ModelAvatar createFromParcel(Parcel in) {
            return new ModelAvatar(in);
        }

        @Override
        public ModelAvatar[] newArray(int size) {
            return new ModelAvatar[size];
        }
    };

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
