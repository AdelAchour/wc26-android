package com.adel.wc26.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.adel.wc26.R
import com.adel.wc26.core.util.RelativeTime
import com.adel.wc26.core.util.WC26DateTime

/**
 * Maps a [RelativeTime] value to localized display text.
 *
 * This is the UI-layer bridge for WC26DateTime.relative() — the helper
 * produces a resource-free value, and this resolves it to a string.
 */
@Composable
fun RelativeTime.format(): String = when (this) {
    RelativeTime.JustNow -> stringResource(R.string.time_just_now)
    is RelativeTime.Minutes -> stringResource(R.string.time_minutes, value)
    is RelativeTime.Hours -> stringResource(R.string.time_hours, value)
    is RelativeTime.Days -> stringResource(R.string.time_days, value)
    is RelativeTime.OlderThanWeek -> WC26DateTime.calendarDate(isoDate)
}