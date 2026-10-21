package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccountType
import com.example.model.AuthMode
import com.example.ui.SiraViewModel
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * WelcomeScreen composable for SiraHub.
 *
 * Adheres strictly to Material 3 design principles:
 * - Prominent Ethiopian-themed SiraHub Branding Logo with Geez 'ሥራ' typography.
 * - Clear, concise value proposition highlighting verified employers, jobs & freelance gigs,
 *   and instant Telebirr & CBE mobile payments.
 * - Two distinct Material 3 action buttons: filled 'Sign Up' and outlined 'Login'.
 * - Interactive registration and login forms with full role differentiation (Job Seeker, Company, Gig Client).
 * - Fast 1-Tap demo personas and guest browsing capabilities.
 */
@Composable
fun WelcomeScreen(
    viewModel: SiraViewModel,
    onExploreAsGuest: () -> Unit = { viewModel.exploreAsGuest() },
    onSignUpClick: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Mode state: null means landing view with Sign Up & Login buttons;
    // AuthMode.REGISTER shows the registration form; AuthMode.LOGIN shows the login form.
    var activeAuthMode by remember { mutableStateOf<AuthMode?>(null) }

    // Registration Form State
    var selectedRole by remember { mutableStateOf(AccountType.JOB_SEEKER) }
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regCompanyName by remember { mutableStateOf("") }
    var regCompanyIndustry by remember { mutableStateOf("Tech & Software") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regErrorMessage by remember { mutableStateOf<String?>(null) }
    var isRegSubmitting by remember { mutableStateOf(false) }

    // Sign In Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoginSubmitting by remember { mutableStateOf(false) }

    // Handle back button: if form is open, return to landing view; otherwise explore as guest
    BackHandler {
        if (activeAuthMode != null) {
            activeAuthMode = null
        } else {
            onExploreAsGuest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // TOP HEADER BAR: FLAG + GUEST SKIP
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EthiopianFlagEmblem()
                    Text(
                        text = "SiraHub Ethiopia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EthioEmeraldPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onExploreAsGuest() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("welcome_skip_to_guest_btn")
                    ) {
                        Text(
                            text = "Explore as Guest",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EthioEmeraldPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Explore as Guest",
                            tint = EthioEmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 1. BRANDING LOGO (Material 3 with Geez Glyphs)
            // ==========================================
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EthioEmeraldPrimary, EthioEmeraldDark)
                        )
                    )
                    .border(BorderStroke(2.5.dp, EthioGoldDark), CircleShape)
                    .testTag("welcome_branding_logo")
            ) {
                Text(
                    text = "ሥራ",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EthioGoldSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Titles
            Text(
                text = "እንኳን ወደ ሥራ በደህና መጡ!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EthioEmeraldDark,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SiraHub",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EthioGoldSecondary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, EthioGoldDark.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "ኢትዮጵያ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioGoldDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // 2. BRIEF VALUE PROPOSITION FOR SIRAHUB
            // ==========================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_value_proposition")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ethiopia's premier employment network connecting ambitious talent with verified corporate careers and instant freelance gigs with secure Telebirr & CBE payments.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal
                    )

                    // 3 Feature Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ValueBadgeItem(
                            icon = Icons.Default.Security,
                            title = "Verified Jobs",
                            subtitle = "TIN & Trade Vetted",
                            color = EthioEmeraldPrimary
                        )
                        ValueBadgeItem(
                            icon = Icons.Default.FlashOn,
                            title = "Telebirr Pay",
                            subtitle = "Instant Escrow",
                            color = EthioGoldDark
                        )
                        ValueBadgeItem(
                            icon = Icons.Default.Business,
                            title = "Top Talent",
                            subtitle = "Addis & Remote",
                            color = EthioCrimsonTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. TWO DISTINCT 'SIGN UP' AND 'LOGIN' BUTTONS
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Filled M3 Button: Sign Up
                Button(
                    onClick = {
                        if (onSignUpClick != null) {
                            onSignUpClick()
                        } else {
                            activeAuthMode = AuthMode.REGISTER
                            regErrorMessage = null
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EthioEmeraldPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("welcome_sign_up_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign Up / ተመዝገብ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Secondary Outlined M3 Button: Login
                OutlinedButton(
                    onClick = {
                        if (onLoginClick != null) {
                            onLoginClick()
                        } else {
                            activeAuthMode = AuthMode.LOGIN
                            loginErrorMessage = null
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, EthioEmeraldPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EthioEmeraldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("welcome_login_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            tint = EthioEmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Login / ግባ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EthioEmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 4. INTERACTIVE AUTH FORMS (Shown when Sign Up or Login is clicked)
            // ==========================================
            AnimatedVisibility(
                visible = activeAuthMode != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Form Header with Toggle & Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (activeAuthMode == AuthMode.REGISTER) "Create SiraHub Account" else "Sign In to Your Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (activeAuthMode == AuthMode.REGISTER) "Choose your role & enter details" else "Enter your email & password",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { activeAuthMode = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Form",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (activeAuthMode == AuthMode.REGISTER) {
                            // ------------------------------------------
                            // REGISTRATION FORM
                            // ------------------------------------------
                            Text(
                                text = "1. Choose Your Account Type:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Role 1: Job Seeker
                            WelcomeRoleCard(
                                title = "Job Seeker (ሥራ ፈላጊ)",
                                badge = "For Careers",
                                description = "Find full-time & remote careers. Build and fill your own CV from scratch.",
                                icon = Icons.Default.School,
                                isSelected = selectedRole == AccountType.JOB_SEEKER,
                                activeColor = EthioEmeraldPrimary,
                                activeDark = EthioEmeraldDark,
                                testTag = "welcome_role_job_seeker",
                                radioTestTag = "role_radio_job_seeker",
                                onClick = { selectedRole = AccountType.JOB_SEEKER }
                            )

                            // Role 2: Company
                            WelcomeRoleCard(
                                title = "Company (ቀጣሪ ድርጅት)",
                                badge = "Hiring Talent",
                                description = "Post vacancies, review applications, and hire top Ethiopian professionals.",
                                icon = Icons.Default.Business,
                                isSelected = selectedRole == AccountType.COMPANY_EMPLOYER,
                                activeColor = EthioGoldSecondary,
                                activeDark = EthioGoldDark,
                                testTag = "welcome_role_company",
                                radioTestTag = "role_radio_company",
                                onClick = { selectedRole = AccountType.COMPANY_EMPLOYER }
                            )

                            // Role 3: Gig Provider
                            WelcomeRoleCard(
                                title = "Gig Provider (የጊግ አቅራቢ / ቀጣሪ)",
                                badge = "Freelance & Tasks",
                                description = "Post freelance tasks or gigs, receive competitive bids, and pay via Telebirr/CBE.",
                                icon = Icons.Default.FlashOn,
                                isSelected = selectedRole == AccountType.GIG_CLIENT,
                                activeColor = EthioCrimsonTertiary,
                                activeDark = EthioCrimsonTertiary,
                                testTag = "welcome_role_gig_provider",
                                radioTestTag = "role_radio_gig_provider",
                                onClick = { selectedRole = AccountType.GIG_CLIENT }
                            )

                            Text(
                                text = "2. Personal & Account Details:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Full Name
                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it; regErrorMessage = null },
                                label = {
                                    Text(if (selectedRole == AccountType.COMPANY_EMPLOYER) "Contact Person / Hiring Lead" else "Full Name (የሙሉ ስም)")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_reg_fullname")
                            )

                            // Company Name if Employer or Gig Client
                            AnimatedVisibility(visible = selectedRole == AccountType.COMPANY_EMPLOYER || selectedRole == AccountType.GIG_CLIENT) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = regCompanyName,
                                        onValueChange = { regCompanyName = it; regErrorMessage = null },
                                        label = { Text(if (selectedRole == AccountType.COMPANY_EMPLOYER) "Company / Organization Name" else "Client / Business Name") },
                                        placeholder = { Text(if (selectedRole == AccountType.COMPANY_EMPLOYER) "e.g. Awash Tech Labs" else "e.g. Kaffa Coffee") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Business, contentDescription = null, tint = EthioGoldDark)
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("welcome_reg_company_name")
                                    )

                                    val industries = listOf("Tech & Software", "Fintech", "Hospitality", "Media", "Health")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        industries.forEach { ind ->
                                            val isSel = regCompanyIndustry == ind
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSel) EthioGoldDark.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                                border = if (isSel) BorderStroke(1.dp, EthioGoldDark) else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { regCompanyIndustry = ind }
                                            ) {
                                                Text(
                                                    text = ind,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) EthioGoldDark else MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Email
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it; regErrorMessage = null },
                                label = { Text("Email Address (ኢሜይል)") },
                                placeholder = { Text("name@example.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_reg_email")
                            )

                            // Phone
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it; regErrorMessage = null },
                                label = { Text("Phone Number (+251...)") },
                                placeholder = { Text("+251 91 123 4567") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_reg_phone")
                            )

                            // Password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it; regErrorMessage = null },
                                label = { Text("Create Password (የይለፍ ቃል)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_reg_password")
                            )

                            // Confirm Password
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it; regErrorMessage = null },
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_reg_confirm_password")
                            )

                            // Error Display
                            regErrorMessage?.let { err ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = err,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // Submit Registration Button
                            Button(
                                onClick = {
                                    if (regFullName.isBlank()) {
                                        regErrorMessage = "Please enter your name"
                                        return@Button
                                    }
                                    if (regEmail.isBlank() || !regEmail.contains("@")) {
                                        regErrorMessage = "Please enter a valid email address"
                                        return@Button
                                    }
                                    if (regPassword.length < 4) {
                                        regErrorMessage = "Password must be at least 4 characters"
                                        return@Button
                                    }
                                    if (regPassword != regConfirmPassword) {
                                        regErrorMessage = "Passwords do not match"
                                        return@Button
                                    }
                                    if ((selectedRole == AccountType.COMPANY_EMPLOYER || selectedRole == AccountType.GIG_CLIENT) && regCompanyName.isBlank()) {
                                        regErrorMessage = "Please enter your organization or business name"
                                        return@Button
                                    }

                                    isRegSubmitting = true
                                    scope.launch {
                                        delay(300)
                                        val res = viewModel.register(
                                            fullName = regFullName,
                                            email = regEmail,
                                            pass = regPassword,
                                            phone = regPhone.ifBlank { "+251 91 123 4567" },
                                            accountType = selectedRole,
                                            companyName = regCompanyName,
                                            companyIndustry = regCompanyIndustry
                                        )
                                        isRegSubmitting = false
                                        if (!res.first) {
                                            regErrorMessage = res.second
                                        }
                                    }
                                },
                                enabled = !isRegSubmitting,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (selectedRole) {
                                        AccountType.JOB_SEEKER -> EthioEmeraldPrimary
                                        AccountType.COMPANY_EMPLOYER -> EthioGoldDark
                                        AccountType.GIG_CLIENT -> EthioCrimsonTertiary
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("welcome_submit_register_btn")
                            ) {
                                if (isRegSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Complete Registration (${selectedRole.label})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // ------------------------------------------
                            // LOGIN FORM
                            // ------------------------------------------
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it; loginErrorMessage = null },
                                label = { Text("Email Address (ኢሜይል)") },
                                placeholder = { Text("you@example.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_login_email")
                            )

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it; loginErrorMessage = null },
                                label = { Text("Password (የይለፍ ቃል)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = EthioEmeraldPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("welcome_login_password")
                            )

                            loginErrorMessage?.let { err ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = err,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (loginEmail.isBlank()) {
                                        loginErrorMessage = "Please enter your email"
                                        return@Button
                                    }
                                    if (loginPassword.length < 4) {
                                        loginErrorMessage = "Password must be at least 4 characters"
                                        return@Button
                                    }

                                    isLoginSubmitting = true
                                    scope.launch {
                                        delay(300)
                                        val res = viewModel.login(loginEmail, loginPassword)
                                        isLoginSubmitting = false
                                        if (!res.first) {
                                            loginErrorMessage = res.second
                                        }
                                    }
                                },
                                enabled = !isLoginSubmitting,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("welcome_submit_login_btn")
                            ) {
                                if (isLoginSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign In Now",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 5. 1-TAP DEMO TEST-DRIVE PERSONAS
            // ==========================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = EthioGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Instant 1-Tap Demo Access",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Experience SiraHub immediately as a verified persona:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DemoPersonaRow(
                        title = "Abebe Kebede",
                        subtitle = "Senior Android Engineer • Job Seeker",
                        roleBadge = "ሥራ ፈላጊ",
                        badgeColor = EthioEmeraldPrimary,
                        testTag = "welcome_demo_job_seeker",
                        onClick = { viewModel.quickDemoLogin(AccountType.JOB_SEEKER) }
                    )

                    DemoPersonaRow(
                        title = "EthioFintech Labs",
                        subtitle = "Company Employer • 5 Open Vacancies",
                        roleBadge = "ቀጣሪ ድርጅት",
                        badgeColor = EthioGoldDark,
                        testTag = "welcome_demo_company",
                        onClick = { viewModel.quickDemoLogin(AccountType.COMPANY_EMPLOYER) }
                    )

                    DemoPersonaRow(
                        title = "Kaffa Origin Roasters",
                        subtitle = "Gig Client / Poster • Addis Ababa",
                        roleBadge = "የጊግ ቀጣሪ",
                        badgeColor = EthioCrimsonTertiary,
                        testTag = "welcome_demo_gig_client",
                        onClick = { viewModel.quickDemoLogin(AccountType.GIG_CLIENT) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 6. EXPLORE AS GUEST TEXT BUTTON
            // ==========================================
            TextButton(
                onClick = onExploreAsGuest,
                modifier = Modifier.testTag("welcome_explore_guest_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = EthioEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Browse Jobs & Freelance Gigs as Guest (ያለ መለያ ቀጥል)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EthioEmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ValueBadgeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WelcomeRoleCard(
    title: String,
    badge: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    activeDark: Color,
    testTag: String,
    radioTestTag: String = "",
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeDark else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = activeDark,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = if (radioTestTag.isNotBlank()) Modifier.testTag(radioTestTag) else Modifier
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) activeDark else MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DemoPersonaRow(
    title: String,
    subtitle: String,
    roleBadge: String,
    badgeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = roleBadge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "1-Tap In",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
