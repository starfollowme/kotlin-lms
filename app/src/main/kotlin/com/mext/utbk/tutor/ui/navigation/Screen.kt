package com.mext.utbk.tutor.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Beranda")
    object Materials : Screen("materials", "Materi")
    object MaterialDetail : Screen("material_detail/{subjectId}/{topicId}", "Detail Materi") {
        fun createRoute(subjectId: String, topicId: String) = "material_detail/$subjectId/$topicId"
    }
    object Quiz : Screen("quiz", "Latihan Soal")
    object Chat : Screen("chat", "Tutor AI")
    object History : Screen("history", "Riwayat")
    object Bookmarks : Screen("bookmarks", "Bookmark")
    object Simulation : Screen("simulation", "Simulasi UTBK/MEXT")
    object Planner : Screen("planner", "Rencana Belajar")
}
