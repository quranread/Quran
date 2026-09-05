package com.quranread.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG

/**
 * PROOF OF CONCEPT ONLY - not the final screen.
 *
 * Renders a single page of the 604-page Hafs Mushaf from the
 * ligature-based SVG dataset (assets/svg_mushaf/NNN.svg, MushafDatabase
 * "SVG V1.01", Sadaqa-e-Jaria license). Each page's text, diacritics,
 * and ayah marks are already laid out inside the SVG itself - so unlike
 * Mushaf16LineScreen, there's no font shaping, no justification, and no
 * mark-positioning logic here at all. This screen's only job is to
 * parse the SVG once and draw it, letterboxed to fit the screen without
 * distortion.
 *
 * Purpose of this POC: confirm AndroidSVG renders these specific files
 * correctly (all the grouped word/diacritic/ornament paths) before we
 * invest in packaging all 604 pages and building real navigation.
 *
 * Only page 604 is bundled right now for testing - drop more pages
 * into assets/svg_mushaf/ (named 001.svg .. 604.svg) as they're added.
 */
@Composable
fun HafsMushafPocScreen(startPage: Int = 604) {
    var pageNumber by remember { mutableIntStateOf(startPage) }

    Box(modifier = Modifier.fillMaxSize()) {
        SvgPage(pageNumber = pageNumber, modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { if (pageNumber > 1) pageNumber-- }) {
                Text("‹ Previous")
            }
            Text("Page $pageNumber", modifier = Modifier.align(Alignment.CenterVertically))
            Button(onClick = { if (pageNumber < 604) pageNumber++ }) {
                Text("Next ›")
            }
        }
    }
}

@Composable
private fun SvgPage(pageNumber: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Parsing is real work (each file is a few hundred KB of paths), so
    // it's re-done only when the page number actually changes, not on
    // every recomposition/animation frame.
    val svg = remember(pageNumber) {
        val fileName = "svg_mushaf/%03d.svg".format(pageNumber)
        runCatching {
            context.assets.open(fileName).use { input -> SVG.getFromInputStream(input) }
        }.getOrNull()
    }

    if (svg == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Page $pageNumber not bundled yet\n(assets/svg_mushaf/%03d.svg missing)".format(pageNumber))
        }
        return
    }

    Canvas(modifier = modifier) {
        drawIntoCanvas { canvas ->
            val docWidth = svg.documentWidth.takeIf { it > 0f } ?: 595f
            val docHeight = svg.documentHeight.takeIf { it > 0f } ?: 842f

            // Fit inside the available space WITHOUT stretching - a
            // print-typeset page distorted to fill the screen would
            // undo the entire point of using pre-typeset SVGs.
            val scale = minOf(size.width / docWidth, size.height / docHeight)
            val renderedWidth = docWidth * scale
            val renderedHeight = docHeight * scale
            val left = (size.width - renderedWidth) / 2f
            val top = (size.height - renderedHeight) / 2f

            svg.setDocumentWidth(renderedWidth)
            svg.setDocumentHeight(renderedHeight)

            val nativeCanvas = canvas.nativeCanvas
            val checkpoint = nativeCanvas.save()
            nativeCanvas.translate(left, top)
            svg.renderToCanvas(nativeCanvas)
            nativeCanvas.restoreToCount(checkpoint)
        }
    }
}
