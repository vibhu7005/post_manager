package com.example.demoapplication.data.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapplication.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DemoViewModel(val repo: PostRepository) : ViewModel() {
    val _postUiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val postUiState = _postUiState.asStateFlow()
    fun fetchPosts() {
        viewModelScope.launch {
            val result = repo.getPosts()
            _postUiState.emit(PostUiState.Loading)
            result.onSuccess { posts ->
                _postUiState.emit(PostUiState.PostLoaded(posts))
            }.onFailure { exception ->
                _postUiState.emit(PostUiState.Error(exception.message ?: "Unknown Error"))
            }
        }
    }
}