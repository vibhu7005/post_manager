package com.example.demoapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapplication.data.model.Post
import com.example.demoapplication.data.repository.PostRepositoryImpl
import com.example.demoapplication.ui.theme.DemoApplicationTheme
import com.example.demoapplication.ui.viewmodel.PostViewModel
import com.example.demoapplication.ui.viewmodel.PostUiState
import com.example.demoapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private val postViewModel: PostViewModel by viewModels {
        ViewModelFactory { PostViewModel(PostRepositoryImpl) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Structure()
        }
    }
}

@Composable
fun PostListScreen(
    modifier: Modifier = Modifier,
    viewModel: PostViewModel = viewModel(
        factory = ViewModelFactory { PostViewModel(PostRepositoryImpl) }
    )
) {
    val uiState by viewModel.uiState.observeAsState(initial = PostUiState())
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(uiState.posts) { post ->
                        PostItem(post = post)
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "User ID: ${post.userId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun Structure() {
    Column {
        var name = remember { mutableStateOf("John") }
        
        Text("Name: $name")
        AutoGreeter(name = name)
        
        Button(onClick = { name.value = if (name.value == "John") "Jane" else "John" }) {
            Text("Toggle Name")
        }
    }
}

@Composable
fun AutoGreeter(name: MutableState<String>) {
//    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(name.value)
//    }
    val currentName by rememberUpdatedState(name)
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            Log.d("MainActivity", "Hello ${name.value}!") // Always uses latest name
        }
    }
}

@Composable
fun MyButton(onClick: ()-> Unit) {
    onClick.invoke()
}

//@Preview(showBackground = true)
//@Composable
//fun PostItemPreview() {
//    DemoApplicationTheme {
//        PostItem(
//            post = Post(
//                id = 1,
//                userId = 1,
//                title = "Sample Post Title",
//                body = "This is a sample post body that demonstrates how the post will look in the UI."
//            )
//        )
//    }
//}