package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AccountType
import com.example.model.AuthMode
import com.example.ui.SiraViewModel
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary

@Composable
fun AuthDialog(
    viewModel: SiraViewModel,
    onDismiss: () -> Unit
) {
    val initialMode by viewModel.authDialogMode.collectAsState()
    var currentMode by remember { mutableStateOf(initialMode) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("auth_dialog_surface")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Ethiopian Emblem & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EthioEmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ስራ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SiraHub Ethiopia",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                EthiopianFlagEmblem()
                            }
                            Text(
                                text = if (currentMode == AuthMode.REGISTER) "Create your Ethiopian work profile" else "Welcome back to SiraHub",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_auth_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs: Register vs Login
                TabRow(
                    selectedTabIndex = if (currentMode == AuthMode.REGISTER) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    indicator = { tabPositions ->
                        val index = if (currentMode == AuthMode.REGISTER) 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = EthioEmeraldPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("auth_mode_tabs")
                ) {
                    Tab(
                        selected = currentMode == AuthMode.REGISTER,
                        onClick = { currentMode = AuthMode.REGISTER },
                        text = {
                            Text(
                                text = "Register (መመዝገቢያ)",
                                fontWeight = if (currentMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentMode == AuthMode.REGISTER) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_auth_register")
                    )
                    Tab(
                        selected = currentMode == AuthMode.LOGIN,
                        onClick = { currentMode = AuthMode.LOGIN },
                        text = {
                            Text(
                                text = "Sign In (ግባ)",
                                fontWeight = if (currentMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentMode == AuthMode.LOGIN) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_auth_login")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (currentMode == AuthMode.REGISTER) {
                    RegistrationForm(
                        viewModel = viewModel,
                        onSwitchToLogin = { currentMode = AuthMode.LOGIN },
                        onSuccess = onDismiss
                    )
                } else {
                    LoginForm(
                        viewModel = viewModel,
                        onSwitchToRegister = { currentMode = AuthMode.REGISTER },
                        onSuccess = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationForm(
    viewModel: SiraViewModel,
    onSwitchToLogin: () -> Unit,
    onSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+251 9") }
    var selectedRole by remember { mutableStateOf(AccountType.JOB_SEEKER) }
    var companyName by remember { mutableStateOf("") }
    var companyIndustry by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Step 1: Who are you? Role Picker Cards
        Text(
            text = "1. Choose Your Account Type",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Select your primary role on SiraHub. You can switch between roles anytime in settings.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Role 1: Job Seeker
        RoleSelectionCard(
            role = AccountType.JOB_SEEKER,
            title = "Job Seeker (ስራ ፈላጊ)",
            subtitle = "Find jobs, bid on freelance gigs & build your Ethiopian digital CV",
            icon = Icons.Default.BusinessCenter,
            isSelected = selectedRole == AccountType.JOB_SEEKER,
            accentColor = EthioEmeraldPrimary,
            onClick = { selectedRole = AccountType.JOB_SEEKER }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role 2: Company / Employer
        RoleSelectionCard(
            role = AccountType.COMPANY_EMPLOYER,
            title = "Company / Employer (ድርጅት / ቀጣሪ)",
            subtitle = "Post job vacancies, review applicant CVs & hire top local talent",
            icon = Icons.Default.Business,
            isSelected = selectedRole == AccountType.COMPANY_EMPLOYER,
            accentColor = EthioGoldSecondary,
            onClick = { selectedRole = AccountType.COMPANY_EMPLOYER }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role 3: Gig Poster / Client
        RoleSelectionCard(
            role = AccountType.GIG_CLIENT,
            title = "Gig Poster / Client (የጊግ ቀጣሪ)",
            subtitle = "Post short-term freelance tasks, review bids & hire freelancers",
            icon = Icons.Default.FlashOn,
            isSelected = selectedRole == AccountType.GIG_CLIENT,
            accentColor = EthioCrimsonTertiary,
            onClick = { selectedRole = AccountType.GIG_CLIENT }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Step 2: Personal & Contact Information
        Text(
            text = "2. Account Credentials & Details",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; errorMessage = null },
            label = { Text("Full Name (ሙሉ ስም)") },
            placeholder = { Text("e.g. Abebech Gobena") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EthioEmeraldPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_full_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email Address") },
            placeholder = { Text("your.name@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EthioEmeraldPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password (የይለፍ ቃል)") },
            placeholder = { Text("At least 4 characters") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EthioEmeraldPrimary) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Phone Number
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; errorMessage = null },
            label = { Text("Phone Number (ስልክ ቁጥር)") },
            placeholder = { Text("+251 91 123 4567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EthioEmeraldPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
        )

        // Conditional Organization fields for Company Employer or Gig Client
        AnimatedVisibility(visible = selectedRole == AccountType.COMPANY_EMPLOYER) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it; errorMessage = null },
                    label = { Text("Company / Organization Name (የድርጅቱ ስም)") },
                    placeholder = { Text("e.g. Nile Tech Solutions PLC") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = EthioGoldSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_company_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = companyIndustry,
                    onValueChange = { companyIndustry = it },
                    label = { Text("Industry / Sector") },
                    placeholder = { Text("e.g. Software, Banking, Healthcare") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_company_industry_input")
                )
            }
        }

        AnimatedVisibility(visible = selectedRole == AccountType.GIG_CLIENT) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Brand / Business Name (Optional)") },
                    placeholder = { Text("e.g. Addis Studio or Personal Project") },
                    leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, tint = EthioCrimsonTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_gig_client_brand_input")
                )
            }
        }

        // Error message banner
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Submit Button
        Button(
            onClick = {
                val res = viewModel.register(
                    fullName = fullName,
                    email = email,
                    pass = password,
                    phone = phone,
                    accountType = selectedRole,
                    companyName = companyName,
                    companyIndustry = companyIndustry
                )
                if (res.first) {
                    onSuccess()
                } else {
                    errorMessage = res.second
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_registration_btn")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Complete Registration (ተመዝገብ)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Already have an account?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onSwitchToLogin, modifier = Modifier.testTag("switch_to_login_btn")) {
                Text(text = "Sign In", color = EthioEmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LoginForm(
    viewModel: SiraViewModel,
    onSwitchToRegister: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Enter your credentials to access your saved applications, bids, and profile.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email Address") },
            placeholder = { Text("abebe.kebede.dev@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EthioEmeraldPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("login_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EthioEmeraldPrimary) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sign In Button
        Button(
            onClick = {
                val res = viewModel.login(email, password)
                if (res.first) {
                    onSuccess()
                } else {
                    errorMessage = res.second
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_login_btn")
        ) {
            Text("Sign In (ግባ)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Demo Accounts Section
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quick Demo Profiles (Instant One-Tap Testing)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any role to immediately test the platform from that perspective:",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Demo 1: Job Seeker
                OutlinedButton(
                    onClick = {
                        viewModel.quickDemoLogin(AccountType.JOB_SEEKER)
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("demo_login_seeker_btn")
                ) {
                    Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = EthioEmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abebe Kebede (Job Seeker / Full CV)", fontSize = 12.sp, color = EthioEmeraldDark, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Demo 2: Company Employer
                OutlinedButton(
                    onClick = {
                        viewModel.quickDemoLogin(AccountType.COMPANY_EMPLOYER)
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("demo_login_company_btn")
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = EthioGoldSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EthioFintech Labs (Company Employer)", fontSize = 12.sp, color = EthioGoldDark, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Demo 3: Gig Client
                OutlinedButton(
                    onClick = {
                        viewModel.quickDemoLogin(AccountType.GIG_CLIENT)
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("demo_login_gig_btn")
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = EthioCrimsonTertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Selamawit Tadesse (Freelance Gig Client)", fontSize = 12.sp, color = EthioCrimsonTertiary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Don't have an account?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onSwitchToRegister, modifier = Modifier.testTag("switch_to_register_btn")) {
                Text(text = "Register Now", color = EthioEmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    role: AccountType,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_card_${role.name.lowercase()}")
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
                    selectedColor = accentColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("role_radio_${role.name.lowercase()}")
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
