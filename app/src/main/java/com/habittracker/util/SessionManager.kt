package com.habittracker.util

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.habittracker.util.Constants

object SessionManager {
    var currentUserAccount: GoogleSignInAccount? = null
    private var applicationContext: Context? = null
    
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
    
    fun getContext(): Context? {
        return applicationContext
    }
    
    fun isLoggedIn(): Boolean {
        return currentUserAccount != null
    }

    suspend fun getAccessToken(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val account = currentUserAccount?.account ?: return@withContext null
                val scope = "oauth2:${Constants.SCOPE_SPREADSHEETS}"
                GoogleAuthUtil.getToken(context, account, scope)
            } catch (e: Exception) {
                Log.e("SessionManager", "Error getting token", e)
                null
            }
        }
    }
}