package com.nplusnapps.hotels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

class Hotel implements Parcelable {

    public static Parcelable.Creator<Hotel> CREATOR = new Parcelable.Creator<Hotel>() {
        @Override
        public Hotel createFromParcel(Parcel source) {
            return new Hotel(source);
        }

        @Override
        public Hotel[] newArray(int size) {
            return new Hotel[size];
        }
    };

    private int mId;
    private double mStars, mDistance, mLatitude, mLongitude;
    private String[] mSuites;
    private String mName, mAddress;
    private String mImage;

    Hotel(int id, String name, String address, double stars, double distance,
          String image, String[] suites, double latitude, double longitude) {
        mId = id;
        mName = name;
        mAddress = address;
        mStars = stars;
        mDistance = distance;
        mImage = image;
        mSuites = suites;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    private Hotel(Parcel parcel) {
        mId = parcel.readInt();
        mName = parcel.readString();
        mAddress = parcel.readString();
        mStars = parcel.readDouble();
        mDistance = parcel.readDouble();
        mImage = parcel.readString();
        mLatitude = parcel.readDouble();
        mLongitude = parcel.readDouble();

        mSuites = new String[parcel.readInt()];
        parcel.readStringArray(mSuites);
    }

    int getId() {
        return mId;
    }

    String getName() {
        return mName;
    }

    String getAddress() {
        return mAddress;
    }

    double getStars() {
        return mStars;
    }

    double getDistance() {
        return mDistance;
    }

    String getImage() {
        return mImage;
    }

    String[] getSuites() {
        return mSuites;
    }

    int getSuitesCount() {
        return mSuites.length;
    }

    String getSuitesString() {
        String array = Arrays.toString(mSuites);

        return array.substring(1, array.length() - 1);
    }

    double getLatitude() {
        return mLatitude;
    }

    double getLongitude() {
        return mLongitude;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeDouble(mStars);
        dest.writeDouble(mDistance);
        dest.writeString(mImage);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);

        dest.writeInt(mSuites.length);
        dest.writeStringArray(mSuites);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
