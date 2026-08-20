package com.quranread.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileOutputStream

data class MushafLine(
    val lineNumber: Int,
    val lineType: String, // "ayah", "surah_name", or "basmallah"
    val isCentered: Boolean,
    val surahNumber: Int?,
    val text: String
)

data class AyahMetadata(
    val surah: Int,
    val ayah: Int,
    val juzNumber: Int?,
    val hizbNumber: Int?,
    val rubElHizbNumber: Int?,
    val rukuNumber: Int?,
    val manzilNumber: Int?,
    val sajdahNumber: Int?
)

class Mushaf16DatabaseHelper(private val context: Context) {

    private val dbName = "mushaf16_v2.db"

    // The Indopak 16-line (Taj company) mushaf edition has 548 pages total.
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
            """SELECT line_number, line_type, is_centered, surah_number, text
               FROM page_lines WHERE page_number = ? ORDER BY line_number""",
            arrayOf(pageNumber.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MushafLine(
                        lineNumber = it.getInt(0),
                        lineType = it.getString(1),
                        isCentered = it.getInt(2) == 1,
                        surahNumber = if (it.isNull(3)) null else it.getInt(3),
                        text = it.getString(4) ?: ""
                    )
                )
            }
        }
        db.close()
        return list
    }

    /**
     * Verse-level metadata (ruku/sajdah/juz/manzil/hizb numbers) for one ayah.
     */
    fun getAyahMetadata(surah: Int, ayah: Int): AyahMetadata? {
        val db = openDatabase()
        val cursor = db.rawQuery(
            """SELECT surah, ayah, juz_number, hizb_number, rub_el_hizb_number,
               ruku_number, manzil_number, sajdah_number
               FROM ayah_metadata WHERE surah = ? AND ayah = ?""",
            arrayOf(surah.toString(), ayah.toString())
        )
        var result: AyahMetadata? = null
        cursor.use {
            if (it.moveToFirst()) {
                result = AyahMetadata(
                    surah = it.getInt(0),
                    ayah = it.getInt(1),
                    juzNumber = if (it.isNull(2)) null else it.getInt(2),
                    hizbNumber = if (it.isNull(3)) null else it.getInt(3),
                    rubElHizbNumber = if (it.isNull(4)) null else it.getInt(4),
                    rukuNumber = if (it.isNull(5)) null else it.getInt(5),
                    manzilNumber = if (it.isNull(6)) null else it.getInt(6),
                    sajdahNumber = if (it.isNull(7)) null else it.getInt(7)
                )
            }
        }
        db.close()
        return result
    }
}
