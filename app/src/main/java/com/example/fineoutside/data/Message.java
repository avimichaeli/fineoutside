package com.example.fineoutside.data;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private String id, user_name, message_content, main_subject, profile_pic, time, latitude, longitude,
        pic, post_latitude, post_longitude, post_id, seen_by_owner, time_in_millis;
    private Boolean status;
    private List<String> watches, seen_users;
    private int unseen_count;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMessage_content() {
        return message_content;
    }

    public void setMessage_content(String message_content) {
        this.message_content = message_content;
    }

    public String getMain_subject() {
        return main_subject;
    }

    public void setMain_subject(String main_subject) {
        this.main_subject = main_subject;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<String> getWatches() {
        if (watches == null) {
            watches = new ArrayList<>();
        }
        return watches;
    }

    public void setWatches(List<String> watches) {
        this.watches = watches;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getTime_in_millis() {
        return time_in_millis == null ? "0" : time_in_millis;
    }

    public void setTime_in_millis(String time_in_millis) {
        this.time_in_millis = time_in_millis;
    }

    public String getPost_latitude() {
        return post_latitude;
    }

    public void setPost_latitude(String post_latitude) {
        this.post_latitude = post_latitude;
    }

    public String getPost_longitude() {
        return post_longitude;
    }

    public void setPost_longitude(String post_longitude) {
        this.post_longitude = post_longitude;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getSeen_by_owner() {
        return seen_by_owner;
    }

    public void setSeen_by_owner(String seen_by_owner) {
        this.seen_by_owner = seen_by_owner;
    }

    public int getUnseen_count() {
        return unseen_count;
    }

    public void setUnseen_count(int unseen_count) {
        this.unseen_count = unseen_count;
    }

    public List<String> getSeen_users() {
        return seen_users;
    }

    public void setSeen_users(List<String> seen_users) {
        this.seen_users = seen_users;
    }
}
