package com.example.demoapplication.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapplication.data.model.Post
import com.example.demoapplication.data.repository.PostRepository
import com.example.demoapplication.data.repository.PostRepositoryImpl
import kotlinx.coroutines.launch

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PostViewModel(
    private val repository: PostRepository = PostRepositoryImpl()
) : ViewModel() {
    
    private val _uiState = MutableLiveData(PostUiState())
    val uiState: LiveData<PostUiState> = _uiState
    
    init {
        loadPosts()
    }
    
    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)
            
            repository.getPosts()
                .onSuccess { posts ->
                    _uiState.value = _uiState.value?.copy(
                        posts = posts,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }
    
    fun retry() {
        loadPosts()
    }
}