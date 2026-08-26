package com.myplaces.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.myplaces.ui.viewmodel.PlacesViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val EMOJI_LIST = listOf(
    "❤️", "😊", "😢", "😮", "⭐", "🏠", "🏖️", "🏔️",
    "🍕", "☕", "🎵", "🎨", "📚", "🌿", "🌸", "🔥",
    "🎉", "✈️", "🏛️", "🌙", "💡", "🎯", "🐾", "🌍"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    lat: Double,
    lon: Double,
    viewModel: PlacesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📍") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // URI temporaire pour CameraX
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                photoPath = copyUriToInternalStorage(context, uri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { photoPath = copyUriToInternalStorage(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un lieu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre du lieu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Souvenir") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Sélecteur d'émoji
            Column {
                Text(
                    text = "Émoji représentant ce lieu",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedEmoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                        Text("Changer")
                    }
                }
                if (showEmojiPicker) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        items(EMOJI_LIST) { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {
                                        selectedEmoji = emoji
                                        showEmojiPicker = false
                                    }
                            )
                        }
                    }
                }
            }

            // Photo
            Column {
                Text(
                    text = "Photo (optionnelle)",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val file = createTempImageFile(context)
                            cameraUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            cameraLauncher.launch(cameraUri!!)
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Appareil photo")
                    }
                    OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galerie")
                    }
                }
                photoPath?.let { path ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = path,
                        contentDescription = "Aperçu photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Coordonnées affichées en info
            Text(
                text = "Coordonnées : ${String.format("%.5f", lat)}, ${String.format("%.5f", lon)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Bouton enregistrer
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addPlace(title, description, selectedEmoji, lat, lon, photoPath)
                        onBack()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer ce lieu")
            }
        }
    }
}

/** Crée un fichier temporaire dans le cache pour la capture photo. */
private fun createTempImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile("PLACE_${timestamp}_", ".jpg", storageDir)
}

/** Copie un URI (galerie ou camera) dans le stockage interne privé de l'app. */
private fun copyUriToInternalStorage(context: Context, uri: Uri): String {
    val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val destFile = File(photosDir, "place_${timestamp}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        destFile.outputStream().use { output -> input.copyTo(output) }
    }
    return destFile.absolutePath
}
