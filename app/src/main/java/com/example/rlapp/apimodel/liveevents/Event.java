package com.example.rlapp.apimodel.liveevents;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Event {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("appId")
    @Expose
    private String appId;
    @SerializedName("eventType")
    @Expose
    private String eventType;
    @SerializedName("introVideo")
    @Expose
    private IntroVideo introVideo;
    @SerializedName("coverImage")
    @Expose
    private CoverImage coverImage;
    @SerializedName("thumbnail")
    @Expose
    private Thumbnail thumbnail;
    @SerializedName("priceInINR")
    @Expose
    private Integer priceInINR;
    @SerializedName("priceInUSD")
    @Expose
    private Integer priceInUSD;
    @SerializedName("isFeatured")
    @Expose
    private Boolean isFeatured;
    @SerializedName("eventCategory")
    @Expose
    private List<EventCategory> eventCategory;
    @SerializedName("tags")
    @Expose
    private List<Tag> tags;
    @SerializedName("shareUrl")
    @Expose
    private String shareUrl;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("cancellationReason")
    @Expose
    private String cancellationReason;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("eventLevel")
    @Expose
    private EventLevel eventLevel;
    @SerializedName("startDateTime")
    @Expose
    private String startDateTime;
    @SerializedName("endDateTime")
    @Expose
    private String endDateTime;
    @SerializedName("instructor")
    @Expose
    private Instructor instructor;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;
    @SerializedName("completedClassCount")
    @Expose
    private Integer completedClassCount;
    @SerializedName("isTrail")
    @Expose
    private Boolean isTrail;
    @SerializedName("isPurchased")
    @Expose
    private Boolean isPurchased;
    @SerializedName("nextSchedule")
    @Expose
    private NextSchedule nextSchedule;
    @SerializedName("participantsCount")
    @Expose
    private Integer participantsCount;
    @SerializedName("originalPriceInINR")
    @Expose
    private Integer originalPriceInINR;
    @SerializedName("originalPriceInUSD")
    @Expose
    private Integer originalPriceInUSD;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("ctaText")
    @Expose
    private String ctaText;
    @SerializedName("offerText")
    @Expose
    private String offerText;
    @SerializedName("rating")
    @Expose
    private Integer rating;
    @SerializedName("liveClassCount")
    @Expose
    private Integer liveClassCount;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public IntroVideo getIntroVideo() {
        return introVideo;
    }

    public void setIntroVideo(IntroVideo introVideo) {
        this.introVideo = introVideo;
    }

    public CoverImage getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(CoverImage coverImage) {
        this.coverImage = coverImage;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getPriceInINR() {
        return priceInINR;
    }

    public void setPriceInINR(Integer priceInINR) {
        this.priceInINR = priceInINR;
    }

    public Integer getPriceInUSD() {
        return priceInUSD;
    }

    public void setPriceInUSD(Integer priceInUSD) {
        this.priceInUSD = priceInUSD;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public List<EventCategory> getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(List<EventCategory> eventCategory) {
        this.eventCategory = eventCategory;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventLevel getEventLevel() {
        return eventLevel;
    }

    public void setEventLevel(EventLevel eventLevel) {
        this.eventLevel = eventLevel;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
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

    public Integer getCompletedClassCount() {
        return completedClassCount;
    }

    public void setCompletedClassCount(Integer completedClassCount) {
        this.completedClassCount = completedClassCount;
    }

    public Boolean getIsTrail() {
        return isTrail;
    }

    public void setIsTrail(Boolean isTrail) {
        this.isTrail = isTrail;
    }

    public Boolean getIsPurchased() {
        return isPurchased;
    }

    public void setIsPurchased(Boolean isPurchased) {
        this.isPurchased = isPurchased;
    }

    public NextSchedule getNextSchedule() {
        return nextSchedule;
    }

    public void setNextSchedule(NextSchedule nextSchedule) {
        this.nextSchedule = nextSchedule;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Integer getOriginalPriceInINR() {
        return originalPriceInINR;
    }

    public void setOriginalPriceInINR(Integer originalPriceInINR) {
        this.originalPriceInINR = originalPriceInINR;
    }

    public Integer getOriginalPriceInUSD() {
        return originalPriceInUSD;
    }

    public void setOriginalPriceInUSD(Integer originalPriceInUSD) {
        this.originalPriceInUSD = originalPriceInUSD;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getLiveClassCount() {
        return liveClassCount;
    }

    public void setLiveClassCount(Integer liveClassCount) {
        this.liveClassCount = liveClassCount;
    }

}
