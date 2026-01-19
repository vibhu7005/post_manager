# SDE 3 Android Developer Interview Preparation Guide

## Table of Contents
1. [Architecture & Design Patterns](#architecture--design-patterns)
2. [Jetpack Compose](#jetpack-compose)
3. [Kotlin & Coroutines](#kotlin--coroutines)
4. [Networking & API Design](#networking--api-design)
5. [Performance Optimization](#performance-optimization)
6. [Memory Management](#memory-management)
7. [Background Processing](#background-processing)
8. [Testing Strategies](#testing-strategies)
9. [System Design](#system-design)
10. [Leadership & Mentoring](#leadership--mentoring)

---

## Architecture & Design Patterns

### Clarification Points

1. **MVVM vs MVI vs Clean Architecture**
   - When to use each pattern
   - Trade-offs between complexity and maintainability
   - How to handle state management in each

2. **Repository Pattern**
   - Single source of truth principle
   - Caching strategies (memory, disk, network)
   - Handling offline scenarios
   - Data synchronization conflicts

3. **Dependency Injection**
   - Manual DI vs Hilt vs Koin
   - Scoping (Singleton, Activity, ViewModel)
   - Testing with DI frameworks

### Interview Questions

**Q1:** Your current `PostRepositoryImpl` is a singleton object. How would you refactor it to support:
- Multiple data sources (API, local DB, cache)
- Offline-first architecture
- Data synchronization
- Unit testing with mock repositories

**Q2:** Design a scalable architecture for an app that needs to:
- Support multiple API endpoints
- Handle real-time updates (WebSocket)
- Work offline with local caching
- Support multiple user sessions
- Handle deep linking

**Q3:** How would you implement a Repository pattern that:
- Caches API responses in Room database
- Invalidates cache based on TTL
- Handles concurrent requests efficiently
- Provides offline-first experience

**Q4:** Explain the differences between:
- `LiveData` vs `StateFlow` vs `SharedFlow`
- When to use each in Compose vs View system
- Thread safety considerations

---

## Jetpack Compose

### Clarification Points

1. **Recomposition Optimization**
   - Understanding recomposition scope
   - `remember`, `derivedStateOf`, `LaunchedEffect`
   - Stable vs unstable parameters
   - `@Stable` and `@Immutable` annotations

2. **State Management**
   - State hoisting principles
   - Unidirectional data flow
   - State machines for complex UIs
   - State restoration

3. **Performance**
   - LazyColumn vs Column
   - Key strategies for lists
   - Modifier chaining and performance
   - Composition vs layout phases

### Interview Questions

**Q1:** Your `PostViewModel` uses `LiveData` with Compose. How would you:
- Migrate to `StateFlow`/`Flow`
- Optimize recomposition
- Handle state restoration
- Implement proper error states

**Q2:** Design a Compose screen that:
- Shows a list of 10,000 items
- Supports pull-to-refresh
- Implements infinite scrolling
- Handles loading, error, and empty states
- Optimizes for performance

**Q3:** Explain the Compose lifecycle:
- Composition
- Layout
- Drawing
- How to optimize each phase

**Q4:** How would you implement:
- Custom animations in Compose
- Shared element transitions
- Complex gesture handling
- Custom layouts

---

## Kotlin & Coroutines

### Clarification Points

1. **Coroutine Scopes**
   - `viewModelScope` vs `lifecycleScope` vs `GlobalScope`
   - Structured concurrency
   - Coroutine cancellation
   - Exception handling in coroutines

2. **Flow vs Channels**
   - When to use `Flow` vs `Channel`
   - Cold vs hot flows
   - Backpressure handling
   - StateFlow vs SharedFlow

3. **Advanced Kotlin**
   - Inline functions and reified generics
   - Sealed classes vs enums
   - Delegates (lazy, observable, etc.)
   - Extension functions best practices

### Interview Questions

**Q1:** Your `loadPosts()` function uses `viewModelScope.launch`. How would you:
- Handle cancellation properly
- Implement retry logic with exponential backoff
- Handle multiple concurrent requests
- Prevent duplicate requests

**Q2:** Design a coroutine-based solution for:
- Downloading multiple files with progress tracking
- Handling network failures gracefully
- Canceling downloads
- Resuming interrupted downloads

**Q3:** Explain the difference between:
- `launch` vs `async`
- `flow` vs `channel`
- `StateFlow` vs `SharedFlow`
- `collect` vs `collectLatest`

**Q4:** How would you implement:
- A debounced search with Flow
- A rate limiter for API calls
- A connection pool manager
- A task queue with priority

---

## Networking & API Design

### Clarification Points

1. **Retrofit Best Practices**
   - Interceptor chains
   - Error handling strategies
   - Request/response logging
   - Certificate pinning

2. **API Design**
   - RESTful principles
   - GraphQL considerations
   - WebSocket implementation
   - gRPC for Android

3. **Error Handling**
   - HTTP error codes
   - Network timeouts
   - Retry strategies
   - Offline handling

### Interview Questions

**Q1:** Your `RetrofitClient` uses a simple OkHttpClient. How would you:
- Implement request/response interceptors
- Add authentication (token refresh)
- Handle SSL pinning
- Implement request queuing
- Add offline support

**Q2:** Design a networking layer that:
- Supports multiple API endpoints
- Handles token refresh automatically
- Implements request/response caching
- Provides offline-first support
- Logs requests for debugging

**Q3:** How would you handle:
- 401 Unauthorized (token expired)
- 429 Too Many Requests (rate limiting)
- Network timeouts
- SSL certificate errors
- Large file downloads/uploads

**Q4:** Explain the differences between:
- OkHttp interceptors vs Retrofit converters
- Synchronous vs asynchronous API calls
- Callback vs Coroutine vs RxJava
- HTTP/1.1 vs HTTP/2 vs HTTP/3

---

## Performance Optimization

### Clarification Points

1. **App Startup**
   - Application initialization
   - Content providers
   - Lazy initialization
   - App startup library

2. **Memory Optimization**
   - Heap dumps analysis
   - Memory leaks detection
   - Bitmap optimization
   - Object pooling

3. **UI Performance**
   - Frame drops detection
   - Overdraw reduction
   - Layout optimization
   - Rendering performance

### Interview Questions

**Q1:** Your app is experiencing:
- Slow startup time (3+ seconds)
- High memory usage
- Frame drops during scrolling
- ANRs in production

How would you diagnose and fix each issue?

**Q2:** Design a performance monitoring system that:
- Tracks app startup time
- Monitors memory usage
- Detects frame drops
- Reports ANRs
- Provides actionable insights

**Q3:** How would you optimize:
- Image loading and caching
- List rendering (10,000+ items)
- Database queries
- Network requests
- View inflation

**Q4:** Explain:
- How Android's garbage collector works
- Memory leak detection tools
- Profiling with Android Studio
- Systrace vs Perfetto

---

## Memory Management

### Clarification Points

1. **Memory Leaks**
   - Common causes (static references, listeners, etc.)
   - Detection tools (LeakCanary, Memory Profiler)
   - Prevention strategies
   - WeakReference vs SoftReference

2. **Memory Optimization**
   - Heap size management
   - Bitmap handling
   - Large object handling
   - Memory-efficient collections

### Interview Questions

**Q1:** Identify potential memory leaks in this code:
```kotlin
class MainActivity : ComponentActivity() {
    private val listener = object : SomeListener {
        override fun onEvent() { /* ... */ }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        SomeManager.getInstance().addListener(listener)
    }
}
```

**Q2:** How would you:
- Detect memory leaks in production
- Implement memory-efficient image loading
- Handle large datasets without OOM
- Optimize bitmap memory usage

**Q3:** Explain:
- Java heap vs native heap
- Memory allocation strategies
- Garbage collection types
- Memory profiling techniques

---

## Background Processing

### Clarification Points

1. **WorkManager**
   - When to use WorkManager vs other solutions
   - Constraints and scheduling
   - Chained work
   - Testing background work

2. **Services**
   - Foreground vs background services
   - Bound services
   - JobScheduler vs WorkManager
   - Background execution limits

3. **Background Tasks**
   - Doze mode handling
   - App standby buckets
   - Background location updates
   - Push notifications

### Interview Questions

**Q1:** You have services in your project (`DownloadSongService`, `MusicPlayerService`). How would you:
- Migrate to WorkManager if needed
- Handle foreground services properly
- Implement proper service lifecycle
- Test background work

**Q2:** Design a background sync system that:
- Syncs data every 15 minutes
- Works offline
- Handles conflicts
- Respects battery constraints
- Works across app restarts

**Q3:** How would you implement:
- Periodic background tasks
- One-time delayed tasks
- Chained work dependencies
- Parallel work execution
- Work cancellation

**Q4:** Explain:
- WorkManager vs JobScheduler vs AlarmManager
- Foreground service requirements
- Background execution limits (Android 8+)
- Doze mode and app standby

---

## Testing Strategies

### Clarification Points

1. **Unit Testing**
   - Testing ViewModels
   - Testing Repositories
   - Mocking dependencies
   - Testing coroutines

2. **Integration Testing**
   - Testing API calls
   - Testing database operations
   - Testing navigation
   - End-to-end testing

3. **UI Testing**
   - Compose testing
   - Espresso for View system
   - Screenshot testing
   - Accessibility testing

### Interview Questions

**Q1:** How would you test:
- `PostViewModel` with coroutines
- `PostRepositoryImpl` with Retrofit
- Compose UI components
- Navigation between screens

**Q2:** Design a testing strategy for:
- Unit tests (fast, isolated)
- Integration tests (API, DB)
- UI tests (Compose, Espresso)
- E2E tests (critical flows)

**Q3:** How would you:
- Mock Retrofit API calls
- Test coroutine-based code
- Test Compose recomposition
- Test state restoration

**Q4:** Explain:
- Test doubles (mocks, stubs, fakes)
- Test coverage metrics
- CI/CD integration
- Flaky test handling

---

## System Design

### Clarification Points

1. **Scalability**
   - Handling millions of users
   - Database design
   - Caching strategies
   - Load balancing

2. **Architecture Patterns**
   - Microservices vs monolith
   - Event-driven architecture
   - CQRS pattern
   - Domain-driven design

### Interview Questions

**Q1:** Design a photo-sharing app (like Instagram) that:
- Handles millions of users
- Supports real-time features
- Works offline
- Syncs across devices
- Handles media uploads efficiently

**Q2:** Design a messaging app that:
- Supports 1-on-1 and group chats
- Handles offline messages
- Syncs across devices
- Supports media sharing
- Scales to millions of users

**Q3:** How would you design:
- A news feed system
- A recommendation engine
- A search system
- A notification system

**Q4:** Explain:
- Database sharding strategies
- Caching layers (L1, L2, L3)
- CDN for media delivery
- API rate limiting

---

## Leadership & Mentoring

### Clarification Points

1. **Code Reviews**
   - What to look for
   - Providing constructive feedback
   - Balancing speed vs quality
   - Mentoring junior developers

2. **Technical Decisions**
   - Technology selection
   - Architecture decisions
   - Trade-off analysis
   - Risk assessment

### Interview Questions

**Q1:** A junior developer submits a PR that:
- Works but has performance issues
- Lacks proper error handling
- Has no tests
- Doesn't follow team conventions

How would you provide feedback?

**Q2:** Your team needs to decide between:
- MVVM vs MVI
- Room vs Realm
- Retrofit vs Ktor
- Compose vs XML

How would you facilitate the decision?

**Q3:** How would you:
- Onboard a new team member
- Share knowledge across the team
- Handle technical debt
- Plan a major refactoring

**Q4:** Describe:
- A time you made a technical mistake and learned from it
- A challenging technical problem you solved
- How you've mentored others
- Your approach to code reviews

---

## Additional Topics to Review

### Security
- Data encryption (at rest, in transit)
- Certificate pinning
- ProGuard/R8 rules
- Secure storage (Keystore)
- OAuth 2.0 / OIDC

### Build System
- Gradle optimization
- Build variants
- ProGuard/R8
- Multi-module projects
- Dependency management

### Kotlin Multiplatform
- KMP architecture
- Shared business logic
- Platform-specific implementations
- Testing KMP code

### Modern Android
- Material Design 3
- Edge-to-edge UI
- Predictive back gesture
- Dynamic colors
- Android 14+ features

---

## Practice Scenarios

### Scenario 1: Performance Issue
Your app's feed screen is slow. Users report:
- 2-second load time
- Janky scrolling
- High battery drain
- Crashes on low-end devices

**Approach:**
1. Profile the app (CPU, Memory, Network)
2. Identify bottlenecks
3. Propose solutions
4. Implement optimizations
5. Measure improvements

### Scenario 2: Architecture Migration
Migrate a legacy app from:
- XML Views → Jetpack Compose
- AsyncTask → Coroutines
- SQLite → Room
- Manual DI → Hilt

**Approach:**
1. Create migration plan
2. Set up feature flags
3. Migrate incrementally
4. Maintain backward compatibility
5. Remove old code

### Scenario 3: Feature Addition
Add a new feature: "Offline Mode"
- Download content for offline viewing
- Sync when online
- Handle conflicts
- Show offline indicator

**Approach:**
1. Design architecture
2. Implement data layer
3. Add UI components
4. Handle edge cases
5. Test thoroughly

---

## Resources for Deep Dive

1. **Android Developer Documentation**
   - Architecture Components
   - Jetpack Compose
   - Performance best practices

2. **Kotlin Documentation**
   - Coroutines guide
   - Flow documentation
   - Kotlin style guide

3. **Books**
   - "Android Internals" by Jonathan Levin
   - "Effective Kotlin" by Marcin Moskala
   - "Clean Architecture" by Robert C. Martin

4. **Blogs**
   - Android Developers Blog
   - Kotlin Blog
   - Square Engineering Blog

---

## Final Tips

1. **Be Specific**: Use examples from your experience
2. **Think Aloud**: Explain your thought process
3. **Ask Questions**: Clarify requirements before solving
4. **Consider Trade-offs**: Every solution has pros/cons
5. **Think at Scale**: Consider millions of users
6. **Security First**: Always consider security implications
7. **Test Everything**: Testing is crucial
8. **Document Decisions**: Explain why, not just what

Good luck with your interview preparation! 🚀
