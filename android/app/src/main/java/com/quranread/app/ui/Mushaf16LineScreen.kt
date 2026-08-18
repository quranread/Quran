package com.quranread.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranread.app.data.Mushaf16DatabaseHelper
import com.quranread.app.data.MushafLine

/**
 * Simple test screen: no border/background yet, just verifies that the
 * correct 16 lines show up for the correct page, in correct RTL order,
 * each staying on a single print-line (auto-shrinking font to fit),
 * and swiping between pages works. Border image + custom IndoPak font
 * get layered on later.
 */
@OptIn(ExperimentalFoundationApi::class)
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

            // Arabic text must lay out right-to-left, otherwise wrapped/
            // constrained text can appear with fragments in the wrong order.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        lines.forEach { line ->
                            AutoSizeMushafLine(
                                text = line.text,
                                maxFontSize = 22.sp
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
}

/**
 * Renders one printed Mushaf line on a single visual line, shrinking the
 * font size just enough for it to fit the available width without
 * wrapping - matching how a real Mushaf page is typeset.
 */
@Composable
private fun AutoSizeMushafLine(text: String, maxFontSize: TextUnit) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        var fontSize by remember(text, maxWidthPx) { mutableStateOf(maxFontSize) }
        var measured by remember(text, maxWidthPx) { mutableStateOf(false) }

        LaunchedEffect(text, maxWidthPx) {
            var size = maxFontSize
            while (size.value > 8f) {
                val result = textMeasurer.measure(
                    text = AnnotatedString(text),
                    style = TextStyle(fontFamily = AmiriQuranFont, fontSize = size),
                    maxLines = 1,
                    softWrap = false
                )
                if (result.size.width <= maxWidthPx) break
                size = (size.value - 0.5f).sp
            }
            fontSize = size
            measured = true
        }

        if (measured) {
            Text(
                text = text,
                fontFamily = AmiriQuranFont,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
