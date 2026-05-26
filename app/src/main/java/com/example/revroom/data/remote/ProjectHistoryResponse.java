package com.example.revroom.data.remote;

import com.example.revroom.features.history.model.ProjectModel;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProjectHistoryResponse {
    @SerializedName("data")
    private List<ProjectModel> data;

    @SerializedName("page")
    private int page;

    @SerializedName("pageSize")
    private int pageSize;

    @SerializedName("total")
    private int total;

    @SerializedName("totalPages")
    private int totalPages;

    public ProjectHistoryResponse() {
    }

    public ProjectHistoryResponse(List<ProjectModel> data, int page, int pageSize, int total, int totalPages) {
        this.data = data;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
    }

    public List<ProjectModel> getData() {
        return data;
    }

    public void setData(List<ProjectModel> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
