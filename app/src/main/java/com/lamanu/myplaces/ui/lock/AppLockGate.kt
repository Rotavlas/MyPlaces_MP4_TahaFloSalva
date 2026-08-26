package com.lamanu.myplaces.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamanu.myplaces.R

/**
 * Enveloppe l'application : tant que le journal est verrouille, [content] n'est pas compose,
 * donc aucune donnee n'est chargee ni affichee.
 */
@Composable
fun AppLockGate(
    activity: FragmentActivity,
    modifier: Modifier = Modifier,
    viewModel: AppLockViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is LockState.Locked) viewModel.requestUnlock(activity)
    }

    when (state) {
        LockState.Unlocked -> content()
        LockState.Checking -> Unit
        else -> LockedScreen(
            modifier = modifier,
            message = (state as? LockState.Failed)?.message,
            onRetry = viewModel::onCancelled,
        )
    }
}

@Composable
private fun LockedScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
        Text(
            text = stringResource(R.string.biometric_locked),
            style = MaterialTheme.typography.titleMedium,
        )
        if (message != null) {
            Text(text = message, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onRetry) {
            Text(stringResource(R.string.biometric_unlock))
        }
    }
}
