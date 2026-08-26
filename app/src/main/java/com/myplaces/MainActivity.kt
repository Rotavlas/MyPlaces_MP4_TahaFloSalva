package com.myplaces

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentActivity
import com.myplaces.ui.screens.AppNavigation
import com.myplaces.ui.theme.MyPlacesTheme
import com.myplaces.utils.BiometricHelper

// FragmentActivity requis par BiometricPrompt
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPlacesTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BiometricGate { AppNavigation() }
                }
            }
        }
    }

    @Composable
    private fun BiometricGate(content: @Composable () -> Unit) {
        var authenticated by remember { mutableStateOf<Boolean?>(null) }
        val biometricEnabled by BiometricHelper.isEnabledFlow(this).collectAsState(initial = null)

        LaunchedEffect(biometricEnabled) {
            when {
                biometricEnabled == null -> {}
                biometricEnabled == true && BiometricHelper.isAvailable(this@MainActivity) -> {
                    BiometricHelper.authenticate(
                        activity = this@MainActivity,
                        onSuccess = { authenticated = true },
                        onFailure = { authenticated = false }
                    )
                }
                else -> authenticated = true
            }
        }

        when {
            biometricEnabled == null || authenticated == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            authenticated == false -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Accès refusé.\nRedémarrez l'application pour réessayer.", textAlign = TextAlign.Center)
                }
            }
            else -> content()
        }
    }
}
