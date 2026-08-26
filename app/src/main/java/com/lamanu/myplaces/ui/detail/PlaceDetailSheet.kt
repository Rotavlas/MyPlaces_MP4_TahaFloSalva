package com.lamanu.myplaces.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lamanu.myplaces.R
import com.lamanu.myplaces.data.media.PhotoStorage
import com.lamanu.myplaces.domain.model.Place
import java.io.File
import java.text.DateFormat
import java.util.Date

/**
 * Fiche detaillee affichee au clic sur un marqueur : titre, date, adresse, ressenti, photo.
 */
@Composable
fun PlaceDetailSheet(
    place: Place,
    onDelete: () -> Unit,
    onRetryAddress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val photoFile = place.photoFileName?.let { PhotoStorage.fileIn(context, it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = place.emoji, fontSize = 32.sp)
            Column {
                Text(text = place.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(place.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (!place.isOwn) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.detail_imported_from, place.author.name)) },
            )
        }

        if (photoFile != null && photoFile.exists()) {
            AsyncImage(
                model = photoFile,
                contentDescription = place.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }

        if (place.description.isNotBlank()) {
            Text(text = place.description, style = MaterialTheme.typography.bodyMedium)
        }

        // L'adresse peut manquer si le lieu a ete cree hors ligne : on propose de relancer l'appel.
        if (place.address != null) {
            Text(text = place.address, style = MaterialTheme.typography.bodySmall)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOff, contentDescription = null)
                TextButton(onClick = onRetryAddress) {
                    Text("Adresse inconnue — reessayer")
                }
            }
        }

        Text(
            text = "%.5f, %.5f".format(place.latitude, place.longitude),
            style = MaterialTheme.typography.labelSmall,
        )

        if (place.isOwn) {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text(stringResource(R.string.detail_delete))
            }
        }
    }
}
