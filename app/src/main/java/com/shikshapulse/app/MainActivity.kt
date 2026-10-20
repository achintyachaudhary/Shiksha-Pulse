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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlin.random.Random

private val Ink = Color(0xFF18233D)
private val Muted = Color(0xFF7C879D)
private val Page = Color(0xFFF5F7FB)
private val Blue = Color(0xFF5575F7)
private val Teal = Color(0xFF20A990)
private val Amber = Color(0xFFFFB547)
private val Lilac = Color(0xFF8C6BE8)
private const val TEACHER_SESSION_PRICE = 100

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

private data class Teacher(
    val name: String,
    val subject: String,
    val available: Boolean
)

private val teachers = listOf(
    Teacher("Ashu Gupta", "Physics • NEET & JEE", true),
    Teacher("Aman Katiyar", "Mathematics • JEE", true),
    Teacher("Priya Thakur", "Biology • NEET", false),
    Teacher("Amisha Shukla", "Chemistry • NEET & JEE", true)
)

private data class TeacherBooking(
    val id: String,
    val teacherName: String,
    val subject: String,
    val accepted: Boolean,
    val timestamp: Long
)

private data class SocialUsageEvent(
    val id: String,
    val date: String,
    val packageName: String,
    val appName: String,
    val minutes: Int,
    val timestamp: Long
)

private data class TimelineEntry(
    val title: String,
    val detail: String,
    val timestamp: Long,
    val icon: ImageVector,
    val color: Color
)

private data class AppUsage(val packageName: String, val name: String, val minutes: Int)
private data class UsageSnapshot(val hasAccess: Boolean, val apps: List<AppUsage>) {
    val totalMinutes: Int get() = apps.sumOf { it.minutes }
}

private data class SocialApp(
    val packageName: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

private val trackedSocialApps = listOf(
    SocialApp("com.instagram.android", "Instagram", Icons.Default.PhotoCamera, Color(0xFFE1306C)),
    SocialApp("com.google.android.youtube", "YouTube", Icons.Default.SmartDisplay, Color(0xFFFF0000)),
    SocialApp("com.snapchat.android", "Snapchat", Icons.Default.Face, Color(0xFFB3A400)),
    SocialApp("com.whatsapp", "WhatsApp", Icons.Default.Call, Color(0xFF25A65A))
)

private enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    STUDY("Study", Icons.Default.MenuBook),
    TEACHERS("Teachers", Icons.Default.School),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {
    private var usageSnapshot by mutableStateOf(UsageSnapshot(false, emptyList()))
    private var logs by mutableStateOf<List<StudyLog>>(emptyList())
    private var usageHistory by mutableStateOf<Map<String, Map<String, Int>>>(emptyMap())
    private var usageEvents by mutableStateOf<List<SocialUsageEvent>>(emptyList())
    private var bookings by mutableStateOf<List<TeacherBooking>>(emptyList())
    private var tapCounts by mutableStateOf<Map<String, Int>>(emptyMap())
    private var studentName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logs = StudyLogStore.load(this)
        usageHistory = UsageHistoryStore.load(this)
        usageEvents = SocialUsageEventStore.load(this)
        bookings = TeacherBookingStore.load(this)
        tapCounts = StudyTapStore.load(this)
        studentName = StudentProfileStore.load(this)
        refreshUsage()
        setContent {
            ShikshaPulseApp(
                logs = logs,
                usage = usageSnapshot,
                usageHistory = usageHistory,
                usageEvents = usageEvents,
                bookings = bookings,
                tapCounts = tapCounts,
                studentName = studentName,
                onAddStudy = ::addStudy,
                onBookTeacher = ::bookTeacher,
                onRequestUsageAccess = ::openUsageAccessSettings,
                onSaveName = ::saveStudentName
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUsage()
    }

    private fun addStudy(subject: String, topic: String, minutes: Int) {
        val tapKey = studyTapKey(subject, topic, minutes)
        tapCounts = tapCounts + (tapKey to ((tapCounts[tapKey] ?: 0) + 1))
        StudyTapStore.save(this, tapCounts)

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

    private fun saveStudentName(name: String) {
        studentName = name.trim().ifEmpty { "Student" }
        StudentProfileStore.save(this, studentName)
    }

    private fun bookTeacher(teacher: Teacher) {
        val hasEnoughBalance = logs.sumOf { it.reward } -
            usageHistory.totalSocialMinutes() -
            bookings.filter { it.accepted }.size * TEACHER_SESSION_PRICE >= TEACHER_SESSION_PRICE
        if (!hasEnoughBalance) return

        val accepted = teacher.available && bookings.none { it.accepted && it.teacherName == teacher.name }
        val booking = TeacherBooking(
            id = UUID.randomUUID().toString(),
            teacherName = teacher.name,
            subject = teacher.subject,
            accepted = accepted,
            timestamp = System.currentTimeMillis()
        )
        bookings = listOf(booking) + bookings
        TeacherBookingStore.save(this, bookings)
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun refreshUsage() {
        usageSnapshot = readSocialUsage(this)
        if (usageSnapshot.hasAccess) {
            val today = dayKey(System.currentTimeMillis())
            val previous = usageHistory[today].orEmpty()
            val current = usageSnapshot.apps.associate { it.packageName to it.minutes }
            val now = System.currentTimeMillis()
            val newEvents = usageSnapshot.apps.mapNotNull { app ->
                val elapsed = (app.minutes - (previous[app.packageName] ?: 0)).coerceAtLeast(0)
                if (elapsed == 0) null else SocialUsageEvent(
                    id = UUID.randomUUID().toString(),
                    date = today,
                    packageName = app.packageName,
                    appName = app.name,
                    minutes = elapsed,
                    timestamp = now
                )
            }
            if (newEvents.isNotEmpty()) {
                usageEvents = newEvents + usageEvents
                SocialUsageEventStore.save(this, usageEvents)
            }
            usageHistory = usageHistory + (today to current)
            UsageHistoryStore.save(this, usageHistory)
        }
    }
}

@Composable
private fun ShikshaPulseApp(
    logs: List<StudyLog>,
    usage: UsageSnapshot,
    usageHistory: Map<String, Map<String, Int>>,
    usageEvents: List<SocialUsageEvent>,
    bookings: List<TeacherBooking>,
    tapCounts: Map<String, Int>,
    studentName: String,
    onAddStudy: (String, String, Int) -> Unit,
    onBookTeacher: (Teacher) -> Unit,
    onRequestUsageAccess: () -> Unit,
    onSaveName: (String) -> Unit
) {
    var selectedTab by androidx.compose.runtime.remember { mutableStateOf(AppTab.HOME) }
    var nameDraft by androidx.compose.runtime.remember { mutableStateOf("") }
    val greeting = androidx.compose.runtime.remember(studentName) {
        listOf(
            "You’re doing great today!",
            "Ready to make today count?",
            "A little progress goes a long way!",
            "Keep that brilliant momentum going!",
            "One focused session at a time!"
        ).random(Random(System.currentTimeMillis()))
    }
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
                    usageHistory = usageHistory,
                    usageEvents = usageEvents,
                    bookings = bookings,
                    studentName = studentName,
                    greeting = greeting,
                    onRequestUsageAccess = onRequestUsageAccess,
                    modifier = Modifier.padding(insets)
                )
                AppTab.STUDY -> StudyScreen(
                    tapCounts = tapCounts,
                    onAddStudy = onAddStudy,
                    modifier = Modifier.padding(insets)
                )
                AppTab.TEACHERS -> TeachersScreen(
                    bookings = bookings,
                    balance = logs.sumOf { it.reward } -
                        usageHistory.totalSocialMinutes() -
                        bookings.filter { it.accepted }.size * TEACHER_SESSION_PRICE,
                    onBookTeacher = onBookTeacher,
                    modifier = Modifier.padding(insets)
                )
                AppTab.WALLET -> WalletScreen(logs, usageHistory, bookings, Modifier.padding(insets))
            }
        }
    }
    if (studentName.isBlank()) {
        AlertDialog(
            onDismissRequest = { onSaveName("Student") },
            title = { Text("Welcome to Shiksha Pulse") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("What should we call you?")
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        singleLine = true,
                        placeholder = { Text("Your name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onSaveName(nameDraft) }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { onSaveName("Student") }) { Text("Skip") }
            }
        )
    }
}

@Composable
private fun DashboardScreen(
    logs: List<StudyLog>,
    usage: UsageSnapshot,
    usageHistory: Map<String, Map<String, Int>>,
    usageEvents: List<SocialUsageEvent>,
    bookings: List<TeacherBooking>,
    studentName: String,
    greeting: String,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLogs = logs.filter { isToday(it.timestamp) }
    val todayMinutes = todayLogs.sumOf { it.minutes }
    val todaySocialMinutes = usage.totalMinutes
    val netBalance = logs.sumOf { it.reward } -
        usageHistory.totalSocialMinutes() -
        bookings.filter { it.accepted }.size * TEACHER_SESSION_PRICE
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SHIKSHA PULSE", color = Blue, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.4.sp)
                Text("Hi $studentName,", color = Ink, fontWeight = FontWeight.Bold, fontSize = 25.sp)
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8ECFF)),
                contentAlignment = Alignment.Center
            ) {
                Text("SP", color = Blue, fontWeight = FontWeight.Bold)
            }
        }
        Text(greeting, color = Muted, fontSize = 14.sp)

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
                title = "Wallet balance",
                value = formatMoney(netBalance),
                caption = "after social and session costs",
                icon = Icons.Default.AccountBalanceWallet,
                accent = Teal,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Social spend today",
                value = if (usage.hasAccess) formatMoney(todaySocialMinutes.toDouble()) else "Not linked",
                caption = if (usage.hasAccess) "₹1 per tracked minute" else "connect to track and deduct",
                icon = Icons.Default.MoreTime,
                accent = Lilac,
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
                        Text("App time & wallet deductions", fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                        Text(
                            if (usage.hasAccess) "₹1 is deducted for every minute used" else "Allow Usage Access to track app time",
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
                trackedSocialApps.forEach { spec ->
                    val appUsage = usage.apps.firstOrNull { it.packageName == spec.packageName }
                    SocialUsageRow(
                        app = spec,
                        minutes = appUsage?.minutes ?: 0,
                        hasAccess = usage.hasAccess
                    )
                }
            }
        }

        TimelineCard(logs = logs, usageEvents = usageEvents, bookings = bookings)

        LeaderboardCard(logs = logs, studentName = studentName)

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
                    Text("₹5 per study hour; social apps deduct ₹1 per minute.", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TimelineCard(
    logs: List<StudyLog>,
    usageEvents: List<SocialUsageEvent>,
    bookings: List<TeacherBooking>
) {
    val today = dayKey(System.currentTimeMillis())
    val entries = buildList {
        logs.filter { isToday(it.timestamp) }.forEach { log ->
            add(
                TimelineEntry(
                    title = "Studied ${log.topic}",
                    detail = "${log.subject} • ${formatDuration(log.minutes)}",
                    timestamp = log.timestamp,
                    icon = Icons.Default.MenuBook,
                    color = Blue
                )
            )
        }
        usageEvents.filter { it.date == today }.forEach { event ->
            add(
                TimelineEntry(
                    title = "${event.appName} used",
                    detail = "${formatDuration(event.minutes)} app time • Reels aren’t tracked separately",
                    timestamp = event.timestamp,
                    icon = trackedSocialApps.firstOrNull { it.packageName == event.packageName }?.icon
                        ?: Icons.Default.MoreTime,
                    color = trackedSocialApps.firstOrNull { it.packageName == event.packageName }?.color
                        ?: Lilac
                )
            )
        }
        bookings.filter { isToday(it.timestamp) }.forEach { booking ->
            add(
                TimelineEntry(
                    title = if (booking.accepted) "Session booked with ${booking.teacherName}" else "Request declined by ${booking.teacherName}",
                    detail = if (booking.accepted) "₹$TEACHER_SESSION_PRICE • ${booking.subject}" else "Teacher unavailable • No charge",
                    timestamp = booking.timestamp,
                    icon = Icons.Default.School,
                    color = if (booking.accepted) Teal else Color(0xFFD94B55)
                )
            )
        }
    }.sortedByDescending { it.timestamp }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Today's timeline", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Your study sessions and app activity", color = Muted, fontSize = 11.sp)
                }
                androidx.compose.material3.Icon(Icons.Default.Schedule, null, tint = Blue)
            }
            if (entries.isEmpty()) {
                Text("Your activity will show up here as you go.", color = Muted, fontSize = 12.sp)
            } else {
                entries.take(12).forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(entry.color.copy(alpha = .12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(entry.icon, null, tint = entry.color, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(entry.detail, color = Muted, fontSize = 10.sp)
                        }
                        Text(timeLabel(entry.timestamp), color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeachersScreen(
    bookings: List<TeacherBooking>,
    balance: Double,
    onBookTeacher: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = balance >= TEACHER_SESSION_PRICE
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Book a teacher", color = Ink, fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Text("Request a one-to-one session with a teacher.", color = Muted, fontSize = 14.sp)
        Text("Demo availability: available requests are accepted; busy requests are declined.", color = Muted, fontSize = 12.sp)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your wallet balance", color = Muted, fontSize = 12.sp)
                    Text(formatMoney(balance), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text("Session ₹$TEACHER_SESSION_PRICE", color = Blue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        if (!canAfford) {
            Text("You need at least ₹$TEACHER_SESSION_PRICE in your wallet to request a session.", color = Color(0xFFD94B55), fontSize = 12.sp)
        }
        Text("TEACHERS", color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(teachers) { teacher ->
                val alreadyBooked = bookings.any { it.accepted && it.teacherName == teacher.name }
                val available = teacher.available && !alreadyBooked
                TeacherCard(
                    teacher = teacher,
                    available = available,
                    canAfford = canAfford,
                    onBook = { onBookTeacher(teacher) }
                )
            }
        }
    }
}

@Composable
private fun TeacherCard(
    teacher: Teacher,
    available: Boolean,
    canAfford: Boolean,
    onBook: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8ECFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(teacher.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""), color = Blue, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(teacher.name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(teacher.subject, color = Muted, fontSize = 11.sp)
                }
                Text(
                    if (available) "Available" else "Busy",
                    color = if (available) Teal else Color(0xFFD94B55),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onBook,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    when {
                        !canAfford -> "Need ₹$TEACHER_SESSION_PRICE to request"
                        available -> "Request session • ₹$TEACHER_SESSION_PRICE if accepted"
                        else -> "Request while busy • no charge if declined"
                    }
                )
            }
        }
    }
}

@Composable
private fun SocialUsageRow(app: SocialApp, minutes: Int, hasAccess: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(app.color.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(app.icon, contentDescription = app.name, tint = app.color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(app.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (!hasAccess) {
            Text("—", color = Muted, fontSize = 13.sp)
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDuration(minutes), color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("-${formatMoney(minutes.toDouble())}", color = Color(0xFFD94B55), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun LeaderboardCard(logs: List<StudyLog>, studentName: String) {
    val currentMonthMinutes = monthStudyMinutes(logs)
    val rankings = listOf(
        "Aarav Sharma" to 2_520,
        "Ananya Patel" to 2_340,
        "Ishaan Verma" to 2_160,
        "Diya Nair" to 1_980,
        "Kabir Singh" to 1_800,
        studentName to currentMonthMinutes
    ).sortedByDescending { it.second }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Monthly leaderboard", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Sample students • this month’s study time", color = Muted, fontSize = 11.sp)
                }
                androidx.compose.material3.Icon(Icons.Default.AutoGraph, null, tint = Blue)
            }
            rankings.forEachIndexed { index, (name, minutes) ->
                val isCurrentStudent = name == studentName
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrentStudent) Color(0xFFEAF0FF) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#${index + 1}", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(30.dp))
                    Text(
                        if (isCurrentStudent) "$name (you)" else name,
                        color = Ink,
                        fontWeight = if (isCurrentStudent) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(formatDuration(minutes), color = Muted, fontSize = 11.sp)
                }
            }
        }
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
    tapCounts: Map<String, Int>,
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
                TopicCard(topic = topic, subject = selectedSubject, tapCounts = tapCounts, onAddStudy = onAddStudy)
            }
        }
    }
}

@Composable
private fun TopicCard(
    topic: String,
    subject: String,
    tapCounts: Map<String, Int>,
    onAddStudy: (String, String, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(topic, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                DurationButton("Low", "30 min", 30, subject, topic, Teal, tapCounts[studyTapKey(subject, topic, 30)] ?: 0, onAddStudy, Modifier.weight(1f))
                DurationButton("Medium", "1 hr", 60, subject, topic, Blue, tapCounts[studyTapKey(subject, topic, 60)] ?: 0, onAddStudy, Modifier.weight(1f))
                DurationButton("High", "2 hr+", 120, subject, topic, Lilac, tapCounts[studyTapKey(subject, topic, 120)] ?: 0, onAddStudy, Modifier.weight(1f))
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
    clickCount: Int,
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (clickCount > 0) {
                    Box(
                        Modifier.size(17.dp).clip(CircleShape).background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(clickCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(duration, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun WalletScreen(
    logs: List<StudyLog>,
    usageHistory: Map<String, Map<String, Int>>,
    bookings: List<TeacherBooking>,
    modifier: Modifier = Modifier
) {
    val total = logs.sumOf { it.reward } -
        usageHistory.totalSocialMinutes() -
        bookings.filter { it.accepted }.size * TEACHER_SESSION_PRICE
    val logsByDay = logs.sortedByDescending { it.timestamp }.groupBy { dayKey(it.timestamp) }
    val bookingsByDay = bookings.filter { it.accepted }.groupBy { dayKey(it.timestamp) }
    val days = (logsByDay.keys + usageHistory.keys + bookingsByDay.keys).distinct().sortedDescending()
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
                Text("TOTAL BALANCE", color = Color.White.copy(alpha = .7f), fontSize = 12.sp, letterSpacing = 1.sp)
                Text(formatMoney(total), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${formatMoney(logs.sumOf { it.reward })} study − ${formatMoney(usageHistory.totalSocialMinutes().toDouble())} social − ${formatMoney((bookings.count { it.accepted } * TEACHER_SESSION_PRICE).toDouble())} sessions",
                    color = Color.White.copy(alpha = .7f),
                    fontSize = 12.sp
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Earning history", color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Text("Newest first", color = Muted, fontSize = 12.sp)
        }
        if (days.isEmpty()) {
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
                days.forEach { day ->
                    val dayLogs = logsByDay[day].orEmpty()
                    val socialMinutes = usageHistory[day].orEmpty().values.sum()
                    val dayBookings = bookingsByDay[day].orEmpty()
                    val bookingTotal = dayBookings.size * TEACHER_SESSION_PRICE
                    val dayNet = dayLogs.sumOf { it.reward } - socialMinutes - bookingTotal
                    item(key = "header-$day") {
                        Column(
                            Modifier.fillMaxWidth().padding(top = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dayLabel(day), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(
                                    formatMoney(dayNet),
                                    color = if (dayNet >= 0) Teal else Color(0xFFD94B55),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                "Study +${formatMoney(dayLogs.sumOf { it.reward })}  •  Social −${formatMoney(socialMinutes.toDouble())}  •  Sessions −${formatMoney(bookingTotal.toDouble())}",
                                color = Muted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    items(dayLogs, key = { it.id }) { log ->
                        WalletEntry(log)
                    }
                    if (socialMinutes > 0) {
                        item(key = "social-$day") {
                            SocialDeductionEntry(socialMinutes)
                        }
                    }
                    items(dayBookings, key = { "booking-${it.id}" }) { booking ->
                        TeacherBookingEntry(booking)
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialDeductionEntry(minutes: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFFFECEE)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(Icons.Default.MoreTime, null, tint = Color(0xFFD94B55), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Social app usage", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("$minutes minutes • ₹1 per minute", color = Muted, fontSize = 11.sp)
            }
            Text("−${formatMoney(minutes.toDouble())}", color = Color(0xFFD94B55), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TeacherBookingEntry(booking: TeacherBooking) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFE8ECFF)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(Icons.Default.School, null, tint = Blue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Session with ${booking.teacherName}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("${booking.subject} • ${timeLabel(booking.timestamp)}", color = Muted, fontSize = 11.sp)
            }
            Text(
                if (booking.accepted) "−${formatMoney(TEACHER_SESSION_PRICE.toDouble())}" else "Declined",
                color = if (booking.accepted) Color(0xFFD94B55) else Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
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

private object UsageHistoryStore {
    private const val PREFS = "shiksha_pulse"
    private const val USAGE_HISTORY = "social_usage_history"

    fun load(context: Context): Map<String, Map<String, Int>> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(USAGE_HISTORY, "{}") ?: "{}"
        val root = JSONObject(raw)
        return buildMap {
            val dates = root.keys()
            while (dates.hasNext()) {
                val date = dates.next()
                val apps = root.optJSONObject(date) ?: continue
                val appUsage = buildMap {
                    val packages = apps.keys()
                    while (packages.hasNext()) {
                        val packageName = packages.next()
                        put(packageName, apps.optInt(packageName).coerceAtLeast(0))
                    }
                }
                put(date, appUsage)
            }
        }
    }

    fun save(context: Context, history: Map<String, Map<String, Int>>) {
        val root = JSONObject()
        history.forEach { (date, apps) ->
            val usage = JSONObject()
            apps.forEach { (packageName, minutes) -> usage.put(packageName, minutes) }
            root.put(date, usage)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(USAGE_HISTORY, root.toString())
            .apply()
    }
}

private object SocialUsageEventStore {
    private const val PREFS = "shiksha_pulse"
    private const val USAGE_EVENTS = "social_usage_events"

    fun load(context: Context): List<SocialUsageEvent> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(USAGE_EVENTS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    SocialUsageEvent(
                        id = item.optString("id"),
                        date = item.optString("date"),
                        packageName = item.optString("packageName"),
                        appName = item.optString("appName"),
                        minutes = item.optInt("minutes").coerceAtLeast(0),
                        timestamp = item.optLong("timestamp")
                    )
                )
            }
        }
    }

    fun save(context: Context, events: List<SocialUsageEvent>) {
        val array = JSONArray()
        events.forEach { event ->
            array.put(
                JSONObject()
                    .put("id", event.id)
                    .put("date", event.date)
                    .put("packageName", event.packageName)
                    .put("appName", event.appName)
                    .put("minutes", event.minutes)
                    .put("timestamp", event.timestamp)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(USAGE_EVENTS, array.toString())
            .apply()
    }
}

private object TeacherBookingStore {
    private const val PREFS = "shiksha_pulse"
    private const val BOOKINGS = "teacher_bookings"

    fun load(context: Context): List<TeacherBooking> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(BOOKINGS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    TeacherBooking(
                        id = item.optString("id"),
                        teacherName = item.optString("teacherName"),
                        subject = item.optString("subject"),
                        accepted = item.optBoolean("accepted"),
                        timestamp = item.optLong("timestamp")
                    )
                )
            }
        }
    }

    fun save(context: Context, bookings: List<TeacherBooking>) {
        val array = JSONArray()
        bookings.forEach { booking ->
            array.put(
                JSONObject()
                    .put("id", booking.id)
                    .put("teacherName", booking.teacherName)
                    .put("subject", booking.subject)
                    .put("accepted", booking.accepted)
                    .put("timestamp", booking.timestamp)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(BOOKINGS, array.toString())
            .apply()
    }
}

private object StudyTapStore {
    private const val PREFS = "shiksha_pulse"
    private const val TAP_COUNTS = "study_tap_counts"

    fun load(context: Context): Map<String, Int> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(TAP_COUNTS, "{}") ?: "{}"
        val json = JSONObject(raw)
        return buildMap {
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, json.optInt(key).coerceAtLeast(0))
            }
        }
    }

    fun save(context: Context, counts: Map<String, Int>) {
        val json = JSONObject()
        counts.forEach { (key, count) -> json.put(key, count) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(TAP_COUNTS, json.toString())
            .apply()
    }
}

private object StudentProfileStore {
    private const val PREFS = "shiksha_pulse"
    private const val STUDENT_NAME = "student_name"

    fun load(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(STUDENT_NAME, "")
            .orEmpty()

    fun save(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(STUDENT_NAME, name)
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
    val apps = trackedSocialApps.map { app ->
        val minutes = (usageByPackage[app.packageName]?.totalTimeInForeground ?: 0L).div(60_000).toInt()
        AppUsage(app.packageName, app.name, minutes)
    }
    return UsageSnapshot(true, apps)
}

private fun Map<String, Map<String, Int>>.totalSocialMinutes(): Int =
    values.sumOf { dailyUsage -> dailyUsage.values.sum() }

private fun studyTapKey(subject: String, topic: String, minutes: Int): String =
    "$subject|$topic|$minutes"

private fun monthStudyMinutes(logs: List<StudyLog>): Int {
    val now = Calendar.getInstance()
    val monthStart = now.apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return logs.filter { it.timestamp >= monthStart }.sumOf { it.minutes }
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

private fun formatMoney(amount: Double): String {
    val sign = if (amount < 0) "−" else ""
    return sign + "₹" + String.format(Locale.US, "%.2f", kotlin.math.abs(amount))
}
