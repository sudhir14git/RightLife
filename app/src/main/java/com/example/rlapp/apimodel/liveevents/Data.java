package com.example.rlapp.apimodel.liveevents;


import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Data {

    @SerializedName("sectionTitle")
    @Expose
    private String sectionTitle;
    @SerializedName("sectionSubtitle")
    @Expose
    private String sectionSubtitle;
    @SerializedName("isShowGrid")
    @Expose
    private Boolean isShowGrid;
    @SerializedName("events")
    @Expose
    private List<Event> events;
    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("totalEventsCount")
    @Expose
    private Integer totalEventsCount;
    @SerializedName("pastEventsCount")
    @Expose
    private Integer pastEventsCount;
    @SerializedName("upcomingEventsCount")
    @Expose
    private Integer upcomingEventsCount;

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionSubtitle() {
        return sectionSubtitle;
    }

    public void setSectionSubtitle(String sectionSubtitle) {
        this.sectionSubtitle = sectionSubtitle;
    }

    public Boolean getIsShowGrid() {
        return isShowGrid;
    }

    public void setIsShowGrid(Boolean isShowGrid) {
        this.isShowGrid = isShowGrid;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getTotalEventsCount() {
        return totalEventsCount;
    }

    public void setTotalEventsCount(Integer totalEventsCount) {
        this.totalEventsCount = totalEventsCount;
    }

    public Integer getPastEventsCount() {
        return pastEventsCount;
    }

    public void setPastEventsCount(Integer pastEventsCount) {
        this.pastEventsCount = pastEventsCount;
    }

    public Integer getUpcomingEventsCount() {
        return upcomingEventsCount;
    }

    public void setUpcomingEventsCount(Integer upcomingEventsCount) {
        this.upcomingEventsCount = upcomingEventsCount;
    }

}