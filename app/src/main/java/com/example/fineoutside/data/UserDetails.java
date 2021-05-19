package com.example.fineoutside.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetails implements Comparable<UserDetails>, Parcelable {

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel in) {
            return new UserDetails(in);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };
    private String uid, name, userType, token, businessName, foodType, age, about_me, selectedBabysitterId, pic;
    private boolean isEnablePics, isEnablePush, isEnableParentCommunityHelpMessages;
    private Double latitude, longitude;
    private FamilyProfile familyProfile;
    private int distance;
    private long joinedDate;
    private boolean selected;

    public UserDetails() {
    }

    public UserDetails(String uid, String name, String userType, boolean isEnablePics, boolean isEnablePush, String token, String businessName, String foodType, String age, String about_me, String selectedBabysitterId, String pic, long joinedDate) {
        this.uid = uid;
        this.name = name;
        this.userType = userType;
        this.isEnablePics = isEnablePics;
        this.isEnablePush = isEnablePush;
        this.isEnableParentCommunityHelpMessages = true;
        this.token = token;
        this.businessName = businessName;
        this.foodType = foodType;
        this.age = age;
        this.about_me = about_me;
        this.selectedBabysitterId = selectedBabysitterId;
        this.pic = pic;
        this.joinedDate = joinedDate;
    }

    protected UserDetails(Parcel in) {
        uid = in.readString();
        name = in.readString();
        userType = in.readString();
        token = in.readString();
        businessName = in.readString();
        foodType = in.readString();
        age = in.readString();
        about_me = in.readString();
        selectedBabysitterId = in.readString();
        pic = in.readString();
        isEnablePics = in.readByte() != 0;
        isEnablePush = in.readByte() != 0;
        isEnableParentCommunityHelpMessages = in.readByte() != 0;
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        familyProfile = in.readParcelable(FamilyProfile.class.getClassLoader());
        distance = in.readInt();
        joinedDate = in.readLong();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isEnablePics() {
        return isEnablePics;
    }

    public void setEnablePics(boolean enablePics) {
        isEnablePics = enablePics;
    }

    public boolean isEnablePush() {
        return isEnablePush;
    }

    public void setEnablePush(boolean enablePush) {
        isEnablePush = enablePush;
    }

    public boolean isEnableParentCommunityHelpMessages() {
        return isEnableParentCommunityHelpMessages;
    }

    public void setEnableParentCommunityHelpMessages(boolean enableParentCommunityHelpMessages) {
        isEnableParentCommunityHelpMessages = enableParentCommunityHelpMessages;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public FamilyProfile getFamilyProfile() {
        return familyProfile;
    }

    public void setFamilyProfile(FamilyProfile familyProfile) {
        this.familyProfile = familyProfile;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAbout_me() {
        return about_me;
    }

    public void setAbout_me(String about_me) {
        this.about_me = about_me;
    }

    public String getSelectedBabysitterId() {
        return selectedBabysitterId;
    }

    public void setSelectedBabysitterId(String selectedBabysitterId) {
        this.selectedBabysitterId = selectedBabysitterId;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public long getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(long joinedDate) {
        this.joinedDate = joinedDate;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(UserDetails other) {
        return this.distance - other.getDistance();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(userType);
        dest.writeString(token);
        dest.writeString(businessName);
        dest.writeString(foodType);
        dest.writeString(age);
        dest.writeString(about_me);
        dest.writeString(selectedBabysitterId);
        dest.writeString(pic);
        dest.writeByte((byte) (isEnablePics ? 1 : 0));
        dest.writeByte((byte) (isEnablePush ? 1 : 0));
        dest.writeByte((byte) (isEnableParentCommunityHelpMessages ? 1 : 0));
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeParcelable(familyProfile, flags);
        dest.writeInt(distance);
        dest.writeLong(joinedDate);
    }
}
