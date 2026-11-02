package com.example.sentinel_ip.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HeatmapPoint(val name: String, val xRatio: Float, val yRatio: Float, val intensity: Float)

@Composable
fun ThreatHeatmap(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by transition.animateFloat(
        initialValue = 8f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRadius"
    )

    val points = listOf(
        HeatmapPoint("US East", 0.25f, 0.35f, 0.9f),
        HeatmapPoint("Europe", 0.50f, 0.30f, 0.85f),
        HeatmapPoint("India", 0.68f, 0.50f, 0.95f),
        HeatmapPoint("East Asia", 0.82f, 0.40f, 0.90f),
        HeatmapPoint("South America", 0.35f, 0.70f, 0.75f)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Live Global Threat Heatmap",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF0F0E13), shape = RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val w = size.width
                    val h = size.height

                    // Draw World Grid Lines
                    for (x in 0..10) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(x * w / 10f, 0f),
                            end = Offset(x * w / 10f, h),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..5) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y * h / 5f),
                            end = Offset(w, y * h / 5f),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Threat Hotspots
                    points.forEach { pt ->
                        val center = Offset(pt.xRatio * w, pt.yRatio * h)
                        drawCircle(
                            color = Color(0xFFFF2B4A).copy(alpha = 0.25f),
                            radius = pulseRadius * pt.intensity,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFFF2B4A),
                            radius = 4f,
                            center = center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "High-density attack origins detected across North America, Europe, India, and East Asia",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
