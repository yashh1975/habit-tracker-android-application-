package com.habittracker.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.data.local.AppDatabase
import com.habittracker.data.repository.GoogleSheetsRepository
import com.habittracker.util.SessionManager
import com.habittracker.util.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.habittracker.data.local.HabitEntity

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                UserPreferences.init(applicationContext)
                
                if (UserPreferences.spreadsheetId.isNullOrBlank()) return@withContext Result.failure()

                val account = SessionManager.currentUserAccount
                if (account == null) {
                     return@withContext Result.retry() 
                }
                
                val token = SessionManager.getAccessToken(applicationContext)
                
                if (token == null) return@withContext Result.failure()
                
                val db = AppDatabase.getDatabase(applicationContext)
                val dao = db.habitDao()
                val repo = GoogleSheetsRepository()
                
                val localHabits = dao.getAllHabitsSync()
                
                repo.syncToMaster(localHabits.map { entity: HabitEntity -> 
                    com.habittracker.data.model.Habit(entity.name, iconName = entity.iconName) 
                }, token)
                
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}