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
import android.util.Base64
import com.adel.wc26.feature.profile.domain.UserRole
import org.json.JSONObject
import java.time.Instant

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
        val KEY_ROLE = stringPreferencesKey("auth_user_role")
    }

    /** Emits the current token, or null when signed out. */
    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

    /** Emits true while a token is stored. Drives launch routing. */
    val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] != null }

    /** Emits the user's role (e.g. "admin" or "user"), or null. */
    val roleFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_ROLE] }

    /** One-shot read of the current token — used by the auth interceptor. */
    suspend fun getToken(): String? =
        context.dataStore.data.first()[KEY_TOKEN]

    /** Stores the token and user id after a successful login/register. */
    suspend fun saveSession(token: String, userId: Long, role: String = UserRole.USER.value) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = userId.toString()
            prefs[KEY_ROLE] = role
        }
    }

    /** Updates just the user role (useful when refreshing profile info). */
    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE] = role
        }
    }

    /** One-shot read of the stored user id. */
    suspend fun getUserId(): Long? =
        context.dataStore.data.first()[KEY_USER_ID]?.toLongOrNull()

    /** Clears the session on logout. */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /**
     * Checks if the stored session is present and has not expired.
     */
    suspend fun hasValidSession(): Boolean {
        val token = getToken() ?: return false
        return !isTokenExpired(token)
    }

    /**
     * Decodes the JWT payload locally and compares the "exp" claim with the current time.
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true // Invalid JWT structure

            // JWT payload is Base64 URL-encoded (second part of the string)
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING)
            val payloadString = String(payloadBytes)
            val json = JSONObject(payloadString)

            val expTimeSeconds = json.optLong("exp", 0)
            val currentTimeSeconds = Instant.now().epochSecond

            currentTimeSeconds >= expTimeSeconds
        } catch (e: Exception) {
            true // If parsing fails, fail-safe and treat it as expired
        }
    }
}