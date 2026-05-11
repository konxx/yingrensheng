package com.yingrensheng.app.data

import android.content.Context

class LoginPreferenceStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun lastUsername(): String = preferences.getString(KEY_LAST_USERNAME, "").orEmpty()

    fun saveLastUsername(username: String) {
        preferences.edit().putString(KEY_LAST_USERNAME, username).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "yingrensheng_auth"
        const val KEY_LAST_USERNAME = "last_username"
    }
}
