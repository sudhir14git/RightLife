package com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ContentDetail {

    @SerializedName("contentType")
    @Expose
    private String contentType;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("previewUrl")
    @Expose
    private String previewUrl;
    @SerializedName("moduleId")
    @Expose
    private String moduleId;
    @SerializedName("categoryId")
    @Expose
    private String categoryId;
    @SerializedName("subCategories")
    @Expose
    private List<SubCategory> subCategories;
    @SerializedName("artist")
    @Expose
    private List<Artist> artist;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("pricing")
    @Expose
    private String pricing;
    @SerializedName("thumbnail")
    @Expose
    private Thumbnail thumbnail;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("tags")
    @Expose
    private List<Tag> tags;
    @SerializedName("youtubeUrl")
    @Expose
    private String youtubeUrl;
    @SerializedName("meta")
    @Expose
    private Meta meta;
    @SerializedName("viewCount")
    @Expose
    private Integer viewCount;
    @SerializedName("shareUrl")
    @Expose
    private String shareUrl;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("categoryName")
    @Expose
    private String categoryName;
    @SerializedName("isFavourited")
    @Expose
    private Boolean isFavourited;
    @SerializedName("isAffirmated")
    @Expose
    private Boolean isAffirmated;
    @SerializedName("isAlarm")
    @Expose
    private Boolean isAlarm;
    @SerializedName("isWatched")
    @Expose
    private Boolean isWatched;
    @SerializedName("isSubscribed")
    @Expose
    private Boolean isSubscribed;
    @SerializedName("isAffirmation")
    @Expose
    private Boolean isAffirmation;
    @SerializedName("leftDuration")
    @Expose
    private String leftDuration;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("episodeDetails")
    @Expose
    private EpisodeDetails episodeDetails;
    @SerializedName("isPromoted")
    @Expose
    private Boolean isPromoted;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;
    @SerializedName("moduleName")
    @Expose
    private String moduleName;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<SubCategory> subCategories) {
        this.subCategories = subCategories;
    }

    public List<Artist> getArtist() {
        return artist;
    }

    public void setArtist(List<Artist> artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPricing() {
        return pricing;
    }

    public void setPricing(String pricing) {
        this.pricing = pricing;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getIsFavourited() {
        return isFavourited;
    }

    public void setIsFavourited(Boolean isFavourited) {
        this.isFavourited = isFavourited;
    }

    public Boolean getIsAffirmated() {
        return isAffirmated;
    }

    public void setIsAffirmated(Boolean isAffirmated) {
        this.isAffirmated = isAffirmated;
    }

    public Boolean getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(Boolean isAlarm) {
        this.isAlarm = isAlarm;
    }

    public Boolean getIsWatched() {
        return isWatched;
    }

    public void setIsWatched(Boolean isWatched) {
        this.isWatched = isWatched;
    }

    public Boolean getIsSubscribed() {
        return isSubscribed;
    }

    public void setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public Boolean getIsAffirmation() {
        return isAffirmation;
    }

    public void setIsAffirmation(Boolean isAffirmation) {
        this.isAffirmation = isAffirmation;
    }

    public String getLeftDuration() {
        return leftDuration;
    }

    public void setLeftDuration(String leftDuration) {
        this.leftDuration = leftDuration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public EpisodeDetails getEpisodeDetails() {
        return episodeDetails;
    }

    public void setEpisodeDetails(EpisodeDetails episodeDetails) {
        this.episodeDetails = episodeDetails;
    }

    public Boolean getIsPromoted() {
        return isPromoted;
    }

    public void setIsPromoted(Boolean isPromoted) {
        this.isPromoted = isPromoted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

}
