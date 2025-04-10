package com.example.team26.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team26.R

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
        Spacer(modifier = Modifier.weight(0.12f))


        Image(  // Title Text
            painter = painterResource(id = R.drawable.pilot_bloks),
            contentDescription = "Pilot Bloks Logo",
            Modifier.scale(1.5f, 1.5f)
            )


        Spacer(modifier = Modifier.weight(0.60f))

        // User Status Text
        Text(
            text = "Logged in as: $userRole",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.weight(0.15f))
    }
}
