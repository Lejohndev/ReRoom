package com.example.revroom.features.design_studio.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom fan-blade spinner animation using Canvas.
 * Draws 4 curved blades rotating continuously — NOT a CircularProgressIndicator.
 * Spec: 80dp, 1200ms per revolution, gradient primary→tertiary.
 */
@Composable
fun FanSpinnerAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fan_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fan_angle"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.size(80.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val bladeLength = size.minDimension * 0.38f
        val bladeWidth = size.minDimension * 0.14f

        rotate(degrees = rotation, pivot = center) {
            for (i in 0 until 4) {
                val angleDeg = i * 90f
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val tipOffset = Offset(
                    center.x + bladeLength * cos(angleRad).toFloat(),
                    center.y + bladeLength * sin(angleRad).toFloat()
                )
                drawFanBlade(
                    center = center,
                    angleDeg = angleDeg,
                    bladeLength = bladeLength,
                    bladeWidth = bladeWidth,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, tertiaryColor),
                        start = center,
                        end = tipOffset
                    )
                )
            }
            // Center hub
            drawCircle(
                color = primaryColor,
                radius = size.minDimension * 0.08f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawFanBlade(
    center: Offset,
    angleDeg: Float,
    bladeLength: Float,
    bladeWidth: Float,
    brush: Brush
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val perpRad = Math.toRadians(angleDeg + 90.0)

    val tipX = center.x + bladeLength * cos(rad).toFloat()
    val tipY = center.y + bladeLength * sin(rad).toFloat()

    val curveOffset = bladeWidth * 0.6f
    val midFactor = 0.5f
    val midX = center.x + bladeLength * midFactor * cos(rad).toFloat()
    val midY = center.y + bladeLength * midFactor * sin(rad).toFloat()

    val path = Path().apply {
        moveTo(center.x, center.y)
        // Left curve to tip
        quadraticTo(
            midX + curveOffset * cos(perpRad).toFloat(),
            midY + curveOffset * sin(perpRad).toFloat(),
            tipX, tipY
        )
        // Right curve back to center
        quadraticTo(
            midX - curveOffset * cos(perpRad).toFloat(),
            midY - curveOffset * sin(perpRad).toFloat(),
            center.x, center.y
        )
        close()
    }
    drawPath(path = path, brush = brush)
}
