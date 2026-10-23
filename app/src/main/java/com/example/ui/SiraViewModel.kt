package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AuthActionResult
import com.example.data.SiraRepository
import com.example.data.SyncResult
import com.example.data.core.DataResult
import com.example.data.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.model.AccountType
import com.example.model.AppSettings
import com.example.model.ApplicationStatus
import com.example.model.AuthMode
import com.example.model.AuthUser
import com.example.model.CompanyProfile
import com.example.model.EducationItem
import com.example.model.EmploymentType
import com.example.model.ExperienceItem
import com.example.model.ExperienceLevel
import com.example.model.GigBid
import com.example.model.GigPost
import com.example.model.GigPricingType
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.UserProfile
import com.example.model.WorkplaceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltViewModel
class SiraViewModel @Inject constructor(
    application: Application,
    private val jobsRepository: JobsRepository
) : AndroidViewModel(application) {

    // Still the legacy store for gigs, applications, bids, profile, company and bookmarks.
    // Jobs have moved to JobsRepository; this class is deleted once every domain has migrated.
    private val repository = SiraRepository(application.applicationContext)

    /** Live jobs from the Room cache, maintained by [JobsRepository]. */
    val jobs: StateFlow<List<JobPost>> = jobsRepository
        .observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gigs = repository.gigs
    val profile = repository.profile
    val companyProfile = repository.companyProfile
    val applications = repository.applications
    val bids = repository.bids
    val bookmarkedJobIds = repository.bookmarkedJobIds
    val bookmarkedGigIds = repository.bookmarkedGigIds
    val supabaseManager = repository.supabaseManager
    val isSyncing = repository.isSyncing
    val lastSyncStatus = repository.lastSyncStatus

    // User & Authentication State
    val currentUser: StateFlow<AuthUser?> = repository.currentUser
    val appSettings: StateFlow<AppSettings> = repository.appSettings

    // Account Type is owned by the server (profiles.account_type) and mirrored from the
    // signed-in session. It is derived from currentUser only - there is no local override.
    val accountType: StateFlow<AccountType> = repository.currentUser
        .map { it?.accountType ?: AccountType.JOB_SEEKER }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repository.currentUser.value?.accountType ?: AccountType.JOB_SEEKER
        )

    // Auth Dialog & Settings Modal States
    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authDialogMode = MutableStateFlow(AuthMode.REGISTER)
    val authDialogMode: StateFlow<AuthMode> = _authDialogMode.asStateFlow()

    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen: StateFlow<Boolean> = _showSettingsScreen.asStateFlow()

    // Greeting / Welcome Onboarding Screen State
    private val _showWelcomeScreen = MutableStateFlow(
        !repository.hasSeenWelcome() || (repository.currentUser.value?.isLoggedIn != true)
    )
    val showWelcomeScreen: StateFlow<Boolean> = _showWelcomeScreen.asStateFlow()

    // Navigation & View State
    private val _currentTab = MutableStateFlow(0) // 0: Jobs, 1: Gigs, 2: Post, 3: Activity/Applicants, 4: Profile/Company
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Jobs Search & Filter
    private val _jobSearchQuery = MutableStateFlow("")
    val jobSearchQuery: StateFlow<String> = _jobSearchQuery.asStateFlow()

    private val _selectedJobCategory = MutableStateFlow("All")
    val selectedJobCategory: StateFlow<String> = _selectedJobCategory.asStateFlow()

    private val _selectedJobLocation = MutableStateFlow("All")
    val selectedJobLocation: StateFlow<String> = _selectedJobLocation.asStateFlow()

    // Gigs Search & Filter
    private val _gigSearchQuery = MutableStateFlow("")
    val gigSearchQuery: StateFlow<String> = _gigSearchQuery.asStateFlow()

    private val _selectedGigCategory = MutableStateFlow("All")
    val selectedGigCategory: StateFlow<String> = _selectedGigCategory.asStateFlow()

    // Active detail selection
    private val _selectedJobForDetail = MutableStateFlow<JobPost?>(null)
    val selectedJobForDetail: StateFlow<JobPost?> = _selectedJobForDetail.asStateFlow()

    private val _selectedGigForDetail = MutableStateFlow<GigPost?>(null)
    val selectedGigForDetail: StateFlow<GigPost?> = _selectedGigForDetail.asStateFlow()

    // Application Modal State
    private val _applyingJob = MutableStateFlow<JobPost?>(null)
    val applyingJob: StateFlow<JobPost?> = _applyingJob.asStateFlow()

    // Bid Modal State
    private val _biddingGig = MutableStateFlow<GigPost?>(null)
    val biddingGig: StateFlow<GigPost?> = _biddingGig.asStateFlow()

    // CV Preview Modal State
    private val _showCvPreview = MutableStateFlow(false)
    val showCvPreview: StateFlow<Boolean> = _showCvPreview.asStateFlow()

    // Edit Profile Modal State
    private val _showEditProfile = MutableStateFlow(false)
    val showEditProfile: StateFlow<Boolean> = _showEditProfile.asStateFlow()

    // Edit Company Profile Modal State
    private val _showEditCompanyProfile = MutableStateFlow(false)
    val showEditCompanyProfile: StateFlow<Boolean> = _showEditCompanyProfile.asStateFlow()

    // Company Applicant Dossier Review Modal
    private val _selectedApplicantForReview = MutableStateFlow<JobApplication?>(null)
    val selectedApplicantForReview: StateFlow<JobApplication?> = _selectedApplicantForReview.asStateFlow()

    // User Feedback Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Activity Screen Sub-Tab
    private val _activitySubTab = MutableStateFlow(0) // 0: Applications, 1: Gigs Bids, 2: My Posts, 3: Saved
    val activitySubTab: StateFlow<Int> = _activitySubTab.asStateFlow()

    // Filtered Jobs
    val filteredJobs: StateFlow<List<JobPost>> = combine(
        jobs, jobSearchQuery, selectedJobCategory, selectedJobLocation
    ) { allJobs, query, category, location ->
        allJobs.filter { job ->
            val matchesQuery = query.isBlank() ||
                job.title.contains(query, ignoreCase = true) ||
                job.company.contains(query, ignoreCase = true) ||
                job.description.contains(query, ignoreCase = true) ||
                job.location.contains(query, ignoreCase = true) ||
                job.requirements.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = category == "All" || job.category.equals(category, ignoreCase = true)
            val matchesLocation = location == "All" || job.location.contains(location, ignoreCase = true)

            matchesQuery && matchesCategory && matchesLocation
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Gigs
    val filteredGigs: StateFlow<List<GigPost>> = combine(
        gigs, gigSearchQuery, selectedGigCategory
    ) { allGigs, query, category ->
        allGigs.filter { gig ->
            val matchesQuery = query.isBlank() ||
                gig.title.contains(query, ignoreCase = true) ||
                gig.clientName.contains(query, ignoreCase = true) ||
                gig.description.contains(query, ignoreCase = true) ||
                gig.requiredSkills.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = category == "All" || gig.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Company specific jobs (all jobs belonging to this company)
    val companyJobs: StateFlow<List<JobPost>> = combine(jobs, companyProfile) { allJobs, company ->
        allJobs.filter {
            it.company.equals(company.name, ignoreCase = true) || it.isPostedByUser
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Company specific applicants (applications submitted to this company's jobs)
    val companyApplicants: StateFlow<List<JobApplication>> = combine(applications, companyProfile, companyJobs) { allApps, company, cJobs ->
        val jobIds = cJobs.map { it.id }.toSet()
        allApps.filter { app ->
            jobIds.contains(app.jobId) || app.companyName.equals(company.name, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User posted listings
    val userPostedJobs: StateFlow<List<JobPost>> = jobs
        .map { allJobs -> allJobs.filter { it.isPostedByUser } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPostedGigs: StateFlow<List<GigPost>> = gigs
        .map { allGigs -> allGigs.filter { it.isPostedByUser } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarked items
    val bookmarkedJobs: StateFlow<List<JobPost>> = combine(jobs, bookmarkedJobIds) { allJobs, ids ->
        allJobs.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedGigs: StateFlow<List<GigPost>> = combine(gigs, bookmarkedGigIds) { allGigs, ids ->
        allGigs.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State Mutators
    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setActivitySubTab(index: Int) {
        _activitySubTab.value = index
    }

    fun setJobSearchQuery(query: String) {
        _jobSearchQuery.value = query
    }

    fun setJobCategory(category: String) {
        _selectedJobCategory.value = category
    }

    fun setJobLocation(location: String) {
        _selectedJobLocation.value = location
    }

    fun setGigSearchQuery(query: String) {
        _gigSearchQuery.value = query
    }

    fun setGigCategory(category: String) {
        _selectedGigCategory.value = category
    }

    fun openJobDetail(job: JobPost?) {
        _selectedJobForDetail.value = job
    }

    fun openGigDetail(gig: GigPost?) {
        _selectedGigForDetail.value = gig
    }

    fun openApplyModal(job: JobPost) {
        _applyingJob.value = job
    }

    fun closeApplyModal() {
        _applyingJob.value = null
    }

    fun openBidModal(gig: GigPost) {
        _biddingGig.value = gig
    }

    fun closeBidModal() {
        _biddingGig.value = null
    }

    fun openApplicantReview(app: JobApplication?) {
        _selectedApplicantForReview.value = app
    }

    fun updateApplicantStatus(applicationId: String, newStatus: ApplicationStatus) {
        repository.updateApplicationStatus(applicationId, newStatus)
        _selectedApplicantForReview.value = _selectedApplicantForReview.value?.copy(status = newStatus)
        _snackbarMessage.value = "Applicant status changed to: ${newStatus.label}"
    }

    fun setShowCvPreview(show: Boolean) {
        _showCvPreview.value = show
    }

    fun setShowEditProfile(show: Boolean) {
        _showEditProfile.value = show
    }

    fun setShowEditCompanyProfile(show: Boolean) {
        _showEditCompanyProfile.value = show
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleJobBookmark(jobId: String) {
        repository.toggleJobBookmark(jobId)
        val isNowBookmarked = !bookmarkedJobIds.value.contains(jobId)
        _snackbarMessage.value = if (isNowBookmarked) "Job saved to your bookmarks" else "Job removed from bookmarks"
    }

    fun toggleGigBookmark(gigId: String) {
        repository.toggleGigBookmark(gigId)
        val isNowBookmarked = !bookmarkedGigIds.value.contains(gigId)
        _snackbarMessage.value = if (isNowBookmarked) "Gig saved to your bookmarks" else "Gig removed from bookmarks"
    }

    // Application submission
    fun submitJobApplication(jobId: String, coverNote: String, expectedSalary: Long) {
        viewModelScope.launch {
            val app = repository.applyToJob(jobId, coverNote, expectedSalary)
            closeApplyModal()
            _snackbarMessage.value = "Application sent to ${app.companyName}! Track in My Activity."
        }
    }

    // Bid submission
    fun submitGigBid(gigId: String, amountETB: Long, deliveryDays: String, pitch: String) {
        viewModelScope.launch {
            val bid = repository.submitBid(gigId, amountETB, deliveryDays, pitch)
            closeBidModal()
            _snackbarMessage.value = "Bid of ${bid.formattedBid} submitted to ${bid.clientName}!"
        }
    }

    // Post a Job
    fun postCompanyJob(
        title: String,
        company: String,
        location: String,
        category: String,
        employmentType: EmploymentType,
        workplaceType: WorkplaceType,
        experienceLevel: ExperienceLevel,
        salaryMinETB: Long,
        salaryMaxETB: Long,
        description: String,
        requirements: List<String>,
        benefits: List<String>,
        contactEmail: String,
        contactPhone: String
    ) {
        val newJob = JobPost(
            // Placeholder only: the server assigns the real uuid on insert. Nothing reads this id.
            id = "",
            title = title,
            company = company,
            location = location,
            category = category,
            employmentType = employmentType,
            workplaceType = workplaceType,
            experienceLevel = experienceLevel,
            salaryMinETB = salaryMinETB,
            salaryMaxETB = salaryMaxETB,
            description = description,
            responsibilities = listOf("Deliver high quality results adhering to Ethiopian industry standards", "Collaborate effectively with team members"),
            requirements = requirements,
            benefits = benefits,
            postedTimeAgo = "Just now",
            deadline = "Open until filled",
            applicantsCount = 0,
            isFeatured = true,
            isUrgent = false,
            isVerifiedCompany = true,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            isPostedByUser = true
        )
        viewModelScope.launch {
            when (val result = jobsRepository.createJob(newJob)) {
                is DataResult.Success -> {
                    _snackbarMessage.value = "Job \"$title\" published successfully on SiraHub!"
                    _currentTab.value = 0 // Navigate to Jobs tab
                }

                is DataResult.NetworkError ->
                    _snackbarMessage.value = getApplication<Application>()
                        .getString(R.string.error_network)

                is DataResult.AuthError ->
                    _snackbarMessage.value = getApplication<Application>()
                        .getString(R.string.error_auth_expired)

                is DataResult.ValidationError ->
                    _snackbarMessage.value = getApplication<Application>()
                        .getString(result.messageRes)

                is DataResult.ServerError ->
                    _snackbarMessage.value = getApplication<Application>()
                        .getString(R.string.error_server)
            }
        }
    }

    // Post a Gig
    fun postShortTermGig(
        title: String,
        category: String,
        budgetETB: Long,
        pricingType: GigPricingType,
        durationText: String,
        description: String,
        deliverables: List<String>,
        requiredSkills: List<String>,
        clientPhone: String,
        clientTelegram: String,
        isUrgent: Boolean
    ) {
        val currentProf = profile.value
        val newGig = GigPost(
            id = "gig_${UUID.randomUUID()}",
            title = title,
            clientName = if (currentUser.value?.accountType == AccountType.COMPANY_EMPLOYER) companyProfile.value.name else currentProf.fullName.ifBlank { "Client" },
            clientType = if (currentUser.value?.accountType == AccountType.COMPANY_EMPLOYER) "Corporate Employer" else "Individual Client",
            location = currentProf.cityLocation.ifBlank { "Addis Ababa" },
            category = category,
            budgetETB = budgetETB,
            pricingType = pricingType,
            durationText = durationText,
            description = description,
            deliverables = deliverables,
            requiredSkills = requiredSkills,
            bidsCount = 0,
            postedTimeAgo = "Just now",
            clientPhone = clientPhone.ifBlank { currentProf.phone },
            clientTelegram = clientTelegram.ifBlank { currentProf.telegramHandle },
            isPostedByUser = true,
            isUrgent = isUrgent
        )
        repository.addGig(newGig)
        _snackbarMessage.value = "Gig \"$title\" posted! Freelancers can now bid."
        _currentTab.value = 1 // Navigate to Gigs tab
    }

    // Profile updates
    fun updateProfile(newProfile: UserProfile) {
        repository.updateProfile(newProfile)
        _showEditProfile.value = false
        _snackbarMessage.value = "Your CV and Profile have been updated!"
    }

    fun updateCompanyProfile(newCompany: CompanyProfile) {
        repository.updateCompanyProfile(newCompany)
        _showEditCompanyProfile.value = false
        _snackbarMessage.value = "Company Profile updated successfully!"
    }

    fun addSkill(skill: String) {
        if (skill.isBlank()) return
        val current = profile.value
        if (!current.skills.contains(skill.trim())) {
            val updated = current.copy(skills = current.skills + skill.trim())
            repository.updateProfile(updated)
        }
    }

    fun removeSkill(skill: String) {
        val current = profile.value
        val updated = current.copy(skills = current.skills.filter { it != skill })
        repository.updateProfile(updated)
    }

    fun addExperience(item: ExperienceItem) {
        val current = profile.value
        val updated = current.copy(experienceList = current.experienceList + item)
        repository.updateProfile(updated)
        _snackbarMessage.value = "Work experience added to CV"
    }

    fun deleteExperience(id: String) {
        val current = profile.value
        val updated = current.copy(experienceList = current.experienceList.filter { it.id != id })
        repository.updateProfile(updated)
    }

    fun addEducation(item: EducationItem) {
        val current = profile.value
        val updated = current.copy(educationList = current.educationList + item)
        repository.updateProfile(updated)
        _snackbarMessage.value = "Education added to CV"
    }

    fun deleteEducation(id: String) {
        val current = profile.value
        val updated = current.copy(educationList = current.educationList.filter { it.id != id })
        repository.updateProfile(updated)
    }

    fun clearJobSeekerCv() {
        val user = currentUser.value
        val fresh = UserProfile(
            id = user?.id ?: "user_default",
            fullName = user?.fullName ?: "",
            email = user?.email ?: "",
            phone = user?.phone ?: "",
            titleHeadline = "",
            bioSummary = "",
            telegramHandle = "",
            cityLocation = "Addis Ababa, Ethiopia",
            skills = emptyList(),
            languages = listOf("Amharic", "English"),
            educationList = emptyList(),
            experienceList = emptyList(),
            portfolioLinks = emptyList(),
            expectedSalaryMinETB = 0,
            availabilityStatus = "Actively seeking opportunities",
            resumeUploadedDate = "Not uploaded yet"
        )
        repository.updateProfile(fresh)
        _snackbarMessage.value = "CV cleared. You can now fill your own CV from scratch!"
    }

    // Cloud database sync actions
    //
    // Jobs go through JobsRepository (Supabase -> Room -> Flow). Every other domain still goes
    // through the legacy SiraRepository until it is migrated.
    fun syncWithSupabase(showSnackbar: Boolean = true, onComplete: ((SyncResult) -> Unit)? = null) {
        viewModelScope.launch {
            val jobsResult = jobsRepository.refresh()
            val legacyResult = repository.syncWithSupabase()

            val message = if (jobsResult is DataResult.Success) {
                legacyResult.message
            } else {
                jobErrorMessage(jobsResult)
            }

            if (showSnackbar) {
                _snackbarMessage.value = message
            }
            onComplete?.invoke(legacyResult)
        }
    }

    /** Turns a failed job refresh into a plain-language message the user can act on. */
    private fun jobErrorMessage(result: DataResult<*>): String {
        val context = getApplication<Application>()
        return when (result) {
            is DataResult.NetworkError -> context.getString(R.string.error_network)
            is DataResult.AuthError -> context.getString(R.string.error_auth_expired)
            is DataResult.ValidationError -> context.getString(result.messageRes)
            is DataResult.ServerError -> context.getString(R.string.error_server)
            is DataResult.Success -> ""
        }
    }

    // ==========================================
    // AUTHENTICATION & SETTINGS UI CONTROLLERS
    // ==========================================
    fun openAuthDialog(mode: AuthMode = AuthMode.REGISTER) {
        _authDialogMode.value = mode
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun setAuthMode(mode: AuthMode) {
        _authDialogMode.value = mode
    }

    fun openSettingsScreen() {
        _showSettingsScreen.value = true
    }

    fun closeSettingsScreen() {
        _showSettingsScreen.value = false
    }

    fun openWelcomeScreen() {
        _showWelcomeScreen.value = true
    }

    fun closeWelcomeScreen() {
        repository.setSeenWelcome(true)
        _showWelcomeScreen.value = false
    }

    fun exploreAsGuest() {
        repository.setSeenWelcome(true)
        _showWelcomeScreen.value = false
        _snackbarMessage.value = "Browsing SiraHub in Guest Mode. Register or sign in anytime!"
    }

    suspend fun login(email: String, pass: String): AuthActionResult {
        val res = repository.login(email, pass)
        if (res.isSuccess) {
            _showAuthDialog.value = false
            if (res.signedIn) {
                _showWelcomeScreen.value = false
            }
            _snackbarMessage.value = res.message
        }
        return res
    }

    suspend fun register(
        fullName: String,
        email: String,
        pass: String,
        phone: String,
        accountType: AccountType,
        companyName: String = "",
        companyIndustry: String = ""
    ): AuthActionResult {
        val res = repository.register(
            fullName = fullName,
            email = email,
            pass = pass,
            phone = phone,
            accountType = accountType,
            companyName = companyName,
            companyIndustry = companyIndustry
        )
        if (res.isSuccess) {
            _showAuthDialog.value = false
            // Stay on the welcome/login screen when Supabase still requires email verification.
            if (res.signedIn) {
                _showWelcomeScreen.value = false
            }
            _snackbarMessage.value = res.message
        }
        return res
    }

    suspend fun logout() {
        repository.logout()
        _showSettingsScreen.value = false
        _showAuthDialog.value = false
        _showWelcomeScreen.value = true
        _snackbarMessage.value = "You have signed out from SiraHub"
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
        _snackbarMessage.value = "App preferences saved"
    }

    fun toggleTheme() {
        val current = appSettings.value
        val isCurrentlyDark = when (current.themeMode.uppercase()) {
            "LIGHT" -> false
            "DARK" -> true
            else -> current.darkThemeEnabled
        }
        val newMode = if (isCurrentlyDark) "LIGHT" else "DARK"
        val updated = current.copy(
            themeMode = newMode,
            darkThemeEnabled = (newMode == "DARK")
        )
        repository.updateSettings(updated)
        _snackbarMessage.value = if (newMode == "DARK") "Dark theme active" else "Clean light theme active"
    }

    fun setThemeMode(mode: String) {
        val normalized = mode.uppercase()
        val updated = appSettings.value.copy(
            themeMode = normalized,
            darkThemeEnabled = (normalized == "DARK")
        )
        repository.updateSettings(updated)
        _snackbarMessage.value = when (normalized) {
            "LIGHT" -> "Clean light theme active"
            "DARK" -> "Dark theme active"
            else -> "System theme active"
        }
    }

    fun clearCacheAndReset() {
        repository.clearCacheAndReset()
        _snackbarMessage.value = "App data and cache reset to default sample state"
    }
}

