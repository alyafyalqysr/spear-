package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(
    onDismiss: () -> Unit,
    onConfirm: (hostname: String, ip: String, type: String, cloud: String, services: String) -> Unit
) {
    var hostname by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Web Server") }
    var cloud by remember { mutableStateOf("AWS (us-east-1)") }
    var services by remember { mutableStateOf("HTTPS, SSH") }

    val typesList = listOf("Web Server", "Database", "S3 Bucket", "API Gateway", "Auth Server")
    val cloudsList = listOf("AWS (us-east-1)", "AWS (eu-west-1)", "GCP (us-central1)", "GCP (europe-west3)", "Azure Cloud")

    var typeExpanded by remember { mutableStateOf(false) }
    var cloudExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("إضافة أصل أمني جديد للرصد", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
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
                OutlinedTextField(
                    value = hostname,
                    onValueChange = { hostname = it },
                    label = { Text("اسم المضيف (Hostname)") },
                    placeholder = { Text("e.g. backend-redis-node") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("عنوان IP") },
                    placeholder = { Text("e.g. 192.168.1.55") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع الأصل") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CyberBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            typesList.forEach { selectedType ->
                                DropdownMenuItem(
                                    text = { Text(selectedType) },
                                    onClick = {
                                        type = selectedType
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Cloud selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = cloudExpanded,
                        onExpandedChange = { cloudExpanded = !cloudExpanded }
                    ) {
                        OutlinedTextField(
                            value = cloud,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("البيئة السحابية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cloudExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CyberBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = cloudExpanded,
                            onDismissRequest = { cloudExpanded = false }
                        ) {
                            cloudsList.forEach { selectedCloud ->
                                DropdownMenuItem(
                                    text = { Text(selectedCloud) },
                                    onClick = {
                                        cloud = selectedCloud
                                        cloudExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = services,
                    onValueChange = { services = it },
                    label = { Text("الخدمات والواجهات المفتوحة") },
                    placeholder = { Text("e.g. HTTPS, GraphQL, SQL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(hostname, ip, type, cloud, services)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Text("تأكيد وحفظ الأصل", style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = CyberTextPrimary)) {
                Text("إلغاء")
            }
        },
        containerColor = CyberCard
    )
}
