package com.quranread.app.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.quranread.app.R

val AmiriQuranFont = FontFamily(
    Font(R.font.amiri_quran)
)

// Used for the 16-line IndoPak Mushaf screen only. Includes the glyphs
// for ayah-end circles and waqf (pause) marks that Amiri Quran doesn't
// have, since those are IndoPak-specific private-use-area codepoints.
val IndoPakFont = FontFamily(
    Font(R.font.indopak_nastaleeq)
)
