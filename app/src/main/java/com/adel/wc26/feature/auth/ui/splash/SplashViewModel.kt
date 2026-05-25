package com.adel.wc26.feature.auth.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.datastore.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The launch routing decision: is there a stored session or not?
 *
 * [SplashRoute] starts as Undecided; once the token check completes it
 * flips to LoggedIn or LoggedOut, and the NavHost routes accordingly.
 */
enum class SplashRoute { Undecided, LoggedIn, LoggedOut }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _route = MutableStateFlow(SplashRoute.Undecided)
    val route: StateFlow<SplashRoute> = _route.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenStore.getToken()
            _route.value =
                if (token != null) SplashRoute.LoggedIn
                else SplashRoute.LoggedOut
        }
    }
}