package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GigBid
import com.example.model.JobApplication
import com.example.ui.SiraViewModel
import com.example.ui.components.ApplicationStatusBadge
import com.example.ui.components.BidStatusBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EtbSalaryBadge
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary

@Composable
fun ActivityScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.activitySubTab.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val bids by viewModel.bids.collectAsState()
    val userPostedJobs by viewModel.userPostedJobs.collectAsState()
    val userPostedGigs by viewModel.userPostedGigs.collectAsState()
    val bookmarkedJobs by viewModel.bookmarkedJobs.collectAsState()
    val bookmarkedGigs by viewModel.bookmarkedGigs.collectAsState()

    val subTabs = listOf(
        "Job Applications (${applications.size})",
        "Gig Proposals (${bids.size})",
        "My Posted Listings (${userPostedJobs.size + userPostedGigs.size})",
        "Saved (${bookmarkedJobs.size + bookmarkedGigs.size})"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("activity_screen_container")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Activity & Tracker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Monitor your applications, proposals, and posted listings",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EthiopianFlagEmblem()
        }

        // Sub Tabs
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = EthioEmeraldPrimary
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("activity_sub_tabs")
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeSubTab == index,
                    onClick = { viewModel.setActivitySubTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSubTab == index) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("activity_sub_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        when (activeSubTab) {
            0 -> ApplicationsList(applications = applications, onExploreJobs = { viewModel.setTab(0) })
            1 -> BidsList(bids = bids, onExploreGigs = { viewModel.setTab(1) })
            2 -> MyPostedListings(
                userJobs = userPostedJobs,
                userGigs = userPostedGigs,
                onPostClick = { viewModel.setTab(2) }
            )
            3 -> SavedBookmarksList(
                savedJobs = bookmarkedJobs,
                savedGigs = bookmarkedGigs,
                onJobClick = { viewModel.openJobDetail(it) },
                onGigClick = { viewModel.openGigDetail(it) }
            )
        }
    }
}

@Composable
fun ApplicationsList(
    applications: List<JobApplication>,
    onExploreJobs: () -> Unit
) {
    if (applications.isEmpty()) {
        EmptyStateCard(
            title = "No Job Applications Yet",
            message = "Apply to verified positions in Ethiopia with your profile CV.",
            actionText = "Explore Open Jobs",
            onActionClick = onExploreJobs,
            icon = Icons.AutoMirrored.Filled.Send
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(applications, key = { it.id }) { app ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.jobTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${app.companyName} • ${app.location}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ApplicationStatusBadge(status = app.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${app.coverNote}\"",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expected: ${String.format("%,d", app.expectedSalaryETB)} ETB/mo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EthioEmeraldDark
                            )
                            Text(
                                text = "Applied: ${app.appliedDate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BidsList(
    bids: List<GigBid>,
    onExploreGigs: () -> Unit
) {
    if (bids.isEmpty()) {
        EmptyStateCard(
            title = "No Gig Proposals Yet",
            message = "Browse short-term tasks and submit your proposals and bids in ETB.",
            actionText = "Browse Gigs",
            onActionClick = onExploreGigs,
            icon = Icons.Default.Gavel
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bids, key = { it.id }) { bid ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bid.gigTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Client: ${bid.clientName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BidStatusBadge(status = bid.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${bid.proposalPitch}\"",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EtbSalaryBadge(amountText = "${bid.formattedBid} • ${bid.deliveryDays}", isGig = true)
                            Text(
                                text = "Bid: ${bid.submittedDate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MyPostedListings(
    userJobs: List<com.example.model.JobPost>,
    userGigs: List<com.example.model.GigPost>,
    onPostClick: () -> Unit
) {
    if (userJobs.isEmpty() && userGigs.isEmpty()) {
        EmptyStateCard(
            title = "No Posted Listings",
            message = "You haven't posted any jobs or freelance tasks yet.",
            actionText = "Post New Listing",
            onActionClick = onPostClick,
            icon = Icons.Default.Business
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (userJobs.isNotEmpty()) {
                item {
                    Text(
                        text = "Company Jobs Posted by You",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioEmeraldDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(userJobs, key = { it.id }) { job ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                        text = "Active Job",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EthioEmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(text = "${job.company} • ${job.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = EthioEmeraldPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${job.applicantsCount} Applicants received",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EthioEmeraldDark
                                )
                            }
                        }
                    }
                }
            }

            if (userGigs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Freelance Tasks / Gigs Posted by You",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioGoldDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(userGigs, key = { it.id }) { gig ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = gig.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Surface(
                                    color = EthioGoldSecondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = gig.status.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EthioGoldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(text = "Budget: ${gig.formattedBudget}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = EthioGoldSecondary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${gig.bidsCount} Proposals submitted",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EthioGoldDark
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SavedBookmarksList(
    savedJobs: List<com.example.model.JobPost>,
    savedGigs: List<com.example.model.GigPost>,
    onJobClick: (com.example.model.JobPost) -> Unit,
    onGigClick: (com.example.model.GigPost) -> Unit
) {
    if (savedJobs.isEmpty() && savedGigs.isEmpty()) {
        EmptyStateCard(
            title = "No Bookmarks Saved",
            message = "Tap the bookmark icon on any job or freelance gig to save it here for quick access.",
            icon = Icons.Default.Bookmark
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (savedJobs.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Jobs (${savedJobs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioEmeraldDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(savedJobs, key = { "book_job_${it.id}" }) { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJobClick(job) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = EthioEmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = job.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${job.company} • ${job.formattedSalary}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (savedGigs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saved Gigs (${savedGigs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EthioGoldDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(savedGigs, key = { "book_gig_${it.id}" }) { gig ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGigClick(gig) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = EthioGoldSecondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = gig.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${gig.clientName} • ${gig.formattedBudget}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
