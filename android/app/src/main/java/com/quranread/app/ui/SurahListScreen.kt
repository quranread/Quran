package com.quranread.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranread.app.data.QuranDatabaseHelper
import com.quranread.app.data.Surah

@Composable
fun SurahListScreen(dbHelper: QuranDatabaseHelper, onSurahClick: (Int) -> Unit) {
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }

    LaunchedEffect(Unit) {
        surahs = dbHelper.getAllSurahs()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(surahs) { surah ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSurahClick(surah.id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "${surah.id}. ${surah.nameEn}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "${surah.revelationPlace} • ${surah.ayahCount} Ayahs", fontSize = 12.sp)
                }
                Text(text = surah.nameAr, fontSize = 18.sp)
            }
            Divider()
        }
    }
}
