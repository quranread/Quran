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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranread.app.data.Mushaf16DatabaseHelper
import com.quranread.app.data.MushafLine

private val MAX_FONT_SIZE = 22.sp
private val MIN_FONT_SIZE = 9.sp
private val SIDE_PADDING = 20.dp
private val PAGE_NUMBER_RESERVED_HEIGHT = 28.dp

/**
 * Simple test screen: no border/background yet. Verifies correct 16
 * lines per page, correct RTL word order, all lines guaranteed to fit
 * on screen (weight-based rows, single page-wide font size so nothing
 * overflows below the visible area), and proper side margins so no
 * letters look clipped at the edges.
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

            MushafPageContent(pageNumber = pageNumber, lines = lines)
        }
    }
}

@Composable
private fun MushafPageContent(pageNumber: Int, lines: List<MushafLine>) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Arabic text must lay out right-to-left, otherwise fragments can
    // end up in the wrong visual order.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = SIDE_PADDING,
                        end = SIDE_PADDING,
                        top = 12.dp,
                        bottom = PAGE_NUMBER_RESERVED_HEIGHT
                    )
            ) {
                val availableWidthPx = with(density) { maxWidth.toPx() }
                var fontSize by remember(pageNumber, lines) { mutableStateOf(MAX_FONT_SIZE) }
                var ready by remember(pageNumber, lines) { mutableStateOf(false) }

                LaunchedEffect(pageNumber, lines, availableWidthPx) {
                    if (lines.isEmpty() || availableWidthPx <= 0f) return@LaunchedEffect
                    var size = MAX_FONT_SIZE
                    while (size.value > MIN_FONT_SIZE.value) {
                        val fitsAll = lines.all { line ->
                            val result = textMeasurer.measure(
                                text = AnnotatedString(line.text),
                                style = TextStyle(fontFamily = AmiriQuranFont, fontSize = size),
                                maxLines = 1,
                                softWrap = false
                            )
                            result.size.width <= availableWidthPx
                        }
                        if (fitsAll) break
                        size = (size.value - 0.5f).sp
                    }
                    fontSize = size
                    ready = true
                }

                if (ready && lines.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        lines.forEach { line ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = line.text,
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
                }
            }

            Text(
                text = "$pageNumber",
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}
