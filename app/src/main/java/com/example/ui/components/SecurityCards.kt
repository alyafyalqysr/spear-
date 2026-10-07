package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DbAsset
import com.example.data.DbVulnerability
import com.example.ui.theme.*

@Composable
fun AssetCard(
    asset: DbAsset,
    onScan: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_card_${asset.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (asset.status.contains("Vulnerable")) CyberAlertCritical.copy(alpha = 0.5f) else CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (asset.status.contains("Vulnerable")) CyberAlertCritical else CyberGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = asset.hostname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Asset",
                        tint = CyberAlertCritical.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle metadata
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("IP address / Target", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                    Text(asset.ip, style = MaterialTheme.typography.bodyMedium, color = CyberBlue, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Environment Node", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                    Text(asset.cloudProvider, style = MaterialTheme.typography.bodyMedium, color = CyberTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Services chips
            Text("Active Interfaces:", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                asset.servicesString.split(",").forEach { service ->
                    Box(
                        modifier = Modifier
                            .background(CyberSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = service.trim(),
                            fontSize = 11.sp,
                            color = CyberBlue,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lower Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Status Indicator", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                    Text(
                        text = if (asset.status.contains("Vulnerable")) "⚠️ ${asset.status}" else "✔️ Safe Asset",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (asset.status.contains("Vulnerable")) CyberAlertCritical else CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberBlue,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Scan icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح الثغرات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VulnerabilityItem(
    vulnerability: DbVulnerability,
    onAssess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val cardBorderColor = when (vulnerability.severity.uppercase()) {
        "CRITICAL" -> CyberAlertCritical
        "HIGH" -> CyberAlertCritical.copy(alpha = 0.7f)
        "MEDIUM" -> CyberAlertWarning
        else -> CyberBlue
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vuln_card_${vulnerability.id}")
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SeverityLabel(severity = vulnerability.severity)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = vulnerability.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberBlue,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vulnerability.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand info",
                    tint = CyberTextSecondary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = CyberBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Description / الوصف", style = MaterialTheme.typography.titleSmall, color = CyberBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vulnerability.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("CVE / المعرّف العالمي", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                            Text(vulnerability.cve, style = MaterialTheme.typography.bodyMedium, color = CyberTextPrimary, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Target Host ID", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                            Text(vulnerability.assetId, style = MaterialTheme.typography.bodyMedium, color = CyberBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Remediation / التوصية البرمجية لحل الثغرة", style = MaterialTheme.typography.titleSmall, color = CyberGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberCard, RoundedCornerShape(6.dp))
                            .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = vulnerability.recommendedFix,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAssess,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Assess", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نمذجة التهديدات وتقييم الحل عبر الذكاء الاصطناعي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeverityLabel(severity: String) {
    val (bg, txt) = when (severity.uppercase()) {
        "CRITICAL" -> CyberAlertCritical.copy(alpha = 0.2f) to CyberAlertCritical
        "HIGH" -> CyberAlertCritical.copy(alpha = 0.15f) to CyberAlertCritical.copy(alpha = 0.85f)
        "MEDIUM" -> CyberAlertWarning.copy(alpha = 0.2f) to CyberAlertWarning
        else -> CyberBlue.copy(alpha = 0.2f) to CyberBlue
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .border(1.dp, txt.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = severity,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = txt
        )
    }
}
