package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.AccountType
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
import com.example.model.UserProfile
import com.example.model.WorkplaceType
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Supabase client configuration helper class.
 * Initializes and manages direct connections to Supabase PostgreSQL database
 * using PostgREST endpoints and credentials from .env / BuildConfig.
 */
class SupabaseClientConfig private constructor(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Raw credentials resolved from .env via BuildConfig
     */
    val activeSupabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim()
            .removeSuffix("/rest/v1/")
            .removeSuffix("/rest/v1")
            .trimEnd('/')

    val activeAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConnectedAndConfigured: Boolean
        get() = activeSupabaseUrl.isNotBlank() && activeAnonKey.isNotBlank()

    /**
     * Pre-configured OkHttpClient with Supabase Authentication Interceptor.
     * Automatically attaches 'apikey' and 'Authorization: Bearer <anon_key>' to all requests.
     */
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(SupabaseAuthInterceptor(
                keyProvider = { activeAnonKey },
                tokenProvider = { sessionTokenProvider() }
            ))
            .build()
    }

    fun getTableEndpoint(tableName: String, queryParams: String? = null): String {
        val base = activeSupabaseUrl.trimEnd('/')
        val tableClean = tableName.trimStart('/')
        val url = "$base/rest/v1/$tableClean"
        return if (!queryParams.isNullOrBlank()) "$url?$queryParams" else url
    }

    // ==========================================
    // CLOUD DATABASE FETCH (SELECT) QUERIES
    // ==========================================

    fun fetchJobs(): List<JobPost> {
        if (!isConnectedAndConfigured) return emptyList()
        return try {
            val endpoint = getTableEndpoint("jobs", "select=*&order=created_at.desc")
            val request = Request.Builder().url(endpoint).get().build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return emptyList()
            }
            val body = response.body?.string() ?: ""
            response.close()
            parseJobsFromJsonArray(JSONArray(body))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching jobs from Supabase", e)
            emptyList()
        }
    }

    fun fetchGigs(): List<GigPost> {
        if (!isConnectedAndConfigured) return emptyList()
        return try {
            val endpoint = getTableEndpoint("gigs", "select=*&order=created_at.desc")
            val request = Request.Builder().url(endpoint).get().build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return emptyList()
            }
            val body = response.body?.string() ?: ""
            response.close()
            parseGigsFromJsonArray(JSONArray(body))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching gigs from Supabase", e)
            emptyList()
        }
    }

    fun fetchApplications(): List<JobApplication> {
        if (!isConnectedAndConfigured) return emptyList()
        return try {
            val endpoint = getTableEndpoint("applications", "select=*&order=created_at.desc")
            val request = Request.Builder().url(endpoint).get().build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return emptyList()
            }
            val body = response.body?.string() ?: ""
            response.close()
            parseApplicationsFromJsonArray(JSONArray(body))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching applications from Supabase", e)
            emptyList()
        }
    }

    fun fetchBids(): List<GigBid> {
        if (!isConnectedAndConfigured) return emptyList()
        return try {
            val endpoint = getTableEndpoint("bids", "select=*&order=created_at.desc")
            val request = Request.Builder().url(endpoint).get().build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return emptyList()
            }
            val body = response.body?.string() ?: ""
            response.close()
            parseBidsFromJsonArray(JSONArray(body))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bids from Supabase", e)
            emptyList()
        }
    }

    // ==========================================
    // CLOUD DATABASE MUTATIONS (INSERT / PATCH)
    // ==========================================

    fun insertJob(job: JobPost): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val json = JSONObject().apply {
                put("id", job.id)
                put("title", job.title)
                put("company", job.company)
                put("location", job.location)
                put("category", job.category)
                put("employment_type", job.employmentType.name)
                put("workplace_type", job.workplaceType.name)
                put("experience_level", job.experienceLevel.name)
                put("salary_min_etb", job.salaryMinETB)
                put("salary_max_etb", job.salaryMaxETB)
                put("salary_period", job.salaryPeriod)
                put("description", job.description)
                put("responsibilities", JSONArray(job.responsibilities))
                put("requirements", JSONArray(job.requirements))
                put("benefits", JSONArray(job.benefits))
                put("deadline", job.deadline)
                put("contact_email", job.contactEmail)
                put("contact_phone", job.contactPhone ?: "")
                put("is_verified", job.isVerifiedCompany)
                put("is_urgent", job.isUrgent)
            }
            val res = insertRecord("jobs", json)
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting job into Supabase", e)
            false
        }
    }

    fun insertGig(gig: GigPost): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val json = JSONObject().apply {
                put("id", gig.id)
                put("title", gig.title)
                put("client_name", gig.clientName)
                put("client_type", gig.clientType)
                put("location", gig.location)
                put("category", gig.category)
                put("budget_etb", gig.budgetETB)
                put("pricing_type", gig.pricingType.name)
                put("duration_text", gig.durationText)
                put("description", gig.description)
                put("deliverables", JSONArray(gig.deliverables))
                put("required_skills", JSONArray(gig.requiredSkills))
                put("client_phone", gig.clientPhone ?: "")
                put("client_telegram", gig.clientTelegram ?: "")
                put("is_urgent", gig.isUrgent)
            }
            val res = insertRecord("gigs", json)
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting gig into Supabase", e)
            false
        }
    }

    fun insertApplication(app: JobApplication): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val json = JSONObject().apply {
                put("id", app.id)
                put("job_id", app.jobId)
                put("job_title", app.jobTitle)
                put("company_name", app.companyName)
                put("applicant_name", app.applicantName)
                put("applicant_email", app.applicantEmail)
                put("applicant_phone", app.applicantPhone)
                put("applicant_headline", app.applicantHeadline)
                put("applicant_skills", JSONArray(app.applicantSkills))
                put("applicant_education", app.applicantEducation)
                put("applicant_experience", app.applicantExperience)
                put("cover_note", app.coverNote)
                put("expected_salary_etb", app.expectedSalaryETB)
                put("status", app.status.name)
                put("applied_date", app.appliedDate)
            }
            val res = insertRecord("applications", json)
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting application into Supabase", e)
            false
        }
    }

    fun insertBid(bid: GigBid): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val json = JSONObject().apply {
                put("id", bid.id)
                put("gig_id", bid.gigId)
                put("gig_title", bid.gigTitle)
                put("client_name", bid.clientName)
                put("bidder_name", bid.bidderName)
                put("bidder_phone", bid.bidderPhone)
                put("bid_amount_etb", bid.bidAmountETB)
                put("delivery_days", bid.deliveryDays)
                put("proposal_pitch", bid.proposalPitch)
                put("status", bid.status.name)
                put("submitted_date", bid.submittedDate)
            }
            val res = insertRecord("bids", json)
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting bid into Supabase", e)
            false
        }
    }

    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val endpoint = getTableEndpoint("applications", "id=eq.$applicationId")
            val payload = JSONObject().apply {
                put("status", newStatus.name)
            }.toString()
            val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Prefer", "return=minimal")
                .patch(body)
                .build()
            val res = httpClient.newCall(request).execute()
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating application status in Supabase", e)
            false
        }
    }

    fun insertProfile(user: AuthUser, accessToken: String? = null): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val dbAccountType = when (user.accountType) {
                AccountType.COMPANY_EMPLOYER -> "company"
                AccountType.GIG_CLIENT -> "gig_provider"
                AccountType.JOB_SEEKER -> "job_seeker"
            }
            val json = JSONObject().apply {
                put("id", user.id)
                put("account_type", dbAccountType)
                put("full_name", user.fullName)
                put("phone", user.phone)
                put("city", user.companyLocation)
            }
            val url = getTableEndpoint("profiles")
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val builder = Request.Builder()
                .url(url)
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body)

            if (!accessToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $accessToken")
            }

            val request = builder.build()
            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting/upserting user profile into Supabase", e)
            false
        }
    }

    fun deleteJob(jobId: String): Boolean = deleteRecord("jobs", jobId)

    fun deleteGig(gigId: String): Boolean = deleteRecord("gigs", gigId)

    fun deleteRecord(tableName: String, id: String): Boolean {
        if (!isConnectedAndConfigured) return false
        return try {
            val endpoint = getTableEndpoint(tableName, "id=eq.$id")
            val request = Request.Builder().url(endpoint).delete().build()
            val res = httpClient.newCall(request).execute()
            val success = res.isSuccessful
            res.close()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting record $id from $tableName", e)
            false
        }
    }

    @Throws(IOException::class)
    fun insertRecord(tableName: String, jsonObject: JSONObject): Response {
        val url = getTableEndpoint(tableName)
        val body = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Prefer", "return=representation")
            .post(body)
            .build()
        return httpClient.newCall(request).execute()
    }

    fun testConnection(): ConnectionResult {
        if (!isConnectedAndConfigured) {
            return ConnectionResult(
                isSuccess = false,
                message = "Missing Supabase URL or Anon Key. Ensure credentials are set in .env or the in-app settings.",
                httpStatusCode = null
            )
        }

        return try {
            val url = getTableEndpoint("jobs", "select=id&limit=1")
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val code = response.code
            val isSuccess = response.isSuccessful
            response.close()

            if (isSuccess) {
                ConnectionResult(
                    isSuccess = true,
                    message = "Connected to Supabase project successfully! (HTTP $code)",
                    httpStatusCode = code
                )
            } else if (code == 404 || code == 400) {
                ConnectionResult(
                    isSuccess = true,
                    message = "Connected to Supabase project! Note: Tables need to be initialized via the SQL script.",
                    httpStatusCode = code
                )
            } else {
                ConnectionResult(
                    isSuccess = false,
                    message = "Supabase responded with HTTP $code: ${response.message}",
                    httpStatusCode = code
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase connection error", e)
            ConnectionResult(
                isSuccess = false,
                message = "Network error connecting to Supabase: ${e.localizedMessage ?: "Connection failed"}",
                httpStatusCode = null
            )
        }
    }

    // ==========================================
    // JSON PARSING HELPERS
    // ==========================================

    private fun parseJobsFromJsonArray(array: JSONArray): List<JobPost> {
        val list = mutableListOf<JobPost>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val respList = mutableListOf<String>()
            obj.optJSONArray("responsibilities")?.let {
                for (j in 0 until it.length()) respList.add(it.getString(j))
            }
            val reqList = mutableListOf<String>()
            obj.optJSONArray("requirements")?.let {
                for (j in 0 until it.length()) reqList.add(it.getString(j))
            }
            val benList = mutableListOf<String>()
            obj.optJSONArray("benefits")?.let {
                for (j in 0 until it.length()) benList.add(it.getString(j))
            }

            list.add(
                JobPost(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    company = obj.optString("company"),
                    location = obj.optString("location"),
                    category = obj.optString("category"),
                    employmentType = EmploymentType.valueOf(obj.optString("employment_type", EmploymentType.FULL_TIME.name)),
                    workplaceType = WorkplaceType.valueOf(obj.optString("workplace_type", WorkplaceType.ON_SITE.name)),
                    experienceLevel = ExperienceLevel.valueOf(obj.optString("experience_level", ExperienceLevel.MID.name)),
                    salaryMinETB = obj.optLong("salary_min_etb", 0),
                    salaryMaxETB = obj.optLong("salary_max_etb", 0),
                    salaryPeriod = obj.optString("salary_period", "month"),
                    description = obj.optString("description", ""),
                    responsibilities = respList,
                    requirements = reqList,
                    benefits = benList,
                    postedTimeAgo = "Recently",
                    deadline = obj.optString("deadline", "Open"),
                    applicantsCount = obj.optInt("applicants_count", 0),
                    isFeatured = true,
                    isUrgent = obj.optBoolean("is_urgent", false),
                    isVerifiedCompany = obj.optBoolean("is_verified", true),
                    contactEmail = obj.optString("contact_email", ""),
                    contactPhone = obj.optString("contact_phone").takeIf { it.isNotBlank() },
                    isPostedByUser = false
                )
            )
        }
        return list
    }

    private fun parseGigsFromJsonArray(array: JSONArray): List<GigPost> {
        val list = mutableListOf<GigPost>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val delList = mutableListOf<String>()
            obj.optJSONArray("deliverables")?.let {
                for (j in 0 until it.length()) delList.add(it.getString(j))
            }
            val skillList = mutableListOf<String>()
            obj.optJSONArray("required_skills")?.let {
                for (j in 0 until it.length()) skillList.add(it.getString(j))
            }

            list.add(
                GigPost(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    clientName = obj.optString("client_name"),
                    clientType = obj.optString("client_type", "Client"),
                    location = obj.optString("location", "Addis Ababa"),
                    category = obj.optString("category", "Freelance"),
                    budgetETB = obj.optLong("budget_etb", 5000),
                    pricingType = GigPricingType.valueOf(obj.optString("pricing_type", GigPricingType.FIXED.name)),
                    durationText = obj.optString("duration_text", "Flexible"),
                    description = obj.optString("description", ""),
                    deliverables = delList,
                    requiredSkills = skillList,
                    bidsCount = obj.optInt("bids_count", 0),
                    postedTimeAgo = "Recently",
                    status = GigStatus.OPEN,
                    clientPhone = obj.optString("client_phone").takeIf { it.isNotBlank() },
                    clientTelegram = obj.optString("client_telegram").takeIf { it.isNotBlank() },
                    isPostedByUser = false,
                    isUrgent = obj.optBoolean("is_urgent", false)
                )
            )
        }
        return list
    }

    private fun parseApplicationsFromJsonArray(array: JSONArray): List<JobApplication> {
        val list = mutableListOf<JobApplication>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val skills = mutableListOf<String>()
            obj.optJSONArray("applicant_skills")?.let {
                for (j in 0 until it.length()) skills.add(it.getString(j))
            }

            list.add(
                JobApplication(
                    id = obj.optString("id"),
                    jobId = obj.optString("job_id"),
                    jobTitle = obj.optString("job_title"),
                    companyName = obj.optString("company_name"),
                    location = obj.optString("location", "Addis Ababa"),
                    applicantName = obj.optString("applicant_name"),
                    applicantEmail = obj.optString("applicant_email"),
                    applicantPhone = obj.optString("applicant_phone"),
                    applicantHeadline = obj.optString("applicant_headline", "Candidate"),
                    applicantSkills = skills,
                    applicantEducation = obj.optString("applicant_education", "Higher Education Graduate"),
                    applicantExperience = obj.optString("applicant_experience", "Relevant Experience"),
                    coverNote = obj.optString("cover_note", ""),
                    expectedSalaryETB = obj.optLong("expected_salary_etb", 0),
                    appliedDate = obj.optString("applied_date", "Recently"),
                    status = ApplicationStatus.valueOf(obj.optString("status", ApplicationStatus.APPLIED.name))
                )
            )
        }
        return list
    }

    private fun parseBidsFromJsonArray(array: JSONArray): List<GigBid> {
        val list = mutableListOf<GigBid>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                GigBid(
                    id = obj.optString("id"),
                    gigId = obj.optString("gig_id"),
                    gigTitle = obj.optString("gig_title"),
                    clientName = obj.optString("client_name"),
                    bidderName = obj.optString("bidder_name"),
                    bidderPhone = obj.optString("bidder_phone"),
                    bidAmountETB = obj.optLong("bid_amount_etb", 0),
                    deliveryDays = obj.optString("delivery_days", "3 Days"),
                    proposalPitch = obj.optString("proposal_pitch", ""),
                    submittedDate = obj.optString("submitted_date", "Recently"),
                    status = BidStatus.valueOf(obj.optString("status", BidStatus.SUBMITTED.name))
                )
            )
        }
        return list
    }

    data class ConnectionResult(
        val isSuccess: Boolean,
        val message: String,
        val httpStatusCode: Int?
    )

    private class SupabaseAuthInterceptor(
        private val keyProvider: () -> String,
        private val tokenProvider: () -> String?
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val key = keyProvider()
            val sessionToken = tokenProvider()?.takeIf { it.isNotBlank() }

            val builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            if (key.isNotBlank()) {
                builder.header("apikey", key)
                val bearer = if (sessionToken != null) {
                    sessionToken
                } else {
                    key
                }
                builder.header("Authorization", "Bearer $bearer")
            }

            return chain.proceed(builder.build())
        }
    }

    companion object {
        private const val TAG = "SupabaseClientConfig"

        /**
         * Supplies the JWT of the currently signed-in Supabase user, if any.
         * Registered by [com.example.data.auth.SupabaseAuthRepository] so that every PostgREST
         * request carries the user's identity and Row Level Security resolves auth.uid().
         */
        @Volatile
        var sessionTokenProvider: () -> String? = { null }

        @Volatile
        private var instance: SupabaseClientConfig? = null

        fun getInstance(context: Context): SupabaseClientConfig {
            return instance ?: synchronized(this) {
                instance ?: SupabaseClientConfig(context).also { instance = it }
            }
        }
    }
}
