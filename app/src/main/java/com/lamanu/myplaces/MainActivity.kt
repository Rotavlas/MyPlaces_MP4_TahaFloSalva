package com.lamanu.myplaces

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.lamanu.myplaces.ui.lock.AppLockGate
import com.lamanu.myplaces.ui.navigation.MyPlacesNavHost
import com.lamanu.myplaces.ui.theme.MyPlacesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activite unique (single-activity + Navigation Compose).
 *
 * Elle herite de [FragmentActivity] et non de ComponentActivity car `BiometricPrompt`
 * s'appuie sur le FragmentManager pour survivre aux changements de configuration.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyPlacesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppLockGate(activity = this) {
                        MyPlacesNavHost()
                    }
                }
            }
        }
    }
}
