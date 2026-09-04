package com.quranread.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clipToBounds
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

// Extra vertical room reserved above/below each line's baseline, as a
// multiple of fontSize, so stacked marks (shadda, waqf/pause signs,
// hamza) that sit above or below a letter always fit inside that
// line's own row instead of bleeding into the row above/below it.
private const val LINE_HEIGHT_MULTIPLIER = 1.55f

// How far (as a fraction of a line's natural width) we're willing to
// stretch it via letter-spacing to reach the shared margin. Beyond
// this, stretching starts looking like unnatural gaps between letters
// rather than a justified line, so very short lines are centered
// instead of forced to fit.
private const val MAX_JUSTIFY_STRETCH_RATIO = 0.35f

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
                val availableHeightPx = with(density) { maxHeight.toPx() }
                var fontSize by remember(pageNumber, lines) { mutableStateOf(BASE_FONT_SIZE) }
                var ready by remember(pageNumber, lines) { mutableStateOf(false) }

                // One shared font size per page: the largest size that
                // satisfies BOTH constraints for every line -
                // (1) natural (unstretched) width still fits within the
                // available space, and (2) the line's height, INCLUDING
                // room for stacked marks (LINE_HEIGHT_MULTIPLIER), still
                // fits inside its own row (availableHeight / 16).
                // Checking width alone let a font size through that was
                // narrow enough but vertically too tall for its row,
                // which is what let waqf/pause marks spill outside
                // their line or into the next one. Shorter lines are
                // then justified (below) to reach the same full width.
                LaunchedEffect(pageNumber, lines, availableWidthPx, availableHeightPx) {
                    if (lines.isEmpty() || availableWidthPx <= 0f || availableHeightPx <= 0f) return@LaunchedEffect
                    val rowHeightPx = availableHeightPx / lines.size
                    var size = BASE_FONT_SIZE
                    while (size.value > MIN_FONT_SIZE.value) {
                        val fitsEveryLine = lines.all { line ->
                            val result = textMeasurer.measure(
                                text = AnnotatedString(line.text),
                                style = TextStyle(
                                    fontFamily = IndoPakFont,
                                    fontSize = size,
                                    lineHeight = size * LINE_HEIGHT_MULTIPLIER
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                            // small buffer so the longest line still has
                            // breathing room, not touching the border
                            result.size.width <= availableWidthPx * 0.97f &&
                                result.size.height <= rowHeightPx
                        }
                        if (fitsEveryLine) break
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
                                    .weight(1f)
                                    // Safety net: even with the height-aware
                                    // sizing above, this guarantees a mark
                                    // can never visually bleed into the row
                                    // above/below it - worst case it gets
                                    // clipped instead of overlapping.
                                    .clipToBounds(),
                                contentAlignment = Alignment.Center
                            ) {
                                MushafLineText(
                                    line = line,
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
 * Renders one Mushaf line.
 *
 * - Surah-name banners and the basmallah are short by design and meant
 *   to sit centered on their own row (this is exactly what the
 *   database's `isCentered` / `lineType` columns encode) - these are
 *   left centered, never stretched.
 * - Ordinary ayah lines are justified via letter-spacing so they start
 *   and end at the same margin as every other line on the page, the
 *   way a printed Mushaf line is typeset. This is a letter-spacing
 *   approximation of true kashida justification (real kashida needs
 *   font-level glyph elongation the font/renderer doesn't expose here)
 *   - it adds even spacing between characters rather than scaling any
 *     glyph, so nothing gets visually distorted, but the space added
 *     between a base letter and its own diacritic can look very
 *     slightly looser than print on heavily-marked lines.
 * - A line far shorter than the target width (rare, e.g. a very short
 *   final line) is centered instead of force-stretched, since spacing
 *   it out that much would look wrong rather than justified.
 */
@Composable
private fun MushafLineText(
    line: MushafLine,
    fontSize: androidx.compose.ui.unit.TextUnit,
    availableWidthPx: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val density = LocalDensity.current
    val shouldCenter = line.isCentered ||
        line.lineType == "surah_name" ||
        line.lineType == "basmallah" ||
        line.text.isEmpty()

    val baseStyle = TextStyle(
        fontFamily = IndoPakFont,
        fontSize = fontSize,
        lineHeight = fontSize * LINE_HEIGHT_MULTIPLIER,
        textAlign = TextAlign.Center
    )

    if (shouldCenter) {
        Text(
            text = line.text,
            style = baseStyle,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    val naturalWidthPx = textMeasurer.measure(
        text = AnnotatedString(line.text),
        style = baseStyle,
        maxLines = 1,
        softWrap = false
    ).size.width

    val extraWidthPx = availableWidthPx - naturalWidthPx
    val visibleCharCount = line.text.count { !it.isWhitespace() }

    val letterSpacing = if (
        extraWidthPx > 0f &&
        visibleCharCount > 0 &&
        extraWidthPx <= naturalWidthPx * MAX_JUSTIFY_STRETCH_RATIO
    ) {
        with(density) { (extraWidthPx / visibleCharCount).toSp() }
    } else {
        0.sp
    }

    Text(
        text = line.text,
        style = baseStyle.copy(letterSpacing = letterSpacing),
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.fillMaxWidth()
    )
}
