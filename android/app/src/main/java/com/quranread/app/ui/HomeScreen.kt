package com.quranread.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenTranslationQuran: () -> Unit,
    onOpenMushaf16: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onOpenTranslationQuran, modifier = Modifier.padding(8.dp)) {
            Text("Quran with Translation")
        }
        Button(onClick = onOpenMushaf16, modifier = Modifier.padding(8.dp)) {
            Text("16 Line Mushaf")
        }
    }
}
