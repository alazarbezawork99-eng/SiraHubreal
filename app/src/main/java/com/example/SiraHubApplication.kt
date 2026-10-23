package com.example

import android.app.Application
import com.example.data.local.LegacyPrefsCleanup
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SiraHubApplication : Application() {

    @Inject
    lateinit var legacyPrefsCleanup: LegacyPrefsCleanup

    override fun onCreate() {
        super.onCreate()
        // One-time removal of the SharedPreferences JSON cache written by the old repository.
        legacyPrefsCleanup.runIfNeeded()
    }
}
