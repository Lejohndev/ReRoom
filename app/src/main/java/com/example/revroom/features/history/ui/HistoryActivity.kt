package com.example.revroom.features.history.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.revroom.databinding.ActivityHistoryBinding
import com.example.revroom.features.history.adapters.ProjectAdapter
import com.example.revroom.features.history.model.ProjectModel
import com.example.revroom.features.history.viewmodel.HistoryViewModel
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(this)
    }
    private lateinit var adapter: ProjectAdapter

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val deletedId = result.data?.getStringExtra(ProjectDetailActivity.EXTRA_DELETED_ID)
            if (deletedId != null) {
                viewModel.removeProjectFromList(deletedId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Initial load
        viewModel.fetchProjects(1)
    }

    private fun setupRecyclerView() {
        adapter = ProjectAdapter { project ->
            navigateToDetail(project)
        }

        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.rvProjects.layoutManager = gridLayoutManager
        binding.rvProjects.adapter = adapter

        // Custom endless scroll pagination listener
        binding.rvProjects.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // Check scroll down
                    val visibleItemCount = gridLayoutManager.childCount
                    val totalItemCount = gridLayoutManager.itemCount
                    val pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition()

                    val isLoading = viewModel.isLoading.value ?: false
                    val hasMore = viewModel.hasMore()

                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                            val nextPage = viewModel.currentPage + 1
                            viewModel.fetchProjects(nextPage)
                        }
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshProjects()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refreshProjects()
        }
    }

    private fun observeViewModel() {
        viewModel.projects.observe(this) { projects ->
            adapter.setProjects(projects)
            updateUiStates(projects, viewModel.isLoading.value ?: false, viewModel.error.value)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Update refreshing status of SwipeRefreshLayout
            binding.swipeRefreshLayout.isRefreshing = isLoading
            
            val currentList = viewModel.projects.value ?: emptyList()
            // Only show central spinner if loading first page and list is currently empty
            if (isLoading && currentList.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                val currentList = viewModel.projects.value ?: emptyList()
                if (currentList.isEmpty()) {
                    binding.layoutError.visibility = View.VISIBLE
                    binding.txtErrorMessage.text = errorMessage
                    binding.rvProjects.visibility = View.GONE
                } else {
                    Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { viewModel.fetchProjects(viewModel.currentPage) }
                        .show()
                }
            } else {
                binding.layoutError.visibility = View.GONE
            }
        }
    }

    private fun updateUiStates(projects: List<ProjectModel>, isLoading: Boolean, error: String?) {
        if (projects.isEmpty()) {
            binding.rvProjects.visibility = View.GONE
            if (error != null) {
                binding.layoutError.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
            } else if (!isLoading) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE
            }
        } else {
            binding.rvProjects.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            binding.layoutError.visibility = View.GONE
        }
    }

    private fun navigateToDetail(project: ProjectModel) {
        val intent = Intent(this, ProjectDetailActivity::class.java).apply {
            putExtra(EXTRA_DESIGN_ID, project.designId)
            putExtra(EXTRA_ORIGINAL_IMAGE, project.originalImageUrl)
            putExtra(EXTRA_DESIGNED_IMAGE, project.designedImageUrl)
            putExtra(EXTRA_STATUS, project.status)
            putExtra(EXTRA_CREATED_AT, project.createdAt)
        }
        detailLauncher.launch(intent)
    }

    companion object {
        const val EXTRA_DESIGN_ID = "extra_design_id"
        const val EXTRA_ORIGINAL_IMAGE = "extra_original_image"
        const val EXTRA_DESIGNED_IMAGE = "extra_designed_image"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_CREATED_AT = "extra_created_at"
    }
}
