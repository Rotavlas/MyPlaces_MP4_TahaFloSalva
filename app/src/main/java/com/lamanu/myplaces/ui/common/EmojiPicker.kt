package com.lamanu.myplaces.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamanu.myplaces.domain.model.Mood
import com.lamanu.myplaces.domain.model.Moods

/** Selecteur d'emoji groupe par categorie (ressenti, nature, gourmandise...). */
@Composable
fun EmojiPicker(
    selected: String,
    onSelect: (Mood) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Moods.byCategory().forEach { (category, moods) ->
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.alpha(0.7f),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = moods, key = { it.emoji }) { mood ->
                    EmojiChip(
                        mood = mood,
                        isSelected = mood.emoji == selected,
                        onClick = { onSelect(mood) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiChip(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = background, shape = CircleShape)
                .clickable(onClick = onClick)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = mood.emoji, fontSize = 22.sp)
        }
    }
}
