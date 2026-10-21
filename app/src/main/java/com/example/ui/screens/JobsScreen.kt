package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.JobPost
import com.example.model.UserProfile
import com.example.ui.SiraViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EtbSalaryBadge
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.components.UrgentBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobsScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.filteredJobs.collectAsState()
    val searchQuery by viewModel.jobSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedJobCategory.collectAsState()
    val selectedLocation by viewModel.selectedJobLocation.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedJobIds.collectAsState()
    val selectedJobForDetail by viewModel.selectedJobForDetail.collectAsState()
    val applyingJob by viewModel.applyingJob.collectAsState()
    val userProfile by viewModel.profile.collectAsState()

    val categories = listOf(
        "All", "Tech & Software", "Banking & Finance", "NGO & Development",
        "Engineering & Construction", "Sales & Marketing", "Design & Creative"
    )

    val locations = listOf("All", "Addis Ababa", "Hawassa", "Remote")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("jobs_screen_list")
    ) {
        // Hero Header Banner
        item {
            JobsHeroBanner(onPostJobClick = { viewModel.setTab(2) })
        }

        // Ethiopian Career Market Highlights Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EthioEmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🇪🇹", fontSize = 12.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Ethiopian Career Pulse",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verified vacancies • Addis Ababa, Hawassa & Remote",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EthioEmeraldPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EthioEmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setJobSearchQuery(it) },
                    placeholder = { Text("Search jobs, companies, skills...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EthioEmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setJobSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_search_input")
                )
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setJobCategory(category) },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EthioEmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("job_filter_${category.replace(" ", "_")}")
                    )
                }
            }
        }

        // City/Location Quick Chips
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EthioEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "City:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    locations.forEach { loc ->
                        val isLocSelected = selectedLocation == loc
                        Surface(
                            color = if (isLocSelected) EthioEmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isLocSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setJobLocation(loc) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = loc,
                                fontSize = 11.sp,
                                fontWeight = if (isLocSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLocSelected) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Jobs Results Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${jobs.size} Open Jobs in Ethiopia",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                EthiopianFlagEmblem()
            }
        }

        // Jobs List
        if (jobs.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No jobs found",
                    message = "Try modifying your search or clearing category filters.",
                    actionText = "Clear Filters",
                    onActionClick = {
                        viewModel.setJobSearchQuery("")
                        viewModel.setJobCategory("All")
                        viewModel.setJobLocation("All")
                    }
                )
            }
        } else {
            items(jobs, key = { it.id }) { job ->
                JobCardItem(
                    job = job,
                    isBookmarked = bookmarkedIds.contains(job.id),
                    onJobClick = { viewModel.openJobDetail(job) },
                    onBookmarkClick = { viewModel.toggleJobBookmark(job.id) },
                    onApplyClick = { viewModel.openApplyModal(job) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Job Detail Sheet / Modal
    selectedJobForDetail?.let { job ->
        JobDetailDialog(
            job = job,
            isBookmarked = bookmarkedIds.contains(job.id),
            onDismiss = { viewModel.openJobDetail(null) },
            onBookmarkClick = { viewModel.toggleJobBookmark(job.id) },
            onApplyClick = {
                viewModel.openJobDetail(null)
                viewModel.openApplyModal(job)
            }
        )
    }

    // Apply Modal
    applyingJob?.let { job ->
        ApplyJobDialog(
            job = job,
            userProfile = userProfile,
            onDismiss = { viewModel.closeApplyModal() },
            onSubmit = { coverNote, expectedSalary ->
                viewModel.submitJobApplication(job.id, coverNote, expectedSalary)
            }
        )
    }
}

@Composable
fun JobsHeroBanner(onPostJobClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Ethiopian Professionals at Work",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD0C3A24))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = EthioGoldSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ስራ / SIRA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ETHIOPIAN CAREERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF08A)
                        )
                    }
                    Text(
                        text = "Build Your Career in Ethiopia",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hiring Ethiopian talent?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Post job openings for free",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onPostJobClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                    modifier = Modifier.testTag("hero_post_job_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post a Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCardItem(
    job: JobPost,
    isBookmarked: Boolean,
    onJobClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .clickable(onClick = onJobClick)
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Company initial icon + Company + Title + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Company Icon Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EthioEmeraldPrimary.copy(alpha = 0.12f))
                        .border(1.dp, EthioEmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.company.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioEmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = job.company,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (job.isVerifiedCompany) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge()
                        }
                    }
                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = job.location,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_job_${job.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Job",
                        tint = if (isBookmarked) EthioGoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Salary Badge
            EtbSalaryBadge(amountText = job.formattedSalary)

            Spacer(modifier = Modifier.height(10.dp))

            // Tag chips row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TagPill(job.employmentType.label)
                TagPill(job.workplaceType.label)
                TagPill(job.experienceLevel.label)
                if (job.isUrgent) {
                    UrgentBadge()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: time ago, applicants, and Quick Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${job.postedTimeAgo} • ${job.applicantsCount} applicants",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("quick_apply_${job.id}")
                ) {
                    Text(text = "Apply Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TagPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun JobDetailDialog(
    job: JobPost,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onBookmarkClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onApplyClick,
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_apply_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply with Profile CV", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = job.company, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) EthioGoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Location & Salary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EthioEmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = job.location, fontSize = 13.sp)
                }
                EtbSalaryBadge(amountText = job.formattedSalary)

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "About the Role", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = job.description, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (job.responsibilities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Key Responsibilities", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    job.responsibilities.forEach { resp ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = EthioEmeraldPrimary)
                            Text(resp, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (job.requirements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Requirements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    job.requirements.forEach { req ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = EthioGoldSecondary)
                            Text(req, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (job.benefits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Perks & Benefits", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    job.benefits.forEach { ben ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("✓ ", fontWeight = FontWeight.Bold, color = EthioEmeraldDark)
                            Text(ben, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Company Contact", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = "Email: ${job.contactEmail}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        job.contactPhone?.let {
                            Text(text = "Phone: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = "Deadline: ${job.deadline}", fontSize = 11.sp, color = EthioGoldDark, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    )
}

@Composable
fun ApplyJobDialog(
    job: JobPost,
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSubmit: (coverNote: String, expectedSalary: Long) -> Unit
) {
    var coverNote by remember {
        mutableStateOf(
            "Greetings ${job.company} Hiring Team,\n\nI am enthusiastic to submit my application for the ${job.title} position. With my background in ${userProfile.titleHeadline} and hands-on skills in ${userProfile.skills.take(3).joinToString(", ")}, I am confident in adding immediate value to your organization."
        )
    }
    var expectedSalaryText by remember { mutableStateOf(userProfile.expectedSalaryMinETB.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Apply to ${job.company}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = job.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EthioEmeraldPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CV Preview Card
                Surface(
                    color = EthioEmeraldPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EthioEmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Applying with Profile CV",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EthioEmeraldDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = userProfile.fullName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = userProfile.titleHeadline, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${userProfile.phone} • ${userProfile.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = expectedSalaryText,
                    onValueChange = { expectedSalaryText = it.filter { char -> char.isDigit() } },
                    label = { Text("Expected Salary (ETB / Month)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_salary_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = coverNote,
                    onValueChange = { coverNote = it },
                    label = { Text("Cover Note / Message to Employer") },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_cover_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = expectedSalaryText.toLongOrNull() ?: 0L
                    onSubmit(coverNote, salary)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_application_btn")
            ) {
                Text("Send Application")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
