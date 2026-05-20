package com.yingrensheng.app.data

import android.content.Context

class LoginPreferenceStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun lastUsername(): String = preferences.getString(KEY_LAST_USERNAME, "").orEmpty()

    fun hasAcceptedAgreement(): Boolean = preferences.getBoolean(KEY_ACCEPTED_AGREEMENT, false)

    fun saveLastUsername(username: String) {
        preferences.edit().putString(KEY_LAST_USERNAME, username).apply()
    }

    fun saveAcceptedAgreement() {
        preferences.edit().putBoolean(KEY_ACCEPTED_AGREEMENT, true).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "yingrensheng_auth"
        const val KEY_LAST_USERNAME = "last_username"
        const val KEY_ACCEPTED_AGREEMENT = "accepted_agreement"
    }
}
