package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlin.random.Random
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class VacancyItem(
    val id: String,
    val universityName: String,
    val locationState: String,
    val city: String,
    val courseName: String,
    val stream: String,
    val remainingSeats: Int,
    val totalSeats: Int,
    val cutOffScore: Double,
    val scoreExamType: String,
    val normalizedScoreRequired: Double,
    val annualFeeInLakhs: Double,
    val isDigiLockerVerified: Boolean
)

data class ApplicationItem(
    val id: String,
    val universityName: String,
    val courseName: String,
    val status: String,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ArhamApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArhamApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStateFilter by remember { mutableStateOf("All") }
    var selectedStreamFilter by remember { mutableStateOf("All") }
    var userDigiLockerVerified by remember { mutableStateOf(false) }
    var userCuetScore by remember { mutableDoubleStateOf(94.5) }
    var activeClaimVacancy by remember { mutableStateOf<VacancyItem?>(null) }
    var selectedForComparison by remember { mutableStateOf(setOf<VacancyItem>()) }
    var showCompareSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("arham_prefs", android.content.Context.MODE_PRIVATE) }
    var favoriteVacancyIds by remember {
        mutableStateOf(sharedPrefs.getStringSet("favorite_vacancies", emptySet())?.toSet() ?: emptySet())
    }
    val firebaseAuthManager = remember { FirebaseAuthManager(context) }
    var firebaseUser by remember { mutableStateOf(firebaseAuthManager.auth.currentUser) }
    val scope = rememberCoroutineScope()

    val authManager = remember { DigiLockerAuthManager(context) }
    
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // In a real app we'd exchange the authorization code for a token here.
        // For the simulation, we'll mark it verified anyway.
        userDigiLockerVerified = true
        Toast.makeText(context, "DigiLocker Auth Success (Simulated)", Toast.LENGTH_SHORT).show()
    }

    val sampleVacancies = remember {
        listOf(
            VacancyItem(
                id = "vac_101",
                universityName = "Manipal Institute of Technology",
                locationState = "Karnataka",
                city = "Manipal",
                courseName = "B.Tech Computer Science Engineering",
                stream = "Engineering",
                remainingSeats = 3,
                totalSeats = 180,
                cutOffScore = 92.0,
                scoreExamType = "MET / CUET",
                normalizedScoreRequired = 91.5,
                annualFeeInLakhs = 4.2,
                isDigiLockerVerified = true
            ),
            VacancyItem(
                id = "vac_102",
                universityName = "SRM Institute of Science & Tech",
                locationState = "Tamil Nadu",
                city = "Kattankulathur",
                courseName = "B.Tech Artificial Intelligence & ML",
                stream = "Engineering",
                remainingSeats = 4,
                totalSeats = 240,
                cutOffScore = 88.5,
                scoreExamType = "JEE Main / CUET",
                normalizedScoreRequired = 88.0,
                annualFeeInLakhs = 3.8,
                isDigiLockerVerified = true
            ),
            VacancyItem(
                id = "vac_103",
                universityName = "Kalinga Institute of Industrial Tech (KIIT)",
                locationState = "Odisha",
                city = "Bhubaneswar",
                courseName = "B.Tech Electronics & Comm. Eng",
                stream = "Engineering",
                remainingSeats = 12,
                totalSeats = 150,
                cutOffScore = 85.0,
                scoreExamType = "CUET / KIITEE",
                normalizedScoreRequired = 84.0,
                annualFeeInLakhs = 3.5,
                isDigiLockerVerified = true
            ),
            VacancyItem(
                id = "vac_104",
                universityName = "Symbiosis International University",
                locationState = "Maharashtra",
                city = "Pune",
                courseName = "B.B.A Finance & International Business",
                stream = "Management",
                remainingSeats = 2,
                totalSeats = 120,
                cutOffScore = 93.0,
                scoreExamType = "SET / CUET UG",
                normalizedScoreRequired = 92.0,
                annualFeeInLakhs = 3.9,
                isDigiLockerVerified = true
            ),
            VacancyItem(
                id = "vac_105",
                universityName = "Kasturba Medical College (KMC)",
                locationState = "Karnataka",
                city = "Mangalore",
                courseName = "M.B.B.S (Management Clearing)",
                stream = "Medical",
                remainingSeats = 1,
                totalSeats = 250,
                cutOffScore = 96.5,
                scoreExamType = "NEET UG",
                normalizedScoreRequired = 96.0,
                annualFeeInLakhs = 14.5,
                isDigiLockerVerified = true
            ),
            VacancyItem(
                id = "vac_106",
                universityName = "Thapar Institute of Eng. & Tech",
                locationState = "Punjab",
                city = "Patiala",
                courseName = "B.Tech Data Science & Engineering",
                stream = "Engineering",
                remainingSeats = 8,
                totalSeats = 120,
                cutOffScore = 90.0,
                scoreExamType = "JEE Main / Board Score",
                normalizedScoreRequired = 89.5,
                annualFeeInLakhs = 4.1,
                isDigiLockerVerified = true
            )
        )
    }

    val statesList = listOf("All", "Karnataka", "Tamil Nadu", "Maharashtra", "Punjab", "Odisha")
    val streamsList = listOf("All", "Engineering", "Management", "Medical")

    val filteredVacancies = sampleVacancies.filter { vac ->
        val matchesSearch = vac.universityName.contains(searchQuery, ignoreCase = true) ||
                vac.courseName.contains(searchQuery, ignoreCase = true) ||
                vac.city.contains(searchQuery, ignoreCase = true)
        val matchesState = selectedStateFilter == "All" || vac.locationState.equals(selectedStateFilter, ignoreCase = true)
        val matchesStream = selectedStreamFilter == "All" || vac.stream.equals(selectedStreamFilter, ignoreCase = true)
        matchesSearch && matchesState && matchesStream
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("arham_scaffold"),
        floatingActionButton = {
            if (selectedTab == 0 && selectedForComparison.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCompareSheet = true },
                    icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, "Compare") },
                    text = { Text("Compare (${selectedForComparison.size}/3)") },
                    containerColor = ArhamPrimary,
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Arham",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF001D35)
                            )
                            Text(
                                "RIGHTFUL MERIT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0061A4),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userDigiLockerVerified = !userDigiLockerVerified
                            val status = if (userDigiLockerVerified) "DigiLocker Verified" else "Unverified"
                            Toast.makeText(context, "DigiLocker Status: $status", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("digilocker_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (userDigiLockerVerified) Icons.Filled.Verified else Icons.Outlined.Shield,
                            contentDescription = "DigiLocker Verification",
                            tint = if (userDigiLockerVerified) ArhamSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF7F9FC)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF2F5F9),
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Vacancies") },
                    label = { Text("Feed", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD1E4FF),
                        selectedIconColor = Color(0xFF001D35),
                        selectedTextColor = Color(0xFF001D35),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_vacancies")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD1E4FF),
                        selectedIconColor = Color(0xFF001D35),
                        selectedTextColor = Color(0xFF001D35),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD1E4FF),
                        selectedIconColor = Color(0xFF001D35),
                        selectedTextColor = Color(0xFF001D35),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFD1E4FF),
                        selectedIconColor = Color(0xFF001D35),
                        selectedTextColor = Color(0xFF001D35),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
        ) {
            when (selectedTab) {
                0 -> {
                    // LIVE VACANCY FEED SCREEN
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        // DigiLocker Status Header
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (userDigiLockerVerified)
                                    Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (userDigiLockerVerified) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (userDigiLockerVerified) ArhamSuccess else ArhamDanger,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (userDigiLockerVerified) "DigiLocker Scores Verified" else "DigiLocker Verification Pending",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (userDigiLockerVerified) Color(0xFF065F46) else Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = if (userDigiLockerVerified) "CUET: $userCuetScore percentile • 12th Board: 92.4%" else "Verify scores to unlock 1-click seat locks",
                                        fontSize = 11.sp,
                                        color = if (userDigiLockerVerified) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }
                                if (!userDigiLockerVerified) {
                                    Button(
                                        onClick = { 
                                            val intent = authManager.getAuthIntent()
                                            authLauncher.launch(intent)
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("verify_now_button")
                                    ) {
                                        Text("Verify via AppAuth", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Search Bar (Material 3 Pill Search Bar)
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search colleges or courses...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("search_bar")
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // State Filter Chips
                        Text("STATE FILTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(statesList) { state ->
                                val selected = selectedStateFilter == state
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) Color(0xFFD1E4FF) else Color.White,
                                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .clickable { selectedStateFilter = state }
                                        .testTag("filter_state_$state")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (selected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF001D35), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            state,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color(0xFF001D35) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        // Stream Filter Chips
                        Text("STREAM FILTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(streamsList) { stream ->
                                val selected = selectedStreamFilter == stream
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) Color(0xFFD1E4FF) else Color.White,
                                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .clickable { selectedStreamFilter = stream }
                                        .testTag("filter_stream_$stream")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (selected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF001D35), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            stream,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color(0xFF001D35) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Results Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LIVE VACANCY FEED (${filteredVacancies.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ArhamSuccess)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("REAL-TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArhamSuccess)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Vacancies List
                        if (filteredVacancies.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("No vacant seats found matching your filter.")
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("vacancies_list")
                            ) {
                                items(filteredVacancies, key = { it.id }) { item ->
                                    VacancyCardItem(
                                        vacancy = item,
                                        userScore = userCuetScore,
                                        isUserVerified = userDigiLockerVerified,
                                        onClaimClick = { activeClaimVacancy = item },
                                        isSelectedForCompare = selectedForComparison.contains(item),
                                        isFavorite = favoriteVacancyIds.contains(item.id),
                                        onToggleFavorite = { isFav ->
                                            val newFavorites = if (isFav) favoriteVacancyIds + item.id else favoriteVacancyIds - item.id
                                            favoriteVacancyIds = newFavorites
                                            sharedPrefs.edit().putStringSet("favorite_vacancies", newFavorites).apply() // Replaced by core-ktx if needed, but standard is fine
                                            
                                            val topic = "vacancy_${item.id}"
                                            if (isFav) {
                                                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) Toast.makeText(context, "Subscribed to ${item.courseName} alerts!", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) Toast.makeText(context, "Unsubscribed from alerts", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        },
                                        onToggleCompare = { isSelected ->
                                            if (isSelected) {
                                                if (selectedForComparison.size < 3) {
                                                    selectedForComparison = selectedForComparison + item
                                                } else {
                                                    Toast.makeText(context, "You can only compare up to 3 colleges", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                selectedForComparison = selectedForComparison - item
                                            }
                                        }
                                    )
                                }
                                item { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
                1 -> {
                    // USER PROFILE SCREEN
                    var userApplications by remember { mutableStateOf(emptyList<ApplicationItem>()) }
                    var isLoadingApplications by remember { mutableStateOf(false) }

                    LaunchedEffect(firebaseUser) {
                        if (firebaseUser != null) {
                            isLoadingApplications = true
                            try {
                                val snapshot = firebaseAuthManager.db.collection("users")
                                    .document(firebaseUser!!.uid)
                                    .collection("applications")
                                    .get()
                                    .await()
                                userApplications = snapshot.documents.mapNotNull { doc ->
                                    ApplicationItem(
                                        id = doc.id,
                                        universityName = doc.getString("universityName") ?: "",
                                        courseName = doc.getString("courseName") ?: "",
                                        status = doc.getString("status") ?: "Pending",
                                        date = doc.getString("date") ?: ""
                                    )
                                }
                            } catch (e: Exception) {
                                // Ignore or handle fetch error
                            } finally {
                                isLoadingApplications = false
                            }
                        } else {
                            userApplications = emptyList()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Header
                            if (firebaseUser == null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF64748B))
                                    Spacer(Modifier.height(16.dp))
                                    Text("Not signed in", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                if (firebaseAuthManager.signInWithGoogle()) {
                                                    firebaseUser = firebaseAuthManager.auth.currentUser
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Sign in with Google")
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFD1E4FF),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF001D35), modifier = Modifier.padding(16.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(firebaseUser?.displayName ?: "User", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF001D35))
                                        Text(firebaseUser?.email ?: "", fontSize = 14.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }

                        if (firebaseUser != null) {
                            item {
                                // DigiLocker Status Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically, 
                                            horizontalArrangement = Arrangement.SpaceBetween, 
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    if (userDigiLockerVerified) Icons.Default.Verified else Icons.Outlined.Shield, 
                                                    contentDescription = null, 
                                                    tint = if (userDigiLockerVerified) ArhamSuccess else ArhamDanger
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text("DigiLocker Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            if (userDigiLockerVerified) {
                                                Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(12.dp)) {
                                                    Text("Verified", color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            } else {
                                                Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(12.dp)) {
                                                    Text("Pending", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            }
                                        }
                                        if (!userDigiLockerVerified) {
                                            Spacer(Modifier.height(16.dp))
                                            Button(
                                                onClick = { 
                                                    val intent = authManager.getAuthIntent()
                                                    authLauncher.launch(intent)
                                                },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ArhamPrimary)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Connect DigiLocker (OAuth2)", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            Spacer(Modifier.height(12.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text("CUET Percentile", fontSize = 12.sp, color = Color(0xFF64748B))
                                                    Text("${"%.1f".format(userCuetScore)}%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF001D35))
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Board Score", fontSize = 12.sp, color = Color(0xFF64748B))
                                                    Text("92.4%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF001D35))
                                                }
                                            }
                                            Spacer(Modifier.height(16.dp))
                                            Text("Simulate CUET Percentile:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                            Slider(
                                                value = userCuetScore.toFloat(),
                                                onValueChange = { userCuetScore = it.toDouble() },
                                                valueRange = 70f..99.9f,
                                                steps = 299,
                                                modifier = Modifier.testTag("score_slider")
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                                Text("Application History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF001D35))
                                Spacer(Modifier.height(2.dp))
                            }

                            if (userApplications.isEmpty() && !isLoadingApplications) {
                                item {
                                    Text("No applications found in Firestore.", color = Color.Gray, modifier = Modifier.padding(8.dp))
                                }
                            } else if (isLoadingApplications) {
                                item {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            } else {
                                items(userApplications) { app ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween, 
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(app.universityName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                                val statusColor = if (app.status == "Payment Pending") Color(0xFFD97706) else Color(0xFFDC2626)
                                                val statusBg = if (app.status == "Payment Pending") Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                                                Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                                                    Text(app.status, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = statusColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(app.courseName, fontSize = 13.sp, color = Color(0xFF475569))
                                            Spacer(Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Applied on ${app.date}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                            
                                            StatusTimelineTracker(status = app.status)
                                        }
                                    }
                                }
                }
                    }
                }
                }
                2 -> {
                    // SETTINGS UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Settings",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001D35)
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = ArhamPrimary)
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Push Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Receive alerts for new vacancies", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Switch(
                                        checked = true,
                                        onCheckedChange = { /* Toggle */ }
                                    )
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = ArhamPrimary)
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Switch to dark theme", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Switch(
                                        checked = false,
                                        onCheckedChange = { /* Toggle */ }
                                    )
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = ArhamPrimary)
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Manage your data and privacy", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = { /* Sign out */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sign Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // Insights / Dashboard
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                "Vacancy Trends",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001D35)
                            )
                            Text(
                                "Last 24 hours seat availability by region",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        
                        item {
                            // Card with Line Chart
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(300.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                                    Text("Engineering Seats (Karnataka)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    val model = remember { entryModelOf(45f, 32f, 20f, 15f, 18f, 10f, 4f) }
                                    Chart(
                                        chart = lineChart(),
                                        model = model,
                                        startAxis = rememberStartAxis(),
                                        bottomAxis = rememberBottomAxis(),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        
                        item {
                            // Card with Column Chart
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(300.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                                    Text("Top Universities Demand", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    val columnModel = remember { entryModelOf(120f, 90f, 60f, 45f) }
                                    Chart(
                                        chart = columnChart(),
                                        model = columnModel,
                                        startAxis = rememberStartAxis(),
                                        bottomAxis = rememberBottomAxis(),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Compare Bottom Sheet
    if (showCompareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCompareSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Compare Colleges", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001D35))
                Spacer(Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(selectedForComparison.toList(), key = { it.id }) { vacancy ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(240.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(vacancy.universityName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("${vacancy.city}, ${vacancy.locationState}", fontSize = 12.sp, color = Color(0xFF64748B))
                                Spacer(Modifier.height(12.dp))
                                
                                Text("COURSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text(vacancy.courseName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                                Spacer(Modifier.height(12.dp))
                                
                                Text("FEE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text("₹${vacancy.annualFeeInLakhs} L/year", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                                Spacer(Modifier.height(12.dp))
                                
                                Text("ELIGIBILITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text("${vacancy.normalizedScoreRequired}% (${vacancy.scoreExamType})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF0061A4))
                                Spacer(Modifier.height(12.dp))
                                
                                Text("AVAILABLE SEATS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text("${vacancy.remainingSeats}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (vacancy.remainingSeats < 5) Color(0xFFDC2626) else Color(0xFF065F46))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Seat Claim Dialog
    if (activeClaimVacancy != null) {
        ModalBottomSheet(
            onDismissRequest = { activeClaimVacancy = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                activeClaimVacancy?.let { vacancy ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockClock, contentDescription = null, tint = ArhamAccent, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Atomic Seat Lock (10 Min)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "You are locking 1 seat in ${vacancy.courseName} at ${vacancy.universityName}.",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DigiLocker Score: ${userCuetScore} Percentile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Cutoff Score: ${vacancy.cutOffScore} Percentile", fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Status: ELIGIBLE VIA AUTOMATED NORMALIZATION", fontSize = 12.sp, color = ArhamSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Once locked via Upstash Redis concurrency queue, you will have 10 minutes to complete the Razorpay UPI clearing fee payment.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { activeClaimVacancy = null }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Seat locked! 10-minute payment timer started.", Toast.LENGTH_LONG).show()
                                activeClaimVacancy = null
                            },
                            modifier = Modifier.testTag("confirm_claim_seat")
                        ) {
                            Text("Proceed to Razorpay UPI")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}@Composable
fun VacancyCardItem(
    vacancy: VacancyItem,
    userScore: Double,
    isUserVerified: Boolean,
    onClaimClick: () -> Unit,
    isSelectedForCompare: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: (Boolean) -> Unit = {},
    onToggleCompare: (Boolean) -> Unit = {}
) {
    val isEligible = userScore >= vacancy.normalizedScoreRequired
    
    var currentSeats by remember { mutableIntStateOf(vacancy.remainingSeats) }
    
    // Simulate Supabase Realtime Subscription updates
    LaunchedEffect(vacancy.id) {
        kotlinx.coroutines.delay((2000L..10000L).random())
        while(currentSeats > 0) {
            currentSeats -= 1
            kotlinx.coroutines.delay((5000L..25000L).random())
        }
    }
    
    val isUrgentSeats = currentSeats < 5

    // Blinking animation for Realtime Indicator
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vacancy_card_${vacancy.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vacancy.universityName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF64748B)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${vacancy.city}, ${vacancy.locationState}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                // Eligibility / Verification Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "ELIGIBILITY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "${vacancy.normalizedScoreRequired}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001D35)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "COURSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = vacancy.courseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Annual Fee: ₹${vacancy.annualFeeInLakhs} L • Exam: ${vacancy.scoreExamType}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    // Real-Time Supabase Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = alpha))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Live Sync",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981).copy(alpha = alpha)
                        )
                    }
                    // Remaining Seats Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        if (isUrgentSeats) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "$currentSeats SEATS LEFT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        } else {
                            Text(
                                text = "$currentSeats Seats Available",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { onToggleFavorite(!isFavorite) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = "Subscribe to alerts",
                                tint = if (isFavorite) ArhamPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggleCompare(!isSelectedForCompare) }) {
                            Checkbox(
                                checked = isSelectedForCompare,
                                onCheckedChange = onToggleCompare,
                                colors = CheckboxDefaults.colors(checkedColor = ArhamPrimary)
                            )
                            Text("Compare", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onClaimClick,
                            enabled = isEligible && isUserVerified,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0061A4),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE2E8F0),
                                disabledContentColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("claim_seat_button_${vacancy.id}")
                        ) {
                            Text(
                                text = if (!isUserVerified) "Verify First" else if (!isEligible) "Ineligible" else "CLAIM SEAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigiLockerDocRow(title: String, subtitle: String, verified: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (verified) {
            Surface(
                color = Color(0xFFD1FAE5),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Verified",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}


@Composable
fun StatusTimelineTracker(status: String) {
    val steps = listOf("Applied", "Verification", "Allocation")
    
    val currentStepIndex = when {
        status.contains("Allocat", ignoreCase = true) || status.contains("Accept", ignoreCase = true) -> 2
        status.contains("Verif", ignoreCase = true) || status.contains("Pending", ignoreCase = true) -> 1
        else -> 0
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentStepIndex
                
                // Circle
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (isCompleted) ArhamPrimary else Color(0xFFE2E8F0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                // Connector line
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                color = if (index < currentStepIndex) ArhamPrimary else Color(0xFFE2E8F0)
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                val isCurrent = index == currentStepIndex
                val isCompleted = index <= currentStepIndex
                
                Text(
                    text = step,
                    fontSize = 10.sp,
                    color = if (isCurrent) Color(0xFF0F172A) else if (isCompleted) Color(0xFF475569) else Color(0xFF94A3B8),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        steps.size - 1 -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = when(index) {
                         0 -> Modifier.weight(1.2f).wrapContentWidth(Alignment.Start)
                         steps.size - 1 -> Modifier.weight(1.2f).wrapContentWidth(Alignment.End)
                         else -> Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
                    }
                )
            }
        }
    }
}
