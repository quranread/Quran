package com.quranread.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quranread.app.data.QuranDatabaseHelper
import com.quranread.app.ui.AyahScreen
import com.quranread.app.ui.SurahListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = QuranDatabaseHelper(this)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "surahs") {
                        composable("surahs") {
                            SurahListScreen(dbHelper = dbHelper) { surahId ->
                                navController.navigate("ayahs/$surahId")
                            }
                        }
                        composable("ayahs/{surahId}") { backStackEntry ->
                            val surahId = backStackEntry.arguments?.getString("surahId")?.toIntOrNull() ?: 1
                            AyahScreen(dbHelper = dbHelper, surahId = surahId) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
