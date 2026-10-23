package com.example.ui.validation

import android.util.Patterns
import com.example.R

/**
 * Pure validation rules for the posting and auth forms.
 *
 * These run before anything is sent to Supabase, so a user who mistypes a field is told exactly
 * which field and why, instead of receiving a generic server error after a round trip. The rules
 * mirror the CHECK constraints in `0001_schema.sql` so the client and server agree.
 *
 * Kept free of Compose and Android UI types so they can be unit tested directly.
 */
object FormValidators {

    /** Mirrors `jobs.title check (char_length(trim(title)) between 3 and 200)`. */
    fun validateJobForm(
        title: String,
        company: String,
        location: String,
        category: String,
        description: String,
        salaryMinText: String,
        salaryMaxText: String,
        contactEmail: String
    ): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        when {
            title.isBlank() -> errors += FieldError(FormField.JOB_TITLE, R.string.field_required)
            title.trim().length < 3 ->
                errors += FieldError(FormField.JOB_TITLE, R.string.field_title_too_short)
        }

        when {
            company.isBlank() ->
                errors += FieldError(FormField.JOB_COMPANY, R.string.field_company_required)
            company.trim().length < 2 ->
                errors += FieldError(FormField.JOB_COMPANY, R.string.field_company_required)
        }

        when {
            location.isBlank() ->
                errors += FieldError(FormField.JOB_LOCATION, R.string.field_location_required)
            location.trim().length < 2 ->
                errors += FieldError(FormField.JOB_LOCATION, R.string.field_location_required)
        }

        if (category.isBlank()) {
            errors += FieldError(FormField.JOB_CATEGORY, R.string.field_required_select)
        }

        // Mirrors `jobs.description check (char_length(trim(description)) >= 10)`.
        when {
            description.isBlank() ->
                errors += FieldError(FormField.JOB_DESCRIPTION, R.string.field_required)
            description.trim().length < 10 ->
                errors += FieldError(FormField.JOB_DESCRIPTION, R.string.field_description_too_short)
        }

        val minSal = parseSalary(salaryMinText)
        val maxSal = parseSalary(salaryMaxText)

        if (salaryMinText.isNotBlank() && minSal == null) {
            errors += FieldError(FormField.JOB_SALARY_MIN, R.string.field_salary_invalid)
        }
        if (salaryMaxText.isNotBlank() && maxSal == null) {
            errors += FieldError(FormField.JOB_SALARY_MAX, R.string.field_salary_invalid)
        }
        // Mirrors `chk_job_salary_range`.
        if (minSal != null && maxSal != null && minSal > maxSal) {
            errors += FieldError(FormField.JOB_SALARY_MAX, R.string.field_salary_range_invalid)
        }

        if (contactEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(contactEmail.trim()).matches()) {
            errors += FieldError(FormField.JOB_CONTACT_EMAIL, R.string.field_email_invalid)
        }

        return errors
    }

    /**
     * Mirrors the gigs CHECK constraints. The schema requires `budget_etb > 0` and a description of
     * at least 10 characters, so an empty budget is a genuine error rather than something to
     * silently replace with a default.
     */
    fun validateGigForm(
        title: String,
        category: String,
        budgetText: String,
        durationText: String,
        description: String
    ): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        when {
            title.isBlank() -> errors += FieldError(FormField.GIG_TITLE, R.string.field_required)
            title.trim().length < 3 ->
                errors += FieldError(FormField.GIG_TITLE, R.string.field_title_too_short)
        }

        if (category.isBlank()) {
            errors += FieldError(FormField.GIG_CATEGORY, R.string.field_required_select)
        }

        val budget = budgetText.trim().toLongOrNull()
        if (budget == null || budget <= 0) {
            errors += FieldError(FormField.GIG_BUDGET, R.string.field_budget_invalid)
        }

        if (durationText.isBlank()) {
            errors += FieldError(FormField.GIG_DURATION, R.string.field_required)
        }

        when {
            description.isBlank() ->
                errors += FieldError(FormField.GIG_DESCRIPTION, R.string.field_required)
            description.trim().length < 10 ->
                errors += FieldError(FormField.GIG_DESCRIPTION, R.string.field_description_too_short)
        }

        return errors
    }

    /** Registration rules, matching `profiles.full_name` and the auth password policy. */
    fun validateRegistration(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        isCompany: Boolean,
        companyName: String
    ): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        when {
            fullName.isBlank() ->
                errors += FieldError(FormField.AUTH_FULL_NAME, R.string.field_full_name_required)
            fullName.trim().length < 2 ->
                errors += FieldError(FormField.AUTH_FULL_NAME, R.string.field_full_name_required)
        }

        if (!email.isBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errors += FieldError(FormField.AUTH_EMAIL, R.string.field_email_invalid)
        } else if (email.isBlank()) {
            errors += FieldError(FormField.AUTH_EMAIL, R.string.field_required)
        }

        if (password.length < 8) {
            errors += FieldError(FormField.AUTH_PASSWORD, R.string.field_password_too_short)
        }

        if (phone.isNotBlank() && !isValidEthiopianPhone(phone)) {
            errors += FieldError(FormField.AUTH_PHONE, R.string.field_phone_invalid)
        }

        if (isCompany && companyName.isBlank()) {
            errors += FieldError(FormField.AUTH_COMPANY_NAME, R.string.field_company_required)
        }

        return errors
    }

    /** Accepts `+2519xxxxxxxx`, `+2517xxxxxxxx`, `09xxxxxxxx`, `07xxxxxxxx`. */
    fun isValidEthiopianPhone(phone: String): Boolean {
        val cleaned = phone.trim().replace(" ", "")
        return Regex("^(\\+251|0)([97]\\d{8})$").matches(cleaned)
    }

    /** Blank means "negotiable", which the schema allows as 0. */
    private fun parseSalary(text: String): Long? =
        if (text.isBlank()) 0L else text.trim().toLongOrNull()
}
