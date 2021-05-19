package com.example.fineoutside.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ChildNameAge implements Parcelable {

    public static final Creator<ChildNameAge> CREATOR = new Creator<ChildNameAge>() {
        @Override
        public ChildNameAge createFromParcel(Parcel in) {
            return new ChildNameAge(in);
        }

        @Override
        public ChildNameAge[] newArray(int size) {
            return new ChildNameAge[size];
        }
    };
    private String child_name;
    private int child_age;

    public ChildNameAge() {
    }

    public ChildNameAge(String child_name, int child_age) {
        this.child_name = child_name;
        this.child_age = child_age;
    }

    protected ChildNameAge(Parcel in) {
        child_name = in.readString();
        child_age = in.readInt();
    }

    public String getChild_name() {
        return child_name;
    }

    public void setChild_name(String child_name) {
        this.child_name = child_name;
    }

    public int getChild_age() {
        return child_age;
    }

    public void setChild_age(int child_age) {
        this.child_age = child_age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(child_name);
        dest.writeInt(child_age);
    }
}
