package com.example.revroom.features.history.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.revroom.R;
import com.example.revroom.core.utils.DateFormatter;
import com.example.revroom.databinding.ItemProjectBinding;
import com.example.revroom.features.history.model.ProjectModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final List<ProjectModel> projectList = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(ProjectModel project);
    }

    public ProjectAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setProjects(List<ProjectModel> newProjects) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return projectList.size();
            }

            @Override
            public int getNewListSize() {
                return newProjects.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                String oldId = projectList.get(oldItemPosition).getDesignId();
                String newId = newProjects.get(newItemPosition).getDesignId();
                return oldId != null && oldId.equals(newId);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ProjectModel oldItem = projectList.get(oldItemPosition);
                ProjectModel newItem = newProjects.get(newItemPosition);
                
                boolean sameStatus = oldItem.getStatus() != null && oldItem.getStatus().equals(newItem.getStatus());
                boolean sameOriginal = oldItem.getOriginalImageUrl() != null && oldItem.getOriginalImageUrl().equals(newItem.getOriginalImageUrl());
                
                boolean sameDesigned = false;
                if (oldItem.getDesignedImageUrl() == null && newItem.getDesignedImageUrl() == null) {
                    sameDesigned = true;
                } else if (oldItem.getDesignedImageUrl() != null && oldItem.getDesignedImageUrl().equals(newItem.getDesignedImageUrl())) {
                    sameDesigned = true;
                }
                
                return sameStatus && sameOriginal && sameDesigned;
            }
        });

        this.projectList.clear();
        this.projectList.addAll(newProjects);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectBinding binding = ItemProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(projectList.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemProjectBinding binding;
        private final Context context;

        ProjectViewHolder(ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        void bind(final ProjectModel project, final OnItemClickListener listener) {
            // Load Thumbnail (Prefers designed image outcome, falls back to original)
            String imageUrl = project.getDesignedImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageUrl = project.getOriginalImageUrl();
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_gallery_empty)
                    .error(R.drawable.ic_gallery_empty)
                    .apply(new RequestOptions().transform(new CenterCrop()))
                    .into(binding.imgThumbnail);

            // Bind project ID/Name
            String designId = project.getDesignId();
            if (designId != null && designId.length() > 8) {
                // Shorten UUID for aesthetic card look
                binding.txtProjectName.setText("Project #" + designId.substring(0, 8).toUpperCase());
            } else {
                binding.txtProjectName.setText("Project #" + (designId != null ? designId : "N/A"));
            }

            // Parse and format Date
            String rawDate = project.getCreatedAt();
            binding.txtCreatedDate.setText(DateFormatter.formatToDayMonthYear(rawDate));

            // Set up Status Badge
            String status = project.getStatus();
            if ("Completed".equalsIgnoreCase(status)) {
                binding.txtStatusBadge.setText("Completed");
                binding.txtStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_completed));
                binding.txtStatusBadge.setVisibility(View.VISIBLE);
            } else if ("Failed".equalsIgnoreCase(status)) {
                binding.txtStatusBadge.setText("Failed");
                binding.txtStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_failed));
                binding.txtStatusBadge.setVisibility(View.VISIBLE);
            } else {
                binding.txtStatusBadge.setText("Processing");
                binding.txtStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_pending));
                binding.txtStatusBadge.setVisibility(View.VISIBLE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(project);
                }
            });
        }
    }
}
