package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.supabase.SupabaseManager
import com.example.model.AccountType
import com.example.model.AppSettings
import com.example.model.ApplicationStatus
import com.example.model.AuthUser
import com.example.model.BidStatus
import com.example.model.CompanyProfile
import com.example.model.EducationItem
import com.example.model.EmploymentType
import com.example.model.ExperienceItem
import com.example.model.ExperienceLevel
import com.example.model.GigBid
import com.example.model.GigPost
import com.example.model.GigPricingType
import com.example.model.GigStatus
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.PortfolioLink
import com.example.model.UserProfile
import com.example.model.WorkplaceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SyncResult(
    val isSuccess: Boolean,
    val message: String,
    val jobsCount: Int = 0,
    val gigsCount: Int = 0,
    val applicationsCount: Int = 0,
    val bidsCount: Int = 0
)

class SiraRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sira_hub_prefs", Context.MODE_PRIVATE)

    val supabaseManager = SupabaseManager(context)
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncStatus = MutableStateFlow<String?>(null)
    val lastSyncStatus: StateFlow<String?> = _lastSyncStatus.asStateFlow()

    private val _jobs = MutableStateFlow<List<JobPost>>(emptyList())
    val jobs: StateFlow<List<JobPost>> = _jobs.asStateFlow()

    private val _gigs = MutableStateFlow<List<GigPost>>(emptyList())
    val gigs: StateFlow<List<GigPost>> = _gigs.asStateFlow()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _companyProfile = MutableStateFlow(CompanyProfile())
    val companyProfile: StateFlow<CompanyProfile> = _companyProfile.asStateFlow()

    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _bids = MutableStateFlow<List<GigBid>>(emptyList())
    val bids: StateFlow<List<GigBid>> = _bids.asStateFlow()

    private val _bookmarkedJobIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedJobIds: StateFlow<Set<String>> = _bookmarkedJobIds.asStateFlow()

    private val _bookmarkedGigIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedGigIds: StateFlow<Set<String>> = _bookmarkedGigIds.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(AuthUser())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    init {
        loadData()
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                syncWithSupabase()
            }
        }
    }

    private fun loadData() {
        // Load Bookmarks
        val savedJobBookmarks = prefs.getStringSet("bookmarked_jobs", emptySet()) ?: emptySet()
        _bookmarkedJobIds.value = savedJobBookmarks

        val savedGigBookmarks = prefs.getStringSet("bookmarked_gigs", emptySet()) ?: emptySet()
        _bookmarkedGigIds.value = savedGigBookmarks

        // Load Jobs
        val jobsJson = prefs.getString("jobs_json", null)
        if (jobsJson != null && jobsJson.isNotBlank()) {
            val list = parseJobsJson(jobsJson)
            _jobs.value = if (list.isNotEmpty()) list else SampleData.initialJobs
        } else {
            // Also seed a job specifically for the default Company Account "EthioFintech & Tech Labs"
            val companyJob = JobPost(
                id = "job_comp_default_1",
                title = "Senior Android & Payment Integration Architect",
                company = "EthioFintech & Tech Labs",
                location = "Addis Ababa (Bole Atlas)",
                category = "Tech & Software",
                employmentType = EmploymentType.FULL_TIME,
                workplaceType = WorkplaceType.HYBRID,
                experienceLevel = ExperienceLevel.SENIOR,
                salaryMinETB = 70000,
                salaryMaxETB = 90000,
                salaryPeriod = "month",
                description = "Lead our mobile engineering team developing secure merchant payment SDKs and consumer apps connected to Telebirr and National Bank of Ethiopia switches.",
                responsibilities = listOf("Architect robust Kotlin Compose applications", "Mentor engineering squads", "Lead code audits"),
                requirements = listOf("5+ years Android development", "Fintech security knowledge", "Fluency in Amharic and English"),
                benefits = listOf("Comprehensive health insurance", "Stock options", "Performance bonuses"),
                postedTimeAgo = "3 days ago",
                deadline = "Open until filled",
                applicantsCount = 3,
                isFeatured = true,
                isUrgent = false,
                isVerifiedCompany = true,
                contactEmail = "careers@ethiofintech.et",
                contactPhone = "+251 91 144 5566",
                isPostedByUser = true
            )
            _jobs.value = listOf(companyJob) + SampleData.initialJobs
        }

        // Load Gigs
        val gigsJson = prefs.getString("gigs_json", null)
        if (gigsJson != null && gigsJson.isNotBlank()) {
            val list = parseGigsJson(gigsJson)
            _gigs.value = if (list.isNotEmpty()) list else SampleData.initialGigs
        } else {
            _gigs.value = SampleData.initialGigs
        }

        // Load Job Seeker Profile
        val profileJson = prefs.getString("profile_json", null)
        if (profileJson != null && profileJson.isNotBlank()) {
            _profile.value = parseProfileJson(profileJson)
        } else {
            _profile.value = UserProfile()
        }

        // Load Company Profile
        val companyJson = prefs.getString("company_profile_json", null)
        if (companyJson != null && companyJson.isNotBlank()) {
            _companyProfile.value = parseCompanyJson(companyJson)
        } else {
            _companyProfile.value = CompanyProfile()
        }

        // Load Applications
        val appsJson = prefs.getString("applications_json", null)
        if (appsJson != null && appsJson.isNotBlank()) {
            _applications.value = parseApplicationsJson(appsJson)
        } else {
            // Seed realistic applicants so Company Dashboard shows applicant stats & candidate dossiers immediately
            _applications.value = listOf(
                JobApplication(
                    id = "app_demo_1",
                    jobId = "job_1",
                    jobTitle = "Senior Android Engineer (Fintech & Telebirr)",
                    companyName = "Ethio Telecom",
                    location = "Addis Ababa (Bole Medhanialem)",
                    applicantName = _profile.value.fullName,
                    applicantEmail = _profile.value.email,
                    applicantPhone = _profile.value.phone,
                    applicantHeadline = _profile.value.titleHeadline,
                    applicantSkills = _profile.value.skills,
                    applicantEducation = "B.Sc. in Computer Science - Addis Ababa University (AAU)",
                    applicantExperience = "4+ years Android & Fintech engineering",
                    coverNote = "I have extensive experience integrating Telebirr API with Jetpack Compose apps. Looking forward to discussing this opportunity.",
                    expectedSalaryETB = 75000,
                    appliedDate = "Yesterday",
                    status = ApplicationStatus.UNDER_REVIEW
                ),
                JobApplication(
                    id = "app_comp_cand_1",
                    jobId = "job_comp_default_1",
                    jobTitle = "Senior Android & Payment Integration Architect",
                    companyName = "EthioFintech & Tech Labs",
                    location = "Addis Ababa (Bole Atlas)",
                    applicantName = "Tadesse Hailu",
                    applicantEmail = "tadesse.hailu@aau.edu.et",
                    applicantPhone = "+251 92 455 7788",
                    applicantHeadline = "Senior Mobile Engineer & Security Specialist",
                    applicantSkills = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Telebirr Gateway", "CBE Birr", "Cryptography"),
                    applicantEducation = "M.Sc. in Software Engineering, Addis Ababa University (AAU)",
                    applicantExperience = "5 years developing high-throughput mobile banking apps at Dashen Bank and commercial fintechs.",
                    coverNote = "I have architected 3 major payment SDKs in Ethiopia with bank-grade encryption and CBE Birr APIs. I am eager to help lead EthioFintech's engineering expansion.",
                    expectedSalaryETB = 85000,
                    appliedDate = "2 days ago",
                    status = ApplicationStatus.SHORTLISTED
                ),
                JobApplication(
                    id = "app_comp_cand_2",
                    jobId = "job_comp_default_1",
                    jobTitle = "Senior Android & Payment Integration Architect",
                    companyName = "EthioFintech & Tech Labs",
                    location = "Addis Ababa (Bole Atlas)",
                    applicantName = "Bethlehem Worku",
                    applicantEmail = "betti.worku.dev@gmail.com",
                    applicantPhone = "+251 91 322 9900",
                    applicantHeadline = "Full-Stack Android & Microservices Developer",
                    applicantSkills = listOf("Kotlin", "Spring Boot", "Jetpack Compose", "PostgreSQL", "Docker", "REST APIs"),
                    applicantEducation = "B.Sc. in Information Technology, Hawassa University",
                    applicantExperience = "4 years building customer checkout systems and microservices in Addis Ababa.",
                    coverNote = "Passionate about scaling Ethiopian tech products. Strong background in both Android UI and robust backend integration.",
                    expectedSalaryETB = 78000,
                    appliedDate = "Yesterday",
                    status = ApplicationStatus.INTERVIEW
                ),
                JobApplication(
                    id = "app_comp_cand_3",
                    jobId = "job_comp_default_1",
                    jobTitle = "Senior Android & Payment Integration Architect",
                    companyName = "EthioFintech & Tech Labs",
                    location = "Addis Ababa (Bole Atlas)",
                    applicantName = "Yonas Mulugeta",
                    applicantEmail = "yonas.mulugeta@techmail.et",
                    applicantPhone = "+251 94 888 1122",
                    applicantHeadline = "Android Developer & Telecommunications Graduate",
                    applicantSkills = listOf("Android SDK", "Kotlin", "Java", "Git", "Clean Architecture"),
                    applicantEducation = "B.Sc. in Electrical & Computer Engineering, ASTU",
                    applicantExperience = "3 years developing client solutions and merchant mobile portals.",
                    coverNote = "Looking forward to bringing my problem solving and clean architecture experience to EthioFintech.",
                    expectedSalaryETB = 72000,
                    appliedDate = "Today",
                    status = ApplicationStatus.UNDER_REVIEW
                )
            )
        }

        // Load Bids
        val bidsJson = prefs.getString("bids_json", null)
        if (bidsJson != null && bidsJson.isNotBlank()) {
            _bids.value = parseBidsJson(bidsJson)
        } else {
            _bids.value = listOf(
                GigBid(
                    id = "bid_demo_1",
                    gigId = "gig_2",
                    gigTitle = "Brand Identity & Logo for Specialty Buna (Coffee) Shop",
                    clientName = "Kaffa Origin Coffee Roasters",
                    bidderName = _profile.value.fullName,
                    bidderPhone = _profile.value.phone,
                    bidAmountETB = 17500,
                    deliveryDays = "5 Days",
                    proposalPitch = "I can deliver a gorgeous, culturally inspired coffee branding kit incorporating traditional Jebena motifs with modern vector precision. I include 3 distinct concept directions.",
                    submittedDate = "2 days ago",
                    status = BidStatus.CLIENT_REVIEWING
                )
            )
        }

        // Load Current User Session
        val userJson = prefs.getString("auth_user_json", null)
        if (userJson != null && userJson.isNotBlank()) {
            _currentUser.value = parseUserJson(userJson)
        } else {
            _currentUser.value = AuthUser(isLoggedIn = false)
        }

        // Load App Settings
        val settingsJson = prefs.getString("app_settings_json", null)
        if (settingsJson != null && settingsJson.isNotBlank()) {
            _appSettings.value = parseSettingsJson(settingsJson)
        } else {
            _appSettings.value = AppSettings()
        }
    }

    // ==========================================
    // SUPABASE FULL TWO-WAY DATABASE SYNC
    // ==========================================
    suspend fun syncWithSupabase(): SyncResult = withContext(Dispatchers.IO) {
        if (!supabaseManager.isConfigured) {
            return@withContext SyncResult(
                isSuccess = false,
                message = "Supabase is not configured yet. Please configure your URL & Anon Key."
            )
        }

        try {
            _isSyncing.value = true
            _lastSyncStatus.value = "Fetching live records from Supabase..."

            val cloudJobs = supabaseManager.fetchJobs()
            val cloudGigs = supabaseManager.fetchGigs()
            val cloudApps = supabaseManager.fetchApplications()
            val cloudBids = supabaseManager.fetchBids()

            var updatedJobs = _jobs.value
            if (cloudJobs.isNotEmpty()) {
                val cloudIds = cloudJobs.map { it.id }.toSet()
                val localPending = _jobs.value.filter { it.isPostedByUser && !cloudIds.contains(it.id) }
                updatedJobs = localPending + cloudJobs
                _jobs.value = updatedJobs
                saveJobs(updatedJobs)
            }

            var updatedGigs = _gigs.value
            if (cloudGigs.isNotEmpty()) {
                val cloudIds = cloudGigs.map { it.id }.toSet()
                val localPending = _gigs.value.filter { it.isPostedByUser && !cloudIds.contains(it.id) }
                updatedGigs = localPending + cloudGigs
                _gigs.value = updatedGigs
                saveGigs(updatedGigs)
            }

            var updatedApps = _applications.value
            if (cloudApps.isNotEmpty()) {
                val cloudIds = cloudApps.map { it.id }.toSet()
                val localPending = _applications.value.filter { !cloudIds.contains(it.id) }
                updatedApps = localPending + cloudApps
                _applications.value = updatedApps
                saveApplications(updatedApps)
            }

            var updatedBids = _bids.value
            if (cloudBids.isNotEmpty()) {
                val cloudIds = cloudBids.map { it.id }.toSet()
                val localPending = _bids.value.filter { !cloudIds.contains(it.id) }
                updatedBids = localPending + cloudBids
                _bids.value = updatedBids
                saveBids(updatedBids)
            }

            val resultMsg = "Synced from Supabase: ${cloudJobs.size} jobs, ${cloudGigs.size} gigs, ${cloudApps.size} applicants"
            _lastSyncStatus.value = resultMsg
            _isSyncing.value = false

            SyncResult(
                isSuccess = true,
                message = resultMsg,
                jobsCount = cloudJobs.size,
                gigsCount = cloudGigs.size,
                applicationsCount = cloudApps.size,
                bidsCount = cloudBids.size
            )
        } catch (e: Exception) {
            _isSyncing.value = false
            val errMsg = "Sync error: ${e.localizedMessage ?: "Failed to connect to Supabase"}"
            _lastSyncStatus.value = errMsg
            SyncResult(isSuccess = false, message = errMsg)
        }
    }

    // Persistence Actions
    fun addJob(newJob: JobPost) {
        val updated = listOf(newJob) + _jobs.value
        _jobs.value = updated
        saveJobs(updated)

        // Sync to Supabase in background if configured
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.insertJob(newJob)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun addGig(newGig: GigPost) {
        val updated = listOf(newGig) + _gigs.value
        _gigs.value = updated
        saveGigs(updated)

        // Sync to Supabase in background if configured
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.insertGig(newGig)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        _profile.value = newProfile
        saveProfile(newProfile)
    }

    fun updateCompanyProfile(newCompany: CompanyProfile) {
        _companyProfile.value = newCompany
        saveCompanyProfile(newCompany)
    }

    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        val updated = _applications.value.map {
            if (it.id == applicationId) it.copy(status = newStatus) else it
        }
        _applications.value = updated
        saveApplications(updated)

        // Sync status update to Supabase in background
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.updateApplicationStatus(applicationId, newStatus)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun applyToJob(
        jobId: String,
        coverNote: String,
        expectedSalary: Long
    ): JobApplication {
        val job = _jobs.value.find { it.id == jobId }
        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
        val userProf = _profile.value

        val application = JobApplication(
            id = "app_${UUID.randomUUID()}",
            jobId = jobId,
            jobTitle = job?.title ?: "Job Opportunity",
            companyName = job?.company ?: "Company",
            location = job?.location ?: "Ethiopia",
            applicantName = userProf.fullName,
            applicantEmail = userProf.email,
            applicantPhone = userProf.phone,
            applicantHeadline = userProf.titleHeadline,
            applicantSkills = userProf.skills,
            applicantEducation = userProf.educationList.firstOrNull()?.let { "${it.degree}, ${it.institution}" } ?: "Higher Education Graduate",
            applicantExperience = userProf.experienceList.firstOrNull()?.let { "${it.roleTitle} at ${it.companyName}" } ?: "Professional Experience",
            coverNote = coverNote,
            expectedSalaryETB = expectedSalary,
            appliedDate = dateStr,
            status = ApplicationStatus.APPLIED
        )

        // Increment applicant count on job
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(applicantsCount = it.applicantsCount + 1) else it
        }
        saveJobs(_jobs.value)

        val updatedApps = listOf(application) + _applications.value
        _applications.value = updatedApps
        saveApplications(updatedApps)

        // Sync to Supabase in background
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.insertApplication(application)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }

        return application
    }

    fun submitBid(
        gigId: String,
        amountETB: Long,
        deliveryDays: String,
        pitch: String
    ): GigBid {
        val gig = _gigs.value.find { it.id == gigId }
        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
        val bid = GigBid(
            id = "bid_${UUID.randomUUID()}",
            gigId = gigId,
            gigTitle = gig?.title ?: "Gig Task",
            clientName = gig?.clientName ?: "Client",
            bidderName = _profile.value.fullName,
            bidderPhone = _profile.value.phone,
            bidAmountETB = amountETB,
            deliveryDays = deliveryDays,
            proposalPitch = pitch,
            submittedDate = dateStr,
            status = BidStatus.SUBMITTED
        )

        // Increment bids count on gig
        _gigs.value = _gigs.value.map {
            if (it.id == gigId) it.copy(bidsCount = it.bidsCount + 1) else it
        }
        saveGigs(_gigs.value)

        val updatedBids = listOf(bid) + _bids.value
        _bids.value = updatedBids
        saveBids(updatedBids)

        // Sync to Supabase in background
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.insertBid(bid)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }

        return bid
    }

    fun toggleJobBookmark(jobId: String) {
        val current = _bookmarkedJobIds.value.toMutableSet()
        if (current.contains(jobId)) {
            current.remove(jobId)
        } else {
            current.add(jobId)
        }
        _bookmarkedJobIds.value = current
        prefs.edit().putStringSet("bookmarked_jobs", current).apply()
    }

    fun toggleGigBookmark(gigId: String) {
        val current = _bookmarkedGigIds.value.toMutableSet()
        if (current.contains(gigId)) {
            current.remove(gigId)
        } else {
            current.add(gigId)
        }
        _bookmarkedGigIds.value = current
        prefs.edit().putStringSet("bookmarked_gigs", current).apply()
    }

    fun deleteJob(jobId: String) {
        val updated = _jobs.value.filter { it.id != jobId }
        _jobs.value = updated
        saveJobs(updated)

        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.deleteJob(jobId)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun deleteGig(gigId: String) {
        val updated = _gigs.value.filter { it.id != gigId }
        _gigs.value = updated
        saveGigs(updated)

        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try {
                    supabaseManager.deleteGig(gigId)
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    // ==========================================
    // AUTHENTICATION & USER MANAGEMENT
    // ==========================================
    fun login(email: String, pass: String): Pair<Boolean, String> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return false to "Please enter your email address"
        }
        if (pass.length < 4) {
            return false to "Password must be at least 4 characters"
        }

        val existing = _currentUser.value
        val determinedRole = when {
            trimmedEmail.contains("ethiofintech", ignoreCase = true) || trimmedEmail.contains("comp", ignoreCase = true) || trimmedEmail.contains("employer", ignoreCase = true) -> AccountType.COMPANY_EMPLOYER
            trimmedEmail.contains("kaffa", ignoreCase = true) || trimmedEmail.contains("client", ignoreCase = true) -> AccountType.GIG_CLIENT
            else -> existing?.accountType ?: AccountType.JOB_SEEKER
        }

        val name = if (existing != null && existing.email.equals(trimmedEmail, ignoreCase = true)) {
            existing.fullName
        } else {
            trimmedEmail.substringBefore("@").replace(".", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }

        val user = AuthUser(
            id = existing?.id ?: "user_${UUID.randomUUID()}",
            email = trimmedEmail,
            fullName = name.ifBlank { "SiraHub User" },
            phone = existing?.phone ?: "+251 91 123 4567",
            accountType = determinedRole,
            companyName = if (determinedRole == AccountType.COMPANY_EMPLOYER) _companyProfile.value.name else "",
            companyIndustry = if (determinedRole == AccountType.COMPANY_EMPLOYER) _companyProfile.value.industry else "",
            isLoggedIn = true,
            registeredDate = existing?.registeredDate ?: "October 2024"
        )

        _currentUser.value = user
        saveUser(user)

        // Push profile to Supabase in background
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try { supabaseManager.insertProfile(user) } catch (_: Exception) {}
            }
        }

        setSeenWelcome(true)
        return true to "Welcome back, ${user.displayName}!"
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
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        if (trimmedName.isBlank()) return false to "Please enter your full name"
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) return false to "Please enter a valid email"
        if (pass.length < 4) return false to "Password must be at least 4 characters"
        if (trimmedPhone.isBlank()) return false to "Please enter a phone number"

        if (accountType == AccountType.COMPANY_EMPLOYER && companyName.isBlank()) {
            return false to "Please enter your company or employer name"
        }

        val formattedPhone = if (trimmedPhone.startsWith("+251") || trimmedPhone.startsWith("09") || trimmedPhone.startsWith("07")) {
            trimmedPhone
        } else {
            "+251 $trimmedPhone"
        }

        val user = AuthUser(
            id = "user_${UUID.randomUUID()}",
            email = trimmedEmail,
            fullName = trimmedName,
            phone = formattedPhone,
            accountType = accountType,
            companyName = companyName,
            companyIndustry = companyIndustry.ifBlank { "Technology & Services" },
            isLoggedIn = true,
            registeredDate = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        )

        _currentUser.value = user
        saveUser(user)

        // Update corresponding profile with fresh empty fields so the user fills their CV themselves
        if (accountType == AccountType.COMPANY_EMPLOYER) {
            val freshComp = CompanyProfile(
                id = "comp_${UUID.randomUUID()}",
                name = companyName,
                industry = companyIndustry.ifBlank { "Technology & Services" },
                contactEmail = trimmedEmail,
                contactPhone = formattedPhone,
                isVerified = false
            )
            _companyProfile.value = freshComp
            saveCompanyProfile(freshComp)
        } else {
            // Fresh, empty CV for the job seeker so they fill this by themselves!
            val freshProf = UserProfile(
                id = user.id,
                fullName = trimmedName,
                email = trimmedEmail,
                phone = formattedPhone,
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
            _profile.value = freshProf
            saveProfile(freshProf)
        }

        // Push profile to Supabase in background
        if (supabaseManager.isConfigured) {
            backgroundScope.launch {
                try { supabaseManager.insertProfile(user) } catch (_: Exception) {}
            }
        }

        setSeenWelcome(true)
        return true to "Account created successfully as ${accountType.label}!"
    }

    fun hasSeenWelcome(): Boolean = prefs.getBoolean("has_seen_welcome_screen", false)

    fun setSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean("has_seen_welcome_screen", seen).apply()
    }

    fun logout() {
        val current = _currentUser.value ?: AuthUser()
        val loggedOut = current.copy(isLoggedIn = false)
        _currentUser.value = loggedOut
        saveUser(loggedOut)
        setSeenWelcome(false)
    }

    fun switchAccountRole(role: AccountType) {
        val current = _currentUser.value ?: AuthUser()
        val updated = current.copy(accountType = role)
        _currentUser.value = updated
        saveUser(updated)
    }

    fun quickDemoLogin(role: AccountType) {
        val user = when (role) {
            AccountType.JOB_SEEKER -> AuthUser(
                id = "user_abebe_1",
                email = "abebe.kebede.dev@gmail.com",
                fullName = "Abebe Kebede",
                phone = "+251 91 123 4567",
                accountType = AccountType.JOB_SEEKER,
                headline = "Senior Mobile & Backend Engineer",
                isLoggedIn = true,
                registeredDate = "October 2024"
            )
            AccountType.COMPANY_EMPLOYER -> AuthUser(
                id = "user_comp_fintech_1",
                email = "recruitment@ethiofintech.et",
                fullName = "EthioFintech Recruitment Lead",
                phone = "+251 91 144 5566",
                accountType = AccountType.COMPANY_EMPLOYER,
                companyName = "EthioFintech & Tech Labs",
                companyIndustry = "Fintech & Software Engineering",
                headline = "Head of Talent & Engineering Hiring",
                isLoggedIn = true,
                registeredDate = "June 2023"
            )
            AccountType.GIG_CLIENT -> AuthUser(
                id = "user_gig_kaffa_1",
                email = "projects@kaffacoffee.et",
                fullName = "Selamawit Tadesse",
                phone = "+251 92 888 3344",
                accountType = AccountType.GIG_CLIENT,
                companyName = "Kaffa Origin Coffee",
                companyIndustry = "Hospitality & Specialty Coffee",
                headline = "Creative Project Director",
                isLoggedIn = true,
                registeredDate = "August 2024"
            )
        }
        _currentUser.value = user
        saveUser(user)

        if (role == AccountType.COMPANY_EMPLOYER) {
            _companyProfile.value = _companyProfile.value.copy(
                name = user.companyName,
                contactEmail = user.email,
                contactPhone = user.phone
            )
        } else if (role == AccountType.JOB_SEEKER) {
            _profile.value = UserProfile.DEMO_ABEBE
            saveProfile(UserProfile.DEMO_ABEBE)
        }
        setSeenWelcome(true)
    }

    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        saveSettings(newSettings)
    }

    fun clearCacheAndReset() {
        prefs.edit().clear().apply()
        loadData()
    }

    // Helper JSON serialization
    private fun saveJobs(list: List<JobPost>) {
        val array = JSONArray()
        list.forEach { job ->
            val obj = JSONObject().apply {
                put("id", job.id)
                put("title", job.title)
                put("company", job.company)
                put("location", job.location)
                put("category", job.category)
                put("employmentType", job.employmentType.name)
                put("workplaceType", job.workplaceType.name)
                put("experienceLevel", job.experienceLevel.name)
                put("salaryMinETB", job.salaryMinETB)
                put("salaryMaxETB", job.salaryMaxETB)
                put("salaryPeriod", job.salaryPeriod)
                put("description", job.description)
                put("responsibilities", JSONArray(job.responsibilities))
                put("requirements", JSONArray(job.requirements))
                put("benefits", JSONArray(job.benefits))
                put("postedTimeAgo", job.postedTimeAgo)
                put("deadline", job.deadline)
                put("applicantsCount", job.applicantsCount)
                put("isFeatured", job.isFeatured)
                put("isUrgent", job.isUrgent)
                put("isVerifiedCompany", job.isVerifiedCompany)
                put("contactEmail", job.contactEmail)
                put("contactPhone", job.contactPhone ?: "")
                put("isPostedByUser", job.isPostedByUser)
            }
            array.put(obj)
        }
        prefs.edit().putString("jobs_json", array.toString()).apply()
    }

    private fun parseJobsJson(jsonStr: String): List<JobPost> {
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<JobPost>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val respList = mutableListOf<String>()
                val respArr = obj.optJSONArray("responsibilities")
                if (respArr != null) {
                    for (r in 0 until respArr.length()) respList.add(respArr.getString(r))
                }
                val reqList = mutableListOf<String>()
                val reqArr = obj.optJSONArray("requirements")
                if (reqArr != null) {
                    for (r in 0 until reqArr.length()) reqList.add(reqArr.getString(r))
                }
                val benList = mutableListOf<String>()
                val benArr = obj.optJSONArray("benefits")
                if (benArr != null) {
                    for (b in 0 until benArr.length()) benList.add(benArr.getString(b))
                }

                result.add(
                    JobPost(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        company = obj.getString("company"),
                        location = obj.getString("location"),
                        category = obj.getString("category"),
                        employmentType = EmploymentType.valueOf(obj.optString("employmentType", EmploymentType.FULL_TIME.name)),
                        workplaceType = WorkplaceType.valueOf(obj.optString("workplaceType", WorkplaceType.ON_SITE.name)),
                        experienceLevel = ExperienceLevel.valueOf(obj.optString("experienceLevel", ExperienceLevel.MID.name)),
                        salaryMinETB = obj.optLong("salaryMinETB", 0),
                        salaryMaxETB = obj.optLong("salaryMaxETB", 0),
                        salaryPeriod = obj.optString("salaryPeriod", "month"),
                        description = obj.optString("description", ""),
                        responsibilities = respList,
                        requirements = reqList,
                        benefits = benList,
                        postedTimeAgo = obj.optString("postedTimeAgo", "Recently"),
                        deadline = obj.optString("deadline", ""),
                        applicantsCount = obj.optInt("applicantsCount", 0),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        isUrgent = obj.optBoolean("isUrgent", false),
                        isVerifiedCompany = obj.optBoolean("isVerifiedCompany", true),
                        contactEmail = obj.optString("contactEmail", ""),
                        contactPhone = obj.optString("contactPhone").takeIf { it.isNotBlank() },
                        isPostedByUser = obj.optBoolean("isPostedByUser", false)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveGigs(list: List<GigPost>) {
        val array = JSONArray()
        list.forEach { gig ->
            val obj = JSONObject().apply {
                put("id", gig.id)
                put("title", gig.title)
                put("clientName", gig.clientName)
                put("clientType", gig.clientType)
                put("location", gig.location)
                put("category", gig.category)
                put("budgetETB", gig.budgetETB)
                put("pricingType", gig.pricingType.name)
                put("durationText", gig.durationText)
                put("description", gig.description)
                put("deliverables", JSONArray(gig.deliverables))
                put("requiredSkills", JSONArray(gig.requiredSkills))
                put("bidsCount", gig.bidsCount)
                put("postedTimeAgo", gig.postedTimeAgo)
                put("status", gig.status.name)
                put("clientPhone", gig.clientPhone ?: "")
                put("clientTelegram", gig.clientTelegram ?: "")
                put("isPostedByUser", gig.isPostedByUser)
                put("isUrgent", gig.isUrgent)
            }
            array.put(obj)
        }
        prefs.edit().putString("gigs_json", array.toString()).apply()
    }

    private fun parseGigsJson(jsonStr: String): List<GigPost> {
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<GigPost>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val delList = mutableListOf<String>()
                val delArr = obj.optJSONArray("deliverables")
                if (delArr != null) {
                    for (d in 0 until delArr.length()) delList.add(delArr.getString(d))
                }
                val skillList = mutableListOf<String>()
                val skillArr = obj.optJSONArray("requiredSkills")
                if (skillArr != null) {
                    for (s in 0 until skillArr.length()) skillList.add(skillArr.getString(s))
                }
                result.add(
                    GigPost(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        clientName = obj.getString("clientName"),
                        clientType = obj.getString("clientType"),
                        location = obj.getString("location"),
                        category = obj.getString("category"),
                        budgetETB = obj.optLong("budgetETB", 0),
                        pricingType = GigPricingType.valueOf(obj.optString("pricingType", GigPricingType.FIXED.name)),
                        durationText = obj.optString("durationText", "Flexible"),
                        description = obj.optString("description", ""),
                        deliverables = delList,
                        requiredSkills = skillList,
                        bidsCount = obj.optInt("bidsCount", 0),
                        postedTimeAgo = obj.optString("postedTimeAgo", "Recently"),
                        status = GigStatus.valueOf(obj.optString("status", GigStatus.OPEN.name)),
                        clientPhone = obj.optString("clientPhone").takeIf { it.isNotBlank() },
                        clientTelegram = obj.optString("clientTelegram").takeIf { it.isNotBlank() },
                        isPostedByUser = obj.optBoolean("isPostedByUser", false),
                        isUrgent = obj.optBoolean("isUrgent", false)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveProfile(profile: UserProfile) {
        val obj = JSONObject().apply {
            put("id", profile.id)
            put("fullName", profile.fullName)
            put("titleHeadline", profile.titleHeadline)
            put("bioSummary", profile.bioSummary)
            put("email", profile.email)
            put("phone", profile.phone)
            put("telegramHandle", profile.telegramHandle)
            put("cityLocation", profile.cityLocation)
            put("expectedSalaryMinETB", profile.expectedSalaryMinETB)
            put("availabilityStatus", profile.availabilityStatus)
            put("skills", JSONArray(profile.skills))
            put("languages", JSONArray(profile.languages))

            val eduArr = JSONArray()
            profile.educationList.forEach { edu ->
                eduArr.put(JSONObject().apply {
                    put("id", edu.id)
                    put("degree", edu.degree)
                    put("fieldOfStudy", edu.fieldOfStudy)
                    put("institution", edu.institution)
                    put("startYear", edu.startYear)
                    put("endYear", edu.endYear)
                })
            }
            put("educationList", eduArr)

            val expArr = JSONArray()
            profile.experienceList.forEach { exp ->
                expArr.put(JSONObject().apply {
                    put("id", exp.id)
                    put("roleTitle", exp.roleTitle)
                    put("companyName", exp.companyName)
                    put("location", exp.location)
                    put("startDate", exp.startDate)
                    put("endDate", exp.endDate)
                    put("description", exp.description)
                })
            }
            put("experienceList", expArr)

            val portArr = JSONArray()
            profile.portfolioLinks.forEach { port ->
                portArr.put(JSONObject().apply {
                    put("id", port.id)
                    put("title", port.title)
                    put("url", port.url)
                })
            }
            put("portfolioLinks", portArr)
        }
        prefs.edit().putString("profile_json", obj.toString()).apply()
    }

    private fun parseProfileJson(jsonStr: String): UserProfile {
        return try {
            val obj = JSONObject(jsonStr)
            val skillsList = mutableListOf<String>()
            obj.optJSONArray("skills")?.let { arr ->
                for (i in 0 until arr.length()) skillsList.add(arr.getString(i))
            }
            val langList = mutableListOf<String>()
            obj.optJSONArray("languages")?.let { arr ->
                for (i in 0 until arr.length()) langList.add(arr.getString(i))
            }
            val eduList = mutableListOf<EducationItem>()
            obj.optJSONArray("educationList")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    eduList.add(
                        EducationItem(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            degree = o.optString("degree"),
                            fieldOfStudy = o.optString("fieldOfStudy"),
                            institution = o.optString("institution"),
                            startYear = o.optString("startYear"),
                            endYear = o.optString("endYear")
                        )
                    )
                }
            }
            val expList = mutableListOf<ExperienceItem>()
            obj.optJSONArray("experienceList")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    expList.add(
                        ExperienceItem(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            roleTitle = o.optString("roleTitle"),
                            companyName = o.optString("companyName"),
                            location = o.optString("location"),
                            startDate = o.optString("startDate"),
                            endDate = o.optString("endDate"),
                            description = o.optString("description")
                        )
                    )
                }
            }
            val portList = mutableListOf<PortfolioLink>()
            obj.optJSONArray("portfolioLinks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    portList.add(
                        PortfolioLink(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title"),
                            url = o.optString("url")
                        )
                    )
                }
            }
            UserProfile(
                id = obj.optString("id", "user_default"),
                fullName = obj.optString("fullName", ""),
                titleHeadline = obj.optString("titleHeadline", ""),
                bioSummary = obj.optString("bioSummary", ""),
                email = obj.optString("email", ""),
                phone = obj.optString("phone", ""),
                telegramHandle = obj.optString("telegramHandle", ""),
                cityLocation = obj.optString("cityLocation", "Addis Ababa, Ethiopia"),
                expectedSalaryMinETB = obj.optLong("expectedSalaryMinETB", 0L),
                availabilityStatus = obj.optString("availabilityStatus", "Actively seeking opportunities"),
                skills = skillsList,
                languages = if (langList.isNotEmpty()) langList else listOf("Amharic", "English"),
                educationList = eduList,
                experienceList = expList,
                portfolioLinks = portList
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    private fun saveCompanyProfile(company: CompanyProfile) {
        val obj = JSONObject().apply {
            put("id", company.id)
            put("name", company.name)
            put("industry", company.industry)
            put("location", company.location)
            put("website", company.website)
            put("contactEmail", company.contactEmail)
            put("contactPhone", company.contactPhone)
            put("description", company.description)
            put("isVerified", company.isVerified)
            put("employeesCount", company.employeesCount)
            put("foundedYear", company.foundedYear)
        }
        prefs.edit().putString("company_profile_json", obj.toString()).apply()
    }

    private fun parseCompanyJson(jsonStr: String): CompanyProfile {
        return try {
            val obj = JSONObject(jsonStr)
            CompanyProfile(
                id = obj.optString("id", "comp_default_1"),
                name = obj.optString("name", "EthioFintech & Tech Labs"),
                industry = obj.optString("industry", "Technology & Financial Services"),
                location = obj.optString("location", "Addis Ababa (Bole Atlas), Ethiopia"),
                website = obj.optString("website", "https://ethiofintech.et"),
                contactEmail = obj.optString("contactEmail", "recruitment@ethiofintech.et"),
                contactPhone = obj.optString("contactPhone", "+251 91 144 5566"),
                description = obj.optString("description", ""),
                isVerified = obj.optBoolean("isVerified", true),
                employeesCount = obj.optString("employeesCount", "50-100 Employees"),
                foundedYear = obj.optString("foundedYear", "2020")
            )
        } catch (e: Exception) {
            CompanyProfile()
        }
    }

    private fun saveApplications(list: List<JobApplication>) {
        val array = JSONArray()
        list.forEach { app ->
            val obj = JSONObject().apply {
                put("id", app.id)
                put("jobId", app.jobId)
                put("jobTitle", app.jobTitle)
                put("companyName", app.companyName)
                put("location", app.location)
                put("applicantName", app.applicantName)
                put("applicantEmail", app.applicantEmail)
                put("applicantPhone", app.applicantPhone)
                put("applicantHeadline", app.applicantHeadline)
                put("applicantSkills", JSONArray(app.applicantSkills))
                put("applicantEducation", app.applicantEducation)
                put("applicantExperience", app.applicantExperience)
                put("coverNote", app.coverNote)
                put("expectedSalaryETB", app.expectedSalaryETB)
                put("appliedDate", app.appliedDate)
                put("status", app.status.name)
            }
            array.put(obj)
        }
        prefs.edit().putString("applications_json", array.toString()).apply()
    }

    private fun parseApplicationsJson(jsonStr: String): List<JobApplication> {
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<JobApplication>()
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val skills = mutableListOf<String>()
                o.optJSONArray("applicantSkills")?.let { arr ->
                    for (s in 0 until arr.length()) skills.add(arr.getString(s))
                }
                result.add(
                    JobApplication(
                        id = o.getString("id"),
                        jobId = o.getString("jobId"),
                        jobTitle = o.getString("jobTitle"),
                        companyName = o.getString("companyName"),
                        location = o.getString("location"),
                        applicantName = o.getString("applicantName"),
                        applicantEmail = o.getString("applicantEmail"),
                        applicantPhone = o.getString("applicantPhone"),
                        applicantHeadline = o.optString("applicantHeadline", "Candidate"),
                        applicantSkills = if (skills.isNotEmpty()) skills else listOf("Kotlin", "Amharic & English"),
                        applicantEducation = o.optString("applicantEducation", "Higher Education Graduate"),
                        applicantExperience = o.optString("applicantExperience", "Industry experience"),
                        coverNote = o.getString("coverNote"),
                        expectedSalaryETB = o.optLong("expectedSalaryETB", 0),
                        appliedDate = o.optString("appliedDate", "Recently"),
                        status = ApplicationStatus.valueOf(o.optString("status", ApplicationStatus.APPLIED.name))
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveBids(list: List<GigBid>) {
        val array = JSONArray()
        list.forEach { bid ->
            val obj = JSONObject().apply {
                put("id", bid.id)
                put("gigId", bid.gigId)
                put("gigTitle", bid.gigTitle)
                put("clientName", bid.clientName)
                put("bidderName", bid.bidderName)
                put("bidderPhone", bid.bidderPhone)
                put("bidAmountETB", bid.bidAmountETB)
                put("deliveryDays", bid.deliveryDays)
                put("proposalPitch", bid.proposalPitch)
                put("submittedDate", bid.submittedDate)
                put("status", bid.status.name)
            }
            array.put(obj)
        }
        prefs.edit().putString("bids_json", array.toString()).apply()
    }

    private fun parseBidsJson(jsonStr: String): List<GigBid> {
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<GigBid>()
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                result.add(
                    GigBid(
                        id = o.getString("id"),
                        gigId = o.getString("gigId"),
                        gigTitle = o.getString("gigTitle"),
                        clientName = o.getString("clientName"),
                        bidderName = o.getString("bidderName"),
                        bidderPhone = o.getString("bidderPhone"),
                        bidAmountETB = o.optLong("bidAmountETB", 0),
                        deliveryDays = o.getString("deliveryDays"),
                        proposalPitch = o.getString("proposalPitch"),
                        submittedDate = o.optString("submittedDate", "Recently"),
                        status = BidStatus.valueOf(o.optString("status", BidStatus.SUBMITTED.name))
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUser(user: AuthUser) {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("email", user.email)
            put("fullName", user.fullName)
            put("phone", user.phone)
            put("accountType", user.accountType.name)
            put("companyName", user.companyName)
            put("companyIndustry", user.companyIndustry)
            put("companyLocation", user.companyLocation)
            put("headline", user.headline)
            put("isLoggedIn", user.isLoggedIn)
            put("registeredDate", user.registeredDate)
        }
        prefs.edit().putString("auth_user_json", obj.toString()).apply()
    }

    private fun parseUserJson(jsonStr: String): AuthUser {
        return try {
            val obj = JSONObject(jsonStr)
            AuthUser(
                id = obj.optString("id", "user_default_1"),
                email = obj.optString("email", "abebe.kebede.dev@gmail.com"),
                fullName = obj.optString("fullName", "Abebe Kebede"),
                phone = obj.optString("phone", "+251 91 123 4567"),
                accountType = AccountType.valueOf(obj.optString("accountType", AccountType.JOB_SEEKER.name)),
                companyName = obj.optString("companyName", ""),
                companyIndustry = obj.optString("companyIndustry", ""),
                companyLocation = obj.optString("companyLocation", "Addis Ababa, Ethiopia"),
                headline = obj.optString("headline", "Senior Mobile & Backend Engineer"),
                isLoggedIn = obj.optBoolean("isLoggedIn", false),
                registeredDate = obj.optString("registeredDate", "October 2024")
            )
        } catch (e: Exception) {
            AuthUser(isLoggedIn = false)
        }
    }

    private fun saveSettings(settings: AppSettings) {
        val obj = JSONObject().apply {
            put("selectedLanguage", settings.selectedLanguage)
            put("notificationsEnabled", settings.notificationsEnabled)
            put("jobAlertsEnabled", settings.jobAlertsEnabled)
            put("bidAlertsEnabled", settings.bidAlertsEnabled)
            put("autoSyncSupabase", settings.autoSyncSupabase)
            put("darkThemeEnabled", settings.darkThemeEnabled)
            put("themeMode", settings.themeMode)
            put("currency", settings.currency)
        }
        prefs.edit().putString("app_settings_json", obj.toString()).apply()
    }

    private fun parseSettingsJson(jsonStr: String): AppSettings {
        return try {
            val obj = JSONObject(jsonStr)
            val darkEnabled = obj.optBoolean("darkThemeEnabled", true)
            AppSettings(
                selectedLanguage = obj.optString("selectedLanguage", "English"),
                notificationsEnabled = obj.optBoolean("notificationsEnabled", true),
                jobAlertsEnabled = obj.optBoolean("jobAlertsEnabled", true),
                bidAlertsEnabled = obj.optBoolean("bidAlertsEnabled", true),
                autoSyncSupabase = obj.optBoolean("autoSyncSupabase", true),
                darkThemeEnabled = darkEnabled,
                themeMode = obj.optString("themeMode", if (darkEnabled) "DARK" else "LIGHT"),
                currency = obj.optString("currency", "ETB")
            )
        } catch (e: Exception) {
            AppSettings()
        }
    }
}
