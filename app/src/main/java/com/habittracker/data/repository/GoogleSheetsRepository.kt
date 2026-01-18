package com.habittracker.data.repository

import com.habittracker.data.model.Habit
import com.habittracker.data.remote.RetrofitClient
import com.habittracker.data.model.ValueRange
import com.habittracker.data.model.BatchUpdateSpreadsheetRequest
import com.habittracker.data.model.Request
import com.habittracker.data.model.AddSheetRequest
import com.habittracker.data.model.SheetProperties
import com.habittracker.data.model.GridProperties
import com.habittracker.util.Constants
import com.habittracker.util.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class GoogleSheetsRepository {

    private val spreadSheetId: String
        get() = UserPreferences.spreadsheetId ?: Constants.SPREADSHEET_ID
        
    // Cache map: "Month-Year-HabitName" -> RowIndex (Integer)
    private val rowIndexCache = ConcurrentHashMap<String, Int>()

    suspend fun fetchMasterHabits(token: String): List<Habit>? {
        return try {
            val range = "MasterHabits!A2:B"
            val response = RetrofitClient.sheetsService.getValues("Bearer $token", spreadSheetId, range)
            response.values?.mapNotNull { row ->
                val rawName = row.getOrNull(0)?.toString() ?: ""
                if (rawName.isBlank()) return@mapNotNull null
                val parts = rawName.split(" | ")
                val name = parts[0]
                val icon = if (parts.size > 1) parts[1] else "Check"
                Habit(name, iconName = icon)
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchMonthData(token: String, month: String, year: String): Map<String, List<Int>> {
        return withContext(Dispatchers.IO) {
            val sheetTitle = "$month-$year"
            try {
                if (getSheetId(token, sheetTitle) == null) return@withContext emptyMap()
                val range = "'$sheetTitle'!A2:AF" 
                val response = RetrofitClient.sheetsService.getValues("Bearer $token", spreadSheetId, range)
                val map = mutableMapOf<String, List<Int>>()
                
                response.values?.forEachIndexed { index, row ->
                    val name = row.getOrNull(0)?.toString() ?: ""
                    val actualRowIndex = index + 2 // A2 starts at row 2
                   
                    if (name.isNotBlank()) {
                         // Update cache
                         val cacheKey = "$sheetTitle-$name"
                         rowIndexCache[cacheKey] = actualRowIndex
                    
                         val days = mutableListOf<Int>()
                         for (i in 1..31) {
                             val cellValue = row.getOrNull(i)?.toString() ?: ""
                             if (cellValue == "1") days.add(i) // Completed
                             if (cellValue == "2") days.add(100 + i) // HIDDEN encoding
                         }
                         map[name] = days
                    }
                }
                map
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }

    suspend fun updateHabitStatus(token: String, month: String, year: String, habitName: String, day: Int, status: String) {
        withContext(Dispatchers.IO) {
             val sheetTitle = "$month-$year"
             val cacheKey = "$sheetTitle-$habitName"
             
             try {
                  // Optimization: Check Cache First
                  var rowNum = rowIndexCache[cacheKey] ?: -1
                  
                  // If not in cache, we MUST ensure sheet exists and read the rows
                  if (rowNum == -1) {
                      ensureSheetExists(token, sheetTitle, 32)
                      val rangeRead = "'$sheetTitle'!A2:A"
                      val readRes = RetrofitClient.sheetsService.getValues("Bearer $token", spreadSheetId, rangeRead)
                      val existingNames = readRes.values?.mapIndexed { index, list -> index to (list.getOrNull(0)?.toString() ?: "") } ?: emptyList()
                      val match = existingNames.find { it.second == habitName }
                      
                      if (match != null) {
                          rowNum = match.first + 2
                          rowIndexCache[cacheKey] = rowNum // Cache it!
                      } else {
                          // Create New Row
                          val appendRange = "'$sheetTitle'!A2:AF"
                          val rowData = MutableList(32) { "" }
                          rowData[0] = habitName
                          if (day in 1..31) {
                             rowData[day] = status
                          }
                          val body = ValueRange(range = appendRange, values = listOf(rowData))
                          RetrofitClient.sheetsService.appendValues("Bearer $token", spreadSheetId, appendRange, body = body)
                          return@withContext 
                      }
                  }

                  if (rowNum != -1) {
                      val colLetter = getColLetter(day) 
                      val cellRange = "'$sheetTitle'!${colLetter}${rowNum}"
                      val body = ValueRange(range = cellRange, values = listOf(listOf(status)))
                      RetrofitClient.sheetsService.updateValues("Bearer $token", spreadSheetId, cellRange, body = body)
                  }
             } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun getColLetter(day: Int): String {
        if (day < 26) {
            return ('A' + day).toString()
        } else {
             val first = (day / 26) - 1
             val second = day % 26
             return "${('A' + first)}${('A' + second)}"
        }
    }

    suspend fun addHabit(habitName: String, iconName: String, token: String) {
        withContext(Dispatchers.IO) {
            try {
                ensureSheetExists(token, "MasterHabits", 2)
                val range = "MasterHabits!A2:B"
                val rawValue = "$habitName | $iconName"
                val body = ValueRange(range = range, values = listOf(listOf(rawValue)))
                RetrofitClient.sheetsService.appendValues("Bearer $token", spreadSheetId, range, body = body)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    suspend fun syncToMaster(localHabits: List<Habit>, token: String) {
        withContext(Dispatchers.IO) {
            try {
                ensureSheetExists(token, "MasterHabits", 2)
                val remote = fetchMasterHabits(token) ?: emptyList()
                val remoteNames = remote.map { it.name.lowercase() }.toSet()
                
                localHabits.forEach { local ->
                    if (!remoteNames.contains(local.name.lowercase())) {
                        addHabit(local.name, local.iconName, token)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun deleteHabitPermanently(token: String, habitName: String, month: String, year: String) {
        withContext(Dispatchers.IO) {
            // 1. Remove from MasterHabits
            try {
                val range = "MasterHabits!A2:B"
                val response = RetrofitClient.sheetsService.getValues("Bearer $token", spreadSheetId, range)
                val values = response.values ?: emptyList()
                val updatedValues = values.filter { row ->
                    val rowNameStr = row.getOrNull(0)?.toString() ?: ""
                    val rowName = rowNameStr.split(" | ")[0]
                    rowName != habitName
                }
                
                // Clear and write updated
                RetrofitClient.sheetsService.clearValues("Bearer $token", spreadSheetId, range, body = emptyMap())
                if (updatedValues.isNotEmpty()) {
                    val body = ValueRange(range = range, values = updatedValues)
                    RetrofitClient.sheetsService.updateValues("Bearer $token", spreadSheetId, range, body = body)
                }
            } catch (e: Exception) { e.printStackTrace() }

            // 2. Remove from Current Month Sheet
            try {
                val sheetTitle = "$month-$year"
                val range = "'$sheetTitle'!A2:AF"
                val response = RetrofitClient.sheetsService.getValues("Bearer $token", spreadSheetId, range)
                val values = response.values ?: emptyList()
                val updatedValues = values.filter { row ->
                    (row.getOrNull(0)?.toString() ?: "") != habitName
                }

                RetrofitClient.sheetsService.clearValues("Bearer $token", spreadSheetId, range, body = emptyMap())
                if (updatedValues.isNotEmpty()) {
                    val body = ValueRange(range = range, values = updatedValues)
                    RetrofitClient.sheetsService.updateValues("Bearer $token", spreadSheetId, range, body = body)
                }
                // Clear cache
                rowIndexCache.remove("$sheetTitle-$habitName")
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun ensureSheetExists(t: String, title: String, cols: Int) {
        val id = getSheetId(t, title)
        if (id == null) {
            createSheetNamed(title, cols, t)
            val header: List<String>
            val range: String
            if (title == "MasterHabits") {
                header = listOf("Habit Name | Icon")
                range = "'$title'!A1:B1"
            } else {
                val list = MutableList(32) { "" }
                list[0] = "Habit Name"
                for (i in 1..31) list[i] = i.toString()
                header = list
                range = "'$title'!A1:AF1"
            }
            val body = ValueRange(range = range, values = listOf(header))
            RetrofitClient.sheetsService.appendValues("Bearer $t", spreadSheetId, range, body = body)
        }
    }

    private suspend fun getSheetId(t: String, title: String): Int? {
        return try {
            val meta = RetrofitClient.sheetsService.getSpreadsheet("Bearer $t", spreadSheetId)
            meta.sheets.find { it.properties.title == title }?.properties?.sheetId
        } catch (_: Exception) { null }
    }

    private suspend fun createSheetNamed(name: String, cols: Int, t: String) {
        val req = BatchUpdateSpreadsheetRequest(
            requests = listOf(Request(addSheet = AddSheetRequest(properties = SheetProperties(title = name, gridProperties = GridProperties(rowCount = 100, columnCount = cols)))))
        )
        RetrofitClient.sheetsService.batchUpdate("Bearer $t", spreadSheetId, body = req)
    }
}
