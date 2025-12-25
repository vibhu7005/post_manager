package com.example.demoapplication.data.repository

import com.example.demoapplication.data.model.Quote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Response
import android.util.Log

interface QuoteApiService {
    @GET("random")
    suspend fun getRandomQuote(): Response<QuoteApiResponse>
}

data class QuoteApiResponse(
    val content: String,
    val author: String
)

class QuoteRepository {
    
    companion object {
        private const val TAG = "QuoteRepository"
        private const val BASE_URL = "https://api.quotable.io/"
    }
    
    private val apiService: QuoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApiService::class.java)
    }
    
    private val quotes = mutableListOf<Quote>()
    private var nextId = 1
    
    suspend fun getRandomQuote(): Result<Quote> {
        return try {
            Log.d(TAG, "Fetching random quote from API...")
            val response = apiService.getRandomQuote()
            
            if (response.isSuccessful && response.body() != null) {
                val apiQuote = response.body()!!
                val quote = Quote(
                    id = nextId++,
                    quote = apiQuote.content
                )
                
                // Store the quote locally
                quotes.add(quote)
                
                Log.d(TAG, "Successfully fetched quote: ${quote.quote}")
                Result.success(quote)
            } else {
                Log.e(TAG, "API call failed with code: ${response.code()}")
                Result.failure(Exception("Failed to fetch quote: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching quote", e)
            Result.failure(e)
        }
    }
    
    fun getAllQuotes(): List<Quote> {
        return quotes.toList()
    }
    
    fun getQuoteById(id: Int): Quote? {
        return quotes.find { it.id == id }
    }
    
    fun getQuotesCount(): Int {
        return quotes.size
    }
    
    fun clearAllQuotes() {
        quotes.clear()
        nextId = 1
    }
}