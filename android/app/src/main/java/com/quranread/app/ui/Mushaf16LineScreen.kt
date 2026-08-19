package com.quranread.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

// Set to true temporarily to see row dividers + the text-block boundary
// box, so misalignment is easy to spot before the real border image is
// added. Set back to false (or delete this + the guide code) once the
// border is in place.
private const val SHOW_DEBUG_GUIDES = true

private val BASE_FONT_SIZE = 20.sp
private val MIN_FONT_SIZE = 9.sp

// Generous reserved space on every side so the decorative border, added
// later, always sits fully outside the text block with room to spare.
private val SIDE_PADDING = 28.dp
private val TOP_PADDING = 28.dp
private val BOTTOM_PADDING = 40.dp // extra room reserved for page number

/**
 * 16-line Mushaf page screen. No visible in-app back button - navigation
 * relies entirely on the device/system back gesture or button, which
 * Compose Navigation already handles automatically via the back stack.
 *
 * Every printed line is stretched (justified) so it starts and ends at
 * exactly the same left/right margin as every other line on the page -
 * matching how a real Mushaf page is typeset - so a rectangular border
 * added later will frame the text block cleanly on all sides.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Mushaf16LineScreen(dbHelper: Mushaf16DatabaseHelper, onBack: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dbHelper.totalPages }
    )

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
                        top = TOP_PADDING,
                        bottom = BOTTOM_PADDING
                    )
            ) {
                val availableWidthPx = with(density) { maxWidth.toPx() }
                var fontSize by remember(pageNumber, lines) { mutableStateOf(BASE_FONT_SIZE) }
                var ready by remember(pageNumber, lines) { mutableStateOf(false) }

                // One shared font size per page: the largest size at
                // which the longest line's *natural* (unstretched)
                // width still fits within the available space. Shorter
                // lines are then stretched (below) to reach the same
                // full width - this mirrors how a real justified
                // Mushaf line is typeset (short lines get more
                // kashida-style stretch, the longest line needs none).
                LaunchedEffect(pageNumber, lines, availableWidthPx) {
                    if (lines.isEmpty() || availableWidthPx <= 0f) return@LaunchedEffect
                    var size = BASE_FONT_SIZE
                    while (size.value > MIN_FONT_SIZE.value) {
                        val longestFits = lines.all { line ->
                            val result = textMeasurer.measure(
                                text = AnnotatedString(line.text),
                                style = TextStyle(fontFamily = AmiriQuranFont, fontSize = size),
                                maxLines = 1,
                                softWrap = false
                            )
                            result.size.width <= availableWidthPx
                        }
                        if (longestFits) break
                        size = (size.value - 0.5f).sp
                    }
                    fontSize = size
                    ready = true
                }

                if (ready && lines.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .let {
                                if (SHOW_DEBUG_GUIDES) {
                                    // Red box = exactly where the border's
                                    // inner edge should sit later.
                                    it.border(1.dp, Color.Red)
                                } else it
                            },
                        verticalArrangement = Arrangement.Top
                    ) {
                        lines.forEachIndexed { index, line ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                JustifiedMushafLine(
                                    text = line.text,
                                    fontSize = fontSize,
                                    availableWidthPx = availableWidthPx,
                                    textMeasurer = textMeasurer
                                )
                            }
                            if (SHOW_DEBUG_GUIDES && index != lines.lastIndex) {
                                Divider(color = Color.Blue, thickness = 0.5.dp)
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
                    .padding(bottom = 10.dp)
            )
        }
    }
}

/**
 * Renders one line stretched (or slightly compressed) horizontally so
 * it exactly fills the available width - every line then starts and
 * ends at the same margin, like a justified line in the printed
 * Mushaf. The Text is first sized to its own natural, unclipped width
 * and only then scaled, so no glyphs are ever cut off.
 */
@Composable
private fun JustifiedMushafLine(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    availableWidthPx: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    var naturalWidthPx by remember(text, fontSize) { mutableStateOf(0f) }
    var scaleX by remember(text, fontSize, availableWidthPx) { mutableStateOf(1f) }
    var ready by remember(text, fontSize, availableWidthPx) { mutableStateOf(false) }

    LaunchedEffect(text, fontSize, availableWidthPx) {
        if (availableWidthPx <= 0f) return@LaunchedEffect
        val result = textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(fontFamily = AmiriQuranFont, fontSize = fontSize),
            maxLines = 1,
            softWrap = false
        )
        if (result.size.width > 0) {
            naturalWidthPx = result.size.width.toFloat()
            scaleX = (availableWidthPx / naturalWidthPx).coerceIn(0.85f, 1.4f)
            ready = true
        }
    }

    if (ready) {
        Text(
            text = text,
            fontFamily = AmiriQuranFont,
            fontSize = fontSize,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { this.scaleX = scaleX }
        )
    }
}
