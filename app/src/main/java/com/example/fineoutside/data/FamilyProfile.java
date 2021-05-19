package com.example.fineoutside.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class FamilyProfile implements Parcelable {

    private int age, mate_age, family_status, num_of_children;
    private String user_name, mate_name, uid, profile_pic;
    private List<ChildNameAge> childNameAgeList;
    private double latitude, longitude;

    public FamilyProfile() {
    }

    public FamilyProfile(String user_name, int age, int family_status, double latitude, double longitude, String uid, String profile_pic) {
        this.user_name = user_name;
        this.age = age;
        this.family_status = family_status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;
        this.profile_pic = profile_pic;
    }

    public FamilyProfile(String user_name, int age, int mate_age, int family_status, int num_of_children, String mate_name, List<ChildNameAge> childNameAgeList, double latitude, double longitude, String uid, String profile_pic) {
        this.user_name = user_name;
        this.age = age;
        this.mate_age = mate_age;
        this.family_status = family_status;
        this.num_of_children = num_of_children;
        this.mate_name = mate_name;
        this.childNameAgeList = childNameAgeList;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;
        this.profile_pic = profile_pic;
    }

    protected FamilyProfile(Parcel in) {
        age = in.readInt();
        mate_age = in.readInt();
        family_status = in.readInt();
        num_of_children = in.readInt();
        user_name = in.readString();
        mate_name = in.readString();
        uid = in.readString();
        profile_pic = in.readString();
        childNameAgeList = in.createTypedArrayList(ChildNameAge.CREATOR);
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<FamilyProfile> CREATOR = new Creator<FamilyProfile>() {
        @Override
        public FamilyProfile createFromParcel(Parcel in) {
            return new FamilyProfile(in);
        }

        @Override
        public FamilyProfile[] newArray(int size) {
            return new FamilyProfile[size];
        }
    };

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getMate_age() {
        return mate_age;
    }

    public void setMate_age(int mate_age) {
        this.mate_age = mate_age;
    }

    public int getFamily_status() {
        return family_status;
    }

    public void setFamily_status(int family_status) {
        this.family_status = family_status;
    }

    public int getNum_of_children() {
        return num_of_children;
    }

    public void setNum_of_children(int num_of_children) {
        this.num_of_children = num_of_children;
    }

    public String getMate_name() {
        return mate_name;
    }

    public void setMate_name(String mate_name) {
        this.mate_name = mate_name;
    }

    public List<ChildNameAge> getChildNameAgeList() {
        return childNameAgeList;
    }

    public void setChildNameAgeList(List<ChildNameAge> childNameAgeList) {
        this.childNameAgeList = childNameAgeList;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(age);
        dest.writeInt(mate_age);
        dest.writeInt(family_status);
        dest.writeInt(num_of_children);
        dest.writeString(user_name);
        dest.writeString(mate_name);
        dest.writeString(uid);
        dest.writeString(profile_pic);
        dest.writeTypedList(childNameAgeList);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
