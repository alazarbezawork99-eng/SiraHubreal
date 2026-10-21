package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SiraRepository
import com.example.data.supabase.SupabaseClientConfig
import com.example.data.supabase.SupabaseManager
import com.example.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SiraHub", appName)
  }

  @Test
  fun `repository loads initial Ethiopian jobs and gigs`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = SiraRepository(context)

    assertTrue("Jobs list should not be empty", repository.jobs.value.isNotEmpty())
    assertTrue("Gigs list should not be empty", repository.gigs.value.isNotEmpty())
    assertNotNull("Profile should have default name", repository.profile.value.fullName)
    assertNotNull("Company profile should have default name", repository.companyProfile.value.name)
  }

  @Test
  fun `repository submit bid on gig successfully`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = SiraRepository(context)

    val firstGig = repository.gigs.value.first()
    val initialBidsCount = repository.bids.value.size

    val newBid = repository.submitBid(
      gigId = firstGig.id,
      amountETB = 15000,
      deliveryDays = "3 Days",
      pitch = "I have solid experience in this domain."
    )

    assertEquals(initialBidsCount + 1, repository.bids.value.size)
    assertEquals(15000L, newBid.bidAmountETB)
  }

  @Test
  fun `company applicant status update works correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = SiraRepository(context)

    val firstApp = repository.applications.value.first()
    repository.updateApplicationStatus(firstApp.id, ApplicationStatus.SHORTLISTED)

    val updatedApp = repository.applications.value.find { it.id == firstApp.id }
    assertEquals(ApplicationStatus.SHORTLISTED, updatedApp?.status)
  }

  @Test
  fun `supabase manager provides valid SQL setup script`() {
    val sql = SupabaseManager.SQL_SETUP_SCRIPT
    assertTrue("SQL script should create jobs table", sql.contains("CREATE TABLE IF NOT EXISTS public.jobs"))
    assertTrue("SQL script should create applications table", sql.contains("CREATE TABLE IF NOT EXISTS public.applications"))
  }

  @Test
  fun `supabase client config helper initializes and builds endpoints`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val config = SupabaseClientConfig.getInstance(context)

    assertNotNull("Client config instance must not be null", config)
    assertNotNull("HttpClient must be created", config.httpClient)

    config.activeSupabaseUrl = "https://myproject.supabase.co"
    config.activeAnonKey = "test-anon-key-12345"

    val jobsEndpoint = config.getTableEndpoint("jobs")
    assertEquals("https://myproject.supabase.co/rest/v1/jobs", jobsEndpoint)

    val queryEndpoint = config.getTableEndpoint("applications", "select=id,status&limit=10")
    assertEquals("https://myproject.supabase.co/rest/v1/applications?select=id,status&limit=10", queryEndpoint)
  }
}
