package com.lamanu.myplaces.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamanu.myplaces.R
import com.lamanu.myplaces.core.biometric.BiometricAvailability
import com.lamanu.myplaces.data.transfer.PlacesExportFile

/**
 * Identite d'auteur, verrouillage biometrique, et import/export du journal.
 *
 * L'acces au fichier passe par le Storage Access Framework (`CreateDocument` / `OpenDocument`) :
 * aucune permission de stockage n'est demandee, l'utilisateur choisit lui-meme l'emplacement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(PlacesExportFile.MIME_TYPE),
        onResult = { uri -> uri?.let(viewModel::export) },
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(viewModel::import) },
    )

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            SectionTitle(stringResource(R.string.settings_identity))
            OutlinedTextField(
                value = uiState.author.name,
                onValueChange = viewModel::onAuthorNameChange,
                label = { Text(stringResource(R.string.settings_author_name)) },
                singleLine = true,
                supportingText = { Text("Ce nom accompagne vos lieux dans le fichier d'export.") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${uiState.ownPlaces} lieu(x) a moi - ${uiState.importedPlaces} importe(s)",
                style = MaterialTheme.typography.bodySmall,
            )

            HorizontalDivider()

            SectionTitle(stringResource(R.string.settings_security))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_biometric_lock))
                    Text(
                        text = if (uiState.biometricAvailability == BiometricAvailability.AVAILABLE) {
                            stringResource(R.string.settings_biometric_lock_desc)
                        } else {
                            stringResource(R.string.settings_biometric_unavailable)
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = uiState.biometricLockEnabled,
                    onCheckedChange = viewModel::onBiometricLockChange,
                    enabled = uiState.biometricAvailability.canBeEnabled || uiState.biometricLockEnabled,
                )
            }

            HorizontalDivider()

            SectionTitle(stringResource(R.string.settings_data))
            OutlinedButton(
                onClick = { exportLauncher.launch(viewModel.suggestedExportFileName()) },
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Text(stringResource(R.string.settings_export))
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf(PlacesExportFile.MIME_TYPE, "*/*")) },
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Text(stringResource(R.string.settings_import))
            }
            Text(
                text = "L'export ne contient que vos propres lieux. Les photos ne sont pas " +
                    "incluses dans le fichier JSON (format v${PlacesExportFile.CURRENT_FORMAT_VERSION}).",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}
