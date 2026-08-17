package com.quranread.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranread.app.data.Ayah
import com.quranread.app.data.QuranDatabaseHelper

@Composable
fun AyahScreen(dbHelper: QuranDatabaseHelper, surahId: Int, onBack: () -> Unit) {
    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }

    LaunchedEffect(surahId) {
        ayahs = dbHelper.getAyahsForSurah(surahId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Text("Back")
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(ayahs) { ayah ->
                Text(
                    text = "${ayah.arabicText} \uFD3F${ayah.ayahNumber}\uFD3E",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }
}
