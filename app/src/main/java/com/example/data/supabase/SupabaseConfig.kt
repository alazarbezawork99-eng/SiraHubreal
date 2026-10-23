package com.example.data.supabase

import android.content.Context
import com.example.model.ApplicationStatus
import com.example.model.AuthUser
import com.example.model.GigBid
import com.example.model.GigPost
import com.example.model.JobApplication
import com.example.model.JobPost
import org.json.JSONObject

class SupabaseManager(context: Context) {

    val clientConfig = SupabaseClientConfig.getInstance(context)

    val supabaseUrl: String
        get() = clientConfig.activeSupabaseUrl

    val supabaseAnonKey: String
        get() = clientConfig.activeAnonKey

    val isConfigured: Boolean
        get() = clientConfig.isConnectedAndConfigured

    fun testConnection(): Pair<Boolean, String> {
        val result = clientConfig.testConnection()
        return result.isSuccess to result.message
    }

    // Direct domain fetch queries
    fun fetchJobs(): List<JobPost> = clientConfig.fetchJobs()
    fun fetchGigs(): List<GigPost> = clientConfig.fetchGigs()
    fun fetchApplications(): List<JobApplication> = clientConfig.fetchApplications()
    fun fetchBids(): List<GigBid> = clientConfig.fetchBids()

    // Domain mutations
    fun insertJob(job: JobPost): Boolean = clientConfig.insertJob(job)
    fun insertGig(gig: GigPost): Boolean = clientConfig.insertGig(gig)
    fun insertApplication(app: JobApplication): Boolean = clientConfig.insertApplication(app)
    fun insertBid(bid: GigBid): Boolean = clientConfig.insertBid(bid)
    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus): Boolean =
        clientConfig.updateApplicationStatus(applicationId, newStatus)
    fun insertProfile(user: AuthUser): Boolean = clientConfig.insertProfile(user)
    fun deleteJob(jobId: String): Boolean = clientConfig.deleteJob(jobId)
    fun deleteGig(gigId: String): Boolean = clientConfig.deleteGig(gigId)

    // Legacy JSON mutations for backwards compatibility
    fun insertJob(jobJson: JSONObject): Boolean {
        if (!isConfigured) return false
        return try {
            val response = clientConfig.insertRecord("jobs", jobJson)
            val success = response.isSuccessful
            response.close()
            success
        } catch (e: Exception) {
            false
        }
    }

    fun insertApplication(appJson: JSONObject): Boolean {
        if (!isConfigured) return false
        return try {
            val response = clientConfig.insertRecord("applications", appJson)
            val success = response.isSuccessful
            response.close()
            success
        } catch (e: Exception) {
            false
        }
    }
}
