package com.devsoft.freshfood.presentation.activation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.devsoft.freshfood.ui.theme.*

@Composable
fun ActivationGate(
    viewModel: ActivationViewModel,
    content: @Composable () -> Unit
) {
    val activationState by viewModel.activationState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check activation when returning to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkActivation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (val state = activationState) {
        is ActivationState.Checking -> {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppBackground
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Verifying application status...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
        is ActivationState.Blocked -> {
            PaymentRequiredScreen(
                isChecking = false,
                errorMessage = null,
                onRefresh = { viewModel.checkActivation() }
            )
        }
        is ActivationState.Error -> {
            PaymentRequiredScreen(
                isChecking = false,
                errorMessage = state.message,
                onRefresh = { viewModel.checkActivation() }
            )
        }
        is ActivationState.Active -> {
            content()
        }
    }
}
