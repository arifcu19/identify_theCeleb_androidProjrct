package com.example.devapp;

public class PostLoaderClass {

    public PostLoaderClass(String post, Object timestamp) {
        this.post = post;
        this.timestamp = timestamp;
    }

    public PostLoaderClass() {
    }

    private String post;

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    private Object timestamp;

}
