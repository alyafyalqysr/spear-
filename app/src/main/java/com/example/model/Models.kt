package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Asset(
    val id: String,
    val ip: String,
    val hostname: String,
    val type: String,
    val status: String,
    val services: List<String>,
    val vulnerabilityCount: Int,
    val cloudProvider: String
)

@JsonClass(generateAdapter = true)
data class ScanResult(
    val target: String,
    val timestamp: String,
    val status: String,
    val durationMs: Long,
    val findings: List<Vulnerability>
)

@JsonClass(generateAdapter = true)
data class Vulnerability(
    val title: String,
    val category: String,
    val severity: String,
    val description: String,
    val attackVector: String,
    val cve: String? = null,
    val recommendedFix: String,
    val simulatedExploitCode: String? = null
)

@JsonClass(generateAdapter = true)
data class AnalysisRequest(
    val assetData: String,
    val prompt: String
)

@JsonClass(generateAdapter = true)
data class AIAnalysisResponse(
    val vulnerabilityFound: Boolean,
    val severityLevel: String,
    val vulnerabilityDetail: String,
    val threatScenario: String,
    val stepByStepMitigation: String,
    val suggestedPatch: String
)
