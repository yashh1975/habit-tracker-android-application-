package com.habittracker.data.remote
import com.habittracker.data.model.BatchUpdateSpreadsheetRequest
import com.habittracker.data.model.Spreadsheet
import com.habittracker.data.model.ValueRange
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
interface GoogleSheetsService {
    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getValues(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String, @Path("range") range: String): ValueRange
    @PUT("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun updateValues(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String, @Path("range") range: String, @Query("valueInputOption") valueInputOption: String = "USER_ENTERED", @Body body: ValueRange): ValueRange
    @POST("v4/spreadsheets/{spreadsheetId}:batchUpdate")
    suspend fun batchUpdate(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String, @Body body: BatchUpdateSpreadsheetRequest): Spreadsheet
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String, @Path("range") range: String, @Query("valueInputOption") valueInputOption: String = "USER_ENTERED", @Body body: ValueRange): ValueRange
    @POST("v4/spreadsheets")
    suspend fun createSpreadsheet(@Header("Authorization") authHeader: String, @Body body: Spreadsheet): Spreadsheet
    @GET("v4/spreadsheets/{spreadsheetId}")
    suspend fun getSpreadsheet(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String): Spreadsheet
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:clear")
    suspend fun clearValues(@Header("Authorization") authHeader: String, @Path("spreadsheetId") spreadsheetId: String, @Path("range") range: String, @Body body: Map<String, String>): Any
}
