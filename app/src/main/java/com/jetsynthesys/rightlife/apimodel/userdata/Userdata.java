package com.jetsynthesys.rightlife.apimodel.userdata;

import com.jetsynthesys.rightlife.apimodel.ProfilePicture;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Userdata {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("weightUnit")
    @Expose
    private String weightUnit;
    @SerializedName("heightUnit")
    @Expose
    private String heightUnit;
    @SerializedName("newEmailStatus")
    @Expose
    private String newEmailStatus;
     @SerializedName("newPhoneStatus")
    @Expose
    private String newPhoneStatus;

    @SerializedName("notificationConfiguration")
    @Expose
    private NotificationConfiguration notificationConfiguration;
    @SerializedName("loginType")
    @Expose
    private String loginType;
    @SerializedName("syncGoogleCalendar")
    @Expose
    private Boolean syncGoogleCalendar;
    @SerializedName("profilePictures")
    @Expose
    private List<ProfilePicture> profilePictures;
    @SerializedName("address")
    @Expose
    private List<Object> address;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("createdAt")
    @Expose
    private Long createdAt;
    @SerializedName("updatedAt")
    @Expose
    private Long updatedAt;
    @SerializedName("newEmail")
    @Expose
    private String newEmail;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("dateofbirth")
    @Expose
    private String dateofbirth;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("height")
    @Expose
    private Double height;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("weight")
    @Expose
    private Double weight;
    @SerializedName("profilePicture")
    @Expose
    private String profilePicture;

    @SerializedName("age")
    @Expose
    private int age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(String heightUnit) {
        this.heightUnit = heightUnit;
    }

    public String getNewEmailStatus() {
        return newEmailStatus;
    }

    public void setNewEmailStatus(String newEmailStatus) {
        this.newEmailStatus = newEmailStatus;
    }

    public NotificationConfiguration getNotificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(NotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public Boolean getSyncGoogleCalendar() {
        return syncGoogleCalendar;
    }

    public void setSyncGoogleCalendar(Boolean syncGoogleCalendar) {
        this.syncGoogleCalendar = syncGoogleCalendar;
    }

    public List<ProfilePicture> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<ProfilePicture> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public List<Object> getAddress() {
        return address;
    }

    public void setAddress(List<Object> address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNewPhoneStatus() {
        return newPhoneStatus;
    }

    public void setNewPhoneStatus(String newPhoneStatus) {
        this.newPhoneStatus = newPhoneStatus;
    }
}
