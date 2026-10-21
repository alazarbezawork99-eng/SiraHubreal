package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SiraRepository
import com.example.model.AccountType
import com.example.model.AppSettings
import com.example.model.ApplicationStatus
import com.example.model.AuthMode
import com.example.model.AuthUser
import com.example.model.CompanyProfile
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SiraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SiraRepository(application.applicationContext)

    val jobs = repository.jobs
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

    // Account Type: Job Seeker vs Company / Employer vs Gig Poster
    private val _accountType = MutableStateFlow(repository.currentUser.value?.accountType ?: AccountType.JOB_SEEKER)
    val accountType: StateFlow<AccountType> = _accountType.asStateFlow()

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

    init {
        // Keep active role aligned with currentUser role
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null) {
                    _accountType.value = user.accountType
                }
            }
        }
    }


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

    // Supabase Settings Modal State
    private val _showSupabaseSettings = MutableStateFlow(false)
    val showSupabaseSettings: StateFlow<Boolean> = _showSupabaseSettings.asStateFlow()

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
    val userPostedJobs: StateFlow<List<JobPost>> = combine(jobs) { list ->
        list[0].filter { it.isPostedByUser }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPostedGigs: StateFlow<List<GigPost>> = combine(gigs) { list ->
        list[0].filter { it.isPostedByUser }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setAccountType(type: AccountType) {
        switchAccountRole(type)
    }

    fun toggleAccountType() {
        val next = when (_accountType.value) {
            AccountType.JOB_SEEKER -> AccountType.COMPANY_EMPLOYER
            AccountType.COMPANY_EMPLOYER -> AccountType.GIG_CLIENT
            AccountType.GIG_CLIENT -> AccountType.JOB_SEEKER
        }
        switchAccountRole(next)
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

    fun setShowSupabaseSettings(show: Boolean) {
        _showSupabaseSettings.value = show
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
            id = "job_${UUID.randomUUID()}",
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
        repository.addJob(newJob)
        _snackbarMessage.value = "Job \"$title\" published successfully on SiraHub!"
        _currentTab.value = 0 // Navigate to Jobs tab
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
            clientName = if (_accountType.value == AccountType.COMPANY_EMPLOYER) companyProfile.value.name else currentProf.fullName.ifBlank { "Client" },
            clientType = if (_accountType.value == AccountType.COMPANY_EMPLOYER) "Corporate Employer" else "Individual Client",
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

    // Supabase actions
    fun saveSupabaseConfig(url: String, key: String) {
        supabaseManager.supabaseUrl = url
        supabaseManager.supabaseAnonKey = key
        _snackbarMessage.value = "Supabase configuration saved! Starting cloud database sync..."
        if (supabaseManager.isConfigured) {
            syncWithSupabase(showSnackbar = true)
        }
    }

    fun syncWithSupabase(showSnackbar: Boolean = true, onComplete: ((com.example.data.SyncResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.syncWithSupabase()
            if (showSnackbar) {
                _snackbarMessage.value = result.message
            }
            onComplete?.invoke(result)
        }
    }

    suspend fun testSupabaseConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        supabaseManager.testConnection()
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

    fun login(email: String, pass: String): Pair<Boolean, String> {
        val res = repository.login(email, pass)
        if (res.first) {
            val role = repository.currentUser.value?.accountType ?: AccountType.JOB_SEEKER
            _accountType.value = role
            _showAuthDialog.value = false
            _showWelcomeScreen.value = false
            _snackbarMessage.value = res.second
        }
        return res
    }

    fun register(
        fullName: String,
        email: String,
        pass: String,
        phone: String,
        accountType: AccountType,
        companyName: String = "",
        companyIndustry: String = ""
    ): Pair<Boolean, String> {
        val res = repository.register(fullName, email, pass, phone, accountType, companyName, companyIndustry)
        if (res.first) {
            _accountType.value = accountType
            _showAuthDialog.value = false
            _showWelcomeScreen.value = false
            _snackbarMessage.value = res.second
        }
        return res
    }

    fun logout() {
        repository.logout()
        _showSettingsScreen.value = false
        _showAuthDialog.value = false
        _showWelcomeScreen.value = true
        _snackbarMessage.value = "You have signed out from SiraHub"
    }

    fun quickDemoLogin(role: AccountType) {
        repository.quickDemoLogin(role)
        _accountType.value = role
        _showAuthDialog.value = false
        _showWelcomeScreen.value = false
        _snackbarMessage.value = "Active profile set to demo ${role.label}"
    }

    fun switchAccountRole(role: AccountType) {
        repository.switchAccountRole(role)
        _accountType.value = role
        _snackbarMessage.value = "Switched to ${role.label} Account (${role.amharicBadge})"
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
        _snackbarMessage.value = "App preferences saved"
    }

    fun clearCacheAndReset() {
        repository.clearCacheAndReset()
        _accountType.value = repository.currentUser.value?.accountType ?: AccountType.JOB_SEEKER
        _snackbarMessage.value = "App data and cache reset to default sample state"
    }
}

