package com.example.demoapplication.data.api

import com.example.demoapplication.data.model.Post
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}