package com.groovernapp.insadi

import android.app.Application
import android.content.ContextWrapper
import com.pixplicity.easyprefs.library.Prefs

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * Easy prefs to help saving data to SharedPreferences
         *
         * you can learn [Prefs](https://github.com/Pixplicity/EasyPrefs)
         */
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }
}