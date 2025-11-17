package com.example.chalkitup.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MovingCharacterGif() {
    // Get screen width and height in Dp
    val screenWidth = LocalDensity.current.density * LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalDensity.current.density * LocalConfiguration.current.screenHeightDp

    // Create an Animatable for controlling the position with Float
    val animatableX = remember { Animatable((-200).toFloat()) }

    // Start the animation in a continuous loop
    LaunchedEffect(true) {
        // Infinite loop for moving the character
        while (true) {
            // Move character to the right
            animatableX.animateTo(
                targetValue = screenWidth, // Move to the right
                animationSpec = tween(durationMillis = 7000, easing = LinearEasing) // edit durationMillis to change walking speed
            )
            // Reset position to the left side after completing the animation
            animatableX.snapTo((-200).toFloat()) // Instantly snap back to the left
            delay(30000) // pause before starting again // edit delay to change pause between animations
        }
    }

    // Render the GIF
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.BottomStart)
    ) {
        GlideImage(
            model = "file:///android_asset/walkingChalkGif.gif",
            contentDescription = "Walking Character",
            modifier = Modifier
                .offset { IntOffset(animatableX.value.roundToInt(), (-20).dp.roundToPx()) }
                .size(100.dp)
        )
    }
}

