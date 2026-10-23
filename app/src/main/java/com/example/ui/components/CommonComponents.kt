package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApplicationStatus
import com.example.model.BidStatus
import com.example.ui.theme.EthioCrimsonTertiary
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary
import com.example.ui.theme.SiraBackgroundDark

/**
 * The Ethiopian flag: three horizontal bands (green, yellow, red from top to bottom) with a blue
 * disc bearing a gold pentagram in the centre.
 *
 * Drawn with Compose primitives rather than an image so it stays crisp at every size and needs no
 * density-specific asset. The bands are HORIZONTAL - a Row of coloured boxes would render the flag
 * rotated 90 degrees.
 */
@Composable
fun EthiopianFlagEmblem(modifier: Modifier = Modifier) {
    val flagShape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .size(width = 28.dp, height = 19.dp)
            .clip(flagShape)
            .background(Color(0xFFDA121A)),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF078930)))
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFCDD09)))
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFDA121A)))
        }
        // Blue disc with the gold pentagram, as on the national flag.
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F47AF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFCDD09),
                modifier = Modifier.size(6.dp)
            )
        }
    }
}

@Composable
fun EtbSalaryBadge(
    amountText: String,
    modifier: Modifier = Modifier,
    isGig: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.background == SiraBackgroundDark
    val bgColor = if (isDark) {
        if (isGig) Color(0xFF2C2415) else Color(0xFF162B1F)
    } else {
        if (isGig) Color(0xFFFAF2E4) else Color(0xFFE2EFE6)
    }
    val textColor = if (isDark) {
        if (isGig) Color(0xFFFDE68A) else Color(0xFF86EFAC)
    } else {
        if (isGig) EthioGoldDark else EthioEmeraldDark
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "ETB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = amountText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verified Employer",
            tint = EthioEmeraldPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "Verified",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = EthioEmeraldPrimary
        )
    }
}

@Composable
fun UrgentBadge(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == SiraBackgroundDark
    val bgColor = if (isDark) Color(0xFF3B1C1C) else Color(0xFFFAECEC)
    val textColor = if (isDark) Color(0xFFFCA5A5) else EthioCrimsonTertiary

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = "Urgent",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ApplicationStatusBadge(
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == SiraBackgroundDark
    val (bgColor, textColor) = if (isDark) {
        when (status) {
            ApplicationStatus.APPLIED -> Color(0xFF1E293B) to Color(0xFF93C5FD)
            ApplicationStatus.UNDER_REVIEW -> Color(0xFF2D2314) to Color(0xFFFDE68A)
            ApplicationStatus.SHORTLISTED -> Color(0xFF132A1C) to Color(0xFF86EFAC)
            ApplicationStatus.INTERVIEW -> Color(0xFF2E1A47) to Color(0xFFD8B4FE)
            ApplicationStatus.OFFER -> Color(0xFF0F3124) to Color(0xFF6EE7B7)
            ApplicationStatus.REJECTED -> Color(0xFF331616) to Color(0xFFFCA5A5)
        }
    } else {
        when (status) {
            ApplicationStatus.APPLIED -> Color(0xFFEEF3FA) to Color(0xFF255CB0)
            ApplicationStatus.UNDER_REVIEW -> Color(0xFFFAF2E4) to Color(0xFF9E6116)
            ApplicationStatus.SHORTLISTED -> Color(0xFFE2EFE6) to Color(0xFF0F683F)
            ApplicationStatus.INTERVIEW -> Color(0xFFF3EDFA) to Color(0xFF6D28D9)
            ApplicationStatus.OFFER -> Color(0xFFDEF5E9) to Color(0xFF065F46)
            ApplicationStatus.REJECTED -> Color(0xFFFAECEC) to Color(0xFFA83A3A)
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BidStatusBadge(
    status: BidStatus,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == SiraBackgroundDark
    val (bgColor, textColor) = if (isDark) {
        when (status) {
            BidStatus.SUBMITTED -> Color(0xFF1E293B) to Color(0xFF93C5FD)
            BidStatus.CLIENT_REVIEWING -> Color(0xFF2D2314) to Color(0xFFFDE68A)
            BidStatus.SHORTLISTED -> Color(0xFF132A1C) to Color(0xFF86EFAC)
            BidStatus.ACCEPTED -> Color(0xFF0F3124) to Color(0xFF6EE7B7)
            BidStatus.COMPLETED -> Color(0xFF1E2442) to Color(0xFFA5B4FC)
            BidStatus.DECLINED -> Color(0xFF331616) to Color(0xFFFCA5A5)
        }
    } else {
        when (status) {
            BidStatus.SUBMITTED -> Color(0xFFEEF3FA) to Color(0xFF255CB0)
            BidStatus.CLIENT_REVIEWING -> Color(0xFFFAF2E4) to Color(0xFF9E6116)
            BidStatus.SHORTLISTED -> Color(0xFFE2EFE6) to Color(0xFF0F683F)
            BidStatus.ACCEPTED -> Color(0xFFDEF5E9) to Color(0xFF065F46)
            BidStatus.COMPLETED -> Color(0xFFECEFFC) to Color(0xFF4338CA)
            BidStatus.DECLINED -> Color(0xFFFAECEC) to Color(0xFFA83A3A)
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Work,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = actionText, fontSize = 13.sp)
                }
            }
        }
    }
}
