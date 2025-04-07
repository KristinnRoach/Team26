package com.example.team26.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    // Replace this with actual logic fetching from preferences/auth state later
    val userRole = remember { "Admin" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.20f))

        // Title Text
        Text(
            text = "Gungnir",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(0.50f))

        // User Status Text
        Text(
            text = "Logged in as: $userRole",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.weight(0.25f))
    }
}
