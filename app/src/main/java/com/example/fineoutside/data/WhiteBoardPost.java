package com.example.fineoutside.data;

import java.util.List;

public class WhiteBoardPost {

    private String post_id, post_subject, post_content, creator_id, groups_name;
    private List<String> groups_names;
    private Long posted_time;

    public WhiteBoardPost() {
    }

    public WhiteBoardPost(String post_id, String post_subject, String post_content, String creator_id, List<String> groups_names, Long posted_time) {
        this.post_id = post_id;
        this.post_subject = post_subject;
        this.post_content = post_content;
        this.creator_id = creator_id;
        this.groups_names = groups_names;
        this.posted_time = posted_time;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_subject() {
        return post_subject;
    }

    public void setPost_subject(String post_subject) {
        this.post_subject = post_subject;
    }

    public String getPost_content() {
        return post_content;
    }

    public void setPost_content(String post_content) {
        this.post_content = post_content;
    }

    public List<String> getGroups_names() {
        return groups_names;
    }

    public void setGroups_names(List<String> groups_names) {
        this.groups_names = groups_names;
    }

    public Long getPosted_time() {
        return posted_time;
    }

    public void setPosted_time(Long posted_time) {
        this.posted_time = posted_time;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }
}
