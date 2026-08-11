package com.devsoft.freshfood

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
        val posFactory = PosViewModelFactory(salesRepo)
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
