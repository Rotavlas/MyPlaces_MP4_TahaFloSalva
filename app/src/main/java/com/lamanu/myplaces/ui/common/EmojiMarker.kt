package com.lamanu.myplaces.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pastille affichee a la place du marqueur Google Maps par defaut.
 * Le liseré change de couleur selon que le lieu est a moi ou importe d'un ami.
 */
@Composable
fun EmojiMarker(
    emoji: String,
    isOwn: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Box(
        modifier = modifier
            .size(44.dp)
            .background(color = Color.White, shape = CircleShape)
            .border(width = 3.dp, color = borderColor, shape = CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 20.sp)
    }
}
