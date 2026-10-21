package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExperienceItem
import com.example.model.UserProfile
import com.example.ui.SiraViewModel
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val showCvPreview by viewModel.showCvPreview.collectAsState()
    val showEditProfile by viewModel.showEditProfile.collectAsState()

    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showAddExperienceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_screen_list")
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Job Seeker Profile & CV",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your digital Ethiopian resume used when applying to jobs & gigs",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EthiopianFlagEmblem()
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circle
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(EthioEmeraldPrimary, EthioEmeraldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.fullName.take(2).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.titleHeadline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = EthioEmeraldPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = profile.cityLocation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setShowEditProfile(true) },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = EthioEmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = EthioEmeraldDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = profile.phone, fontSize = 11.sp)
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(13.dp), tint = EthioEmeraldDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = profile.email, fontSize = 11.sp)
                            }
                        }
                        if (profile.telegramHandle.isNotBlank()) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Telegram: ${profile.telegramHandle}", fontSize = 11.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CV Completion Meter
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "CV Completeness", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${profile.completionPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { profile.completionPercentage / 100f },
                            color = EthioEmeraldPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action: Preview Full CV Button
                    Button(
                        onClick = { viewModel.setShowCvPreview(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("preview_cv_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Preview & Share Full CV Document", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // About & Bio Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Professional Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.bioSummary.ifBlank { "Add your career summary to impress recruiters." },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Skills Cloud Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Skills & Expertise (${profile.skills.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                        OutlinedButton(
                            onClick = { showAddSkillDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_skill_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Add Skill", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.skills.forEach { skill ->
                            Surface(
                                color = EthioEmeraldPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = EthioEmeraldDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove skill",
                                        tint = EthioEmeraldDark.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.removeSkill(skill) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Work Experience Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Work Experience", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                        OutlinedButton(
                            onClick = { showAddExperienceDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_exp_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Add Role", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (profile.experienceList.isEmpty()) {
                        Text(
                            text = "No work experience listed yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        profile.experienceList.forEachIndexed { index, exp ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = exp.roleTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${exp.companyName} • ${exp.location}",
                                            fontSize = 12.sp,
                                            color = EthioEmeraldDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${exp.startDate} - ${exp.endDate}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteExperience(exp.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exp.description,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (index < profile.experienceList.size - 1) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Education Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Education & Degrees", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    profile.educationList.forEach { edu ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EthioGoldSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = EthioGoldDark, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = edu.degree, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = edu.institution, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${edu.startYear} - ${edu.endYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Languages & Preferences
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Languages & Availability", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Languages: ${profile.languages.joinToString(", ")}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Availability: ${profile.availabilityStatus}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = EthioEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Expected Minimum: ${String.format("%,d", profile.expectedSalaryMinETB)} ETB/month",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EthioGoldDark
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }

    // Add Skill Dialog with suggestions
    if (showAddSkillDialog) {
        AddSkillDialog(
            onDismiss = { showAddSkillDialog = false },
            onAddSkill = { skill ->
                viewModel.addSkill(skill)
                showAddSkillDialog = false
            }
        )
    }

    // Add Experience Dialog
    if (showAddExperienceDialog) {
        AddExperienceDialog(
            onDismiss = { showAddExperienceDialog = false },
            onAdd = { exp ->
                viewModel.addExperience(exp)
                showAddExperienceDialog = false
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfile) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { viewModel.setShowEditProfile(false) },
            onSave = { updated -> viewModel.updateProfile(updated) }
        )
    }

    // Full CV Preview Dialog
    if (showCvPreview) {
        CvPreviewDialog(
            profile = profile,
            onDismiss = { viewModel.setShowCvPreview(false) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onAddSkill: (String) -> Unit
) {
    var skillInput by remember { mutableStateOf("") }
    val suggestedSkills = listOf(
        "Telebirr API", "CBE Birr", "Kotlin", "Jetpack Compose",
        "Amharic Translation", "Figma Design", "Flutter", "Accounting & Tax",
        "Digital Marketing", "QuickBooks", "AutoCAD", "Customer Care"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Skill to CV", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = skillInput,
                    onValueChange = { skillInput = it },
                    label = { Text("Skill Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("skill_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Popular Ethiopian Skills:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedSkills.forEach { suggestion ->
                        Surface(
                            color = EthioEmeraldPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onAddSkill(suggestion) }
                        ) {
                            Text(
                                text = "+ $suggestion",
                                fontSize = 11.sp,
                                color = EthioEmeraldDark,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (skillInput.isNotBlank()) onAddSkill(skillInput) },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                modifier = Modifier.testTag("confirm_add_skill_btn")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddExperienceDialog(
    onDismiss: () -> Unit,
    onAdd: (ExperienceItem) -> Unit
) {
    var roleTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Addis Ababa, Ethiopia") }
    var startDate by remember { mutableStateOf("2023") }
    var endDate by remember { mutableStateOf("Present") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Work Experience", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = roleTitle, onValueChange = { roleTitle = it }, label = { Text("Role / Job Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("exp_role_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Company Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("exp_company_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Year") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Year") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Role Summary & Achievements") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roleTitle.isNotBlank() && companyName.isNotBlank()) {
                        onAdd(
                            ExperienceItem(
                                id = "exp_${System.currentTimeMillis()}",
                                roleTitle = roleTitle,
                                companyName = companyName,
                                location = location,
                                startDate = startDate,
                                endDate = endDate,
                                description = description.ifBlank { "Contributed to company projects and technical deliverables." }
                            )
                        )
                    }
                },
                enabled = roleTitle.isNotBlank() && companyName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                modifier = Modifier.testTag("save_exp_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var titleHeadline by remember { mutableStateOf(profile.titleHeadline) }
    var bioSummary by remember { mutableStateOf(profile.bioSummary) }
    var phone by remember { mutableStateOf(profile.phone) }
    var email by remember { mutableStateOf(profile.email) }
    var telegramHandle by remember { mutableStateOf(profile.telegramHandle) }
    var cityLocation by remember { mutableStateOf(profile.cityLocation) }
    var expectedSalaryText by remember { mutableStateOf(profile.expectedSalaryMinETB.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile & CV Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("profile_name_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = titleHeadline, onValueChange = { titleHeadline = it }, label = { Text("Headline / Current Role") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("profile_headline_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Ethiopian Phone (+251)") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = telegramHandle, onValueChange = { telegramHandle = it }, label = { Text("Telegram (@handle)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = cityLocation, onValueChange = { cityLocation = it }, label = { Text("City / Sub-city in Ethiopia") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = expectedSalaryText, onValueChange = { expectedSalaryText = it.filter { c -> c.isDigit() } }, label = { Text("Minimum Expected Salary (ETB/month)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = bioSummary, onValueChange = { bioSummary = it }, label = { Text("Professional Bio") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            fullName = fullName,
                            titleHeadline = titleHeadline,
                            bioSummary = bioSummary,
                            phone = phone,
                            email = email,
                            telegramHandle = telegramHandle,
                            cityLocation = cityLocation,
                            expectedSalaryMinETB = expectedSalaryText.toLongOrNull() ?: profile.expectedSalaryMinETB
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CvPreviewDialog(
    profile: UserProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    val cvPlainText = buildString {
        appendLine("==========================================")
        appendLine("CURRICULUM VITAE: ${profile.fullName.uppercase()}")
        appendLine("${profile.titleHeadline}")
        appendLine("Location: ${profile.cityLocation}")
        appendLine("Phone: ${profile.phone} | Email: ${profile.email}")
        if (profile.telegramHandle.isNotBlank()) appendLine("Telegram: ${profile.telegramHandle}")
        appendLine("==========================================")
        appendLine("\n[PROFESSIONAL SUMMARY]")
        appendLine(profile.bioSummary)
        appendLine("\n[CORE SKILLS]")
        appendLine(profile.skills.joinToString(" • "))
        appendLine("\n[WORK EXPERIENCE]")
        profile.experienceList.forEach {
            appendLine("• ${it.roleTitle} at ${it.companyName} (${it.startDate} - ${it.endDate}, ${it.location})")
            appendLine("  ${it.description}")
        }
        appendLine("\n[EDUCATION]")
        profile.educationList.forEach {
            appendLine("• ${it.degree} - ${it.institution} (${it.startYear} - ${it.endYear})")
        }
        appendLine("\n[LANGUAGES]")
        appendLine(profile.languages.joinToString(", "))
        appendLine("==========================================")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Official SiraHub CV Document", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                EthiopianFlagEmblem()
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = cvPlainText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SiraHub CV", cvPlainText)
                    clipboard.setPrimaryClip(clip)
                    copied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                modifier = Modifier.testTag("copy_cv_btn")
            ) {
                Icon(if (copied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (copied) "CV Copied to Clipboard!" else "Copy Formatted CV")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
