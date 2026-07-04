# WhatsApp Ban Shield 🛡️

**Account Safety Monitor & Ban Recovery Tool**

A comprehensive Android application designed to help users monitor their WhatsApp account safety, detect ban risks, and guide them through the recovery process if their account gets banned.

## 📋 Features

### 🔍 **Ban Risk Detection**
- **Real-time Risk Analysis**: Evaluates account behavior against WhatsApp's policies
- **8 Risk Factors Monitored**:
  - 📊 Messaging Rates (rapid messaging detection)
  - 📁 Media Upload Frequency
  - 👥 Contact/Group Addition Speed
  - 📅 Account Age Analysis
  - 🤖 Bot-like Activity Detection
  - 🔧 Third-party Tool Usage
  - 🔐 Failed Login Attempts
  - 📲 Rapid Status Updates

- **Risk Scoring**: 0-100 scale with 4 severity levels
  - 🟢 **LOW** (0-40): Account appears safe
  - 🟡 **MEDIUM** (40-60): Some risk indicators
  - 🟠 **HIGH** (60-80): Significant risk
  - 🔴 **CRITICAL** (80-100): Severe risk of ban

### 💾 **Ban Recovery Guide**
- **6-Step Recovery Process**:
  1. ⏳ Wait Period (7-30 days based on severity)
  2. 🧹 Complete Uninstall & Device Clean
  3. ✅ Phone Number Verification
  4. 📤 Submit Ban Appeal
  5. 🔄 Change Behavior & Usage Habits
  6. 📧 Contact WhatsApp Support

- **Success Rate Prediction**: Based on violation severity and compliance
- **Personalized Recommendations**: Tailored based on account status
- **Detailed Action Steps**: Clear instructions for each recovery step

### 📚 **Educational Resources**
- WhatsApp Terms of Service guidelines
- Ban causes and prevention tips
- Recovery timeline expectations
- Best practices for account safety

## 🏗️ Project Structure

```
WhatsApp-Ban-Shield/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/
│   │       │   ├── MainActivity.kt              # Main UI entry point
│   │       │   ├── BanShieldScreen()            # Risk detection UI
│   │       │   ├── BanDetectionEngine.kt        # Risk analysis logic
│   │       │   ├── BanRecoveryEngine.kt         # Recovery guidance logic
│   │       │   ├── RecoveryUI.kt                # Recovery screen UI
│   │       │   └── ui/theme/                    # Material Design theme
│   │       ├── AndroidManifest.xml
│   │       └── res/
│   ├── build.gradle.kts                        # App build config
│   └── proguard-rules.pro
├── build.gradle.kts                            # Project build config
├── settings.gradle.kts
├── gradle.properties
├── .env.example                                # Environment template
├── metadata.json                               # App metadata
└── README.md                                   # This file
```

## 🎯 Core Components

### 1. **BanDetectionEngine.kt**
Analyzes account behavior and calculates ban risk:
- `WhatsAppActivity` - Data class holding account metrics
- `BanRiskLevel` - Risk analysis result with score and factors
- `analyzeRisk()` - Main analysis function
- `getDetailedAnalysis()` - Formatted analysis report

```kotlin
val activity = WhatsAppActivity(
    messagesPerMinute = 15f,
    mediaUploadPerHour = 20f,
    newContactsPerDay = 5,
    groupAdditionsPerDay = 2,
    accountAge = 180,
    usesThirdPartyTools = false,
    frequentBotActivity = false
)

val risk = engine.analyzeRisk(activity)
// Returns: BanRiskLevel with score, level, factors, recommendations
```

### 2. **BanRecoveryEngine.kt**
Guides users through account recovery:
- `BannedAccountInfo` - Information about the banned account
- `BanRecoveryStatus` - Recovery plan and timeline
- `analyzeAndRecover()` - Creates personalized recovery plan
- `getDetailedRecoveryGuide()` - Complete recovery documentation

```kotlin
val account = BannedAccountInfo(
    phoneNumber = "+92XXXXXXXXXX",
    daysSinceBan = 5,
    violationSeverity = "MODERATE",
    wasUsingThirdPartyApp = false,
    wasSpamming = false
)

val recovery = engine.analyzeAndRecover(account)
// Returns: Recovery steps, timeline, success rate, warnings
```

### 3. **UI Components**

#### BanShieldScreen()
Main risk detection interface with:
- Interactive sliders for activity metrics
- Real-time risk score display
- Risk factor list
- Safety recommendations
- Behavioral flags (toggles)

#### BanRecoveryScreen()
Recovery guidance with:
- Ban status display
- Severity selection
- Violation history input
- Step-by-step recovery instructions
- WhatsApp contact information
- Estimated recovery timeline

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Android SDK 24+
- Kotlin 1.9+
- Gradle 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/avresort0-oss/WhatsApp-Ban-Shield.git
   cd WhatsApp-Ban-Shield
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync

3. **Configure Environment**
   - Copy `.env.example` to `.env`
   - Update environment variables if needed

4. **Remove Debug Signing Config** (for local development)
   - Edit `app/build.gradle.kts`
   - Remove or comment: `signingConfig = signingConfigs.getByName("debugConfig")`

5. **Build & Run**
   ```bash
   # Via Android Studio: Run > Run 'app'
   # Or via Gradle
   ./gradlew assembleDebug
   ```

## 📊 Usage Examples

### Check Account Risk

```kotlin
val engine = BanDetectionEngine()
val activity = WhatsAppActivity(
    messagesPerMinute = 75f,      // Too high!
    mediaUploadPerHour = 120f,    // Too high!
    newContactsPerDay = 15,
    groupAdditionsPerDay = 8,
    accountAge = 45,
    usesThirdPartyTools = false,
    frequentBotActivity = false
)

val risk = engine.analyzeRisk(activity)
println(risk.level)           // "HIGH"
println(risk.score)           // 65
println(risk.riskFactors)     // List of detected issues
println(risk.recommendations) // Safety tips
```

### Get Recovery Plan

```kotlin
val recovery = WhatsAppBanRecoveryEngine()
val account = BannedAccountInfo(
    phoneNumber = "+92XXXXXXXXXX",
    banReasonEstimate = "Bot-like behavior",
    daysSinceBan = 10,
    violationSeverity = "MODERATE",
    wasAutomating = true
)

val plan = recovery.analyzeAndRecover(account)
println(plan.status)                  // "RECOVERY_IN_PROGRESS"
println(plan.successRate)             // 65%
println(plan.estimatedRecoveryTime)   // "14-21 days"
plan.recoverySteps.forEach { step ->
    println("${step.stepNumber}. ${step.title}")
    println("   Action: ${step.action}")
    println("   Duration: ${step.duration}")
}
```

## ⚠️ Risk Detection Thresholds

| Metric | Safe | Warning | Danger |
|--------|------|---------|--------|
| Messages/Min | <30 | 30-60 | >60 |
| Media/Hour | <50 | 50-100 | >100 |
| New Contacts/Day | <20 | 20-50 | >50 |
| Group Adds/Day | <10 | 10-30 | >30 |
| Account Age | >30 days | 7-30 days | <7 days |
| Failed Logins | <3 | 3-5 | >5 |

## 🔧 Recovery Timeline

### MINOR Violations
- ⏳ **Wait**: 7 days
- 📈 **Success Rate**: 85%+
- 📧 **Appeal**: Usually approved

### MODERATE Violations
- ⏳ **Wait**: 14 days
- 📈 **Success Rate**: 50-70%
- 📧 **Appeal**: May need detailed explanation

### SEVERE Violations
- ⏳ **Wait**: 30 days
- 📈 **Success Rate**: 20-40%
- 📧 **Appeal**: Often permanent

## 📧 WhatsApp Support

**Email**: support@support.whatsapp.com

**Appeal Template**:
```
Subject: Account Ban Appeal - [Your Phone Number]

Dear WhatsApp Support,

I'm writing to appeal the ban on my account ([phone number]).

I understand the ban was issued due to [explain reason].

I sincerely regret this behavior and have taken the following 
steps to ensure it doesn't happen again:
- [Change 1]
- [Change 2]
- [Change 3]

I promise to follow WhatsApp's Terms of Service strictly going forward.

Thank you for considering my appeal.

Best regards,
[Your Name]
```

## 🎓 Educational Purpose

This app is designed for **educational purposes only**:
- ✅ Learn about WhatsApp's policies
- ✅ Understand ban causes
- ✅ Know recovery procedures
- ✅ Monitor account health

**This is NOT a tool to circumvent WhatsApp's security measures.**

## ⚖️ Legal Notice

- WhatsApp Ban Shield is an independent educational tool
- Not affiliated with WhatsApp, Meta, or Facebook
- Use responsibly and ethically
- Always follow WhatsApp's Terms of Service
- Some bans may be permanent and irreversible
- This tool does not guarantee account recovery

## 🔐 Privacy & Security

- No data is sent to external servers
- All analysis happens on-device
- No personal information is stored
- No tracking or analytics

## 🛠️ Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM pattern
- **Testing**: JUnit, Robolectric, Roborazzi
- **Database**: Room (optional for future features)
- **Networking**: Retrofit, OkHttp (optional)
- **Build**: Gradle (Kotlin DSL)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)

## 📦 Dependencies

Core dependencies:
- `androidx.compose.ui:ui`
- `androidx.compose.material3:material3`
- `androidx.lifecycle:lifecycle-runtime-compose`
- `androidx.activity:activity-compose`
- `kotlinx.coroutines`

## 🐛 Known Issues & Limitations

1. **Permanent Bans**: Cannot recover from serious violations
2. **Appeal Success**: WhatsApp's decision is final
3. **Recovery Time**: Varies greatly by individual case
4. **No Guarantee**: This tool cannot guarantee recovery
5. **Terms Change**: WhatsApp policies may change anytime

## 🔄 Future Enhancements

- [ ] Account monitoring background service
- [ ] Ban status API integration (if WhatsApp provides)
- [ ] Auto-backup before major changes
- [ ] Multiple account management
- [ ] Detailed activity logging
- [ ] Community recovery stories
- [ ] Push notifications for status updates
- [ ] Web dashboard
- [ ] API for third-party integrations

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Follow Kotlin style guidelines
4. Submit a pull request

## 📄 License

This project is provided as-is for educational purposes.

## 📞 Support

- **Issues**: GitHub Issues
- **Email**: avresort0@gmail.com
- **WhatsApp Support**: support@support.whatsapp.com

## ✅ Checklist for Users

Before using WhatsApp again after recovery:

- [ ] Wait full recommended period
- [ ] Uninstall and reinstall official app
- [ ] Clear all cache
- [ ] Use official WhatsApp only
- [ ] Enable two-step verification
- [ ] Update to latest version
- [ ] Use normal message pace
- [ ] Avoid bulk operations
- [ ] Don't use any mods/hacks
- [ ] Monitor account closely

## 🎯 Best Practices

1. **Prevention First**: Use the detection tool regularly
2. **Normal Patterns**: Mimic real human behavior
3. **Official Only**: Use only official WhatsApp
4. **Take Breaks**: Don't send messages continuously
5. **Verify Account**: Keep phone number verified
6. **Update App**: Keep WhatsApp updated
7. **Report Bugs**: Report suspicious account activity
8. **Backup Chats**: Export important conversations
9. **Monitor Usage**: Check activity regularly
10. **Respect Policies**: Always follow Terms of Service

## 📚 Resources

- [WhatsApp Terms of Service](https://www.whatsapp.com/legal)
- [WhatsApp FAQ](https://faq.whatsapp.com)
- [WhatsApp Community Guidelines](https://www.whatsapp.com/community-guidelines)
- [Android Developers Guide](https://developer.android.com)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)

---

**Made with ❤️ for WhatsApp account safety**

*Last Updated: July 2026*

**Disclaimer**: This application is not endorsed by, directly affiliated with, or in any way officially connected to WhatsApp, Meta Platforms, Inc., or any of its subsidiaries or affiliates.
