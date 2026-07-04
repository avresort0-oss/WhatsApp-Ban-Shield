package com.example

/**
 * WhatsApp Ban Risk Detection Engine
 * Analyzes account behavior and provides safety recommendations
 */
data class BanRiskLevel(
    val score: Int, // 0-100 scale
    val level: String, // LOW, MEDIUM, HIGH, CRITICAL
    val riskFactors: List<String>,
    val recommendations: List<String>
)

data class WhatsAppActivity(
    val messagesPerMinute: Float = 0f,
    val mediaUploadPerHour: Float = 0f,
    val newContactsPerDay: Int = 0,
    val groupAdditionsPerDay: Int = 0,
    val accountAge: Int = 0, // in days
    val suspiciousPatterns: List<String> = emptyList(),
    val usesThirdPartyTools: Boolean = false,
    val frequentBotActivity: Boolean = false,
    val failedLoginAttempts: Int = 0,
    val rapidStatusUpdates: Boolean = false
)

class BanDetectionEngine {
    
    fun analyzeRisk(activity: WhatsAppActivity): BanRiskLevel {
        var riskScore = 0
        val riskFactors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        // Check messaging rate (max 60 msg/min is concerning)
        if (activity.messagesPerMinute > 60) {
            riskScore += 25
            riskFactors.add("Unusually high messaging rate (${activity.messagesPerMinute}/min)")
            recommendations.add("Reduce message frequency - bot-like behavior triggers ban")
        }
        
        // Check media uploads (more than 100/hour is suspicious)
        if (activity.mediaUploadPerHour > 100) {
            riskScore += 20
            riskFactors.add("Excessive media uploads (${activity.mediaUploadPerHour}/hour)")
            recommendations.add("Limit bulk media uploads")
        }
        
        // Check new contacts added
        if (activity.newContactsPerDay > 50) {
            riskScore += 15
            riskFactors.add("Adding too many new contacts per day (${activity.newContactsPerDay})")
            recommendations.add("Slow down contact additions - avoid spammy behavior")
        }
        
        // Check group additions
        if (activity.groupAdditionsPerDay > 30) {
            riskScore += 20
            riskFactors.add("Rapid group additions (${activity.groupAdditionsPerDay}/day)")
            recommendations.add("Avoid adding accounts to multiple groups rapidly")
        }
        
        // Account age check (new accounts are riskier)
        if (activity.accountAge < 30) {
            riskScore += 15
            riskFactors.add("Very new account (${activity.accountAge} days old)")
            recommendations.add("Let account mature for 30+ days before heavy use")
        }
        
        // Check suspicious patterns
        if (activity.suspiciousPatterns.isNotEmpty()) {
            riskScore += (activity.suspiciousPatterns.size * 5)
            riskFactors.addAll(activity.suspiciousPatterns)
        }
        
        // Third-party tools detection
        if (activity.usesThirdPartyTools) {
            riskScore += 30
            riskFactors.add("Third-party tool usage detected")
            recommendations.add("Stop using WhatsApp mods, automation tools, or unofficial clients")
            recommendations.add("Use official WhatsApp or WhatsApp Business only")
        }
        
        // Bot activity
        if (activity.frequentBotActivity) {
            riskScore += 25
            riskFactors.add("Bot-like patterns detected")
            recommendations.add("Avoid automated replies, bulk messaging, or scheduled posts")
        }
        
        // Failed logins
        if (activity.failedLoginAttempts > 5) {
            riskScore += 10
            riskFactors.add("Multiple failed login attempts (${activity.failedLoginAttempts})")
            recommendations.add("Verify account credentials and phone number")
        }
        
        // Rapid status updates
        if (activity.rapidStatusUpdates) {
            riskScore += 10
            riskFactors.add("Rapid status updates detected")
            recommendations.add("Reduce frequency of status changes")
        }
        
        // Cap score at 100
        riskScore = minOf(riskScore, 100)
        
        // Determine risk level
        val level = when {
            riskScore >= 80 -> "CRITICAL"
            riskScore >= 60 -> "HIGH"
            riskScore >= 40 -> "MEDIUM"
            else -> "LOW"
        }
        
        // Add general recommendations
        if (recommendations.isEmpty()) {
            recommendations.add("Your account appears safe - maintain normal usage patterns")
        } else {
            recommendations.add("Follow these steps to reduce ban risk")
            recommendations.add("Wait 24-48 hours after correcting behavior before heavy use")
        }
        
        return BanRiskLevel(
            score = riskScore,
            level = level,
            riskFactors = riskFactors,
            recommendations = recommendations
        )
    }
    
    fun getDetailedAnalysis(activity: WhatsAppActivity): String {
        val risk = analyzeRisk(activity)
        return buildString {
            appendLine("=== WhatsApp Ban Shield Analysis ===")
            appendLine("Risk Score: ${risk.score}/100")
            appendLine("Risk Level: ${risk.level}")
            appendLine()
            
            if (risk.riskFactors.isNotEmpty()) {
                appendLine("Risk Factors:")
                risk.riskFactors.forEach { factor ->
                    appendLine("  ⚠️  $factor")
                }
                appendLine()
            }
            
            appendLine("Recommendations:")
            risk.recommendations.forEach { rec ->
                appendLine("  ✓ $rec")
            }
        }
    }
}
