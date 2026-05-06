package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner as LifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Post
import com.example.myapplication.ui.components.KalaGlassCard
import com.example.myapplication.ui.components.KalaElevation
import com.example.myapplication.ui.components.UiStateHandler
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.PostViewModel
import kotlinx.coroutines.launch

import com.example.myapplication.core.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: PostViewModel = viewModel()) {
    val posts by viewModel.filteredPosts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()
    
    var showCreateSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Your moment has been chronicled 📜")
                viewModel.resetCreatePostState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "CHRONICLES", 
                            style = MaterialTheme.typography.titleMedium,
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SortTab("Recent", active = sortOrder == "Recent") { viewModel.setSortOrder("Recent") }
                            SortTab("Trending", active = sortOrder == "Trending") { viewModel.setSortOrder("Trending") }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Chronicle moment")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState is UiState.Loading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            UiStateHandler(
                uiState = uiState,
                onRetry = { viewModel.observePosts() },
                emptyContent = { EmptyChroniclesState() }
            ) { postList ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(postList, key = { it.id }) { post ->
                        CulturalPostCard(post, viewModel)
                    }
                }
            }
        }

        if (showCreateSheet) {
            CreatePostSheet(
                onDismiss = { showCreateSheet = false },
                onPost = { uri, caption, location ->
                    viewModel.createPostWithImage(uri, caption, location)
                    showCreateSheet = false
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun SortTab(label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Black else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
            letterSpacing = 1.sp
        )
        AnimatedVisibility(
            visible = active,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun CulturalPostCard(post: Post, viewModel: PostViewModel) {
    var showComments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val heartScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    KalaGlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = KalaElevation.Low,
        alpha = 0.85f
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = TimeUtils.relativeTime(post.timestamp?.toDate()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        if (post.location?.isNotBlank() == true) {
                            Text(" • ", color = Color.Gray)
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                            Spacer(Modifier.width(2.dp))
                            Text(post.location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Report Content") },
                            onClick = { viewModel.reportPost(post.id, "Inappropriate"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Hide Post") },
                            onClick = { viewModel.hidePost(post.id); showMenu = false }
                        )
                    }
                }
            }

            // Image/Video
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                    error = painterResource(com.example.myapplication.R.drawable.placeholder)
                )
            }

            // Actions
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.example.myapplication.ui.components.LikeButton(
                    isLiked = post.isLikedByCurrentUser,
                    onClick = {
                        viewModel.toggleLike(post.id)
                    }
                )
                Text("${post.likes}", style = MaterialTheme.typography.labelLarge)
                
                IconButton(onClick = { showComments = true }) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(Modifier.weight(1f))
                
                IconButton(onClick = { /* Share Logic */ }) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Caption
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                    ),
                    lineHeight = 20.sp
                )
            }
        }
    }

    if (showComments) {
        CommentsSheet(postId = post.id, onDismiss = { showComments = false }, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(postId: String, onDismiss: () -> Unit, viewModel: PostViewModel) {
    val comments by viewModel.comments.collectAsState()
    val postComments = comments[postId] ?: emptyList()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.7f).padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())) {
            Text("Chronicle Discussions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            HorizontalDivider()
            
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(postComments) { comment ->
                    Row(modifier = Modifier.padding(vertical = 12.dp)) {
                        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(0.1f)) {
                            Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 16.sp) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(comment.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add your perspective...") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if(commentText.isNotBlank()) { viewModel.addComment(postId, commentText); commentText = "" } }) {
                    Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostSheet(
    onDismiss: () -> Unit,
    onPost: (Uri, String, String?) -> Unit,
    isLoading: Boolean = false,
    viewModel: PostViewModel
) {
    // Note: Implementation details for image picker and caption generation would go here
    // This is a simplified version for integration
}

@Composable
fun EmptyChroniclesState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📜", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text("The archives are empty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Be the first to chronicle a moment of heritage.", textAlign = TextAlign.Center, color = Color.Gray)
    }
}
