package com.shikshapulse.app

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Ink = Color(0xFF18233D)
private val Muted = Color(0xFF7C879D)
private val Page = Color(0xFFF5F7FB)
private val Blue = Color(0xFF5575F7)
private val Teal = Color(0xFF20A990)
private val Amber = Color(0xFFFFB547)
private val Lilac = Color(0xFF8C6BE8)

private val topicsBySubject = mapOf(
    "Physics" to listOf(
        "Units & Measurements", "Motion in a Straight Line", "Motion in a Plane",
        "Laws of Motion", "Work, Energy & Power", "System of Particles",
        "Rotational Motion", "Gravitation", "Mechanical Properties of Solids",
        "Thermodynamics", "Oscillations", "Waves"
    ),
    "Chemistry" to listOf(
        "Some Basic Concepts", "Structure of Atom", "Classification of Elements",
        "Chemical Bonding", "States of Matter", "Thermodynamics", "Equilibrium",
        "Redox Reactions", "Organic Chemistry Basics", "Hydrocarbons",
        "Solutions", "Electrochemistry"
    ),
    "Biology" to listOf(
        "The Living World", "Biological Classification", "Plant Kingdom",
        "Animal Kingdom", "Morphology of Flowering Plants", "Cell: The Unit of Life",
        "Biomolecules", "Cell Cycle & Cell Division", "Plant Physiology",
        "Human Physiology", "Reproduction", "Genetics & Evolution"
    ),
    "Maths" to listOf(
        "Sets", "Relations & Functions", "Trigonometric Functions",
        "Complex Numbers", "Linear Inequalities", "Permutations & Combinations",
        "Binomial Theorem", "Sequences & Series", "Straight Lines",
        "Conic Sections", "Limits & Derivatives", "Probability"
    )
)

private data class StudyLog(
    val id: String,
    val subject: String,
    val topic: String,
    val minutes: Int,
    val reward: Double,
    val timestamp: Long
)

private data class AppUsage(val name: String, val minutes: Int)
private data class UsageSnapshot(val hasAccess: Boolean, val apps: List<AppUsage>) {
    val totalMinutes: Int get() = apps.sumOf { it.minutes }
}

private enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    STUDY("Study", Icons.Default.MenuBook),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {
    private var usageSnapshot by mutableStateOf(UsageSnapshot(false, emptyList()))
    private var logs by mutableStateOf<List<StudyLog>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logs = StudyLogStore.load(this)
        refreshUsage()
        setContent {
            ShikshaPulseApp(
                logs = logs,
                usage = usageSnapshot,
                onAddStudy = ::addStudy,
                onRequestUsageAccess = ::openUsageAccessSettings
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUsage()
    }

    private fun addStudy(subject: String, topic: String, minutes: Int) {
        val factor = if (!usageSnapshot.hasAccess) {
            1.0
        } else {
            when {
                usageSnapshot.totalMinutes <= 60 -> 1.25
                usageSnapshot.totalMinutes <= 120 -> 1.10
                else -> 1.0
            }
        }
        val amount = (minutes / 60.0 * 5.0 * factor * 100).toInt() / 100.0
        logs = listOf(
            StudyLog(UUID.randomUUID().toString(), subject, topic, minutes, amount, System.currentTimeMillis())
        ) + logs
        StudyLogStore.save(this, logs)
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun refreshUsage() {
        usageSnapshot = readSocialUsage(this)
    }
}

@Composable
private fun ShikshaPulseApp(
    logs: List<StudyLog>,
    usage: UsageSnapshot,
    onAddStudy: (String, String, Int) -> Unit,
    onRequestUsageAccess: () -> Unit
) {
    var selectedTab by androidx.compose.runtime.remember { mutableStateOf(AppTab.HOME) }
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Blue,
            secondary = Teal,
            background = Page,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = Ink
        )
    ) {
        Scaffold(
            containerColor = Page,
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blue,
                                selectedTextColor = Blue,
                                indicatorColor = Blue.copy(alpha = 0.10f),
                                unselectedIconColor = Muted
                            )
                        )
                    }
                }
            }
        ) { insets ->
            when (selectedTab) {
                AppTab.HOME -> DashboardScreen(
                    logs = logs,
                    usage = usage,
                    onRequestUsageAccess = onRequestUsageAccess,
                    modifier = Modifier.padding(insets)
                )
                AppTab.STUDY -> StudyScreen(
                    onAddStudy = onAddStudy,
                    modifier = Modifier.padding(insets)
                )
                AppTab.WALLET -> WalletScreen(logs, Modifier.padding(insets))
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    logs: List<StudyLog>,
    usage: UsageSnapshot,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLogs = logs.filter { isToday(it.timestamp) }
    val todayMinutes = todayLogs.sumOf { it.minutes }
    val todayReward = todayLogs.sumOf { it.reward }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SHIKSHA PULSE", color = Blue, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.4.sp)
                Text("Your daily pulse", color = Ink, fontWeight = FontWeight.Bold, fontSize = 25.sp)
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8ECFF)),
                contentAlignment = Alignment.Center
            ) {
                Text("SP", color = Blue, fontWeight = FontWeight.Bold)
            }
        }
        Text("Small steps today. Big results tomorrow.", color = Muted, fontSize = 14.sp)

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Ink),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(Icons.Default.AutoGraph, null, tint = Color(0xFF9DB1FF))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("TODAY'S STUDY", color = Color.White.copy(alpha = .72f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .8.sp)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatDuration(todayMinutes), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("focused", color = Color.White.copy(alpha = .72f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 5.dp))
                }
                LinearProgressIndicator(
                    progress = { (todayMinutes / 240f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = Color(0xFF90A6FF),
                    trackColor = Color.White.copy(alpha = .14f)
                )
                Text("Daily goal: 4 hours", color = Color.White.copy(alpha = .70f), fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Social media",
                value = if (usage.hasAccess) formatDuration(usage.totalMinutes) else "Not linked",
                caption = if (usage.hasAccess) "across tracked apps today" else "connect to see your time",
                icon = Icons.Default.MoreTime,
                accent = Lilac,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Earned today",
                value = formatMoney(todayReward),
                caption = "added to your wallet",
                icon = Icons.Default.Bolt,
                accent = Amber,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Social media check-in", fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                        Text(
                            if (usage.hasAccess) "Usage updates from your phone" else "Allow Usage Access to track app time",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                    if (!usage.hasAccess) {
                        OutlinedButton(onClick = onRequestUsageAccess, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("Connect", fontSize = 12.sp)
                        }
                    }
                }
                if (usage.hasAccess && usage.apps.isNotEmpty()) {
                    usage.apps.sortedByDescending { it.minutes }.take(4).forEach { app ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(app.name, color = Ink, fontSize = 13.sp)
                            Text(formatDuration(app.minutes), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (usage.hasAccess) {
                    Text("No tracked social-app time yet today.", color = Muted, fontSize = 13.sp)
                } else {
                    Text("Only app usage totals are read on this device.", color = Muted, fontSize = 12.sp)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7F4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Default.Bolt, null, tint = Teal)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Your focus earns rewards", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹5 per study hour, plus a balance bonus for lower social time.", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    caption: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            androidx.compose.material3.Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            Text(title, color = Muted, fontSize = 12.sp)
            Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
            Text(caption, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun StudyScreen(
    onAddStudy: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by androidx.compose.runtime.remember { mutableStateOf("Physics") }
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Study tracker", color = Ink, fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Text("Choose a subject and log a focused session.", color = Muted, fontSize = 14.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            topicsBySubject.keys.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { selectedSubject = subject },
                    label = { Text(subject) }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${topicsBySubject[selectedSubject]?.size ?: 0} topics", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Tap a duration to log", color = Muted, fontSize = 12.sp)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(topicsBySubject[selectedSubject].orEmpty()) { topic ->
                TopicCard(topic = topic, subject = selectedSubject, onAddStudy = onAddStudy)
            }
        }
    }
}

@Composable
private fun TopicCard(topic: String, subject: String, onAddStudy: (String, String, Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(topic, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                DurationButton("Low", "30 min", 30, subject, topic, Teal, onAddStudy, Modifier.weight(1f))
                DurationButton("Medium", "1 hr", 60, subject, topic, Blue, onAddStudy, Modifier.weight(1f))
                DurationButton("High", "2 hr+", 120, subject, topic, Lilac, onAddStudy, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DurationButton(
    label: String,
    duration: String,
    minutes: Int,
    subject: String,
    topic: String,
    accent: Color,
    onAddStudy: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onAddStudy(subject, topic, minutes) },
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = .10f), contentColor = accent)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(duration, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun WalletScreen(logs: List<StudyLog>, modifier: Modifier = Modifier) {
    val total = logs.sumOf { it.reward }
    val groups = logs.sortedByDescending { it.timestamp }.groupBy { dayKey(it.timestamp) }.toList()
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("My wallet", color = Ink, fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Text("Every focused session adds up.", color = Muted, fontSize = 14.sp)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Ink),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("TOTAL EARNED", color = Color.White.copy(alpha = .7f), fontSize = 12.sp, letterSpacing = 1.sp)
                Text(formatMoney(total), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("${logs.size} study sessions logged", color = Color.White.copy(alpha = .7f), fontSize = 13.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Earning history", color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Text("Newest first", color = Muted, fontSize = 12.sp)
        }
        if (groups.isEmpty()) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Icon(Icons.Default.AccountBalanceWallet, null, tint = Blue, modifier = Modifier.size(30.dp))
                    Text("Your story starts with one session.", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("Log study time to see your earnings here.", color = Muted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groups.forEach { (day, dayLogs) ->
                    item(key = "header-$day") {
                        Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(dayLabel(day), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(formatMoney(dayLogs.sumOf { it.reward }), color = Teal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    items(dayLogs, key = { it.id }) { log ->
                        WalletEntry(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletEntry(log: StudyLog) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFEAF7F4)), contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(Icons.Default.CheckCircle, null, tint = Teal, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.topic, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${log.subject} • ${formatDuration(log.minutes)} • ${timeLabel(log.timestamp)}", color = Muted, fontSize = 11.sp)
            }
            Text("+${formatMoney(log.reward)}", color = Teal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

private object StudyLogStore {
    private const val PREFS = "shiksha_pulse"
    private const val LOGS = "study_logs"

    fun load(context: Context): List<StudyLog> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(LOGS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    StudyLog(
                        id = item.optString("id"),
                        subject = item.optString("subject"),
                        topic = item.optString("topic"),
                        minutes = item.optInt("minutes"),
                        reward = item.optDouble("reward"),
                        timestamp = item.optLong("timestamp")
                    )
                )
            }
        }
    }

    fun save(context: Context, logs: List<StudyLog>) {
        val array = JSONArray()
        logs.forEach { log ->
            array.put(
                JSONObject()
                    .put("id", log.id)
                    .put("subject", log.subject)
                    .put("topic", log.topic)
                    .put("minutes", log.minutes)
                    .put("reward", log.reward)
                    .put("timestamp", log.timestamp)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(LOGS, array.toString())
            .apply()
    }
}

private fun readSocialUsage(context: Context): UsageSnapshot {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    if (mode != AppOpsManager.MODE_ALLOWED) return UsageSnapshot(false, emptyList())

    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val end = System.currentTimeMillis()
    val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val usageByPackage = manager.queryAndAggregateUsageStats(start, end)
    val trackedApps = listOf(
        "com.instagram.android" to "Instagram",
        "com.facebook.katana" to "Facebook",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.google.android.youtube" to "YouTube",
        "com.snapchat.android" to "Snapchat",
        "com.twitter.android" to "X",
        "com.reddit.frontpage" to "Reddit"
    )
    val apps = trackedApps.mapNotNull { (packageName, label) ->
        val minutes = (usageByPackage[packageName]?.totalTimeInForeground ?: 0L).div(60_000).toInt()
        if (minutes > 0) AppUsage(label, minutes) else null
    }
    return UsageSnapshot(true, apps)
}

private fun isToday(timestamp: Long): Boolean = dayKey(timestamp) == dayKey(System.currentTimeMillis())

private fun dayKey(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))

private fun dayLabel(key: String): String {
    if (key == dayKey(System.currentTimeMillis())) return "Today"
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    if (key == SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yesterday)) return "Yesterday"
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key) ?: return key
    return SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(date)
}

private fun timeLabel(timestamp: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainder = minutes % 60
    return when {
        hours == 0 -> "${remainder}m"
        remainder == 0 -> "${hours}h"
        else -> "${hours}h ${remainder}m"
    }
}

private fun formatMoney(amount: Double): String = "₹" + String.format(Locale.US, "%.2f", amount)
