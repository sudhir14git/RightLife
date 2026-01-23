package com.jetsynthesys.rightlife.apimodel.newreportfacescan;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recommendation {
    @SerializedName("_id")
    public String id;
    public String contentType;

    public String url;              // NEW: "url": "media/PRODUCT/..."
    public String previewUrl;       // NEW: "previewUrl": ""
    public String youtubeUrl;       // NEW: "youtubeUrl": ""
    public String readingTime;      // NEW: "readingTime": "5"

    public String moduleId;
    public String categoryId;
    public String title;
    public Thumbnail thumbnail;
    public String desc;
    public int episodeCount;
    public boolean isPromoted;
    public String seriesType;
    public String createdAt;
    public String updatedAt;
    public String promotedAt;
    public List<SubCategory> subCategories;
    public Meta meta;
    public List<Artist> artist;
    public String shareUrl;
    public String pricing;
    public int viewCount;
    public String moduleName;
    public String categoryName;
    //public List<String> tagsAsStrings;
    public boolean isWatched;
    public boolean isAffirmated;
    public boolean isFavourited;
    public boolean isAlarm;
}