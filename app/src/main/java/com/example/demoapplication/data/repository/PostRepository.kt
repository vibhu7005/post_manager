package com.example.demoapplication.data.repository

import com.example.demoapplication.data.model.Post

interface PostRepository {
    suspend fun getPosts(): Result<List<Post>>
}