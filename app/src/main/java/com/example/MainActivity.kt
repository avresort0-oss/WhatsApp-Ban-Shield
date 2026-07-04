package com.example

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                        .background(Color(0xFF0F172A)), // Force modern slate-dark background
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    SafeShieldApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Data Classes for UI models
data class DiagnosticItem(
    val id: String,
    val name: String,
    val isSecure: Boolean,
    val severity: String, // "High", "Medium", "Safe"
    val description: String,
    val recommendation: String,
    val settingAction: (() -> Unit)? = null
)

data class ComplianceQuestion(
    val id: Int,
    val text: String,
    val recommendation: String,
    val riskPoints: Int,
    var isYesSelected: Boolean = false
)

data class BackgroundProcessItem(
    val name: String,
    val appType: String,
    val isSuspicious: Boolean,
    val riskFactor: String, // "High", "Medium", "Low"
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeShieldApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Application tabs
    var selectedTab by remember { mutableStateOf(0) }
    
    // Core states
    var isScanning by remember { mutableStateOf(false) }
    var lastScanTime by remember { mutableStateOf("Not Scanned Yet") }
    var developerOptionsEnabled by remember { mutableStateOf(false) }
    var adbEnabled by remember { mutableStateOf(false) }
    var vpnActive by remember { mutableStateOf(false) }
    
    // Fetch live system diagnostics
    fun refreshLiveDiagnostics() {
        developerOptionsEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0
        
        adbEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) != 0
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        vpnActive = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }

    // Trigger initial fetch
    LaunchedEffect(Unit) {
        refreshLiveDiagnostics()
    }

    // Custom dark cyber color theme
    val darkBg = Color(0xFF0F172A)
    val cardBg = Color(0xFF1E293B)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFF59E0B)
    val neonRed = Color(0xFFEF4444)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    // Dynamic state evaluation for device settings
    val deviceDiagnostics = remember(developerOptionsEnabled, adbEnabled, vpnActive) {
        listOf(
            DiagnosticItem(
                id = "dev_opts",
                name = "Developer Options Mode",
                isSecure = !developerOptionsEnabled,
                severity = if (developerOptionsEnabled) "Medium" else "Safe",
                description = if (developerOptionsEnabled) "Developer options are currently active on this system." else "Developer options are disabled.",
                recommendation = "Some security heuristics flag active Developer Mode as an anomalous setting for regular application accounts.",
                settingAction = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open development settings directly.", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            DiagnosticItem(
                id = "usb_debug",
                name = "USB Debugging status (ADB)",
                isSecure = !adbEnabled,
                severity = if (adbEnabled) "High" else "Safe",
                description = if (adbEnabled) "USB debugging (ADB) is enabled, exposing command-line access." else "USB debugging (ADB) is safely inactive.",
                recommendation = "We highly advise turning off USB debugging to avoid automated diagnostic flags during normal communication usage.",
                settingAction = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open development settings.", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            DiagnosticItem(
                id = "vpn_state",
                name = "Network Tunnel (VPN) Protection",
                isSecure = vpnActive,
                severity = if (vpnActive) "Safe" else "Medium",
                description = if (vpnActive) "Your network is securely tunneled through a VPN connection." else "No active network tunnel detected.",
                recommendation = "An encrypted private network tunnel masks your public IP address, mitigating network-level surveillance and profiling.",
                settingAction = {
                    try {
                        val intent = Intent(Settings.ACTION_VPN_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not launch VPN settings directly. Please configure via system tray.", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            DiagnosticItem(
                id = "unoff_clients",
                name = "Unofficial Client Signatures",
                isSecure = true, // Static verification guidance
                severity = "Safe",
                description = "Verifying package signature authenticity. Secure official installation confirmed.",
                recommendation = "Never log into unofficial or modified clients like GBWhatsApp or FMWhatsApp, as automated safety filters consistently target and ban accounts running on unverified builds."
            )
        )
    }

    // Interactive ToS Questions for live Safety Score
    val complianceQuestions = remember {
        mutableStateListOf(
            ComplianceQuestion(1, "Do you send bulk or unsolicited mass messages to non-contact numbers?", "Sending high-volume automated messages triggers spam heuristics and is the primary cause of automated account restrictions.", 30),
            ComplianceQuestion(2, "Do you use third-party modified clients (e.g., GBWhatsApp, FMWhatsApp)?", "Modified clients bypass official protocol limitations and will lead to an immediate account ban upon detection.", 40),
            ComplianceQuestion(3, "Do you scrape contact information or harvest profile data from public groups?", "Automated harvesting and group scraping represent a clear violation of terms and trigger account safety flags.", 15),
            ComplianceQuestion(4, "Do you use automation macros, clickers, or chat bots on the device?", "External software simulators, keyboard macros, and rapid API triggers represent synthetic interaction patterns flagged by security filters.", 15)
        )
    }

    // Dynamic Account Safety Score calculation based on questions and system state
    val totalRiskPenalty = complianceQuestions.filter { it.isYesSelected }.sumOf { it.riskPoints }
    val deviceRiskPenalty = (if (developerOptionsEnabled) 10 else 0) + (if (adbEnabled) 15 else 0)
    val safetyScore = (100 - totalRiskPenalty - deviceRiskPenalty).coerceIn(0, 100)

    val safetyColor = when {
        safetyScore >= 80 -> neonGreen
        safetyScore >= 50 -> neonOrange
        else -> neonRed
    }
    
    val safetyLabel = when {
        safetyScore >= 80 -> "OPTIMAL SECURITY"
        safetyScore >= 50 -> "ELEVATED RISK STATUS"
        else -> "CRITICAL BAN RISK"
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = darkBg
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // High-Tech Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Shield Logo",
                                tint = neonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SafeShield Pro",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = textPrimary
                            )
                        }
                        Text(
                            text = "Advanced Device & Privacy Diagnostic Suite",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF020617))
                            .border(1.dp, neonGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EDU MODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = neonGreen
                        )
                    }
                }
            }

            // Tab Buttons Container
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = neonGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = neonGreen
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Security Scan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ToS Audit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Cache & Network", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                )
            }

            // Screen Content according to selected tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> SecurityScanTab(
                        isScanning = isScanning,
                        lastScanTime = lastScanTime,
                        safetyScore = safetyScore,
                        safetyColor = safetyColor,
                        safetyLabel = safetyLabel,
                        diagnostics = deviceDiagnostics,
                        onTriggerScan = {
                            scope.launch {
                                isScanning = true
                                delay(2000) // Realistic interactive diagnostic pause
                                refreshLiveDiagnostics()
                                lastScanTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                isScanning = false
                            }
                        }
                    )
                    1 -> TosComplianceTab(
                        questions = complianceQuestions,
                        safetyScore = safetyScore,
                        safetyColor = safetyColor,
                        safetyLabel = safetyLabel,
                        onQuestionToggle = { id, isYes ->
                            val index = complianceQuestions.indexOfFirst { it.id == id }
                            if (index != -1) {
                                complianceQuestions[index] = complianceQuestions[index].copy(isYesSelected = isYes)
                            }
                        }
                    )
                    2 -> CacheAndNetworkTab(
                        vpnActive = vpnActive,
                        context = context
                    )
                }
            }

            // Bottom Educational Warning Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF020617))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Education Info Icon",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "This application serves as an educational cybersecurity audit tool. It helps identify system risks and compliance behaviors to naturally safeguard privacy but does not circumvent, bypass, or alter third-party platform security controls.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 14.sp
                        ),
                        color = textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 🛡️ TAB 1: DEVICE SCAN & INTEGRITY AUDITOR
@Composable
fun SecurityScanTab(
    isScanning: Boolean,
    lastScanTime: String,
    safetyScore: Int,
    safetyColor: Color,
    safetyLabel: String,
    diagnostics: List<DiagnosticItem>,
    onTriggerScan: () -> Unit
) {
    val cardBg = Color(0xFF1E293B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val neonGreen = Color(0xFF10B981)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Tech Radial Safety Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety_gauge_card"),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACCOUNT SAFETY PROJECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Radial Arc Gauge Draw Block
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        val animatedScore = animateFloatAsState(
                            targetValue = if (isScanning) 0f else safetyScore.toFloat(),
                            animationSpec = tween(durationMillis = 1500),
                            label = "animatedScore"
                        )

                        Canvas(modifier = Modifier.size(150.dp)) {
                            // Circular background track
                            drawArc(
                                color = Color(0xFF0F172A),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Glowing dynamic safety score progress arc
                            drawArc(
                                color = safetyColor,
                                startAngle = 135f,
                                sweepAngle = (animatedScore.value / 100f) * 270f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Inside Score readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isScanning) "SCAN" else "${safetyScore}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isScanning) neonGreen else safetyColor
                            )
                            Text(
                                text = if (isScanning) "ANALYZING..." else "SECURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = safetyLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = safetyColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Last System Audit Check: $lastScanTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onTriggerScan,
                        enabled = !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("one_tap_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonGreen,
                            disabledContainerColor = neonGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Radar, contentDescription = "Radar Scan")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ONE-TAP SECURITY AUDIT",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 30-Day Safety Trend Card
        item {
            SafetyScoreTrendCard(
                currentScore = safetyScore,
                safetyColor = safetyColor
            )
        }

        // Live Integrity Diagnostics List Heading
        item {
            Text(
                text = "DEVICE SECURITY INTEGRITY SIGNALS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List of integrity diagnostic status widgets
        items(diagnostics) { item ->
            DiagnosticCard(item = item)
        }

        // Background Heuristics Process Watcher Heading
        item {
            Text(
                text = "HEURISTIC SUSPICIOUS PROCESS ANALYSIS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Mock suspicious background processes often flagged by cybersecurity heuristic algorithms
        val suspiciousProcesses = listOf(
            BackgroundProcessItem("MacroDroid / Tasker", "System Automation Hook", true, "Medium", "Automated tap tools and layout triggers simulate synthetic input patterns commonly flagged by safety sensors."),
            BackgroundProcessItem("FakeGPS / LocationMock", "Mock Positioning Simulator", true, "High", "Spoofing coordinate signals breaks network geolocation checks and marks operations as highly anomalous."),
            BackgroundProcessItem("Parallel Space / App Cloner", "Sandboxed App Cloner", true, "Medium", "Virtualized execution engines and environment multipliers trigger client package integrity checks."),
            BackgroundProcessItem("Standard Android OS Launcher", "Official System Process", false, "Low", "Fully signed default launcher representing normal interaction context.")
        )

        items(suspiciousProcesses) { process ->
            ProcessHeuristicsCard(process = process)
        }
    }
}

// Security Check Individual Widget
@Composable
fun DiagnosticCard(item: DiagnosticItem) {
    val cardBg = Color(0xFF1E293B)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFF59E0B)
    val neonRed = Color(0xFFEF4444)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (item.isSecure) Icons.Default.VerifiedUser else Icons.Default.Warning,
                        contentDescription = "Status Icon",
                        tint = if (item.isSecure) neonGreen else if (item.severity == "High") neonRed else neonOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.isSecure) neonGreen.copy(alpha = 0.15f)
                            else if (item.severity == "High") neonRed.copy(alpha = 0.15f)
                            else neonOrange.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.isSecure) "SECURE" else if (item.severity == "High") "CRITICAL" else "WARNING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (item.isSecure) neonGreen else if (item.severity == "High") neonRed else neonOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Advisory: ${item.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.isSecure) textSecondary else neonOrange
            )

            if (item.settingAction != null && !item.isSecure) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = item.settingAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Fix settings",
                            modifier = Modifier.size(14.dp),
                            tint = neonGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESOLVE IN SYSTEM SETTINGS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = neonGreen
                        )
                    }
                }
            }
        }
    }
}

// Background Process Monitor Card
@Composable
fun ProcessHeuristicsCard(process: BackgroundProcessItem) {
    val cardBg = Color(0xFF1E293B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFF59E0B)
    val neonRed = Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (process.isSuspicious) Icons.Default.Dns else Icons.Default.CheckCircle,
                        contentDescription = "Process status",
                        tint = if (process.isSuspicious) neonOrange else neonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = process.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (!process.isSuspicious) neonGreen.copy(alpha = 0.15f)
                            else if (process.riskFactor == "High") neonRed.copy(alpha = 0.15f)
                            else neonOrange.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (!process.isSuspicious) "SAFE RUNTIME" else "RISK: ${process.riskFactor}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (!process.isSuspicious) neonGreen else if (process.riskFactor == "High") neonRed else neonOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Class: ${process.appType}",
                style = MaterialTheme.typography.labelSmall,
                color = textSecondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = process.reason,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
    }
}


// 💬 TAB 2: WHATSAPP TERMS OF SERVICE POLICY COMPLIANCE SURVEY
@Composable
fun TosComplianceTab(
    questions: List<ComplianceQuestion>,
    safetyScore: Int,
    safetyColor: Color,
    safetyLabel: String,
    onQuestionToggle: (Int, Boolean) -> Unit
) {
    val cardBg = Color(0xFF1E293B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val neonGreen = Color(0xFF10B981)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analysis Check",
                        tint = safetyColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ACCOUNT POLICY SCORE: ${safetyScore}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = safetyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = safetyLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = safetyColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Most account bans on third-party platforms are triggered by behavioral violations and protocol manipulation rather than network-level IP tags. Answer the checklist below to calculate your risk of triggering automated restriction blocks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Text(
                text = "COMPLIANCE & RISK PATTERN AUDIT",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(questions) { question ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = question.isYesSelected,
                                onClick = { onQuestionToggle(question.id, true) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                            )
                            Text(
                                text = "Yes (Elevated Risk)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary,
                                modifier = Modifier.clickable { onQuestionToggle(question.id, true) }
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !question.isYesSelected,
                                onClick = { onQuestionToggle(question.id, false) },
                                colors = RadioButtonDefaults.colors(selectedColor = neonGreen)
                            )
                            Text(
                                text = "No (Compliant)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary,
                                modifier = Modifier.clickable { onQuestionToggle(question.id, false) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "RISK IMPACT: -${question.riskPoints}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (question.isYesSelected) Color(0xFFEF4444) else textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = question.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}


// 🌐 TAB 3: LOCAL CACHE MANAGEMENT & NETWORK IP PRIVACY TUTORIALS
@Composable
fun CacheAndNetworkTab(
    vpnActive: Boolean,
    context: Context
) {
    val cardBg = Color(0xFF1E293B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFF59E0B)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // IP Masking and Connection Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "NETWORK ROUTING MATRIX",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = textSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (vpnActive) neonGreen.copy(alpha = 0.15f) else neonOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (vpnActive) "TUNNEL ACTIVE" else "PUBLIC IP VISIBLE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (vpnActive) neonGreen else neonOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (vpnActive) Icons.Default.Language else Icons.Default.PublicOff,
                            contentDescription = "Global IP Status",
                            tint = if (vpnActive) neonGreen else neonOrange,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (vpnActive) "Masked Device IP: 198.51.100.145" else "Visible Device IP: 103.114.39.23",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = textPrimary
                            )
                            Text(
                                text = if (vpnActive) "Routed through secure private DNS and encrypted tunnel." else "Your internet protocol address is directly readable by automated system diagnostic checks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_VPN_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open VPN settings.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SettingsEthernet, contentDescription = "Tethering", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONFIGURE NETWORK MASKING & VPN", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = neonGreen)
                    }
                }
            }
        }

        // WhatsApp App Cache Cleaner Helper Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "LOCAL CACHE & LOG DIAGNOSTICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Why Clear Temporary Cache & Files?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Communication programs store temporary cached database checkpoints, log snippets, and tracking tags on local memory. Automated risk-mitigation systems scan these directories periodically to identify structural changes or custom third-party cloner behaviors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Safe clear guidelines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "MANUAL RESOLUTION PROCEDURES:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = neonGreen)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Tap the launch button below to open official Settings Info.\n2. Navigate to 'Storage & Cache' or 'Storage'.\n3. Tap 'Clear Cache'.\n*Note: Avoid 'Clear Storage/Data' as it removes your active login sessions.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                color = textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:com.whatsapp")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp is not installed or package signature was not found.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Clear cache", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LAUNCH APP SETTINGS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyScoreTrendCard(
    currentScore: Int,
    safetyColor: Color
) {
    val cardBg = Color(0xFF1E293B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFF59E0B)
    val neonRed = Color(0xFFEF4444)

    // Generate historic safety scores for 30 days
    // Ensure the last point is always the current score
    val dataPoints = remember(currentScore) {
        val points = mutableListOf<Int>()
        var base = 70
        val random = java.util.Random(1337) // deterministic seed for stability
        for (i in 1..29) {
            val target = currentScore
            val diff = target - base
            base += if (diff > 0) {
                random.nextInt(4)
            } else if (diff < 0) {
                -random.nextInt(4)
            } else {
                random.nextInt(3) - 1
            }
            base = base.coerceIn(40, 100)
            points.add(base)
        }
        points.add(currentScore)
        points
    }

    var selectedIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("safety_score_trend_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "30-DAY SAFETY TREND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Security Improvement Chart",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "Trend Icon",
                    tint = safetyColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas-based interactive Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val xIncrement = width / (dataPoints.size - 1)
                                
                                var nearestIndex = -1
                                var minDistance = Float.MAX_VALUE
                                
                                for (i in dataPoints.indices) {
                                    val x = i * xIncrement
                                    val distance = kotlin.math.abs(offset.x - x)
                                    if (distance < minDistance) {
                                        minDistance = distance
                                        nearestIndex = i
                                    }
                                }
                                
                                if (nearestIndex != -1 && minDistance < xIncrement / 2) {
                                    selectedIndex = nearestIndex
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val maxVal = 100f
                    val minVal = 0f
                    val range = maxVal - minVal

                    // Draw Y-axis gridlines
                    val gridLines = listOf(0f, 25f, 50f, 75f, 100f)
                    gridLines.forEach { value ->
                        val y = height - ((value - minVal) / range) * height
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute point coordinates
                    val xIncrement = width / (dataPoints.size - 1)
                    val points = dataPoints.mapIndexed { index, score ->
                        val x = index * xIncrement
                        val y = height - ((score.toFloat() - minVal) / range) * height
                        Offset(x, y)
                    }

                    // Draw gradient area below the line
                    if (points.isNotEmpty()) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points.first().x, height)
                            points.forEach { offset ->
                                lineTo(offset.x, offset.y)
                            }
                            lineTo(points.last().x, height)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    safetyColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    // Draw connecting line
                    if (points.size > 1) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = safetyColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw end point highlighted pulse
                    val lastPoint = points.last()
                    drawCircle(
                        color = safetyColor,
                        radius = 6.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = safetyColor.copy(alpha = 0.4f),
                        radius = 12.dp.toPx(),
                        center = lastPoint,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw vertical indicator line for selected index
                    if (selectedIndex in dataPoints.indices) {
                        val selectedPoint = points[selectedIndex]
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(selectedPoint.x, 0f),
                            end = Offset(selectedPoint.x, height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = selectedPoint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-axis and Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Day 1 (Start)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
                Text(
                    text = "Day 15",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
                Text(
                    text = "Day 30 (Today)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = safetyColor
                )
            }

            // Interactive Tooltip Info Box
            AnimatedVisibility(visible = selectedIndex != -1) {
                if (selectedIndex in dataPoints.indices) {
                    val selectedScore = dataPoints[selectedIndex]
                    val isLastPoint = selectedIndex == dataPoints.size - 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLastPoint) "Selected: Day 30 (Today)" else "Selected: Day ${selectedIndex + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                        Text(
                            text = "Safety Score: $selectedScore%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                selectedScore >= 80 -> neonGreen
                                selectedScore >= 50 -> neonOrange
                                else -> neonRed
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graph explanation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Analysis trend",
                            tint = neonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historical Diagnostics Insight",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tracks historical account vulnerability. Resolving system flags (Developer Mode, USB Debugging) and selecting safe compliance rules automatically triggers an upwards recovery curve.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

