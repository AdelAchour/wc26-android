package com.adel.wc26.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.OffsetDateTime

/**
 * The result of a relative-time calculation — a value, NOT a string.
 * The UI layer maps this to localized text (see core/ui/RelativeTimeText).
 * Keeping this resource-free lets WC26DateTime stay plain, testable Kotlin.
 */
sealed interface RelativeTime {
    data object JustNow : RelativeTime
    data class Minutes(val value: Long) : RelativeTime
    data class Hours(val value: Long) : RelativeTime
    data class Days(val value: Long) : RelativeTime
    data class Months(val value: Long) : RelativeTime
    data class Years(val value: Long) : RelativeTime
}

/**
 * Formats the backend's ISO-8601 UTC timestamps for display.
 *
 * The backend sends timestamps like "2026-05-01T10:00:00Z" (UTC). These
 * helpers parse that and render it in the device's local time zone.
 *
 * java.time is available natively from API 26 (the app's minSdk), so no
 * desugaring is needed.
 *
 * The date functions return "" for an unparseable input, and relative()
 * returns null — a malformed timestamp should never crash a screen.
 *
 * Note: calendarDate / dateTime / timeOnly are already locale-aware via
 * DateTimeFormatter — they are not hardcoded to any language. Only
 * relative() needed words ("just now", unit suffixes), and that text now
 * lives in string resources, reached through the RelativeTime type.
 */
object WC26DateTime {

    private const val MINUTES_PER_HOUR = 60L
    private const val MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR
    private const val MINUTES_PER_MONTH = 31L * MINUTES_PER_DAY
    private const val MINUTES_PER_YEAR = 12L * MINUTES_PER_MONTH

    private val zone: ZoneId get() = ZoneId.systemDefault()

    private fun parse(iso: String): Instant? =
        runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrNull()

    /** A localized calendar date — e.g. "1 May 2026". */
    fun calendarDate(iso: String): String {
        val instant = parse(iso) ?: return ""
        val formatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
        return instant.atZone(zone).format(formatter)
    }

    /** Date only — e.g. "Sat 14 Jun". */
    fun dateOnly(iso: String): String {
        val instant = parse(iso) ?: return ""
        val formatter = DateTimeFormatter
            .ofPattern("EEE d MMM", Locale.getDefault())
        return instant.atZone(zone).format(formatter)
    }

    /** Date + time — e.g. "Sat 14 Jun, 21:00". For match kickoff. */
    fun dateTime(iso: String): String {
        val instant = parse(iso) ?: return ""
        val formatter = DateTimeFormatter
            .ofPattern("EEE d MMM, HH:mm", Locale.getDefault())
        return instant.atZone(zone).format(formatter)
    }

    /** Time of day only — e.g. "21:00". For a match known to be today. */
    fun timeOnly(iso: String): String {
        val instant = parse(iso) ?: return ""
        val formatter = DateTimeFormatter
            .ofPattern("HH:mm", Locale.getDefault())
        return instant.atZone(zone).format(formatter)
    }

    /** Full detailed timestamp — e.g. "14:51 · 3 Jun 2026". */
    fun detailedTimestamp(iso: String): String {
        val instant = parse(iso) ?: return ""
        val formatter = DateTimeFormatter
            .ofPattern("HH:mm · d MMM yyyy", Locale.getDefault())
        return instant.atZone(zone).format(formatter)
    }

    /**
     * Relative time as a [RelativeTime] value — the UI maps it to text.
     * Returns null for an unparseable input.
     */
    fun relative(iso: String): RelativeTime? {
        val instant = parse(iso) ?: return null
        val minutes = ChronoUnit.MINUTES.between(instant, Instant.now())
        return when {
            minutes < 1 -> RelativeTime.JustNow
            minutes < MINUTES_PER_HOUR -> RelativeTime.Minutes(minutes)
            minutes < MINUTES_PER_DAY -> RelativeTime.Hours(minutes / MINUTES_PER_HOUR)
            minutes < MINUTES_PER_MONTH -> RelativeTime.Days(minutes / MINUTES_PER_DAY)
            minutes < MINUTES_PER_YEAR -> RelativeTime.Months(minutes / MINUTES_PER_MONTH)
            else -> RelativeTime.Years(minutes / MINUTES_PER_YEAR)
        }
    }
}