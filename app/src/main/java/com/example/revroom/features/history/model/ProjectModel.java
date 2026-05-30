package com.example.revroom.features.history.model;

import com.google.gson.annotations.SerializedName;

public class ProjectModel {
    @SerializedName(value = "id", alternate = {"designId"})
    private String designId;

    @SerializedName("originalImageUrl")
    private String originalImageUrl;

    @SerializedName("designedImageUrl")
    private String designedImageUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    public ProjectModel() {
    }

    public ProjectModel(String designId, String originalImageUrl, String designedImageUrl, String status, String createdAt) {
        this.designId = designId;
        this.originalImageUrl = originalImageUrl;
        this.designedImageUrl = designedImageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }

    public String getDesignedImageUrl() {
        return designedImageUrl;
    }

    public void setDesignedImageUrl(String designedImageUrl) {
        this.designedImageUrl = designedImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
