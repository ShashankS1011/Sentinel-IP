package com.example.sentinel_ip.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel_ip.ui.theme.ThreatDanger
import com.example.sentinel_ip.ui.theme.ThreatSafe
import com.example.sentinel_ip.ui.theme.ThreatWarning

fun getThreatColor(score: Int): Color {
    return when {
        score < 25 -> ThreatSafe
        score < 75 -> ThreatWarning
        else -> ThreatDanger
    }
}

fun getThreatLabel(score: Int): String {
    return when {
        score == 0 -> "Clean / Safe"
        score < 25 -> "Low Risk"
        score < 75 -> "Suspicious"
        else -> "Malicious / Dangerous"
    }
}

@Composable
fun ThreatGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val threatColor = getThreatColor(score)

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()

            // Background Arc (Gray track)
            drawArc(
                color = Color(0xFF2A2836),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Threat Progress Arc
            drawArc(
                color = threatColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = threatColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = getThreatLabel(score),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
