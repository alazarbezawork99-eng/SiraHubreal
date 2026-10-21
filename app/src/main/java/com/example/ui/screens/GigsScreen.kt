package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SendAndArchive
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GigPost
import com.example.model.UserProfile
import com.example.ui.SiraViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EtbSalaryBadge
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.components.UrgentBadge
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary
import com.example.ui.theme.SiraBackgroundDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GigsScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    val gigs by viewModel.filteredGigs.collectAsState()
    val searchQuery by viewModel.gigSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedGigCategory.collectAsState()
    val bookmarkedGigIds by viewModel.bookmarkedGigIds.collectAsState()
    val selectedGigForDetail by viewModel.selectedGigForDetail.collectAsState()
    val biddingGig by viewModel.biddingGig.collectAsState()
    val userProfile by viewModel.profile.collectAsState()

    val categories = listOf(
        "All", "Translation & Writing", "Graphic Design", "Web & Mobile Dev",
        "Photography & Video", "Accounting & Finance", "Tutoring & Education", "Errands & Logistics"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("gigs_screen_list")
    ) {
        // Gigs Banner
        item {
            GigsBannerCard(onPostGigClick = { viewModel.setTab(2) })
        }

        // Freelance & Tasks Pulse Highlights Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                            color = EthioGoldSecondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 12.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Freelance & Quick Tasks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Direct Birr escrow • Addis Ababa & nationwide tasks",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EthioGoldSecondary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "INSTANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EthioGoldSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setGigSearchQuery(it) },
                    placeholder = { Text("Search freelance tasks, skills...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EthioGoldSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setGigSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gig_search_input")
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
                        onClick = { viewModel.setGigCategory(category) },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EthioGoldSecondary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("gig_filter_${category.replace(" ", "_")}")
                    )
                }
            }
        }

        // Gigs Counter & Quick Info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = EthioGoldSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gigs.size} Short-Term Gigs Open",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                EthiopianFlagEmblem()
            }
        }

        // Gigs Feed
        if (gigs.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No freelance gigs found",
                    message = "Try clearing search keywords or selecting 'All' categories.",
                    actionText = "Clear Filters",
                    onActionClick = {
                        viewModel.setGigSearchQuery("")
                        viewModel.setGigCategory("All")
                    },
                    icon = Icons.Default.Gavel
                )
            }
        } else {
            items(gigs, key = { it.id }) { gig ->
                GigCardItem(
                    gig = gig,
                    isBookmarked = bookmarkedGigIds.contains(gig.id),
                    onGigClick = { viewModel.openGigDetail(gig) },
                    onBookmarkClick = { viewModel.toggleGigBookmark(gig.id) },
                    onBidClick = { viewModel.openBidModal(gig) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Gig Detail Modal
    selectedGigForDetail?.let { gig ->
        GigDetailDialog(
            gig = gig,
            isBookmarked = bookmarkedGigIds.contains(gig.id),
            onDismiss = { viewModel.openGigDetail(null) },
            onBookmarkClick = { viewModel.toggleGigBookmark(gig.id) },
            onBidClick = {
                viewModel.openGigDetail(null)
                viewModel.openBidModal(gig)
            }
        )
    }

    // Bid Submission Modal
    biddingGig?.let { gig ->
        SubmitBidDialog(
            gig = gig,
            userProfile = userProfile,
            onDismiss = { viewModel.closeBidModal() },
            onSubmit = { amount, deliveryDays, pitch ->
                viewModel.submitGigBid(gig.id, amount, deliveryDays, pitch)
            }
        )
    }
}

@Composable
fun GigsBannerCard(onPostGigClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        val isDark = MaterialTheme.colorScheme.background == SiraBackgroundDark
        val gradientColors = if (isDark) {
            listOf(Color(0xFF241D12), Color(0xFF1B150C))
        } else {
            listOf(Color(0xFFFAF5EC), Color(0xFFF3EADB))
        }
        val titleColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF6B4412)
        val bodyColor = if (isDark) Color(0xFFD4AF37) else Color(0xFF8C5819)
        val payInEtbColor = if (isDark) Color(0xFFFBBF24) else EthioGoldDark

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EthioGoldSecondary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "FREELANCE & QUICK TASKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "ክፍያ በብር / Pay in ETB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = payInEtbColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bid on Gigs or Post a Quick Task",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )

            Text(
                text = "Translation, design, IT, tutoring, and errands across Ethiopia. Flexible projects with quick turnaround.",
                fontSize = 12.sp,
                color = bodyColor,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPostGigClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                modifier = Modifier.testTag("banner_post_gig_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post a Freelance Task / Gig", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GigCardItem(
    gig: GigPost,
    isBookmarked: Boolean,
    onGigClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBidClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .clickable(onClick = onGigClick)
            .testTag("gig_card_${gig.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Client Name + Category + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EthioGoldSecondary.copy(alpha = 0.15f))
                        .border(1.dp, EthioGoldSecondary.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = EthioGoldDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gig.clientName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = gig.title,
                        fontSize = 15.sp,
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
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = gig.location,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_gig_${gig.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Gig",
                        tint = if (isBookmarked) EthioGoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Budget Badge (Golden ETB)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtbSalaryBadge(amountText = gig.formattedBudget, isGig = true)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = EthioCrimsonTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = gig.durationText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EthioCrimsonTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skills / Deliverables Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = EthioGoldSecondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = gig.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = EthioGoldDark,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                gig.requiredSkills.take(3).forEach { skill ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
                if (gig.isUrgent) {
                    UrgentBadge()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: time, bids count, and Submit Bid button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${gig.postedTimeAgo} • ${gig.bidsCount} proposals",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onBidClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("bid_btn_${gig.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Place Bid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GigDetailDialog(
    gig: GigPost,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBidClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onBidClick,
                colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_place_bid_btn")
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit Proposal & Bid", fontWeight = FontWeight.Bold)
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
                    Text(text = gig.clientName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = gig.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EtbSalaryBadge(amountText = gig.formattedBudget, isGig = true)
                    Text(
                        text = "Turnaround: ${gig.durationText}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioCrimsonTertiary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Task Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = gig.description, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (gig.deliverables.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Required Deliverables", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    gig.deliverables.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = EthioGoldSecondary)
                            Text(item, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (gig.requiredSkills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Skills Needed", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        gig.requiredSkills.forEach { skill ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = skill,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
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
                        Text(text = "Client Information", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = "Type: ${gig.clientType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        gig.clientPhone?.let {
                            Text(text = "Phone: $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        gig.clientTelegram?.let {
                            Text(text = "Telegram: $it", fontSize = 12.sp, color = EthioEmeraldPrimary, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "Location: ${gig.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    )
}

@Composable
fun SubmitBidDialog(
    gig: GigPost,
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSubmit: (amountETB: Long, deliveryDays: String, pitch: String) -> Unit
) {
    var bidAmountText by remember { mutableStateOf(gig.budgetETB.toString()) }
    var deliveryDaysText by remember { mutableStateOf(gig.durationText) }
    var pitchText by remember {
        mutableStateOf(
            "Hello ${gig.clientName},\n\nI can deliver this task with high quality and on schedule. I have hands-on experience in ${gig.requiredSkills.take(2).joinToString(" and ")}. Please review my profile and let's get started!"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bid on Freelance Task",
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
                    text = gig.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EthioGoldDark
                )

                Text(
                    text = "Client's Budget: ${gig.formattedBudget}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                OutlinedTextField(
                    value = bidAmountText,
                    onValueChange = { bidAmountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Your Bid Amount (ETB)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bid_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deliveryDaysText,
                    onValueChange = { deliveryDaysText = it },
                    label = { Text("Estimated Delivery (e.g. 3 Days)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bid_delivery_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pitchText,
                    onValueChange = { pitchText = it },
                    label = { Text("Your Proposal / Why choose you") },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bid_pitch_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Applying as: ${userProfile.fullName} (${userProfile.phone})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = bidAmountText.toLongOrNull() ?: gig.budgetETB
                    onSubmit(amount, deliveryDaysText, pitchText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_bid_btn")
            ) {
                Text("Submit Proposal")
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
