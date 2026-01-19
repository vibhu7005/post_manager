# Quick Reference: Code Examples & Patterns

## 1. ViewModel with StateFlow (Better than LiveData)

```kotlin
class PostViewModel(
    private val repository: PostRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()
    
    init {
        loadPosts()
    }
    
    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            repository.getPosts()
                .onSuccess { posts ->
                    _uiState.update { 
                        it.copy(posts = posts, isLoading = false) 
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        ) 
                    }
                }
        }
    }
    
    fun retry() {
        loadPosts()
    }
}
```

## 2. Repository with Caching (Offline-First)

```kotlin
class PostRepositoryImpl(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PostRepository {
    
    override fun getPosts(): Flow<Resource<List<Post>>> = flow {
        // Emit cached data first
        emit(Resource.Loading(postDao.getAllPosts()))
        
        try {
            // Fetch from network
            val posts = apiService.getPosts()
            
            // Cache in database
            postDao.insertAll(posts)
            
            // Emit success
            emit(Resource.Success(postDao.getAllPosts()))
        } catch (e: Exception) {
            // Emit error with cached data
            emit(Resource.Error(e.message ?: "Unknown error", postDao.getAllPosts()))
        }
    }.flowOn(dispatcher)
    
    override suspend fun refreshPosts(): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts()
            postDao.insertAll(posts)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 3. Retrofit with Interceptors & Error Handling

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${TokenManager.getToken()}")
            .build()
        chain.proceed(request)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val errorInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        
        when (response.code) {
            401 -> {
                // Handle token refresh
                TokenManager.refreshToken()
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${TokenManager.getToken()}")
                    .build()
                chain.proceed(newRequest)
            }
            429 -> {
                // Handle rate limiting
                throw RateLimitException("Too many requests")
            }
        }
        
        response
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

## 4. Compose Screen with StateFlow

```kotlin
@Composable
fun PostScreen(
    viewModel: PostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading && uiState.posts.isEmpty() -> {
            LoadingScreen()
        }
        uiState.errorMessage != null && uiState.posts.isEmpty() -> {
            ErrorScreen(
                message = uiState.errorMessage,
                onRetry = { viewModel.retry() }
            )
        }
        else -> {
            PostListScreen(
                posts = uiState.posts,
                isLoading = uiState.isLoading,
                onRetry = { viewModel.retry() }
            )
        }
    }
}

@Composable
fun PostListScreen(
    posts: List<Post>,
    isLoading: Boolean,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = posts,
            key = { it.id }
        ) { post ->
            PostItem(post = post)
        }
        
        if (isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
```

## 5. Coroutine with Retry & Backoff

```kotlin
suspend fun loadPostsWithRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L
): Result<List<Post>> {
    var currentDelay = initialDelay
    
    repeat(maxRetries) { attempt ->
        try {
            val posts = apiService.getPosts()
            return Result.success(posts)
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) {
                return Result.failure(e)
            }
            
            delay(currentDelay)
            currentDelay *= 2 // Exponential backoff
        }
    }
    
    return Result.failure(Exception("Max retries exceeded"))
}
```

## 6. WorkManager for Background Tasks

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Perform sync operation
            val repository = PostRepositoryImpl(...)
            repository.refreshPosts()
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

// Schedule work
fun scheduleSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
    
    val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .setInitialDelay(15, TimeUnit.MINUTES)
        .build()
    
    WorkManager.getInstance(context).enqueue(syncWork)
}
```

## 7. Room Database with Flow

```kotlin
@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): Post?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)
    
    @Delete
    suspend fun delete(post: Post)
    
    @Query("DELETE FROM posts")
    suspend fun deleteAll()
}

@Database(entities = [Post::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
```

## 8. Dependency Injection with Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    // ...
}
```

## 9. Testing ViewModel

```kotlin
class PostViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var repository: PostRepository
    private lateinit var viewModel: PostViewModel
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = PostViewModel(repository)
    }
    
    @Test
    fun `loadPosts emits loading then success`() = runTest {
        // Given
        val posts = listOf(Post(1, "Title", "Body"))
        coEvery { repository.getPosts() } returns Result.success(posts)
        
        // When
        viewModel.loadPosts()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(posts, uiState.posts)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `loadPosts emits error on failure`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { repository.getPosts() } returns Result.failure(error)
        
        // When
        viewModel.loadPosts()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.posts.isEmpty())
        assertFalse(uiState.isLoading)
        assertEquals("Network error", uiState.errorMessage)
    }
}
```

## 10. Resource Wrapper for API Responses

```kotlin
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val data: T? = null) : Resource<Nothing>()
    data class Loading<out T>(val data: T? = null) : Resource<T>()
}

// Usage in Repository
override fun getPosts(): Flow<Resource<List<Post>>> = flow {
    emit(Resource.Loading())
    
    try {
        val posts = apiService.getPosts()
        emit(Resource.Success(posts))
    } catch (e: Exception) {
        emit(Resource.Error(e.message ?: "Unknown error"))
    }
}
```

## 11. Debounced Search with Flow

```kotlin
fun searchPosts(query: String): Flow<List<Post>> = flow {
    delay(300) // Debounce delay
    val results = repository.searchPosts(query)
    emit(results)
}.flowOn(Dispatchers.IO)

// Usage
viewModelScope.launch {
    searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                searchPosts(query)
            }
        }
        .collect { results ->
            _searchResults.value = results
        }
}
```

## 12. Image Loading with Coil

```kotlin
@Composable
fun PostImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

## 13. Navigation with Compose

```kotlin
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "posts"
    ) {
        composable("posts") {
            PostScreen(
                onPostClick = { postId ->
                    navController.navigate("post/$postId")
                }
            )
        }
        
        composable(
            route = "post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            PostDetailScreen(postId = postId)
        }
    }
}
```

## 14. State Restoration

```kotlin
@Composable
fun PostScreen(
    viewModel: PostViewModel = hiltViewModel(),
    savedStateHandle: SavedStateHandle
) {
    // Save state
    LaunchedEffect(viewModel.uiState.value.posts) {
        savedStateHandle["posts"] = viewModel.uiState.value.posts
    }
    
    // Restore state
    LaunchedEffect(Unit) {
        val savedPosts = savedStateHandle.get<List<Post>>("posts")
        if (savedPosts != null) {
            viewModel.restoreState(savedPosts)
        }
    }
    
    // UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

## 15. Error Handling Pattern

```kotlin
sealed class AppError {
    object NetworkError : AppError()
    object ServerError : AppError()
    object UnknownError : AppError()
    data class CustomError(val message: String) : AppError()
}

fun handleError(throwable: Throwable): AppError {
    return when (throwable) {
        is IOException -> AppError.NetworkError
        is HttpException -> {
            when (throwable.code()) {
                in 500..599 -> AppError.ServerError
                else -> AppError.CustomError(throwable.message())
            }
        }
        else -> AppError.UnknownError
    }
}
```

---

## Key Patterns Summary

1. **State Management**: Use `StateFlow` instead of `LiveData` for Compose
2. **Repository Pattern**: Always return `Flow` for reactive data
3. **Error Handling**: Use sealed classes for type-safe error handling
4. **Coroutines**: Use `viewModelScope` for ViewModel, `lifecycleScope` for UI
5. **Testing**: Mock dependencies, use `runTest` for coroutines
6. **Caching**: Implement offline-first with Room + Flow
7. **Networking**: Use interceptors for auth, logging, error handling
8. **Background Work**: Use WorkManager for reliable background tasks
9. **DI**: Use Hilt for dependency injection
10. **Navigation**: Use Navigation Compose for type-safe navigation
