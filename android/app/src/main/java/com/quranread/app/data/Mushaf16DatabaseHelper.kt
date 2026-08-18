package com.quranread.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileOutputStream

data class MushafLine(
    val lineNumber: Int,
    val text: String
)

class Mushaf16DatabaseHelper(private val context: Context) {

    private val dbName = "mushaf16_lines.db"

    // A 16-line Taj Company mushaf edition has 548 pages total.
    val totalPages = 548

    private fun getDatabasePath(): String {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(dbName).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return dbFile.path
    }

    private fun openDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
    }

    fun getLinesForPage(pageNumber: Int): List<MushafLine> {
        val list = mutableListOf<MushafLine>()
        val db = openDatabase()
        val cursor = db.rawQuery(
            "SELECT line_number, text FROM page_lines WHERE page_number = ? ORDER BY line_number",
            arrayOf(pageNumber.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MushafLine(
                        lineNumber = it.getInt(0),
                        text = it.getString(1)
                    )
                )
            }
        }
        db.close()
        return list
    }
}
