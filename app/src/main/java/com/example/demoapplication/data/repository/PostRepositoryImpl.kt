package com.example.demoapplication.data.repository

import com.example.demoapplication.data.api.RetrofitClient
import com.example.demoapplication.data.model.Post

object PostRepositoryImpl : PostRepository {
    private val apiService = RetrofitClient.apiService
    
    override suspend fun getPosts(): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts()
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}