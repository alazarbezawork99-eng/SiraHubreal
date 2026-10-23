package com.example.ui.validation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single field-level validation failure.
 *
 * [field] identifies which input is at fault so the screen can highlight it; [messageRes] is the
 * plain-language explanation shown directly beneath that input. Keeping both means the user is
 * never told merely that "something" is wrong.
 */
data class FieldError(
    val field: String,
    @StringRes val messageRes: Int
)

/** Field identifiers shared between the forms and their validators. */
object FormField {
    const val JOB_TITLE = "job_title"
    const val JOB_COMPANY = "job_company"
    const val JOB_LOCATION = "job_location"
    const val JOB_CATEGORY = "job_category"
    const val JOB_DESCRIPTION = "job_description"
    const val JOB_SALARY_MIN = "job_salary_min"
    const val JOB_SALARY_MAX = "job_salary_max"
    const val JOB_CONTACT_EMAIL = "job_contact_email"

    const val GIG_TITLE = "gig_title"
    const val GIG_CATEGORY = "gig_category"
    const val GIG_BUDGET = "gig_budget"
    const val GIG_DURATION = "gig_duration"
    const val GIG_DESCRIPTION = "gig_description"

    const val AUTH_FULL_NAME = "auth_full_name"
    const val AUTH_EMAIL = "auth_email"
    const val AUTH_PASSWORD = "auth_password"
    const val AUTH_PHONE = "auth_phone"
    const val AUTH_COMPANY_NAME = "auth_company_name"
}

/**
 * Inline error text rendered under an input.
 *
 * Shown as a small red row with an icon so the offending field is obvious at a glance, which is
 * the pattern used by most production forms.
 */
@Composable
fun FieldErrorText(
    @StringRes messageRes: Int?,
    modifier: Modifier = Modifier
) {
    if (messageRes == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = LocalContext.current.getString(messageRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/** Wraps a form input and renders its error underneath, keeping spacing consistent. */
@Composable
fun FieldContainer(
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        content()
        FieldErrorText(errorRes)
    }
}

/** Convenience: the error resource for [field], or null when that field is valid. */
fun List<FieldError>.errorFor(field: String): Int? =
    firstOrNull { it.field == field }?.messageRes
