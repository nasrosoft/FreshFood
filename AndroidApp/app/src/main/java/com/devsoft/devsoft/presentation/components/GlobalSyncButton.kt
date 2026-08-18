package com.devsoft.devsoft.presentation.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


@Composable
fun GlobalSyncButton(tint: Color = MaterialTheme.colorScheme.onPrimary) {
    val context = LocalContext.current
    IconButton(onClick = {
        Toast.makeText(context, "Data is live via Supabase!", Toast.LENGTH_SHORT).show()
    }) {
        Icon(Icons.Filled.Refresh, contentDescription = "Sync", tint = tint)
    }
}
