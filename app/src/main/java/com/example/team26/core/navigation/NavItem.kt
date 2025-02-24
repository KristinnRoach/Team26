package com.example.team26.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
    //val route: String
)

