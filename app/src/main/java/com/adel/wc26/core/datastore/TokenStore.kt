package com.adel.wc26.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Single DataStore instance for the whole app, scoped to the Context.
private val Context.dataStore by preferencesDataStore(name = "wc26_prefs")

/**
 * Persists the authentication token (JWT) and the signed-in user's id.
 *
 * Exposes the token both as a [Flow] (for reactive auth-state observation)
 * and via a one-shot [getToken] (for the OkHttp interceptor, which is
 * synchronous and just needs the current value).
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = stringPreferencesKey("auth_user_id")
    }

    /** Emits the current token, or null when signed out. */
    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

    /** Emits true while a token is stored. Drives launch routing. */
    val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] != null }

    /** One-shot read of the current token — used by the auth interceptor. */
    suspend fun getToken(): String? =
        context.dataStore.data.first()[KEY_TOKEN]

    /** Stores the token and user id after a successful login/register. */
    suspend fun saveSession(token: String, userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = userId.toString()
        }
    }

    /** One-shot read of the stored user id. */
    suspend fun getUserId(): Long? =
        context.dataStore.data.first()[KEY_USER_ID]?.toLongOrNull()

    /** Clears the session on logout. */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}