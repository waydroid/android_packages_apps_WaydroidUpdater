/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import android.os.Parcelable;
import android.os.Parcel;
import org.json.JSONObject;
import org.json.JSONException;

public class UpdateInfo implements Comparable<UpdateInfo>, Parcelable {
    private long datetime;
    private String filename;
    private String id;
    private String imageType;
    private long size;
    private String url;
    private String version;

    public UpdateInfo(long datetime, String filename, String id,
            String imageType, long size, String url, String version) {
        this.datetime = datetime * 1000;
        this.filename = filename;
        this.id = id;
        this.imageType = imageType;
        this.size = size;
        this.url = url;
        this.version = version;
    }

    public UpdateInfo(JSONObject jo) throws JSONException {
        this(jo.getLong("datetime"), jo.getString("filename"),
            jo.getString("id"), jo.getString("romtype"), jo.getLong("size"),
            jo.getString("url"), jo.getString("version"));
    }

    public long getTimestamp() {
        return datetime;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }

    public String imageType() {
        return imageType;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public int compareTo(String version, long datetime) {
        Version a = new Version(this.version + "." + this.datetime);
        Version b = new Version(version + "." + datetime);
        return a.compareTo(b);
    }

    // Comparable<> implementation
    @Override
    public int compareTo(UpdateInfo other) {
        return compareTo(other.version, other.datetime);
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
         return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.datetime);
        out.writeString(this.filename);
        out.writeString(this.id);
        out.writeString(this.imageType);
        out.writeLong(this.size);
        out.writeString(this.url);
        out.writeString(this.version);
    }
    public static final Parcelable.Creator<UpdateInfo> CREATOR
            = new Parcelable.Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
    private UpdateInfo(Parcel in) {
        this.datetime = in.readLong();
        this.filename = in.readString();
        this.id = in.readString();
        this.imageType = in.readString();
        this.size = in.readLong();
        this.url = in.readString();
        this.version = in.readString();
    }
}
