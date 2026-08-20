package com.devsoft.devsoft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devsoft.devsoft.presentation.activation.ActivationGate
import com.devsoft.devsoft.presentation.activation.ActivationViewModel
import com.devsoft.devsoft.presentation.activation.ActivationViewModelFactory
import com.devsoft.devsoft.presentation.auth.*
import com.devsoft.devsoft.presentation.navigation.MainAppScreen
import com.devsoft.devsoft.presentation.dashboard.DashboardViewModelFactory
import com.devsoft.devsoft.presentation.products.ProductsViewModelFactory
import com.devsoft.devsoft.presentation.sales.PosViewModelFactory
import com.devsoft.devsoft.presentation.customers.CustomersViewModelFactory
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.devsoft.devsoft.data.repository.*
import com.devsoft.devsoft.ui.theme.DevsoftTheme
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private var targetDeliveryOrderId by androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = com.devsoft.devsoft.utils.LocaleHelper.getPersistedLanguage(newBase)
        val context = com.devsoft.devsoft.utils.LocaleHelper.updateResources(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val orderId = intent.getStringExtra("EXTRA_ORDER_ID")
        if (!orderId.isNullOrBlank()) {
            targetDeliveryOrderId = orderId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        targetDeliveryOrderId = intent?.getStringExtra("EXTRA_ORDER_ID")

        // Initialize notification channels
        com.devsoft.devsoft.utils.NotificationHelper.createNotificationChannels(this)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        val supabaseClient = createSupabaseClient(
            supabaseUrl = "https://rjlacsdehgxzsghdzbul.supabase.co",
            supabaseKey = "sb_publishable_oln733bAmVovVJLPQ_BFvQ_AaEr1D0Q"
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
            defaultSerializer = KotlinXSerializer(Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            })
        }

        val activationRepo = ActivationRepositoryImpl(supabaseClient)
        val authRepo = AuthRepositoryImpl(supabaseClient)
        val productRepo = ProductRepositoryImpl(supabaseClient)
        val salesRepo = SalesRepositoryImpl(supabaseClient)
        val customerRepo = CustomerRepositoryImpl(supabaseClient)
        val dashboardRepo = DashboardRepositoryImpl(supabaseClient)
        val purchaseRepo = PurchaseRepositoryImpl(supabaseClient)
        val deliveryRepo = DeliveryRepositoryImpl(supabaseClient)
        val inventoryRepo = InventoryRepositoryImpl(supabaseClient)
        val profileRepo = ProfileRepositoryImpl(supabaseClient)

        // Factories
        val activationFactory = ActivationViewModelFactory(activationRepo, applicationContext)
        val authFactory = AuthViewModelFactory(authRepo, profileRepo)
        val dashFactory = DashboardViewModelFactory(dashboardRepo)
        val prodFactory = ProductsViewModelFactory(productRepo)
        val posFactory = PosViewModelFactory(salesRepo, profileRepo)
        val custFactory = CustomersViewModelFactory(customerRepo)
        val purchaseFactory = com.devsoft.devsoft.presentation.purchases.PurchaseViewModelFactory(purchaseRepo)
        val deliveryFactory = com.devsoft.devsoft.presentation.deliveries.DeliveryViewModelFactory(deliveryRepo)
        val inventoryFactory = com.devsoft.devsoft.presentation.inventory.InventoryViewModelFactory(inventoryRepo)
        val returnsFactory = com.devsoft.devsoft.presentation.inventory.ReturnsViewModelFactory(inventoryRepo)
        
        setContent {
            DevsoftTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val activationViewModel: ActivationViewModel = viewModel(factory = activationFactory)

                    // Gate the entire application behind the real-time Supabase activation check
                    ActivationGate(viewModel = activationViewModel) {
                        val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                        val authState by authViewModel.authState.collectAsState()

                        if (authState is AuthState.Authenticated) {
                            val userRole = (authState as AuthState.Authenticated).role
                            val userId = (authState as AuthState.Authenticated).userId

                            LaunchedEffect(userId, userRole) {
                                com.devsoft.devsoft.utils.NotificationHelper.persistCurrentUser(this@MainActivity, userId, userRole)
                                if (userRole == "DELIVERY" && !userId.isNullOrBlank()) {
                                    com.devsoft.devsoft.utils.NotificationHelper.scheduleBackgroundDeliverySync(this@MainActivity)
                                } else {
                                    com.devsoft.devsoft.utils.NotificationHelper.cancelBackgroundDeliverySync(this@MainActivity)
                                }
                            }

                            MainAppScreen(
                                userId = userId,
                                userRole = userRole,
                                dashboardViewModel = viewModel(factory = dashFactory),
                                productsViewModel = viewModel(factory = prodFactory),
                                posViewModel = viewModel(factory = posFactory),
                                customersViewModel = viewModel(factory = custFactory),
                                purchaseViewModel = viewModel(factory = purchaseFactory),
                                deliveryViewModel = viewModel(factory = deliveryFactory),
                                inventoryViewModel = viewModel(factory = inventoryFactory),
                                returnsViewModel = viewModel(factory = returnsFactory),
                                profileRepository = profileRepo,
                                activationRepository = activationRepo,
                                targetDeliveryId = targetDeliveryOrderId,
                                onTargetDeliveryHandled = { targetDeliveryOrderId = null },
                                onLogout = { 
                                    com.devsoft.devsoft.utils.NotificationHelper.persistCurrentUser(this@MainActivity, null, null)
                                    com.devsoft.devsoft.utils.NotificationHelper.cancelBackgroundDeliverySync(this@MainActivity)
                                    authViewModel.logout() 
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                com.devsoft.devsoft.utils.NotificationHelper.persistCurrentUser(this@MainActivity, null, null)
                                com.devsoft.devsoft.utils.NotificationHelper.cancelBackgroundDeliverySync(this@MainActivity)
                            }
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
}
