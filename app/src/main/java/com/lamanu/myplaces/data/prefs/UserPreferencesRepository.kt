package com.lamanu.myplaces.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lamanu.myplaces.domain.model.Author
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Identite locale de l'utilisateur et reglages.
 *
 * L'[Author.id] est un UUID tire une seule fois, a la premiere lecture, et conserve ensuite :
 * c'est lui qui signe les lieux exportes et permet a un ami de distinguer nos souvenirs des siens.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            author = Author(
                id = prefs[KEY_AUTHOR_ID].orEmpty(),
                name = prefs[KEY_AUTHOR_NAME] ?: DEFAULT_AUTHOR_NAME,
            ),
            biometricLockEnabled = prefs[KEY_BIOMETRIC_LOCK] ?: false,
            onboardingDone = prefs[KEY_ONBOARDING_DONE] ?: false,
        )
    }

    /** Renvoie l'auteur courant en creant son identifiant au premier appel. */
    suspend fun currentAuthor(): Author {
        val existing = context.dataStore.data.first()[KEY_AUTHOR_ID]
        if (!existing.isNullOrBlank()) {
            return Author(id = existing, name = context.dataStore.data.first()[KEY_AUTHOR_NAME] ?: DEFAULT_AUTHOR_NAME)
        }
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            // `edit` est atomique : on ne reecrit que si personne ne nous a devance.
            if (prefs[KEY_AUTHOR_ID].isNullOrBlank()) prefs[KEY_AUTHOR_ID] = generated
        }
        val prefs = context.dataStore.data.first()
        return Author(
            id = prefs[KEY_AUTHOR_ID] ?: generated,
            name = prefs[KEY_AUTHOR_NAME] ?: DEFAULT_AUTHOR_NAME,
        )
    }

    suspend fun setAuthorName(name: String) {
        context.dataStore.edit { it[KEY_AUTHOR_NAME] = name.trim().ifBlank { DEFAULT_AUTHOR_NAME } }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_LOCK] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    private companion object {
        val KEY_AUTHOR_ID = stringPreferencesKey("author_id")
        val KEY_AUTHOR_NAME = stringPreferencesKey("author_name")
        val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        const val DEFAULT_AUTHOR_NAME = "Moi"
    }
}
