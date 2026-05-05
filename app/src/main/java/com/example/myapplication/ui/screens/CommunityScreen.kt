package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Comment
import com.example.myapplication.data.model.Post
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.core.utils.TimeUtils
import com.example.myapplication.ui.components.UiStateHandler
import com.example.myapplication.viewmodel.PostViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import com.example.myapplication.ui.components.KalaActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: PostViewModel = viewModel()) {
    val posts by viewModel.filteredPosts.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createPostState) {
        val state = createPostState
        when (state) {
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
        containerColor = Color(0xFFFCF9F2),
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            UiStateHandler(
                uiState = uiState,
                onRetry = { /* Automatically handled by Flow subscription */ },
                emptyContent = { EmptyChroniclesState() }
            ) { posts ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts, key = { it.id }) { post ->
                        CulturalPostCard(post)
                    }
                }
            }

            if (showCreateSheet) {
                CreatePostSheet(
                    onDismiss = { showCreateSheet = false },
                    onPost = { uri, cap, loc ->
                        viewModel.createPostWithImage(uri, cap, loc)
                        showCreateSheet = false
                    },
                    isLoading = createPostState is UiState.Loading
                )
            }
        }
    }
}

@Composable
fun SortTab(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text = label.uppercase(),
        modifier = Modifier.clickable { onClick() },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (active) FontWeight.Black else FontWeight.Normal,
        color = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
        letterSpacing = 1.sp
    )
}

@Composable
fun CulturalPostCard(post: Post, viewModel: PostViewModel = viewModel()) {
    val userId = viewModel.currentUserId
    var localLiked by remember(post.id) { mutableStateOf(post.likedBy.contains(userId)) }
    var localLikeCount by remember(post.id) { mutableStateOf(post.likes) }
    var isProcessingLike by remember { mutableStateOf(false) }
    
    var showComments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(48.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Post media with aspect ratio constraint
            Box(modifier = Modifier.aspectRatio(0.8f)) {
                if (post.mediaType == "video" && post.imageUrl.isNotBlank()) {
                    VideoPlayer(videoUrl = post.imageUrl)
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(64.dp).alpha(0.7f),
                        tint = Color.White
                    )
                } else {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Location Overlay (Glassmorphism inspired)
                post.location?.let { loc ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(loc, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Content Section
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (post.userAvatar.isNotEmpty()) post.userAvatar else "https://ui-avatars.com/api/?name=${post.userName}",
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                post.userName.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                TimeUtils.formatDate(post.timestamp?.toDate()),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Report ⚠️") },
                                onClick = {
                                    viewModel.reportPost(post.id, "Spam/Inappropriate")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block Artist 🚫") },
                                onClick = {
                                    viewModel.blockUser(post.userId)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hide Post") },
                                onClick = {
                                    viewModel.hidePost(post.id)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
                
                Text(
                    text = "\"${post.caption}\"",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.padding(vertical = 24.dp),
                    lineHeight = 32.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val likeScale = remember { Animatable(1f) }
                        val coroutineScope = rememberCoroutineScope()
                        
                        IconButton(
                            onClick = { 
                                if (isProcessingLike) return@IconButton
                                isProcessingLike = true
                                
                                // Optimistic Update
                                localLiked = !localLiked
                                localLikeCount += if (localLiked) 1 else -1
                                
                                coroutineScope.launch {
                                    likeScale.animateTo(1.4f, tween(100))
                                    likeScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                    viewModel.toggleLike(post.id) 
                                    isProcessingLike = false
                                }
                            }
                        ) {
                            Icon(
                                if (localLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (localLiked) Color.Red else Color.Gray,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = likeScale.value
                                    scaleY = likeScale.value
                                }
                            )
                        }
                        Text("$localLikeCount", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        IconButton(onClick = { showComments = true }) {
                            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.Gray)
                        }
                    }
                    
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this cultural chronicle on Karunada Kala: \"${post.caption}\"")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Chronicle"))
                    }) {
                        Icon(Icons.Default.Share, null, tint = Color.Gray)
                    }
                }
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
    val commentsByPost by viewModel.comments.collectAsState()
    val postComments = commentsByPost[postId] ?: emptyList()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
            Text("Stories & Reflections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(postComments) { comment ->
                    Row(modifier = Modifier.padding(vertical = 12.dp)) {
                        AsyncImage(
                            model = if (comment.userAvatar.isNotEmpty()) comment.userAvatar else "https://ui-avatars.com/api/?name=${comment.userName}",
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(comment.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                            Text(TimeUtils.formatDate(comment.timestamp?.toDate()), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add a reflection...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
                IconButton(onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(postId, commentText)
                        commentText = ""
                    }
                }) {
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
    isLoading: Boolean,
    viewModel: PostViewModel = viewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val aiCaptionState by viewModel.aiCaptionState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        viewModel.resetAiCaption()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Chronicle a Moment",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Tap to select image", color = Color.Gray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedImageUri != null) {
                KalaActionButton(
                    text = when (aiCaptionState) {
                        is UiState.Loading -> "✨ Kala is crafting..."
                        is UiState.Success -> "✨ Suggest Another"
                        else -> "✨ Suggest AI Caption"
                    },
                    onClick = {
                        val bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                            ImageDecoder.decodeBitmap(source)
                        }
                        viewModel.generateAiCaption(bitmap)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    enabled = aiCaptionState !is UiState.Loading
                )

                if (aiCaptionState is UiState.Success) {
                    val suggested = (aiCaptionState as UiState.Success<String>).data
                    Card(
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(suggested, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                            TextButton(onClick = { caption = suggested }) {
                                Text("Use this caption", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (aiCaptionState is UiState.Error) {
                    Text(
                        (aiCaptionState as UiState.Error).message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Your Story (Caption)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { if (selectedImageUri != null && caption.isNotBlank()) onPost(selectedImageUri!!, caption, location.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Chronicle this moment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                setShowNextButton(false)
                setShowPreviousButton(false)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun EmptyChroniclesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🏺", fontSize = 64.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "The archives are quiet",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Be the first to chronicle a cultural moment and share the legacy with the world.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
