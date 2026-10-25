package com.example.data.mapper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Shared formatting for the `timestamptz` columns PostgREST returns.
 *
 * Kept in one place so jobs, gigs and applications all render timestamps identically, and so the
 * awkward ISO-8601 parsing lives in exactly one spot.
 */
internal object DateTimeFormatting {

    /**
     * Parses the ISO-8601 timestamps Supabase emits. They carry fractional seconds and a timezone
     * offset; `SimpleDateFormat` cannot handle the `T` separator or 6-digit fractions, so the value
     * is normalised first rather than pulling in a date library.
     */
    fun parseIso8601(value: String): Date? {
        val normalised = value
            .replace('T', ' ')
            .substringBefore('.')
            .substringBefore('+')
            .trim()
            .removeSuffix("Z")

        val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(normalised)
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    /** Turns a `created_at` timestamp into "Just now", "3h ago", "2d ago", or a date. */
    fun relativeTime(createdAt: String): String {
        if (createdAt.isBlank()) return "Recently"
        val created = parseIso8601(createdAt) ?: return "Recently"
        val diffMs = System.currentTimeMillis() - created.time
        if (diffMs < 0) return "Just now"

        val minutes = diffMs / 60_000
        val hours = diffMs / 3_600_000
        val days = diffMs / 86_400_000

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(created)
        }
    }

    /** Formats a date as e.g. "Jun 15, 2024". */
    fun shortDate(date: Date): String =
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(date)
}
