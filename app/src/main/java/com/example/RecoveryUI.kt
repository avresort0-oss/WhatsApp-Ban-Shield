package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll

@Composable
fun BanRecoveryScreen(modifier: Modifier = Modifier) {
    val engine = remember { WhatsAppBanRecoveryEngine() }
    
    var daysSinceBan by remember { mutableStateOf(5) }
    var violationSeverity by remember { mutableStateOf("MODERATE") }
    var wasUsingThirdParty by remember { mutableStateOf(false) }
    var wasSpamming by remember { mutableStateOf(false) }
    var wasAutomating by remember { mutableStateOf(false) }
    
    val account = BannedAccountInfo(
        phoneNumber = "+92XXXXXXXXXX",
        banReasonEstimate = "Behavior detected as spam/bot",
        daysSinceBan = daysSinceBan,
        previousViolations = emptyList(),
        violationSeverity = violationSeverity,
        wasUsingThirdPartyApp = wasUsingThirdParty,
        wasSpamming = wasSpamming,
        wasAutomating = wasAutomating
    )
    
    val recovery by remember(account) {
        mutableStateOf(engine.analyzeAndRecover(account))
    }
    
    val statusColor by animateColorAsState(
        when (recovery.status) {
            "BANNED" -> Color(0xFFD32F2F)
            "RECOVERY_IN_PROGRESS" -> Color(0xFFF57C00)
            "RECOVERED" -> Color(0xFF388E3C)
            else -> Color(0xFF9C27B0)
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
            text = "WhatsApp Ban Recovery",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Account Recovery Guide",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status Card
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
                    imageVector = when (recovery.status) {
                        "BANNED" -> Icons.Default.Lock
                        "RECOVERY_IN_PROGRESS" -> Icons.Default.Schedule
                        "RECOVERED" -> Icons.Default.CheckCircle
                        else -> Icons.Default.HelpOutline
                    },
                    contentDescription = "Status",
                    tint = statusColor,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = recovery.status.replace("_", " "),
                    style = MaterialTheme.typography.headlineSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Days Since Ban: ${recovery.daysSinceBan}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Estimated Recovery: ${recovery.estimatedRecoveryTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Success Rate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Recovery Success Rate: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "${recovery.successRate}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Ban Info Input
        Text(
            text = "Ban Information",
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
                // Days since ban
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Days Since Ban: $daysSinceBan",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Slider(
                    value = daysSinceBan.toFloat(),
                    onValueChange = { daysSinceBan = it.toInt() },
                    valueRange = 0f..60f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Violation Severity
                Text(
                    text = "Violation Severity",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("MINOR", "MODERATE", "SEVERE").forEach { severity ->
                        Button(
                            onClick = { violationSeverity = severity },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (violationSeverity == severity) {
                                    when (severity) {
                                        "MINOR" -> Color(0xFF388E3C)
                                        "MODERATE" -> Color(0xFFF57C00)
                                        else -> Color(0xFFD32F2F)
                                    }
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        ) {
                            Text(
                                text = severity,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Violation Flags
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
                    text = "What Led to Ban?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ToggleRow(
                    label = "Used Third-Party Apps/Mods",
                    checked = wasUsingThirdParty,
                    onCheckedChange = { wasUsingThirdParty = it },
                    color = Color(0xFFD32F2F)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleRow(
                    label = "Spamming/Bulk Messaging",
                    checked = wasSpamming,
                    onCheckedChange = { wasSpamming = it },
                    color = Color(0xFFF57C00)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleRow(
                    label = "Using Automation/Bot Tools",
                    checked = wasAutomating,
                    onCheckedChange = { wasAutomating = it },
                    color = Color(0xFFFBC02D)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Warning Messages
        if (recovery.warningMessages.isNotEmpty()) {
            Text(
                text = "⚠️ Important Warnings",
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
                    recovery.warningMessages.forEach { warning ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B0000)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Recovery Steps
        Text(
            text = "Recovery Steps (${recovery.recoverySteps.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )
        
        recovery.recoverySteps.forEach { step ->
            RecoveryStepCard(step)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Next Action
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Next Action",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF01579B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recovery.nextAction,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0277BD),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // WhatsApp Contact Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F8E9)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📧 Contact WhatsApp Support",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF33691E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Email: support@support.whatsapp.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Text(
                    text = "Website: faq.whatsapp.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Be polite and honest in your appeal. Explain what happened and what changes you've made.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF33691E),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
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
                text = "⚖️ Educational Tool Only: This recovery guide is for educational purposes. WhatsApp's decision is final. Some bans may be permanent. Always follow WhatsApp's Terms of Service.",
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
fun RecoveryStepCard(step: RecoveryStep) {
    val backgroundColor = when (step.difficulty) {
        "EASY" -> Color(0xFFE8F5E9)
        "MEDIUM" -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }
    
    val borderColor = when (step.difficulty) {
        "EASY" -> Color(0xFF388E3C)
        "MEDIUM" -> Color(0xFFF57C00)
        else -> Color(0xFFD32F2F)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Step Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(borderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step ${step.stepNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    modifier = Modifier
                        .background(borderColor, RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    color = borderColor
                ) {
                    Text(
                        text = step.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Action
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(1.dp),
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = step.action,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Duration",
                    tint = borderColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Duration: ${step.duration}",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
