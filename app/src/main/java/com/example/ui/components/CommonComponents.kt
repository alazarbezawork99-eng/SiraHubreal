package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun EthiopianFlagEmblem(modifier: Modifier = Modifier) {
    // Elegant mini Ethiopian tri-color accent badge
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .height(18.dp)
            .width(28.dp)
    ) {
        Box(modifier = Modifier.weight(1f).height(18.dp).background(Color(0xFF009A44)))
        Box(modifier = Modifier.weight(1f).height(18.dp).background(Color(0xFFFED100)))
        Box(modifier = Modifier.weight(1f).height(18.dp).background(Color(0xFFEF3340)))
    }
}

@Composable
fun EtbSalaryBadge(
    amountText: String,
    modifier: Modifier = Modifier,
    isGig: Boolean = false
) {
    Surface(
        color = if (isGig) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
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
                color = if (isGig) EthioGoldDark else EthioEmeraldDark,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = amountText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGig) EthioGoldDark else EthioEmeraldDark
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
    Surface(
        color = Color(0xFFFEE2E2),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = "Urgent",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EthioCrimsonTertiary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ApplicationStatusBadge(
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ApplicationStatus.APPLIED -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        ApplicationStatus.UNDER_REVIEW -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        ApplicationStatus.SHORTLISTED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        ApplicationStatus.INTERVIEW -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        ApplicationStatus.OFFER -> Color(0xFFD1FAE5) to Color(0xFF047857)
        ApplicationStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
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
    val (bgColor, textColor) = when (status) {
        BidStatus.SUBMITTED -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        BidStatus.CLIENT_REVIEWING -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        BidStatus.SHORTLISTED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        BidStatus.ACCEPTED -> Color(0xFFD1FAE5) to Color(0xFF047857)
        BidStatus.COMPLETED -> Color(0xFFE0E7FF) to Color(0xFF4338CA)
        BidStatus.DECLINED -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
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
