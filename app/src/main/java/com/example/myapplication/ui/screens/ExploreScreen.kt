package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.ArtCard
import com.example.myapplication.viewmodel.ArtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController, viewModel: ArtViewModel = viewModel()) {

    val artList by viewModel.artForms.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredList = artList.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        it.name.contains(searchQuery, ignoreCase = true)
    }

    // Fetch data when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchArtForms()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Explore Arts") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search arts...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                listOf("All", "Dance", "Craft", "Music", "Painting").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(if (artList.isEmpty()) "Loading or No Data..." else "No results found for \"$searchQuery\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { art ->
                        ArtCard(
                            name = art.name,
                            description = art.description,
                            imageUrl = art.imageUrl,
                            onClick = {
                                val encodedDescription = Uri.encode(art.description)
                                val encodedImageUrl = Uri.encode(art.imageUrl)
                                navController.navigate("detail/${art.name}/$encodedDescription/$encodedImageUrl")
                            }
                        )
                    }
                }
            }
        }
    }
}