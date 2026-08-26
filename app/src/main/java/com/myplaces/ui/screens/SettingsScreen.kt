package com.myplaces.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaces.ui.viewmodel.PlacesViewModel
import com.myplaces.utils.BiometricHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: PlacesViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val biometricEnabled by BiometricHelper.isEnabledFlow(context).collectAsState(initial = false)
    val biometricAvailable = BiometricHelper.isAvailable(context)

    var importAuthorName by remember { mutableStateOf("") }
    var importDialogVisible by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { pendingImportUri = it; importDialogVisible = true }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Paramètres", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // Section sécurité
            SettingsSection(title = "Sécurité") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Verrouillage biométrique", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (biometricAvailable) "Empreinte ou schéma au démarrage"
                            else "Non disponible sur cet appareil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { BiometricHelper.setEnabled(context, enabled) }
                        },
                        enabled = biometricAvailable
                    )
                }
            }

            // Section données
            SettingsSection(title = "Données") {
                // Export
                OutlinedButton(
                    onClick = {
                        viewModel.exportJournal { uri ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (uri != null) "Journal exporté en places_export.json ✓" else "Erreur lors de l'export"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exporter mon journal (JSON)")
                }

                // Import
                OutlinedButton(
                    onClick = { importLauncher.launch("application/json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importer les lieux d'un ami")
                }
            }

            // Info app
            Spacer(Modifier.weight(1f))
            Text(
                "My Places v1.0 • LA MANU",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (importDialogVisible) {
        AlertDialog(
            onDismissRequest = { importDialogVisible = false },
            title = { Text("Importer des lieux") },
            text = {
                Column {
                    Text("Nom de l'ami dont tu importes les lieux :")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importAuthorName,
                        onValueChange = { importAuthorName = it },
                        label = { Text("Nom de l'auteur") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportUri?.let { uri ->
                        viewModel.importFromUri(uri, importAuthorName.ifBlank { "Ami" }) { count ->
                            scope.launch { snackbarHostState.showSnackbar("$count lieu(x) importé(s) ✓") }
                        }
                    }
                    importDialogVisible = false
                    importAuthorName = ""
                }) { Text("Importer") }
            },
            dismissButton = {
                TextButton(onClick = { importDialogVisible = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}
