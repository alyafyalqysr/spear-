package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.DbAsset
import com.example.data.DbVulnerability
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AnalysisUiState
import com.example.viewmodel.ScanUiState
import com.example.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusMainScreen(
    viewModel: SecurityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val assetsList by viewModel.assets.collectAsStateWithLifecycle(emptyList())
    val vulnerabilitiesList by viewModel.vulnerabilities.collectAsStateWithLifecycle(emptyList())

    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val showApiKeyWarning by viewModel.showApiKeyWarning.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, assets, vulnerabilities, education

    var showAddDialog by remember { mutableStateOf(false) }
    var activeVulnerabilityForAnalysis by remember { mutableStateOf<DbVulnerability?>(null) }
    var showScanSuccessDialog by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // Show popup notification after scan completes

    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface)
            ) {
                // Browser URL / Web Navigation Bar Simulation
                Surface(
                    color = Color(0xFF131B2A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Web Browser Controls
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFFF5F56)))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFFFBD2E)))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF27C93F)))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Browser Address Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0D131F))
                                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "SSL Encrypted Website",
                                    tint = CyberGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "https://www.aman-cyber.sa/portal",
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Live Portal Badge
                        Surface(
                            color = CyberGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, CyberGreen)
                        ) {
                            Text(
                                text = "موقع نشط 🟢",
                                fontSize = 9.sp,
                                color = CyberGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Web Portal Brand Header
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberBlue)
                                    .border(1.dp, CyberBlue, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "آ",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "موقع آمن سيبراني | Aman Cyber Web Portal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    "الموقع الإلكتروني الوطني لفحص الأنظمة والمواقع بالذكاء الاصطناعي",
                                    fontSize = 10.sp,
                                    color = CyberGreen
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.clearAllVulnerabilityHistory() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث نتائج الموقع",
                                tint = CyberTextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberSurface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("الرئيسية", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = CyberTextSecondary,
                        unselectedTextColor = CyberTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "assets",
                    onClick = { activeTab = "assets" },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Web Assets") },
                    label = { Text("فحص المواقع", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = CyberTextSecondary,
                        unselectedTextColor = CyberTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "vulnerabilities",
                    onClick = { activeTab = "vulnerabilities" },
                    icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Threats") },
                    label = { Text("سجل الثغرات", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = CyberTextSecondary,
                        unselectedTextColor = CyberTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "education",
                    onClick = { activeTab = "education" },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Architecture") },
                    label = { Text("دليل الأمن", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = CyberTextSecondary,
                        unselectedTextColor = CyberTextSecondary
                    )
                )
            }
        },
        floatingActionButton = {
            if (activeTab == "assets") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CyberBlue,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_asset_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Asset")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CyberDarkBg, CyberSurface)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Warning Banner
                if (showApiKeyWarning) {
                    ApiKeyWarningBanner(
                        onDismiss = { viewModel.showApiKeyWarning.value = false },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                when (activeTab) {
                    "dashboard" -> DashboardView(
                        assets = assetsList,
                        vulnerabilities = vulnerabilitiesList,
                        scanState = scanState,
                        onScanTrigger = { viewModel.executeQuickScan(it) }
                    )
                    "assets" -> AssetsView(
                        assets = assetsList,
                        onScanTrigger = { viewModel.executeQuickScan(it) },
                        onDeleteTrigger = { viewModel.removeAsset(it) }
                    )
                    "vulnerabilities" -> VulnerabilitiesView(
                        vulnerabilities = vulnerabilitiesList,
                        onAnalyzeTrigger = { activeVulnerabilityForAnalysis = it }
                    )
                    "education" -> EducationView()
                }
            }

            // Progress hud overlay during high-level analysis block
            if (scanState is ScanUiState.Scanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        border = BorderStroke(1.dp, CyberBlue),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyberBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "جاري مسح واكتشاف وتحليل الثغرات...",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "يقوم محرك الذكاء الاصطناعي بتقييم ميكانيكيات الهجمات على الأصل...",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Add Asset Dialog
        if (showAddDialog) {
            AddAssetDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { host, ip, type, cloud, services ->
                    viewModel.addNewAsset(host, ip, type, cloud, services)
                    showAddDialog = false
                }
            )
        }

        // Threat Assessment Analyzer
        activeVulnerabilityForAnalysis?.let { vulnerability ->
            ThreatModelingDialog(
                vulnerability = vulnerability,
                analysisState = analysisState,
                onClose = {
                    activeVulnerabilityForAnalysis = null
                    // Reset analysis state to idle for next analysis
                    viewModel.submitToAiAnalysis(vulnerability, "") // Reset or set back state
                },
                onSubmitPatch = { patchCode ->
                    viewModel.submitToAiAnalysis(vulnerability, patchCode)
                }
            )
        }

        // Scan Success Trigger Popup
        LaunchedEffect(scanState) {
            if (scanState is ScanUiState.Success) {
                val state = scanState as ScanUiState.Success
                showScanSuccessDialog = true to "اكتمل مسح الأصول بنجاح! تم رصد ${state.findings.size} من التهديدات البرمجية المعتبرة."
            }
        }

        showScanSuccessDialog?.let { (show, msg) ->
            if (show) {
                AlertDialog(
                    onDismissRequest = { showScanSuccessDialog = null },
                    title = { Text("تم الفحص والتحليل الذكي بنجاح", color = CyberGreen, fontWeight = FontWeight.Bold) },
                    text = { Text(msg, color = CyberTextPrimary) },
                    confirmButton = {
                        Button(
                            onClick = { showScanSuccessDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.Black)
                        ) {
                            Text("استعراض الثغرات المكتشفة")
                        }
                    },
                    containerColor = CyberCard,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardView(
    assets: List<DbAsset>,
    vulnerabilities: List<DbVulnerability>,
    scanState: ScanUiState,
    onScanTrigger: (DbAsset) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero visual banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                // Embedded cyberpunk image generated via tool
                val imgResId = R.drawable.img_cyber_hud_1782157237936
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = "Futuristic Cyber Guard HUD HUD Screen Pattern",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Shading layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, CyberGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PORTAL WEB SCANNER: ONLINE 🟢", fontSize = 9.sp, color = CyberGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "موقع آمن سيبراني | بوابة كشف الثغرات بالم الذكاء الاصطناعي",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Web URL Direct Scanner Widget
        item {
            var inputUrl by remember { mutableStateOf("https://") }
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Website Search", tint = CyberBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فحص موقع إلكتروني مباشر",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أدخل رابط أي موقع إلكتروني أو نطاق لبدء الفحص والتحليل الفوري عبر الموقع:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("https://example.sa", color = CyberTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberBlue,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary,
                                focusedContainerColor = CyberSurface,
                                unfocusedContainerColor = CyberSurface
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                val targetHost = inputUrl.removePrefix("https://").removePrefix("http://").ifEmpty { "target-website.com" }
                                val sampleAsset = DbAsset(
                                    id = "asset-${System.currentTimeMillis()}",
                                    hostname = targetHost,
                                    ip = "104.21.55.90",
                                    type = "WEBSITE",
                                    status = "HEALTHY",
                                    servicesString = "80/HTTP, 443/HTTPS",
                                    vulnerabilityCount = 0,
                                    cloudProvider = "Cloudflare SSL"
                                )
                                onScanTrigger(sampleAsset)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("فحص الموقع", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Statistics Visualizer
        item {
            val critical = vulnerabilities.count { it.severity.uppercase() == "CRITICAL" }
            val high = vulnerabilities.count { it.severity.uppercase() == "HIGH" }
            val medium = vulnerabilities.count { it.severity.uppercase() == "MEDIUM" }
            val low = vulnerabilities.count { it.severity.uppercase() == "LOW" }

            Column {
                Text("توزيع التهديدات وتصنيف المخاطر المكتشفة", style = MaterialTheme.typography.titleSmall, color = CyberTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                SecurityRadarChart(
                    criticalCount = critical,
                    highCount = high,
                    mediumCount = medium,
                    lowCount = low
                )
            }
        }

        // Active critical threat notification banner
        if (vulnerabilities.any { it.severity.uppercase() == "CRITICAL" }) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberAlertCritical.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberAlertCritical.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Critical Warning", tint = CyberAlertCritical)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("⚠️ تحذير: تم اكتشاف تسريب برمجي حرج جداً!", color = CyberTextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text("يجب حل الثغرة الحرجة في أسرع وقت ممكن لحماية البنية الأساسية للمنظمة.", color = CyberTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Quick scanned list trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الأصول النشطة في المنظومة", style = MaterialTheme.typography.titleSmall, color = CyberTextPrimary)
                Text("${assets.size} أصل مسجل", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
            }
        }

        items(assets.take(3)) { asset ->
            AssetCard(
                asset = asset,
                onScan = { onScanTrigger(asset) },
                onDelete = {} // handled in specialized lists tab page
            )
        }
    }
}

@Composable
fun AssetsView(
    assets: List<DbAsset>,
    onScanTrigger: (DbAsset) -> Unit,
    onDeleteTrigger: (DbAsset) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("البنى الأساسية والأصول المرصودة", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        Text("قائمة بجميع الخوادم، القواعد وقنوات السحاب المسجلة تحت مظلة نظام نيكسوس للفحص المبرمج.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary, modifier = Modifier.padding(bottom = 14.dp))

        if (assets.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Empty", tint = CyberTextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد أصول أمنية مسجلة حالياً.", color = CyberTextSecondary)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(assets) { asset ->
                    AssetCard(
                        asset = asset,
                        onScan = { onScanTrigger(asset) },
                        onDelete = { onDeleteTrigger(asset) }
                    )
                }
            }
        }
    }
}

@Composable
fun VulnerabilitiesView(
    vulnerabilities: List<DbVulnerability>,
    onAnalyzeTrigger: (DbVulnerability) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("سجل اكتشافات صائد الثغرات (Vulnerabilities Logs)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        Text("نتائج المسح الذكي لجميع الأنظمة محددة بمكان الثغرة، سيناريو التهديد وقابلية الحل الذاتي بالتدقيق التوليدي.", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary, modifier = Modifier.padding(bottom = 12.dp))

        if (vulnerabilities.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CyberSurface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Shield State System Healthy", tint = CyberGreen, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("النظام سليم بالكامل! ولا توجد ثغرات نشطة مكتشفة", fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("انتقل لتبويب 'الأصول' واضغط على 'مسح الثغرات' لبدء الكشف الذكي المدعوم بالذكاء الاصطناعي.", color = CyberTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(vulnerabilities) { vulnerability ->
                    VulnerabilityItem(
                        vulnerability = vulnerability,
                        onAssess = { onAnalyzeTrigger(vulnerability) }
                    )
                }
            }
        }
    }
}

@Composable
fun EducationView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("منهجية وهندسة نظام NEXUS البرمجية الموزعة", style = MaterialTheme.typography.titleMedium)
            Text("مراجعة شاملة لخصائص ومقومات منصة نيكسوس التقنية والمحاكاة الدفاعية:", style = MaterialTheme.typography.bodySmall, color = CyberTextSecondary)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. طبقة الاستطلاع الذكي (Smart Reconnaissance)",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يقوم محرك نيكسوس باستطلاع كامل الأسطح التقنية للأصل (Asset Surface Mapping). يتتبع خرائط المنافذ العامة والبروتوكولات النشطة ومستودعات التخزين السحابية (S3, Azure Blobs) من دون استهلاك زائد لشبكات الاتصال.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. مستودع الثغرات المحاكي والذكاء الاصطناعي",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يستدع محرك نيكسوس نماذج تفضيلية ذكية (Gemini 3.5 Flash) لترجمة هندسة الأنظمة ومحاكاة تكتيكات الاختراقات المتطورة. يحسب الذكاء الاصطناعي سيناريو التهديد وتأثير الهجمات الحيوية على المخدمات.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. نمذجة وتقييم التهديدات وتطبيقات الدفاع",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberAlertWarning,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتيح نظام نيكسوس إمكانية مراجعة الأكواد البرمجية الأمنية. حيث يحلل النظام بذكاء ويدقق في الكود المدخل المقترح للتأكد من زوال ثغرات الالتفاف بمرونة عالية، مع تقديم حل بديل مطابق للمقاييس العالمية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary
                    )
                }
            }
        }
    }
}
