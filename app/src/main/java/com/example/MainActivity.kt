package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          BanShieldScreen(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun BanShieldScreen(modifier: Modifier = Modifier) {
    val engine = remember { BanDetectionEngine() }
    
    var messagesPerMin by remember { mutableStateOf(15f) }
    var mediaPerHour by remember { mutableStateOf(20f) }
    var newContactsPerDay by remember { mutableStateOf(5) }
    var groupAdditionsPerDay by remember { mutableStateOf(2) }
    var accountAgeDays by remember { mutableStateOf(180) }
    var usesThirdPartyTools by remember { mutableStateOf(false) }
    var frequentBotActivity by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableStateOf(0) }
    var rapidStatusUpdates by remember { mutableStateOf(false) }
    
    val activity = WhatsAppActivity(
        messagesPerMinute = messagesPerMin,
        mediaUploadPerHour = mediaPerHour,
        newContactsPerDay = newContactsPerDay,
        groupAdditionsPerDay = groupAdditionsPerDay,
        accountAge = accountAgeDays,
        suspiciousPatterns = emptyList(),
        usesThirdPartyTools = usesThirdPartyTools,
        frequentBotActivity = frequentBotActivity,
        failedLoginAttempts = failedAttempts,
        rapidStatusUpdates = rapidStatusUpdates
    )
    
    val riskAnalysis by remember(activity) {
        mutableStateOf(engine.analyzeRisk(activity))
    }
    
    val statusColor by animateColorAsState(
        when (riskAnalysis.level) {
            "CRITICAL" -> Color(0xFFD32F2F)
            "HIGH" -> Color(0xFFF57C00)
            "MEDIUM" -> Color(0xFFFBC02D)
            else -> Color(0xFF388E3C)
        },
        label = "statusColor"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "WhatsApp Ban Shield",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Account Safety Monitor",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Risk Score Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (riskAnalysis.level) {
                        "CRITICAL" -> Icons.Default.Warning
                        "HIGH" -> Icons.Default.Error
                        "MEDIUM" -> Icons.Default.Info
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = "Risk Level",
                    tint = statusColor,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "${riskAnalysis.score}/100",
                    style = MaterialTheme.typography.headlineMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
                
                Text(
                    text = riskAnalysis.level,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = when (riskAnalysis.level) {
                        "CRITICAL" -> "⚠️ Account at severe risk of ban"
                        "HIGH" -> "⚠️ Account at significant risk"
                        "MEDIUM" -> "ℹ️ Account showing some risk indicators"
                        else -> "✓ Account appears safe"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Activity Input Section
        Text(
            text = "Account Activity Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Messages per minute
                ActivitySlider(
                    label = "Messages/Min: ${messagesPerMin.toInt()}",
                    value = messagesPerMin,
                    onValueChange = { messagesPerMin = it },
                    range = 0f..100f,
                    warningThreshold = 60f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Media uploads per hour
                ActivitySlider(
                    label = "Media/Hour: ${mediaPerHour.toInt()}",
                    value = mediaPerHour,
                    onValueChange = { mediaPerHour = it },
                    range = 0f..150f,
                    warningThreshold = 100f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // New contacts per day
                ActivitySlider(
                    label = "New Contacts/Day: ${newContactsPerDay}",
                    value = newContactsPerDay.toFloat(),
                    onValueChange = { newContactsPerDay = it.toInt() },
                    range = 0f..100f,
                    warningThreshold = 50f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Group additions per day
                ActivitySlider(
                    label = "Group Adds/Day: ${groupAdditionsPerDay}",
                    value = groupAdditionsPerDay.toFloat(),
                    onValueChange = { groupAdditionsPerDay = it.toInt() },
                    range = 0f..50f,
                    warningThreshold = 30f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Account age
                ActivitySlider(
                    label = "Account Age: ${accountAgeDays} days",
                    value = accountAgeDays.toFloat(),
                    onValueChange = { accountAgeDays = it.toInt() },
                    range = 0f..365f,
                    warningThreshold = 30f,
                    isReversed = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Failed login attempts
                ActivitySlider(
                    label = "Failed Logins: ${failedAttempts}",
                    value = failedAttempts.toFloat(),
                    onValueChange = { failedAttempts = it.toInt() },
                    range = 0f..20f,
                    warningThreshold = 5f
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Behavioral Flags",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ToggleRow(
                    label = "Uses Third-Party Tools",
                    checked = usesThirdPartyTools,
                    onCheckedChange = { usesThirdPartyTools = it },
                    color = Color(0xFFD32F2F)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleRow(
                    label = "Bot-like Activity Detected",
                    checked = frequentBotActivity,
                    onCheckedChange = { frequentBotActivity = it },
                    color = Color(0xFFF57C00)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleRow(
                    label = "Rapid Status Updates",
                    checked = rapidStatusUpdates,
                    onCheckedChange = { rapidStatusUpdates = it },
                    color = Color(0xFFFBC02D)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Risk Factors
        if (riskAnalysis.riskFactors.isNotEmpty()) {
            Text(
                text = "Detected Risk Factors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    riskAnalysis.riskFactors.forEach { factor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Risk",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = factor,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B0000)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Recommendations
        Text(
            text = "Safety Recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                riskAnalysis.recommendations.forEach { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Recommendation",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Safety Tips Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "✓ WhatsApp Ban Prevention Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                listOf(
                    "Use official WhatsApp app only",
                    "Avoid sending messages too rapidly",
                    "Don't use automation or bot tools",
                    "Limit bulk adding to groups",
                    "Verify your phone number",
                    "Maintain normal usage patterns",
                    "Don't share account credentials",
                    "Keep app updated",
                    "Avoid spamming contacts",
                    "Monitor failed login attempts"
                ).forEach { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Disclaimer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFF3E0),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "⚠️ WhatsApp Ban Shield is an educational tool for account safety awareness. It does not provide legal advice or guarantee ban prevention. Always follow WhatsApp's Terms of Service and community guidelines.",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = Color(0xFFE65100),
                modifier = Modifier.padding(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ActivitySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    warningThreshold: Float,
    isReversed: Boolean = false
) {
    val isAtRisk = if (isReversed) value < warningThreshold else value > warningThreshold
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isAtRisk) FontWeight.Bold else FontWeight.Normal,
            color = if (isAtRisk) Color(0xFFF57C00) else MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = if (isAtRisk) Color(0xFFF57C00) else Color(0xFF388E3C),
                activeTrackColor = if (isAtRisk) Color(0xFFF57C00) else Color(0xFF388E3C)
            )
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
            color = if (checked) color else MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BanShieldScreenPreview() {
    MyApplicationTheme {
        BanShieldScreen()
    }
}
