package com.example.demoapplication.data.model

sealed class PostUiState {
    class PostLoaded(val posts: List<Post>) : PostUiState()
    class Error(val message : String) : PostUiState()
    object Loading : PostUiState()
    object Idle : PostUiState()
}