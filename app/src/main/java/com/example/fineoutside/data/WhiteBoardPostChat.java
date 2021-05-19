package com.example.fineoutside.data;

public class WhiteBoardPostChat {

    private String post_id, message, user_name, time, pic;

    public WhiteBoardPostChat() {
    }

    public WhiteBoardPostChat(String post_id, String message, String user_name, String time, String pic) {
        this.post_id = post_id;
        this.message = message;
        this.user_name = user_name;
        this.time = time;
        this.pic = pic;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
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
}
