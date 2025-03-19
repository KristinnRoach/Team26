package com.example.team26.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing an item in the navigation bar.
 * @param label The display label for the navigation item
 * @param icon The icon to display for the navigation item
 * @param route The route that this navigation item points to
 * @param badgeCount Optional badge count to display on the navigation item
 */
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route,
    val badgeCount: Int? = null
)

/*
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
    //val route: String
)

 */

