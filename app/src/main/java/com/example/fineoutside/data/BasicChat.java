package com.example.fineoutside.data;

public class BasicChat {

    private String my_uid, other_uid, my_user_name, other_user_name, message, time, pic, latitude, longitude, profile_pic;
    private boolean seen_by_my_user, seen_by_other_user;

    public BasicChat() {
    }

    public String getMy_uid() {
        return my_uid;
    }

    public void setMy_uid(String my_uid) {
        this.my_uid = my_uid;
    }

    public String getOther_uid() {
        return other_uid;
    }

    public void setOther_uid(String other_uid) {
        this.other_uid = other_uid;
    }

    public String getMy_user_name() {
        return my_user_name;
    }

    public void setMy_user_name(String my_user_name) {
        this.my_user_name = my_user_name;
    }

    public String getOther_user_name() {
        return other_user_name;
    }

    public void setOther_user_name(String other_user_name) {
        this.other_user_name = other_user_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public boolean isSeen_by_my_user() {
        return seen_by_my_user;
    }

    public void setSeen_by_my_user(boolean seen_by_my_user) {
        this.seen_by_my_user = seen_by_my_user;
    }

    public boolean isSeen_by_other_user() {
        return seen_by_other_user;
    }

    public void setSeen_by_other_user(boolean seen_by_other_user) {
        this.seen_by_other_user = seen_by_other_user;
    }
}
