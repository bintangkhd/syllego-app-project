package com.example.nye_ken;

public class Model {

    String id;
    String title;
    String path;
    String size;

    public Model(String id, String path, String title, String size) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }
}
