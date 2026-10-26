package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmploymentType
import com.example.model.ExperienceLevel
import com.example.model.GigPricingType
import com.example.model.WorkplaceType
import com.example.ui.SiraViewModel
import com.example.ui.components.EthiopianFlagEmblem
import com.example.ui.theme.EthioEmeraldDark
import com.example.ui.theme.EthioEmeraldPrimary
import com.example.ui.theme.EthioGoldDark
import com.example.ui.theme.EthioGoldSecondary
import com.example.ui.validation.FieldContainer
import com.example.ui.validation.FieldError
import com.example.ui.validation.FormField
import com.example.ui.validation.FormValidators
import com.example.ui.validation.errorFor

@Composable
fun PostScreen(
    viewModel: SiraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPostType by remember { mutableIntStateOf(0) } // 0: Company Job, 1: Quick Gig

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("post_screen_container")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Post on SiraHub",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Reach thousands of skilled job seekers & freelancers across Ethiopia",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EthiopianFlagEmblem()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Tab Switcher: Job vs Gig
        TabRow(
            selectedTabIndex = selectedPostType,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedPostType]),
                    color = if (selectedPostType == 0) EthioEmeraldPrimary else EthioGoldSecondary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_type_tab_row")
        ) {
            Tab(
                selected = selectedPostType == 0,
                onClick = { selectedPostType = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (selectedPostType == 0) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Company Job",
                            fontWeight = if (selectedPostType == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedPostType == 0) EthioEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.testTag("tab_post_job")
            )
            Tab(
                selected = selectedPostType == 1,
                onClick = { selectedPostType = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = if (selectedPostType == 1) EthioGoldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Freelance Gig / Task",
                            fontWeight = if (selectedPostType == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedPostType == 1) EthioGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.testTag("tab_post_gig")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedPostType == 0) {
            PostJobForm(viewModel = viewModel)
        } else {
            PostGigForm(viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobForm(viewModel: SiraViewModel) {
    val companyProfile by viewModel.companyProfile.collectAsState()
    val isCompany = viewModel.accountType.collectAsState().value == com.example.model.AccountType.COMPANY_EMPLOYER

    var title by remember { mutableStateOf("") }
    var company by remember(companyProfile.name) {
        mutableStateOf(if (isCompany) companyProfile.name else "")
    }
    var location by remember(companyProfile.location) {
        mutableStateOf(if (isCompany) companyProfile.location else "Addis Ababa (Bole)")
    }
    var category by remember { mutableStateOf("Tech & Software") }
    var salaryMinText by remember { mutableStateOf("45000") }
    var salaryMaxText by remember { mutableStateOf("65000") }
    var description by remember { mutableStateOf("") }
    var requirementsText by remember { mutableStateOf("Relevant university degree (e.g. AAU / ASTU)\nFluency in Amharic & English\n3+ years industry experience") }
    var benefitsText by remember { mutableStateOf("Comprehensive medical insurance\nTransport & telecom allowance\nAnnual performance bonus") }
    var contactEmail by remember(companyProfile.contactEmail) {
        mutableStateOf(if (isCompany) companyProfile.contactEmail else "careers@company.et")
    }
    var contactPhone by remember(companyProfile.contactPhone) {
        mutableStateOf(if (isCompany) companyProfile.contactPhone else "+251 91 123 4567")
    }

    var selectedEmploymentType by remember { mutableStateOf(EmploymentType.FULL_TIME) }
    var selectedWorkplaceType by remember { mutableStateOf(WorkplaceType.HYBRID) }
    var selectedExperienceLevel by remember { mutableStateOf(ExperienceLevel.MID) }

    // Populated on submit. Each entry highlights one field and explains what is wrong with it.
    var fieldErrors by remember { mutableStateOf<List<FieldError>>(emptyList()) }

    val categories = listOf(
        "Tech & Software", "Banking & Finance", "NGO & Development",
        "Engineering & Construction", "Sales & Marketing", "Design & Creative", "Healthcare & Pharma"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Company & Job Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EthioEmeraldDark
            )
            Spacer(modifier = Modifier.height(14.dp))

            FieldContainer(errorRes = fieldErrors.errorFor(FormField.JOB_TITLE)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title (e.g. Senior Mobile Developer)") },
                    singleLine = true,
                    isError = fieldErrors.errorFor(FormField.JOB_TITLE) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_job_title_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FieldContainer(errorRes = fieldErrors.errorFor(FormField.JOB_COMPANY)) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / Organization Name") },
                    singleLine = true,
                    isError = fieldErrors.errorFor(FormField.JOB_COMPANY) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_job_company_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FieldContainer(errorRes = fieldErrors.errorFor(FormField.JOB_LOCATION)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Addis Ababa - Bole, Hawassa, Remote)") },
                    singleLine = true,
                    isError = fieldErrors.errorFor(FormField.JOB_LOCATION) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_job_location_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Selector
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Industry Category") },
                    isError = fieldErrors.errorFor(FormField.JOB_CATEGORY) != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Salary Range in ETB
            Text(text = "Monthly Salary Range (ETB)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldContainer(
                    errorRes = fieldErrors.errorFor(FormField.JOB_SALARY_MIN),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = salaryMinText,
                        onValueChange = { salaryMinText = it.filter { c -> c.isDigit() } },
                        label = { Text("Min (ETB)") },
                        singleLine = true,
                        isError = fieldErrors.errorFor(FormField.JOB_SALARY_MIN) != null,
                        modifier = Modifier.fillMaxWidth().testTag("post_job_min_salary_input")
                    )
                }
                FieldContainer(
                    errorRes = fieldErrors.errorFor(FormField.JOB_SALARY_MAX),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = salaryMaxText,
                        onValueChange = { salaryMaxText = it.filter { c -> c.isDigit() } },
                        label = { Text("Max (ETB)") },
                        singleLine = true,
                        isError = fieldErrors.errorFor(FormField.JOB_SALARY_MAX) != null,
                        modifier = Modifier.fillMaxWidth().testTag("post_job_max_salary_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldContainer(errorRes = fieldErrors.errorFor(FormField.JOB_DESCRIPTION)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Job Description & Scope") },
                    placeholder = { Text("Describe the day-to-day responsibilities, mission, and team...") },
                    minLines = 3,
                    maxLines = 6,
                    isError = fieldErrors.errorFor(FormField.JOB_DESCRIPTION) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_job_desc_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = requirementsText,
                onValueChange = { requirementsText = it },
                label = { Text("Key Requirements (one per line)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = benefitsText,
                onValueChange = { benefitsText = it },
                label = { Text("Perks & Benefits (one per line)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Application Contact Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Contact Phone (+251...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    // Validate every field before touching the network so the user is told exactly
                    // which input is wrong, rather than getting a generic server error.
                    val found = FormValidators.validateJobForm(
                        title = title,
                        company = company,
                        location = location,
                        category = category,
                        description = description,
                        salaryMinText = salaryMinText,
                        salaryMaxText = salaryMaxText,
                        contactEmail = contactEmail
                    )
                    fieldErrors = found

                    if (found.isEmpty()) {
                        val reqs = requirementsText.lines().filter { it.isNotBlank() }
                        val bens = benefitsText.lines().filter { it.isNotBlank() }
                        viewModel.postCompanyJob(
                            title = title.trim(),
                            company = company.trim(),
                            location = location.trim(),
                            category = category,
                            employmentType = selectedEmploymentType,
                            workplaceType = selectedWorkplaceType,
                            experienceLevel = selectedExperienceLevel,
                            salaryMinETB = salaryMinText.trim().toLongOrNull() ?: 0L,
                            salaryMaxETB = salaryMaxText.trim().toLongOrNull() ?: 0L,
                            description = description.trim(),
                            requirements = reqs,
                            benefits = bens,
                            contactEmail = contactEmail.trim(),
                            contactPhone = contactPhone.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioEmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_job_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Job Opening", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostGigForm(viewModel: SiraViewModel) {
    val userProfile by viewModel.profile.collectAsState()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Translation & Writing") }
    var budgetText by remember { mutableStateOf("15000") }
    var durationText by remember { mutableStateOf("3 Days") }
    var description by remember { mutableStateOf("") }
    var deliverablesText by remember { mutableStateOf("Completed document or project files\nOne round of revisions") }
    var skillsText by remember { mutableStateOf("Amharic, English, Quick Turnaround") }
    var clientPhone by remember { mutableStateOf(userProfile.phone) }
    var clientTelegram by remember { mutableStateOf(userProfile.telegramHandle) }
    var isUrgent by remember { mutableStateOf(false) }

    // Populated on submit. Each entry highlights one field and explains what is wrong with it.
    var gigFieldErrors by remember { mutableStateOf<List<FieldError>>(emptyList()) }

    val categories = listOf(
        "Translation & Writing", "Graphic Design", "Web & Mobile Dev",
        "Photography & Video", "Accounting & Finance", "Tutoring & Education", "Errands & Logistics"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Freelance Task / Gig Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EthioGoldDark
            )
            Spacer(modifier = Modifier.height(14.dp))

            FieldContainer(errorRes = gigFieldErrors.errorFor(FormField.GIG_TITLE)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Gig Title (e.g. Translate 15-page document)") },
                    singleLine = true,
                    isError = gigFieldErrors.errorFor(FormField.GIG_TITLE) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_gig_title_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Selector
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Task Category") },
                    isError = gigFieldErrors.errorFor(FormField.GIG_CATEGORY) != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldContainer(
                    errorRes = gigFieldErrors.errorFor(FormField.GIG_BUDGET),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                        label = { Text("Budget (ETB)") },
                        singleLine = true,
                        isError = gigFieldErrors.errorFor(FormField.GIG_BUDGET) != null,
                        modifier = Modifier.fillMaxWidth().testTag("post_gig_budget_input")
                    )
                }
                FieldContainer(
                    errorRes = gigFieldErrors.errorFor(FormField.GIG_DURATION),
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Turnaround (e.g. 3 Days)") },
                        singleLine = true,
                        isError = gigFieldErrors.errorFor(FormField.GIG_DURATION) != null,
                        modifier = Modifier.fillMaxWidth().testTag("post_gig_duration_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FieldContainer(errorRes = gigFieldErrors.errorFor(FormField.GIG_DESCRIPTION)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Description") },
                    placeholder = { Text("Describe what needs to be done, guidelines, and context...") },
                    minLines = 3,
                    maxLines = 6,
                    isError = gigFieldErrors.errorFor(FormField.GIG_DESCRIPTION) != null,
                    modifier = Modifier.fillMaxWidth().testTag("post_gig_desc_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = deliverablesText,
                onValueChange = { deliverablesText = it },
                label = { Text("Expected Deliverables (one per line)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = skillsText,
                onValueChange = { skillsText = it },
                label = { Text("Required Skills (comma separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = clientTelegram,
                    onValueChange = { clientTelegram = it },
                    label = { Text("Telegram (@handle)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Mark as Urgent Task", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(text = "Highlights gig with urgent tag for faster bids", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isUrgent,
                    onCheckedChange = { isUrgent = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    // Validate before touching the network so the user is told exactly which input
                    // is wrong, instead of receiving a generic server error after a round trip.
                    val found = FormValidators.validateGigForm(
                        title = title,
                        category = category,
                        budgetText = budgetText,
                        durationText = durationText,
                        description = description
                    )
                    gigFieldErrors = found

                    if (found.isEmpty()) {
                        val delivs = deliverablesText.lines().filter { it.isNotBlank() }
                        val skills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        viewModel.postShortTermGig(
                            title = title.trim(),
                            category = category,
                            budgetETB = budgetText.trim().toLongOrNull() ?: 0L,
                            pricingType = GigPricingType.FIXED,
                            durationText = durationText.trim(),
                            description = description.trim(),
                            deliverables = delivs,
                            requiredSkills = skills,
                            clientPhone = clientPhone.trim(),
                            clientTelegram = clientTelegram.trim(),
                            isUrgent = isUrgent
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioGoldSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_gig_btn")
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Freelance Gig", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
