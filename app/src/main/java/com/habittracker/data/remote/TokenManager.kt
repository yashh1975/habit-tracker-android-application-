package com.habittracker.data.remote

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.habittracker.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper to retrieve OAuth 2.0 Access Token
 */
object TokenManager {
    
    suspend fun getAccessToken(context: Context, account: Account): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Scope must match what was requested in GoogleSignInOptions
                // Prefix "oauth2:" is required for GoogleAuthUtil
                val scope = "oauth2:${Constants.SCOPE_SPREADSHEETS}"
                
                // This call blocks and must be run on background thread
                GoogleAuthUtil.getToken(context, account, scope)
            } catch (e: Exception) {
                Log.e("TokenManager", "Error getting token: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Invalidate token (if 401 occurs)
     */
    suspend fun invalidateToken(context: Context, token: String) {
        withContext(Dispatchers.IO) {
            try {
                GoogleAuthUtil.clearToken(context, token)
            } catch (e: Exception) {
                Log.e("TokenManager", "Error clearing token: ${e.message}")
            }
        }
    }
}
