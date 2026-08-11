import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\devsoft\freshfood'
domain_dir = os.path.join(base_dir, 'domain', 'repository')
data_dir = os.path.join(base_dir, 'data', 'repository')
auth_dir = os.path.join(base_dir, 'presentation', 'auth')
nav_dir = os.path.join(base_dir, 'presentation', 'navigation')

os.makedirs(auth_dir, exist_ok=True)
os.makedirs(nav_dir, exist_ok=True)

files = {
    # 1. Auth Repository
    os.path.join(domain_dir, 'AuthRepository.kt'): '''package com.devsoft.freshfood.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
}
''',
    os.path.join(data_dir, 'AuthRepositoryImpl.kt'): '''package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            // Ignore failure
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}
''',
    # 2. Auth ViewModel
    os.path.join(auth_dir, 'AuthViewModel.kt'): '''package com.devsoft.freshfood.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(if(repository.isUserLoggedIn()) AuthState.Authenticated else AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password).fold(
                onSuccess = { _authState.value = AuthState.Authenticated },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}
''',
    # 3. Login Screen
    os.path.join(auth_dir, 'LoginScreen.kt'): '''package com.devsoft.freshfood.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FreshFood", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }
    }
}
''',
    # 4. Main App Screen (Bottom Nav)
    os.path.join(nav_dir, 'MainAppScreen.kt'): '''package com.devsoft.freshfood.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devsoft.freshfood.presentation.dashboard.DashboardScreen
import com.devsoft.freshfood.presentation.dashboard.DashboardViewModel
import com.devsoft.freshfood.presentation.products.ProductListScreen
import com.devsoft.freshfood.presentation.products.ProductsViewModel
import com.devsoft.freshfood.presentation.sales.PosScreen
import com.devsoft.freshfood.presentation.sales.PosViewModel
import com.devsoft.freshfood.presentation.customers.CustomersScreen
import com.devsoft.freshfood.presentation.customers.CustomersViewModel

@Composable
fun MainAppScreen(
    dashboardViewModel: DashboardViewModel,
    productsViewModel: ProductsViewModel,
    posViewModel: PosViewModel,
    customersViewModel: CustomersViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "POS") },
                    label = { Text("POS") },
                    selected = currentRoute == "pos",
                    onClick = { navController.navigate("pos") { launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = currentRoute == "products",
                    onClick = { navController.navigate("products") { launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Customers") },
                    label = { Text("Customers") },
                    selected = currentRoute == "customers",
                    onClick = { navController.navigate("customers") { launchSingleTop = true; restoreState = true } }
                )
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(paddingValues)) {
            composable("dashboard") { DashboardScreen(dashboardViewModel) }
            composable("pos") { PosScreen(posViewModel) }
            composable("products") { 
                ProductListScreen(
                    viewModel = productsViewModel, 
                    onAddProductClick = {}, 
                    onProductClick = {}
                ) 
            }
            composable("customers") { CustomersScreen(customersViewModel) }
        }
    }
}
''',
    # 5. Overwrite MainActivity.kt completely
    os.path.join(base_dir, 'MainActivity.kt'): '''package com.devsoft.freshfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devsoft.freshfood.data.repository.*
import com.devsoft.freshfood.presentation.auth.*
import com.devsoft.freshfood.presentation.navigation.MainAppScreen
import com.devsoft.freshfood.presentation.dashboard.DashboardViewModelFactory
import com.devsoft.freshfood.presentation.products.ProductsViewModelFactory
import com.devsoft.freshfood.presentation.sales.PosViewModelFactory
import com.devsoft.freshfood.presentation.customers.CustomersViewModelFactory
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val supabaseClient = createSupabaseClient(
            supabaseUrl = "https://rjlacsdehgxzsghdzbul.supabase.co",
            supabaseKey = "sb_publishable_oln733bAmVovVJLPQ_BFvQ_AaEr1D0Q"
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }

        // Initialize Repositories
        val authRepo = AuthRepositoryImpl(supabaseClient)
        val productRepo = ProductRepositoryImpl(supabaseClient)
        val salesRepo = SalesRepositoryImpl(supabaseClient)
        val customerRepo = CustomerRepositoryImpl(supabaseClient)
        val dashboardRepo = DashboardRepositoryImpl(supabaseClient)

        // Factories
        val authFactory = AuthViewModelFactory(authRepo)
        val dashFactory = DashboardViewModelFactory(dashboardRepo)
        val prodFactory = ProductsViewModelFactory(productRepo)
        val posFactory = PosViewModelFactory(salesRepo, productRepo)
        val custFactory = CustomersViewModelFactory(customerRepo)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                    val authState by authViewModel.authState.collectAsState()

                    if (authState is AuthState.Authenticated) {
                        MainAppScreen(
                            dashboardViewModel = viewModel(factory = dashFactory),
                            productsViewModel = viewModel(factory = prodFactory),
                            posViewModel = viewModel(factory = posFactory),
                            customersViewModel = viewModel(factory = custFactory)
                        )
                    } else {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {}
                        )
                    }
                }
            }
        }
    }
}
'''
}

for path, content in files.items():
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Phase 9 Assembly created successfully.")
