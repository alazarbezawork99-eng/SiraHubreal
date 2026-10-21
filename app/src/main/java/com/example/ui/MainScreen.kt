package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccountType
import com.example.model.AuthUser
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AuthDialog
import com.example.ui.screens.CompanyScreen
import com.example.ui.screens.GigsScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.PostScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SupabaseSettingsDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary


data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SiraViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val accountType by viewModel.accountType.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val companyApplicants by viewModel.companyApplicants.collectAsState()
    val bids by viewModel.bids.collectAsState()
    val showSupabaseSettings by viewModel.showSupabaseSettings.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val showSettingsScreen by viewModel.showSettingsScreen.collectAsState()
    val showWelcomeScreen by viewModel.showWelcomeScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button: if welcome screen is open, allow explore as guest; if settings is open, close it; else if not on Home/Jobs tab (tab 0), navigate back to Home
    BackHandler(enabled = showWelcomeScreen || showSettingsScreen || currentTab != 0) {
        if (showWelcomeScreen) {
            viewModel.exploreAsGuest()
        } else if (showSettingsScreen) {
            viewModel.closeSettingsScreen()
        } else {
            viewModel.setTab(0)
        }
    }

    // If Welcome Greeting Screen is open, greet user with onboarding & registration
    if (showWelcomeScreen) {
        WelcomeScreen(
            viewModel = viewModel,
            onExploreAsGuest = { viewModel.exploreAsGuest() }
        )
    } else if (showSettingsScreen) {
        SettingsScreen(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSettingsScreen() }
        )
    } else {
        val isCompany = accountType == AccountType.COMPANY_EMPLOYER
        val isGigClient = accountType == AccountType.GIG_CLIENT

        val navItems = listOf(
            NavItem(
                if (isCompany) "Vacancies" else "Jobs",
                Icons.Default.BusinessCenter,
                Icons.Default.BusinessCenter,
                "nav_jobs"
            ),
            NavItem(
                "Gigs & Tasks",
                Icons.Default.FlashOn,
                Icons.Default.FlashOn,
                "nav_gigs"
            ),
            NavItem(
                if (isCompany) "Post Job" else if (isGigClient) "Post Gig" else "Post",
                Icons.Default.AddCircle,
                Icons.Default.PostAdd,
                "nav_post"
            ),
            NavItem(
                if (isCompany) "Applicants" else if (isGigClient) "Gig Bids" else "Activity",
                if (isCompany) Icons.Default.Groups else Icons.AutoMirrored.Filled.Assignment,
                if (isCompany) Icons.Default.Groups else Icons.Default.WorkHistory,
                "nav_activity"
            ),
            NavItem(
                if (isCompany) "Company" else if (isGigClient) "Client Profile" else "My CV",
                if (isCompany) Icons.Default.Business else Icons.Default.AccountCircle,
                if (isCompany) Icons.Default.Business else Icons.Default.Person,
                "nav_profile"
            )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SiraTopAppBar(
                    accountType = accountType,
                    currentUser = currentUser,
                    onToggleAccountType = { viewModel.toggleAccountType() },
                    onOpenDatabaseSettings = { viewModel.setShowSupabaseSettings(true) },
                    onOpenSettings = { viewModel.openSettingsScreen() },
                    onOpenAuth = { viewModel.openAuthDialog(com.example.model.AuthMode.LOGIN) },
                    onOpenWelcome = { viewModel.openWelcomeScreen() },
                    isSupabaseConnected = viewModel.supabaseManager.isConfigured,
                    isSyncing = isSyncing,
                    onQuickSync = { viewModel.syncWithSupabase(showSnackbar = true) }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    val activeColor = when (accountType) {
                        AccountType.JOB_SEEKER -> EthioEmeraldPrimary
                        AccountType.COMPANY_EMPLOYER -> EthioGoldSecondary
                        AccountType.GIG_CLIENT -> EthioCrimsonTertiary
                    }
                    val activeDarkColor = when (accountType) {
                        AccountType.JOB_SEEKER -> EthioEmeraldDark
                        AccountType.COMPANY_EMPLOYER -> EthioGoldDark
                        AccountType.GIG_CLIENT -> EthioCrimsonTertiary
                    }

                    navItems.forEachIndexed { index, item ->
                        val isSelected = currentTab == index
                        val badgeCount = if (index == 3) {
                            if (isCompany) companyApplicants.size else (applications.size + bids.size)
                        } else 0

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(index) },
                            icon = {
                                if (badgeCount > 0) {
                                    BadgedBox(badge = {
                                        Badge(
                                            containerColor = activeColor,
                                            contentColor = Color.White
                                        ) {
                                            Text("$badgeCount", fontSize = 10.sp)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeDarkColor,
                                indicatorColor = activeColor.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> JobsScreen(viewModel = viewModel)
                    1 -> GigsScreen(viewModel = viewModel)
                    2 -> PostScreen(viewModel = viewModel)
                    3 -> {
                        if (isCompany) {
                            CompanyScreen(viewModel = viewModel)
                        } else {
                            ActivityScreen(viewModel = viewModel)
                        }
                    }
                    4 -> {
                        if (isCompany) {
                            CompanyScreen(viewModel = viewModel)
                        } else {
                            ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    // Supabase Cloud Database Dialog
    if (showSupabaseSettings) {
        SupabaseSettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowSupabaseSettings(false) }
        )
    }

    // Registration and Login Dialog Modal
    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeAuthDialog() }
        )
    }
}

@Composable
fun SiraTopAppBar(
    accountType: AccountType,
    currentUser: AuthUser?,
    onToggleAccountType: () -> Unit,
    onOpenDatabaseSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenWelcome: () -> Unit = {},
    isSupabaseConnected: Boolean,
    isSyncing: Boolean = false,
    onQuickSync: () -> Unit = {}
) {
    val roleColor = when (accountType) {
        AccountType.JOB_SEEKER -> EthioEmeraldPrimary
        AccountType.COMPANY_EMPLOYER -> EthioGoldSecondary
        AccountType.GIG_CLIENT -> EthioCrimsonTertiary
    }
    val roleDarkColor = when (accountType) {
        AccountType.JOB_SEEKER -> EthioEmeraldDark
        AccountType.COMPANY_EMPLOYER -> EthioGoldDark
        AccountType.GIG_CLIENT -> EthioCrimsonTertiary
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Title + Amharic Emblem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onOpenSettings)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(roleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = accountType.amharicBadge,
                        fontSize = if (accountType.amharicBadge.length > 2) 9.sp else 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SiraHub",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        EthiopianFlagEmblem()
                    }
                    Text(
                        text = when (accountType) {
                            AccountType.JOB_SEEKER -> "Job Seeker • CV"
                            AccountType.COMPANY_EMPLOYER -> "Employer Portal"
                            AccountType.GIG_CLIENT -> "Gig Poster • Hiring"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Actions: Cloud Icon + Settings Gear + Role Pill + User Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                // If connected, show quick sync icon button
                if (isSupabaseConnected) {
                    IconButton(
                        onClick = onQuickSync,
                        enabled = !isSyncing,
                        modifier = Modifier.size(30.dp).testTag("quick_sync_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = EthioEmeraldPrimary,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync from Supabase",
                                tint = EthioEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Supabase Database Status Button
                IconButton(
                    onClick = onOpenDatabaseSettings,
                    modifier = Modifier.size(32.dp).testTag("top_supabase_btn")
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = if (isSupabaseConnected) Color(0xFF16A34A) else Color(0xFFD97706)
                            ) {
                                Text(if (isSyncing) "..." else if (isSupabaseConnected) "DB✓" else "Setup", fontSize = 8.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Supabase Database Settings",
                            tint = if (isSupabaseConnected) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Dedicated Settings Gear Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(32.dp).testTag("top_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Account Type Switcher Pill Button
                Surface(
                    color = roleColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onToggleAccountType)
                        .testTag("toggle_account_type_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Account Type",
                            tint = roleDarkColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = accountType.label.split("/")[0].trim(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = roleDarkColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // User Profile Avatar or Guest Sign In Button
                if (currentUser?.isLoggedIn != true) {
                    Surface(
                        color = EthioEmeraldPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenWelcome)
                            .testTag("top_open_welcome_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Sign In / Register",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(roleColor)
                            .clickable(onClick = onOpenSettings)
                            .testTag("top_user_avatar_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

