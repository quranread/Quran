package com.quranread.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileOutputStream

data class Surah(
    val id: Int,
    val nameAr: String,
    val nameEn: String,
    val revelationPlace: String,
    val ayahCount: Int
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val ayahNumber: Int,
    val arabicText: String
)

class QuranDatabaseHelper(private val context: Context) {

    private val dbName = "quran.db"

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

    fun getAllSurahs(): List<Surah> {
        val list = mutableListOf<Surah>()
        val db = openDatabase()
        val cursor = db.rawQuery(
            "SELECT id, name_ar, name_en, revelation_place, ayah_count FROM surahs ORDER BY id",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Surah(
                        id = it.getInt(0),
                        nameAr = it.getString(1),
                        nameEn = it.getString(2),
                        revelationPlace = it.getString(3),
                        ayahCount = it.getInt(4)
                    )
                )
            }
        }
        db.close()
        return list
    }

    fun getAyahsForSurah(surahId: Int): List<Ayah> {
        val list = mutableListOf<Ayah>()
        val db = openDatabase()
        val cursor = db.rawQuery(
            "SELECT id, surah_id, ayah_number, arabic_text FROM ayahs WHERE surah_id = ? ORDER BY ayah_number",
            arrayOf(surahId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Ayah(
                        id = it.getInt(0),
                        surahId = it.getInt(1),
                        ayahNumber = it.getInt(2),
                        arabicText = it.getString(3)
                    )
                )
            }
        }
        db.close()
        return list
    }
}
