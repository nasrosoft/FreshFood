package com.devsoft.freshfood.presentation.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.devsoft.freshfood.domain.model.Profile
import com.devsoft.freshfood.domain.repository.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onOpenDrawer: () -> Unit,
    profileRepository: ProfileRepository
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    
    fun loadProfiles() {
        scope.launch {
            profileRepository.getProfilesByRole("DELIVERY").collect { loadedProfiles ->
                profiles = loadedProfiles
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfiles()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                editingProfile = null
            },
            title = { Text(if (editingProfile == null) "Add Delivery Driver" else "Edit Driver") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editingProfile == null) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email (for Login)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        singleLine = true
                    )
                    Text("Note: User will be created with DELIVERY role.", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editingProfile == null) {
                        if (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            scope.launch {
                                val result = profileRepository.createDeliveryUser(
                                    email = email,
                                    password = password,
                                    firstName = firstName,
                                    lastName = lastName
                                )
                                
                                result.fold(
                                    onSuccess = {
                                        showDialog = false
                                        email = ""
                                        password = ""
                                        firstName = ""
                                        lastName = ""
                                        android.widget.Toast.makeText(context, "Driver created successfully", android.widget.Toast.LENGTH_SHORT).show()
                                        loadProfiles()
                                    },
                                    onFailure = { e ->
                                        android.widget.Toast.makeText(context, "Failed to create driver: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                        e.printStackTrace()
                                    }
                                )
                            }
                        }
                    } else {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            scope.launch {
                                val updated = editingProfile!!.copy(
                                    first_name = firstName,
                                    last_name = lastName
                                )
                                
                                profileRepository.updateProfile(updated)
                                
                                showDialog = false
                                editingProfile = null
                                firstName = ""
                                lastName = ""
                                loadProfiles()
                            }
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false
                    editingProfile = null 
                }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Drivers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingProfile = null
                email = ""
                password = ""
                firstName = ""
                lastName = ""
                showDialog = true 
            }, containerColor = Color(0xFF679B50)) {
                Icon(Icons.Filled.Add, contentDescription = "Add Driver", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles.size) { index ->
                val profile = profiles[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column {
                                Text(text = "${profile.first_name} ${profile.last_name}", fontWeight = FontWeight.Bold)
                                Text(text = "Role: ${profile.role}", color = Color.Gray)
                            }
                            Row {
                                IconButton(onClick = {
                                    editingProfile = profile
                                    firstName = profile.first_name ?: ""
                                    lastName = profile.last_name ?: ""
                                    showDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        profileRepository.deleteProfile(profile.id)
                                        loadProfiles()
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
