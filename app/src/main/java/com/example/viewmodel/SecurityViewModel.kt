package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.DbAsset
import com.example.data.DbVulnerability
import com.example.data.NexusDatabase
import com.example.model.AIAnalysisResponse
import com.example.model.Vulnerability
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val findings: List<Vulnerability>, val durationMs: Long) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Assessing : AnalysisUiState
    data class Completed(val response: AIAnalysisResponse) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NexusDatabase.getDatabase(application)
    private val dao = db.dao

    // State holders
    val assets = MutableStateFlow<List<DbAsset>>(emptyList())
    val vulnerabilities = MutableStateFlow<List<DbVulnerability>>(emptyList())

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState

    // API Key warning reminder tracking
    val showApiKeyWarning = MutableStateFlow(false)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        // Hydrate DB with starting assets if database is empty
        viewModelScope.launch {
            launch {
                dao.getAllAssets().catch { e ->
                    // handle error
                }.collectLatest { list ->
                    if (list.isEmpty()) {
                        seedAssets()
                    } else {
                        assets.value = list
                    }
                }
            }
            launch {
                dao.getAllVulnerabilities().collectLatest { list ->
                    vulnerabilities.value = list
                }
            }

            // Simple API Key validation check
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key.contains("PLACEHOLDER") || key.contains("MY_")) {
                showApiKeyWarning.value = true
            }
        }
    }

    private suspend fun seedAssets() {
        val initialList = listOf(
            DbAsset("as-92", "nexus-portal-gateway.sec", "192.168.1.10", "API Gateway", "Safe", "HTTPS, REST API, SSH", 0, "AWS (eu-west-1)"),
            DbAsset("as-45", "auth-identity-service-prod", "10.0.4.88", "Web Server", "Vulnerable", "HTTPS, OAuth, Redis", 2, "GCP (us-central1)"),
            DbAsset("as-11", "customer-spanner-database", "10.0.12.19", "Database", "Safe", "Cloud Spanner, TCP/9321", 0, "GCP (europe-west3)"),
            DbAsset("as-84", "audit-logs-cold-storage", "s3://nexus-financial-audits", "S3 Bucket", "Vulnerable", "Static Web Hosting, ReadPublic", 1, "AWS (us-east-1)")
        )
        for (item in initialList) {
            dao.insertAsset(item)
        }
    }

    fun addNewAsset(hostname: String, ip: String, type: String, cloudProvider: String, services: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "as-" + Random().nextInt(100)
            val newAsset = DbAsset(
                id = id,
                hostname = hostname.trim().ifEmpty { "unnamed-node" },
                ip = ip.trim().ifEmpty { "127.0.0.1" },
                type = type,
                status = "Safe",
                servicesString = services.trim().ifEmpty { "HTTP, HTTPS" },
                vulnerabilityCount = 0,
                cloudProvider = cloudProvider
            )
            dao.insertAsset(newAsset)
        }
    }

    fun removeAsset(asset: DbAsset) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAsset(asset)
        }
    }

    fun executeQuickScan(asset: DbAsset) {
        _scanState.value = ScanUiState.Scanning
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val apiKey = BuildConfig.GEMINI_API_KEY

            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // Return offline simulated report if no API key is specified yet
                val simulatedResult = getSimulatedScanResults(asset)
                saveSimulatedFindings(asset, simulatedResult)
                val duration = System.currentTimeMillis() - startTime
                _scanState.value = ScanUiState.Success(simulatedResult, duration)
                return@launch
            }

            try {
                // Query Gemini API to brainstorm real vulnerability scenario based on asset parameters
                val prompt = """
                    You are NEXUS security analysis assistant. Based on this scanned asset, generate a highly technical mockup security report of the asset.
                    Asset Parameters:
                    - Hostname: ${asset.hostname}
                    - IP: ${asset.ip}
                    - Asset Type: ${asset.type}
                    - Cloud Environment: ${asset.cloudProvider}
                    - Active services: ${asset.servicesString}

                    Generate exactly 1 realistic or representative security vulnerability threat.
                    You must only respond in JSON format, without markdown formatting.
                    Return an array containing one object with the keys: "title", "category", "severity", "description", "attackVector", "cve", "recommendedFix", "simulatedExploitCode".
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                var rawJson = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from model")

                // Simple sanitization to clean any accidental markdown codeblock wrapper
                if (rawJson.contains("```json")) {
                    rawJson = rawJson.substringAfter("```json").substringBefore("```")
                } else if (rawJson.contains("```")) {
                    rawJson = rawJson.substringAfter("```").substringBefore("```")
                }
                rawJson = rawJson.trim()

                val typeToken = Types.newParameterizedType(List::class.java, Vulnerability::class.java)
                val adapter = moshi.adapter<List<Vulnerability>>(typeToken)
                val parsedFindings = adapter.fromJson(rawJson) ?: throw Exception("JSON conversion error")

                // Save to local database
                viewModelScope.launch(Dispatchers.IO) {
                    val highestSeverity = parsedFindings.maxByOrNull { f ->
                        when(f.severity.uppercase()) {
                            "CRITICAL" -> 4
                            "HIGH" -> 3
                            "MEDIUM" -> 2
                            "LOW" -> 1
                            else -> 0
                        }
                    }?.severity ?: "Safe"

                    val updatedAsset = asset.copy(
                        status = if (parsedFindings.isNotEmpty()) "Vulnerable ($highestSeverity)" else "Safe",
                        vulnerabilityCount = asset.vulnerabilityCount + parsedFindings.size
                    )
                    dao.insertAsset(updatedAsset)

                    for (finding in parsedFindings) {
                        dao.insertVulnerability(
                            DbVulnerability(
                                assetId = asset.id,
                                title = finding.title,
                                category = finding.category,
                                severity = finding.severity,
                                description = finding.description,
                                cve = finding.cve ?: "N/A",
                                recommendedFix = finding.recommendedFix,
                                detectedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                _scanState.value = ScanUiState.Success(parsedFindings, duration)

            } catch (e: Exception) {
                // If anything fails in networking (e.g. rate limit, bad response format), fallback seamlessly to offline scanner simulation
                val simResult = getSimulatedScanResults(asset)
                saveSimulatedFindings(asset, simResult)
                val duration = System.currentTimeMillis() - startTime
                _scanState.value = ScanUiState.Success(simResult, duration)
            }
        }
    }

    private fun saveSimulatedFindings(asset: DbAsset, findings: List<Vulnerability>) {
        viewModelScope.launch(Dispatchers.IO) {
            val highestSeverity = findings.maxByOrNull { f ->
                when(f.severity.uppercase()) {
                    "CRITICAL" -> 4
                    "HIGH" -> 3
                    "MEDIUM" -> 2
                    "LOW" -> 1
                    else -> 0
                }
            }?.severity ?: "Safe"

            dao.insertAsset(asset.copy(
                status = "Vulnerable ($highestSeverity)",
                vulnerabilityCount = asset.vulnerabilityCount + findings.size
            ))

            for (f in findings) {
                dao.insertVulnerability(DbVulnerability(
                    assetId = asset.id,
                    title = f.title,
                    category = f.category,
                    severity = f.severity,
                    description = f.description,
                    cve = f.cve ?: "CVE-2026-TEMP",
                    recommendedFix = f.recommendedFix,
                    detectedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    fun clearAllVulnerabilityHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAllVulnerabilities()
            // Reset asset alerts
            val currentAssets = assets.value
            for (asset in currentAssets) {
                dao.insertAsset(asset.copy(status = "Safe", vulnerabilityCount = 0))
            }
        }
    }

    fun submitToAiAnalysis(vulnerability: DbVulnerability, patchCode: String) {
        if (patchCode.isEmpty()) {
            _analysisState.value = AnalysisUiState.Idle
            return
        }

        _analysisState.value = AnalysisUiState.Assessing
        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // Offline response simulation
                _analysisState.value = AnalysisUiState.Completed(
                    AIAnalysisResponse(
                        vulnerabilityFound = true,
                        severityLevel = vulnerability.severity,
                        vulnerabilityDetail = "Simulated deep risk analysis on ${vulnerability.title}.",
                        threatScenario = "An attacker exploits the vector identified in ${vulnerability.title} via network injection.",
                        stepByStepMitigation = "1. Patch code to sanitize input.\n2. Configure firewall filters.\n3. Add unit test assertions.",
                        suggestedPatch = "Secure patch loaded sequentially based on input constraints."
                    )
                )
                return@launch
            }

            try {
                val prompt = """
                    Analyze this security vulnerability record and the user's attempted security mitigation patch. Determine if the mitigation successfully secures the application or if loopholes still remain.
                    
                    Vulnerability Information:
                    - Title: ${vulnerability.title}
                    - Severity: ${vulnerability.severity}
                    - Description: ${vulnerability.description}
                    - Suggested fix guidelines: ${vulnerability.recommendedFix}
                    
                    Attempted Security Patch/Action:
                    ${patchCode}
                    
                    Analyze for edge cases, evasion bypasses, or secondary security impacts. 
                    You must respond only with a raw JSON object containing the keys:
                    "vulnerabilityFound": boolean, "severityLevel": string, "vulnerabilityDetail": string, "threatScenario": string, "stepByStepMitigation": string, "suggestedPatch": string.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                var rawJson = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Unable to get analytical response from security engine")

                if (rawJson.contains("```json")) {
                    rawJson = rawJson.substringAfter("```json").substringBefore("```")
                } else if (rawJson.contains("```")) {
                    rawJson = rawJson.substringAfter("```").substringBefore("```")
                }
                rawJson = rawJson.trim()

                val adapter = moshi.adapter(AIAnalysisResponse::class.java)
                val parsedRes = adapter.fromJson(rawJson) ?: throw Exception("JSON conversion error")
                _analysisState.value = AnalysisUiState.Completed(parsedRes)

            } catch (e: Exception) {
                // network or parse error fallback
                _analysisState.value = AnalysisUiState.Error("Failed to finish AI threat modeling assessments: ${e.localizedMessage}")
            }
        }
    }

    private fun getSimulatedScanResults(asset: DbAsset): List<Vulnerability> {
        return when (asset.type) {
            "API Gateway" -> listOf(
                Vulnerability(
                    title = "JWT Secret Key Entropy Flaw",
                    category = "Broken Authentication",
                    severity = "HIGH",
                    description = "The API Gateway signature verification checks allow weak signature validation due to low-entropy secret keys.",
                    attackVector = "HTTP Authorization Bearer Header",
                    cve = "CVE-2026-2812",
                    recommendedFix = "Rotate verification tokens to strong, cryptographically secure keys (HmacSHA256 with minimum 256-bit key value)."
                )
            )
            "Database" -> listOf(
                Vulnerability(
                    title = "Cloud Spanner Unauthorized Direct Query access",
                    category = "Weak Encryption",
                    severity = "MEDIUM",
                    description = "Direct database connection exposes metadata schemas and records due to loose local network policy bindings.",
                    attackVector = "Direct Port Access TCP 9321",
                    cve = null,
                    recommendedFix = "Incorporate robust IAM constraints and enforce Virtual Private Cloud service boundaries."
                )
            )
            "S3 Bucket" -> listOf(
                Vulnerability(
                    title = "Insecure Bucket ACL Exposing Logs",
                    category = "Insecure Cloud Storage",
                    severity = "HIGH",
                    description = "Read access configuration exposes active financial audit log directories to public queries.",
                    attackVector = "HTTP Public Request GET /logs/",
                    cve = "CVE-2026-9218",
                    recommendedFix = "Execute complete public access blocking (Block Public Access rule) on bucket policy scopes."
                )
            )
            else -> listOf(
                Vulnerability(
                    title = "SQL Injection in User Profile Lookup",
                    category = "SQL Injection",
                    severity = "CRITICAL",
                    description = "Input parameter values are concatenated directly inside raw search lookup commands.",
                    attackVector = "GET /api/v1/profile?user_id=1%27%20OR%201=1",
                    cve = "CVE-2026-1049",
                    recommendedFix = "Replace raw dynamic execution queries with type-safe precompiled Prepared Statements and parameterized input bindings."
                ),
                Vulnerability(
                    title = "Excessive CORS Scope Binding Allowed",
                    category = "Cross-Site Scripting",
                    severity = "LOW",
                    description = "CORS metadata allows requests originating from generic wildcard (*) resource domains.",
                    attackVector = "HTTP Origin Header Verification",
                    cve = null,
                    recommendedFix = "Replace generic global CORS headers with verified system service whitelist patterns."
                )
            )
        }
    }
}
