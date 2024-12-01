package com.example.rlapp.apimodel.liveevents;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventCategory {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("_id")
    @Expose
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
