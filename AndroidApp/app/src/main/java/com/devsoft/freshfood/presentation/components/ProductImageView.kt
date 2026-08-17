package com.devsoft.freshfood.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.devsoft.freshfood.utils.ProductCategoryEmojiResolver

@Composable
fun ProductImageView(
    imageUrl: String?,
    emoji: String? = null,
    productName: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val resolvedEmoji = emoji?.takeIf { it.isNotBlank() }
        ?: ProductCategoryEmojiResolver.resolveEmoji(productName)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = productName ?: "Product image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size / 3),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    EmojiFallback(emoji = resolvedEmoji, size = size)
                }
            )
        } else {
            EmojiFallback(emoji = resolvedEmoji, size = size)
        }
    }
}

@Composable
private fun EmojiFallback(emoji: String, size: Dp) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = when {
            size <= 40.dp -> 18.sp
            size <= 60.dp -> 26.sp
            size <= 90.dp -> 38.sp
            else -> 48.sp
        }
        Text(
            text = emoji,
            fontSize = fontSize
        )
    }
}
