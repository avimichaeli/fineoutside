package com.example.fineoutside.data;

import java.util.List;

public class Group {

    private String id, name, description, latitude, longitude, creator_id;
    private List<String> registered_users;

    public Group() {
    }

    public Group(String id, String name, String description, String latitude, String longitude, String creator_id, List<String> registered_users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creator_id = creator_id;
        this.registered_users = registered_users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public List<String> getRegistered_users() {
        return registered_users;
    }

    public void setRegistered_users(List<String> registered_users) {
        this.registered_users = registered_users;
    }
}
