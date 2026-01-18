package com.habittracker.util
import android.content.Context
import android.content.SharedPreferences
object UserPreferences {
    private const val PREF_NAME = "habit_pulse_prefs"
    private const val KEY_SPREADSHEET_ID = "spreadsheet_id"
    private var prefs: SharedPreferences? = null
    fun init(context: Context) {
        if (prefs == null) prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    var spreadsheetId: String?
        get() = prefs?.getString(KEY_SPREADSHEET_ID, null)
        set(value) = prefs?.edit()?.putString(KEY_SPREADSHEET_ID, value)?.apply() ?: Unit
    val isSetupComplete: Boolean get() = !spreadsheetId.isNullOrBlank()
}
