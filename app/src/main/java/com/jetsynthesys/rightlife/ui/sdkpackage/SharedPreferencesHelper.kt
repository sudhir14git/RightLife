package com.jetsynthesys.rightlife.ui.sdkpackage

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.jetsynthesys.rightlife.BuildConfig
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
class SharedPreferencesHelper<T>(
        private val name: String,
        private val default: T
) :
        ReadWriteProperty<Any?, T> {
    companion object {
        lateinit var sp: SharedPreferences
        fun initialize(application: Application) {
            sp = application.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        }

        fun clearPreference() = sp.edit().clear().apply()

        fun clearPreference(key: String) = sp.edit().remove(key).commit()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSharePreferences(name, default)

    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        return putSharePreferences(name, value)
    }

    private fun getSharePreferences(name: String, default: T): T = with(sp) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> this.getString(name, default)!!
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can be get from Preferences")
        }
        return res as T
    }

    private fun putSharePreferences(name: String, value: T) = with(sp.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is Int -> putInt(name, value)
            is String -> putString(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }.apply()
    }
}