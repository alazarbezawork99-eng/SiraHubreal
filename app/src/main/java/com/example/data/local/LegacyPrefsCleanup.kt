package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deletes the JSON blobs the old `SiraRepository` wrote into SharedPreferences.
 *
 * Before Room, every list and object was serialised by hand with `org.json` into
 * `sira_hub_prefs`. That store is now dead weight and, worse, a second source of truth that could
 * disagree with the server. This runs once on upgrade.
 *
 * What is removed: the JSON caches (jobs, gigs, applications, bids, profile, company profile,
 * cached auth user, settings blob) and the bookmark ID sets, which now live in Room.
 *
 * What is deliberately kept:
 *  - `has_seen_welcome_screen` - a genuine user preference, not cached server data.
 *  - the encrypted auth session (`secure_auth_session_prefs`) - owned by the auth layer, in a
 *    different preferences file, and required to keep the user signed in across the upgrade.
 */
@Singleton
class LegacyPrefsCleanup @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun runIfNeeded() {
        val prefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_CLEANUP_DONE, false)) {
            val removed = removeLegacyKeys(prefs)
            prefs.edit { putBoolean(KEY_CLEANUP_DONE, true) }
            Log.i(TAG, "Removed $removed legacy SharedPreferences cache keys.")
        }
    }

    private fun removeLegacyKeys(prefs: SharedPreferences): Int {
        val present = LEGACY_KEYS.filter { prefs.contains(it) }
        prefs.edit {
            present.forEach { remove(it) }
        }
        return present.size
    }

    companion object {
        private const val TAG = "LegacyPrefsCleanup"
        const val LEGACY_PREFS_NAME = "sira_hub_prefs"
        private const val KEY_CLEANUP_DONE = "legacy_json_cache_cleared_v1"

        /** Every JSON cache key the old repository wrote, plus the bookmark ID sets. */
        val LEGACY_KEYS = listOf(
            "jobs_json",
            "gigs_json",
            "applications_json",
            "bids_json",
            "profile_json",
            "company_profile_json",
            "auth_user_json",
            "app_settings_json",
            "bookmarked_jobs",
            "bookmarked_gigs"
        )
    }
}
