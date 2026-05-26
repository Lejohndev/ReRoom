package com.example.revroom.features.history.viewmodel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.revroom.core.network.ApiClient;
import com.example.revroom.data.local.LocalUserIdProvider;
import com.example.revroom.data.remote.ApiService;
import com.example.revroom.data.remote.DeleteResponse;
import com.example.revroom.data.remote.ProjectHistoryResponse;
import com.example.revroom.features.history.model.ProjectModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryViewModel extends ViewModel {
    private final ApiService apiService;
    private final LocalUserIdProvider userIdProvider;

    private final MutableLiveData<List<ProjectModel>> projectsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    
    private final MutableLiveData<Boolean> isDeletingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteSuccessLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> deleteErrorLiveData = new MutableLiveData<>(null);

    private int currentPage = 1;
    private int totalPages = 1;
    private boolean hasMore = true;

    public HistoryViewModel(Context context) {
        this.apiService = ApiClient.INSTANCE.getRetrofitInstance().create(ApiService.class);
        this.userIdProvider = new LocalUserIdProvider(context);
    }

    public LiveData<List<ProjectModel>> getProjects() {
        return projectsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsDeleting() {
        return isDeletingLiveData;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccessLiveData;
    }

    public LiveData<String> getDeleteError() {
        return deleteErrorLiveData;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void refreshProjects() {
        currentPage = 1;
        hasMore = true;
        projectsLiveData.setValue(new ArrayList<>());
        fetchProjects(1);
    }

    public void fetchProjects(int page) {
        if (Boolean.TRUE.equals(isLoadingLiveData.getValue())) return;
        if (page > 1 && !hasMore) return;

        isLoadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        String userId = userIdProvider.getOrCreateUserId();
        android.util.Log.d("HistoryVM", "fetchProjects: userId = " + userId + ", page = " + page);
        apiService.getProjects(userId, page, 10).enqueue(new Callback<ProjectHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectHistoryResponse> call, @NonNull Response<ProjectHistoryResponse> response) {
                isLoadingLiveData.setValue(false);
                android.util.Log.d("HistoryVM", "onResponse: code = " + response.code() + ", isSuccessful = " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    ProjectHistoryResponse body = response.body();
                    currentPage = body.getPage();
                    totalPages = body.getTotalPages();
                    hasMore = currentPage < totalPages;

                    List<ProjectModel> newProjects = body.getData();
                    android.util.Log.d("HistoryVM", "onResponse success: size = " + (newProjects != null ? newProjects.size() : "null"));
                    List<ProjectModel> currentList = projectsLiveData.getValue();
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                    }

                    if (page == 1) {
                        currentList = new ArrayList<>(newProjects);
                    } else {
                        // Prevent duplicates and append
                        List<ProjectModel> filteredList = new ArrayList<>(currentList);
                        for (ProjectModel newProj : newProjects) {
                            boolean exists = false;
                            for (ProjectModel existingProj : filteredList) {
                                if (existingProj.getDesignId() != null && 
                                    existingProj.getDesignId().equals(newProj.getDesignId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                filteredList.add(newProj);
                            }
                        }
                        currentList = filteredList;
                    }

                    projectsLiveData.setValue(currentList);
                } else {
                    android.util.Log.e("HistoryVM", "onResponse error: code = " + response.code());
                    errorLiveData.setValue("Failed to load projects: Status " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectHistoryResponse> call, @NonNull Throwable t) {
                isLoadingLiveData.setValue(false);
                android.util.Log.e("HistoryVM", "onFailure: " + t.getLocalizedMessage(), t);
                errorLiveData.setValue("Network error: " + t.getLocalizedMessage());
            }
        });
    }

    public void removeProjectFromList(String designId) {
        List<ProjectModel> currentList = projectsLiveData.getValue();
        if (currentList != null) {
            List<ProjectModel> updatedList = new ArrayList<>(currentList);
            for (int i = 0; i < updatedList.size(); i++) {
                if (updatedList.get(i).getDesignId() != null && 
                    updatedList.get(i).getDesignId().equals(designId)) {
                    updatedList.remove(i);
                    break;
                }
            }
            projectsLiveData.setValue(updatedList);
        }
    }

    public void deleteProject(String designId) {
        if (Boolean.TRUE.equals(isDeletingLiveData.getValue())) return;

        isDeletingLiveData.setValue(true);
        deleteErrorLiveData.setValue(null);
        deleteSuccessLiveData.setValue(false);

        String userId = userIdProvider.getOrCreateUserId();
        apiService.deleteProject(userId, designId).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteResponse> call, @NonNull Response<DeleteResponse> response) {
                isDeletingLiveData.setValue(false);
                if (response.isSuccessful()) {
                    deleteSuccessLiveData.setValue(true);
                    
                    // Remove the project from our local list to update UI immediately
                    List<ProjectModel> currentList = projectsLiveData.getValue();
                    if (currentList != null) {
                        List<ProjectModel> updatedList = new ArrayList<>(currentList);
                        for (int i = 0; i < updatedList.size(); i++) {
                            if (updatedList.get(i).getDesignId() != null && 
                                updatedList.get(i).getDesignId().equals(designId)) {
                                updatedList.remove(i);
                                break;
                            }
                        }
                        projectsLiveData.setValue(updatedList);
                    }
                } else {
                    deleteErrorLiveData.setValue("Failed to delete project: Status " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteResponse> call, @NonNull Throwable t) {
                isDeletingLiveData.setValue(false);
                deleteErrorLiveData.setValue("Network error: " + t.getLocalizedMessage());
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context context;

        public Factory(Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
                return (T) new HistoryViewModel(context);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
