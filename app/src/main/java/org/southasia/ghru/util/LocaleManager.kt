package org.southasia.ghru.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(private val context: Context) {
    private val LANGUAGE_KEY = "language_key"

    fun setLocale(): Context {
        return updateResources(getLanguage())
    }

    fun setNewLocale(language: String): Context {
        persistLanguage(language)
        return updateResources(language)
    }

    fun getLanguage(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LANGUAGE_KEY, "en")
    }

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        prefs.edit().putString(LANGUAGE_KEY, language).commit()
    }

    private fun updateResources(language: String): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        return context
    }

    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= 24) config.locales.get(0) else {
            config.locale
        }
    }

}