package com.adel.wc26.core.network

import com.adel.wc26.BuildConfig

/**
 * Network-wide constants.
 */
object ApiConstants {
    private const val USE_LOCALHOST = false
    /**
     * Base URL of the WC26 backend.
     * Note: "10.0.2.2" is the standard Android emulator loopback address that
     * resolves to the host computer's "localhost" port.
     */
    val BASE_URL = if (USE_LOCALHOST) {
        "http://10.0.2.2:8080/api/"
    } else {
        "https://wc26.adelash.dev/api/"
    }
}