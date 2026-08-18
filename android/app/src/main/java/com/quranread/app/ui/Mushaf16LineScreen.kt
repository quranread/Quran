package com.quranread.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranread.app.data.Mushaf16DatabaseHelper
import com.quranread.app.data.MushafLine

/**
 * Simple test screen: no border/background yet, just verifies that the
 * correct 16 lines show up for the correct page and swiping between
 * pages works. Border image + custom IndoPak font get layered on later.
 */
@Composable
fun Mushaf16LineScreen(dbHelper: Mushaf16DatabaseHelper, onBack: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dbHelper.totalPages }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Text("Back")
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageNumber = pageIndex + 1
            var lines by remember(pageNumber) { mutableStateOf<List<MushafLine>>(emptyList()) }

            LaunchedEffect(pageNumber) {
                lines = dbHelper.getLinesForPage(pageNumber)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    lines.forEach { line ->
                        Text(
                            text = line.text,
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = "$pageNumber",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}
