package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.components.KalaActionButton
import com.example.myapplication.viewmodel.WorkshopViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopRegistrationScreen(
    navController: NavController,
    workshopId: String,
    workshopTitle: String,
    viewModel: WorkshopViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    val uiState by viewModel.enrollmentUiState.collectAsState()
    val isSubmitted = uiState is com.example.myapplication.ui.state.UiState.Success
    val isLoading by viewModel.isEnrolling.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workshop Sign-up", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.clearEnrollmentState()
                        navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isSubmitted) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("🎨", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Interest Registered!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("The artist will contact you soon. Namaskara!", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { 
                        viewModel.clearEnrollmentState()
                        navController.popBackStack() 
                    }) {
                        Text("Back to Explore")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Registering for: $workshopTitle",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Please provide your details so the Guru can reach out to you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Why do you want to learn this art?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 4
                )

                Spacer(Modifier.height(40.dp))

                KalaActionButton(
                    text = if (isLoading) "Submitting..." else "Submit Interest",
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            viewModel.registerInterest(
                                workshopId = workshopId,
                                workshopTitle = workshopTitle,
                                name = name,
                                phone = phone,
                                email = email,
                                reason = reason
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primary,
                    enabled = !isLoading && name.isNotBlank() && phone.isNotBlank()
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Note: This is a directory of pride. Your details will be shared directly with the artisan for the 'Guru-Shishya' workshop.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
