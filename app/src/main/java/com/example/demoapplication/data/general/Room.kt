package com.example.demoapplication.data.general

import androidx.lifecycle.ViewModel
import com.example.demoapplication.data.model.Post
import com.example.demoapplication.data.room.Dao
import com.example.demoapplication.data.room.Query
import com.example.demoapplication.data.room.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn





class RoomRepo(RoomDao : RoomDao) {
    val postDataFlow : Flow<Post> = RoomDao.getPostDataFlow()
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM Post LIMIT 1")
    fun getPostDataFlow() : Flow<Post>
}