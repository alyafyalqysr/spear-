package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun ApiKeyWarningBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("api_key_warning_banner"),
        colors = CardDefaults.cardColors(
            containerColor = CyberAlertWarning.copy(alpha = 0.15f),
            contentColor = CyberAlertWarning
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CyberAlertWarning.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Api Alert",
                tint = CyberAlertWarning,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مفتاح الذكاء الاصطناعي (Gemini) غير مفعّل",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyberTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "يعمل النظام حالياً بالوضع المحاكي (Offline Sandbox). لتفعيل المسح الحقيقي بالذكاء الاصطناعي، يرجى إعداد مفتاح GEMINI_API_KEY عبر لوحة أسرار AI Studio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = CyberTextPrimary)
            ) {
                Text("تخطي")
            }
        }
    }
}

@Composable
fun SecurityRadarChart(
    criticalCount: Int,
    highCount: Int,
    mediumCount: Int,
    lowCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(CyberSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw cyber grids representation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f

            // Radar sweeps rings
            drawCircle(
                color = CyberBorder.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
            drawCircle(
                color = CyberBorder.copy(alpha = 0.2f),
                radius = radius * 0.66f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
            drawCircle(
                color = CyberBorder.copy(alpha = 0.1f),
                radius = radius * 0.33f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            // Crosshairs lines
            drawLine(
                color = CyberBorder.copy(alpha = 0.25f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawLine(
                color = CyberBorder.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Stats Overlay
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegendNode(label = "CRITICAL", count = criticalCount, color = CyberAlertCritical)
            ChartLegendNode(label = "HIGH", count = highCount, color = CyberAlertWarning)
            ChartLegendNode(label = "MEDIUM", count = mediumCount, color = CyberAlertLow)
            ChartLegendNode(label = "LOW", count = lowCount, color = CyberBlue)
        }
    }
}

@Composable
fun ChartLegendNode(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 44.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .border(2.dp, color, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CyberTextSecondary
        )
    }
}
