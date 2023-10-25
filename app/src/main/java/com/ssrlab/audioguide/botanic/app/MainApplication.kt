package com.ssrlab.audioguide.botanic.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class MainApplication: Application() {

    val constPreferences = "preferences"
    val constLocale = "locale"

    private var locale = Locale("en")
    private val config = Configuration()
    private var localeInt = 0

    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(locale)
    }

    fun setLocale(loc: Locale){
        locale = loc
        config.setLocale(loc)
        context.resources.configuration.setLocale(loc)
    }
    fun getLocale() = locale

    fun setLocaleInt(locale: String) {
        when (locale) {
            "ru" -> localeInt = 1
            "be" -> localeInt = 2
            "en" -> localeInt = 3
        }
    }
    fun getLocaleInt() = localeInt

    private lateinit var context: Context

    fun getContext() = context
    fun setContext(context: Context){
        this.context = context
    }
}