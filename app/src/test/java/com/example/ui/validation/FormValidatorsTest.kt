package com.example.ui.validation

import com.example.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FormValidatorsTest {

    @Test
    fun `a complete job form has no errors`() {
        val errors = FormValidators.validateJobForm(
            title = "Senior Android Engineer",
            company = "Linux Tech",
            location = "Addis Ababa",
            category = "Tech & Software",
            description = "Build and ship the Telebirr payment integration.",
            salaryMinText = "45000",
            salaryMaxText = "65000",
            contactEmail = "jobs@linux.et"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `blank title is reported against the title field`() {
        val errors = FormValidators.validateJobForm(
            title = "  ", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "A long enough description.",
            salaryMinText = "", salaryMaxText = "", contactEmail = ""
        )
        assertEquals(R.string.field_required, errors.errorFor(FormField.JOB_TITLE))
    }

    @Test
    fun `short title reports the length rule not the required rule`() {
        val errors = FormValidators.validateJobForm(
            title = "QA", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "A long enough description.",
            salaryMinText = "", salaryMaxText = "", contactEmail = ""
        )
        assertEquals(R.string.field_title_too_short, errors.errorFor(FormField.JOB_TITLE))
    }

    @Test
    fun `description shorter than the schema minimum is rejected`() {
        val errors = FormValidators.validateJobForm(
            title = "Senior Android Engineer", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "Too short",
            salaryMinText = "", salaryMaxText = "", contactEmail = ""
        )
        assertEquals(R.string.field_description_too_short, errors.errorFor(FormField.JOB_DESCRIPTION))
    }

    @Test
    fun `min salary above max is flagged on the max field`() {
        val errors = FormValidators.validateJobForm(
            title = "Senior Android Engineer", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "A long enough description.",
            salaryMinText = "90000", salaryMaxText = "45000", contactEmail = ""
        )
        assertEquals(R.string.field_salary_range_invalid, errors.errorFor(FormField.JOB_SALARY_MAX))
    }

    @Test
    fun `blank salary means negotiable and is not an error`() {
        val errors = FormValidators.validateJobForm(
            title = "Senior Android Engineer", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "A long enough description.",
            salaryMinText = "", salaryMaxText = "", contactEmail = ""
        )
        assertNull(errors.errorFor(FormField.JOB_SALARY_MIN))
        assertNull(errors.errorFor(FormField.JOB_SALARY_MAX))
    }

    @Test
    fun `invalid contact email is flagged`() {
        val errors = FormValidators.validateJobForm(
            title = "Senior Android Engineer", company = "Linux Tech", location = "Addis Ababa",
            category = "Tech", description = "A long enough description.",
            salaryMinText = "", salaryMaxText = "", contactEmail = "not-an-email"
        )
        assertEquals(R.string.field_email_invalid, errors.errorFor(FormField.JOB_CONTACT_EMAIL))
    }

    @Test
    fun `gig budget must be greater than zero`() {
        val zero = FormValidators.validateGigForm(
            title = "Design a logo", category = "Design",
            budgetText = "0", durationText = "3 days",
            description = "A long enough description."
        )
        assertEquals(R.string.field_budget_invalid, zero.errorFor(FormField.GIG_BUDGET))

        val blank = FormValidators.validateGigForm(
            title = "Design a logo", category = "Design",
            budgetText = "", durationText = "3 days",
            description = "A long enough description."
        )
        assertEquals(R.string.field_budget_invalid, blank.errorFor(FormField.GIG_BUDGET))
    }

    @Test
    fun `the gig description that Postgres rejected is caught on the client`() {
        // Regression: "sdfddsf" is 7 characters. gigs_description_check requires >= 10, so the
        // server returned 23514 and the user only saw "some details are not valid".
        val errors = FormValidators.validateGigForm(
            title = "Fix the table",
            category = "Errands & Logistics",
            budgetText = "15000",
            durationText = "3 Days",
            description = "sdfddsf"
        )
        assertEquals(
            R.string.field_description_too_short,
            errors.errorFor(FormField.GIG_DESCRIPTION)
        )
    }

    @Test
    fun `a valid gig form has no errors`() {
        val errors = FormValidators.validateGigForm(
            title = "Fix the table",
            category = "Errands & Logistics",
            budgetText = "15000",
            durationText = "3 Days",
            description = "Repair the wooden dining table and re-varnish it."
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `ethiopian phone formats are accepted and others rejected`() {
        assertTrue(FormValidators.isValidEthiopianPhone("+251911234567"))
        assertTrue(FormValidators.isValidEthiopianPhone("0911234567"))
        assertTrue(FormValidators.isValidEthiopianPhone("0711234567"))
        assertTrue(FormValidators.isValidEthiopianPhone("+251 91 123 4567"))

        assertEquals(false, FormValidators.isValidEthiopianPhone("12345"))
        assertEquals(false, FormValidators.isValidEthiopianPhone("+15551234567"))
    }

    @Test
    fun `registration requires a company name only for company accounts`() {
        val asSeeker = FormValidators.validateRegistration(
            fullName = "Abebe Bikila", email = "a@b.et", password = "password123",
            phone = "0911234567", isCompany = false, companyName = ""
        )
        assertNull(asSeeker.errorFor(FormField.AUTH_COMPANY_NAME))

        val asCompany = FormValidators.validateRegistration(
            fullName = "Abebe Bikila", email = "a@b.et", password = "password123",
            phone = "0911234567", isCompany = true, companyName = ""
        )
        assertEquals(R.string.field_company_required, asCompany.errorFor(FormField.AUTH_COMPANY_NAME))
    }

    @Test
    fun `short password is rejected`() {
        val errors = FormValidators.validateRegistration(
            fullName = "Abebe Bikila", email = "a@b.et", password = "short",
            phone = "", isCompany = false, companyName = ""
        )
        assertEquals(R.string.field_password_too_short, errors.errorFor(FormField.AUTH_PASSWORD))
    }
}
