package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DbVulnerability
import com.example.model.AIAnalysisResponse
import com.example.ui.theme.*
import com.example.viewmodel.AnalysisUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatModelingDialog(
    vulnerability: DbVulnerability,
    analysisState: AnalysisUiState,
    onClose: () -> Unit,
    onSubmitPatch: (patchCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var patchCodeState by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقييم أمني ودمج نموذج التهديد الذكي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = CyberTextPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SeverityLabel(severity = vulnerability.severity)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = vulnerability.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = vulnerability.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }
                }

                Text(
                    text = "أدرج الكود البرمجي المقترح للحل، أو صِف الإجراء الأمني الذي تود اختباره:",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )

                OutlinedTextField(
                    value = patchCodeState,
                    onValueChange = { patchCodeState = it },
                    placeholder = {
                        Text(
                            text = "e.g. preparedStatement.setString(1, userId); ... // Or describe firewall block rule",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("security_patch_textfield"),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedBorderColor = CyberGreen,
                        unfocusedBorderColor = CyberBorder
                    )
                )

                Button(
                    onClick = { onSubmitPatch(patchCodeState) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    enabled = analysisState != AnalysisUiState.Assessing
                ) {
                    if (analysisState == AnalysisUiState.Assessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("يجري الفحص والتحليل الذكي...")
                    } else {
                        Text("إرسال للذكاء الاصطناعي لتقييم الفعالية الجدارية")
                    }
                }

                // AI Response block
                when (analysisState) {
                    is AnalysisUiState.Assessing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CyberBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("جاري فحص الرمز بحثاً عن ثغرات الالتفاف المتقدمة...", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
                            }
                        }
                    }
                    is AnalysisUiState.Completed -> {
                        AiResultsView(response = analysisState.response)
                    }
                    is AnalysisUiState.Error -> {
                        Text(
                            text = "❌ خطأ في محاكاة التهديدات: ${analysisState.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberAlertCritical
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = CyberCard,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

@Composable
fun AiResultsView(response: AIAnalysisResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Divider(color = CyberBorder)

        // Result Banner
        val bannerBg = if (response.vulnerabilityFound) CyberAlertCritical.copy(alpha = 0.15f) else CyberGreen.copy(alpha = 0.15f)
        val bannerColor = if (response.vulnerabilityFound) CyberAlertCritical else CyberGreen
        val bannerText = if (response.vulnerabilityFound) {
            "⚠️ الثغرة لا تزال نشطة! تم العثور على ثغرات التفافية"
        } else {
            "✔️ حل أمني متكامل ومستقر! تم إغلاق الثغرة بنجاح"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(bannerBg)
                .border(1.dp, bannerColor, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Text(
                text = bannerText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = bannerColor
            )
        }

        // Detailed explanation
        Text("Detailed Review / تقرير المراجعة الشامل", style = MaterialTheme.typography.titleSmall, color = CyberBlue)
        Text(
            text = response.vulnerabilityDetail,
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextPrimary
        )

        // Threat Scenario Detail
        if (response.vulnerabilityFound) {
            Text("Exploitation Scenario / سيناريو الاستغلال المتوقع", style = MaterialTheme.typography.titleSmall, color = CyberAlertWarning)
            Text(
                text = response.threatScenario,
                style = MaterialTheme.typography.bodySmall,
                color = CyberTextSecondary
            )
        }

        // Step by Step guide
        Text("Remediation Blueprint / خارطة الطريق للإصلاح التام", style = MaterialTheme.typography.titleSmall, color = CyberGreen)
        Text(
            text = response.stepByStepMitigation,
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextPrimary
        )

        // Perfect patch code
        Text("Implementation Guide / الكود الآمن النهائي والمعياري", style = MaterialTheme.typography.titleSmall, color = CyberBlue)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface, RoundedCornerShape(6.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Text(
                text = response.suggestedPatch,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = CyberTextPrimary
            )
        }
    }
}
