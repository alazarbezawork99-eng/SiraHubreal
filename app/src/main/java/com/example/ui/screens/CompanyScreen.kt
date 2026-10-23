package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApplicationStatus
import com.example.model.CompanyProfile
import com.example.model.JobApplication
import com.example.ui.SiraViewModel
import com.example.ui.components.ApplicationStatusBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EtbSalaryBadge
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary

@Composable
fun CompanyScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    val companyProfile by viewModel.companyProfile.collectAsState()
    val companyJobs by viewModel.companyJobs.collectAsState()
    val companyApplicants by viewModel.companyApplicants.collectAsState()
    val selectedApplicant by viewModel.selectedApplicantForReview.collectAsState()
    val showEditCompanyProfile by viewModel.showEditCompanyProfile.collectAsState()

    var selectedCompanyTab by remember { mutableIntStateOf(0) } // 0: Applicants Pipeline, 1: Posted Jobs, 2: Database / Supabase

    // Calculate candidate stats
    val totalApplicants = companyApplicants.size
    val underReviewCount = companyApplicants.count { it.status == ApplicationStatus.UNDER_REVIEW || it.status == ApplicationStatus.APPLIED }
    val shortlistedCount = companyApplicants.count { it.status == ApplicationStatus.SHORTLISTED }
    val interviewCount = companyApplicants.count { it.status == ApplicationStatus.INTERVIEW || it.status == ApplicationStatus.OFFER }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("company_screen_list")
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = EthioGoldSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EMPLOYER PORTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Company Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Manage corporate job openings & review applicant CV dossiers",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EthiopianFlagEmblem()
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Company Profile Card
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
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(EthioEmeraldPrimary, EthioEmeraldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = companyProfile.name.take(2).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = companyProfile.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge()
                            }
                            Text(
                                text = companyProfile.industry,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = EthioEmeraldPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = companyProfile.location,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setShowEditCompanyProfile(true) },
                            modifier = Modifier.testTag("edit_company_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Company", tint = EthioEmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = companyProfile.description,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Email: ${companyProfile.contactEmail}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Phone: ${companyProfile.contactPhone}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Applicant Analytics & Hiring Pipeline Stats
        item {
            Text(
                text = "Applicant Pipeline & Stats",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    title = "Active Jobs",
                    count = companyJobs.size.toString(),
                    color = EthioEmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Total CVs",
                    count = totalApplicants.toString(),
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Shortlisted",
                    count = shortlistedCount.toString(),
                    color = EthioGoldSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Interviews",
                    count = interviewCount.toString(),
                    color = Color(0xFF7E22CE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Company Tab Row: Applicants vs Posted Jobs vs Database
        item {
            TabRow(
                selectedTabIndex = selectedCompanyTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCompanyTab]),
                        color = EthioEmeraldPrimary
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("company_tab_row")
            ) {
                Tab(
                    selected = selectedCompanyTab == 0,
                    onClick = { selectedCompanyTab = 0 },
                    text = {
                        Text(
                            text = "Applicants (${companyApplicants.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedCompanyTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCompanyTab == 0) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_company_applicants")
                )
                Tab(
                    selected = selectedCompanyTab == 1,
                    onClick = { selectedCompanyTab = 1 },
                    text = {
                        Text(
                            text = "My Jobs (${companyJobs.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedCompanyTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCompanyTab == 1) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_company_jobs")
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Tab Content
        when (selectedCompanyTab) {
            0 -> {
                // Applicants Pipeline
                if (companyApplicants.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Applicants Received Yet",
                            message = "Candidates who apply to your job postings will appear here with full CV profiles.",
                            actionText = "Post a Company Vacancy",
                            onActionClick = { viewModel.setTab(2) },
                            icon = Icons.Default.Groups
                        )
                    }
                } else {
                    items(companyApplicants, key = { it.id }) { app ->
                        CompanyApplicantCard(
                            application = app,
                            onReviewClick = { viewModel.openApplicantReview(app) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            1 -> {
                // Company Posted Jobs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jobs Posted by ${companyProfile.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { viewModel.setTab(2) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("company_post_job_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Job", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (companyJobs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Job Openings Posted",
                            message = "Publish vacancies to attract skilled Ethiopian candidates.",
                            actionText = "Post First Job Opening",
                            onActionClick = { viewModel.setTab(2) }
                        )
                    }
                } else {
                    items(companyJobs, key = { it.id }) { job ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = job.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = EthioEmeraldPrimary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EthioEmeraldDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(text = "${job.location} • ${job.formattedSalary}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${job.applicantsCount} Applicants received",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EthioEmeraldDark
                                    )
                                    OutlinedButton(
                                        onClick = { selectedCompanyTab = 0 },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text("View Candidates", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }

    // Applicant Dossier Review Dialog
    selectedApplicant?.let { app ->
        ApplicantDossierDialog(
            application = app,
            onDismiss = { viewModel.openApplicantReview(null) },
            onUpdateStatus = { newStatus ->
                viewModel.updateApplicantStatus(app.id, newStatus)
            }
        )
    }

    // Edit Company Profile Dialog
    if (showEditCompanyProfile) {
        EditCompanyDialog(
            company = companyProfile,
            onDismiss = { viewModel.setShowEditCompanyProfile(false) },
            onSave = { updated -> viewModel.updateCompanyProfile(updated) }
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanyApplicantCard(
    application: JobApplication,
    onReviewClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
            .testTag("applicant_card_${application.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EthioEmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.applicantName.take(1).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EthioEmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = application.applicantName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = application.applicantHeadline,
                            fontSize = 12.sp,
                            color = EthioEmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                ApplicationStatusBadge(status = application.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Applied for: ${application.jobTitle}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Education: ${application.applicantEducation}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                application.applicantSkills.take(3).forEach { skill ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expected: ${String.format("%,d", application.expectedSalaryETB)} ETB/mo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EthioEmeraldDark
                )
                Button(
                    onClick = onReviewClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("review_applicant_btn_${application.id}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review CV Dossier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplicantDossierDialog(
    application: JobApplication,
    onDismiss: () -> Unit,
    onUpdateStatus: (ApplicationStatus) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Candidate CV Dossier", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = application.jobTitle, fontSize = 12.sp, color = EthioEmeraldDark)
                }
                ApplicationStatusBadge(status = application.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Personal Header
                Surface(
                    color = EthioEmeraldPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = application.applicantName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = application.applicantHeadline, fontSize = 13.sp, color = EthioEmeraldDark, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Phone: ${application.applicantPhone}", fontSize = 12.sp)
                        Text(text = "Email: ${application.applicantEmail}", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Education & Qualifications
                Text(text = "Education & Degree", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = application.applicantEducation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                // Work Experience
                Text(text = "Experience Background", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = application.applicantExperience, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                // Skills
                Text(text = "Skills & Technologies", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    application.applicantSkills.forEach { skill ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cover Note
                Text(text = "Candidate Cover Note", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${application.coverNote}\"",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Expected Monthly Salary: ${String.format("%,d", application.expectedSalaryETB)} ETB",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EthioEmeraldDark
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Action: Update Hiring Status
                Text(text = "Change Candidate Hiring Status:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(ApplicationStatus.SHORTLISTED) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("status_shortlist_btn")
                    ) {
                        Text("Shortlist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUpdateStatus(ApplicationStatus.INTERVIEW) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                        modifier = Modifier.weight(1f).testTag("status_interview_btn")
                    ) {
                        Text("Interview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUpdateStatus(ApplicationStatus.OFFER) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                        modifier = Modifier.weight(1f).testTag("status_offer_btn")
                    ) {
                        Text("Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary)
            ) {
                Text("Close Dossier")
            }
        }
    )
}

@Composable
fun EditCompanyDialog(
    company: CompanyProfile,
    onDismiss: () -> Unit,
    onSave: (CompanyProfile) -> Unit
) {
    var name by remember { mutableStateOf(company.name) }
    var industry by remember { mutableStateOf(company.industry) }
    var location by remember { mutableStateOf(company.location) }
    var website by remember { mutableStateOf(company.website) }
    var email by remember { mutableStateOf(company.contactEmail) }
    var phone by remember { mutableStateOf(company.contactPhone) }
    var description by remember { mutableStateOf(company.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Company Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("company_name_input"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = industry, onValueChange = { industry = it }, label = { Text("Industry Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Office Location in Ethiopia") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Website") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Recruitment Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Company Overview") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        company.copy(
                            name = name,
                            industry = industry,
                            location = location,
                            website = website,
                            contactEmail = email,
                            contactPhone = phone,
                            description = description
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                modifier = Modifier.testTag("save_company_profile_btn")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
