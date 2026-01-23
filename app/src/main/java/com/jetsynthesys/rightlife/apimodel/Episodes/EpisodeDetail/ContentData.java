package com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.Tag;

import java.util.List;

public class ContentData {
    public String _id;
    public String contentId;
    public String type;
    public String title;
    public int episodeNumber;
    public String desc;
    public List<Tag> tags;
    public List<Artist> artist;
    public Thumbnail thumbnail;
    public String pricingTier;
    public String url;
    public String previewUrl;
    public String youtubeUrl;
    public String shareUrl;
    public String createdAt;
    public String updatedAt;
    public int viewCount;
    public Meta meta;
    public boolean isWatched;
    public boolean isFavourited;
    public String seriesId;
    public String seriesTitle;
    public String moduleId;
    public String moduleName;
    public Object recommended; // This is null in response thats why its object
    public NextEpisode nextEpisode;
    public boolean isBookmarked;

    public Boolean isLike;

    public Integer likeCount;
}