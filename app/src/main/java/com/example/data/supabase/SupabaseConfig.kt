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

    var supabaseUrl: String
        get() = clientConfig.activeSupabaseUrl
        set(value) {
            clientConfig.activeSupabaseUrl = value
        }

    var supabaseAnonKey: String
        get() = clientConfig.activeAnonKey
        set(value) {
            clientConfig.activeAnonKey = value
        }

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

    companion object {
        /**
         * Comprehensive, ready-to-run Master SQL Query for Supabase.
         * Creates all tables, sets up Row Level Security (RLS) policies,
         * and populates initial verified Ethiopian jobs, companies, gigs, and applicants.
         */
        val SQL_SETUP_SCRIPT: String = """
-- ===============================================================
-- SIRAHUB (ETHIOPIAN JOBS & GIGS) MASTER DATABASE SCHEMA & SEED
-- Paste and Run this in your Supabase SQL Editor (supabase.com)
-- ===============================================================

-- 1. JOBS TABLE (Company vacancies & opportunities)
CREATE TABLE IF NOT EXISTS public.jobs (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    company TEXT NOT NULL,
    location TEXT NOT NULL,
    category TEXT NOT NULL,
    employment_type TEXT NOT NULL DEFAULT 'FULL_TIME',
    workplace_type TEXT NOT NULL DEFAULT 'HYBRID',
    experience_level TEXT NOT NULL DEFAULT 'MID',
    salary_min_etb BIGINT DEFAULT 0,
    salary_max_etb BIGINT DEFAULT 0,
    salary_period TEXT DEFAULT 'month',
    description TEXT,
    responsibilities JSONB DEFAULT '[]'::jsonb,
    requirements JSONB DEFAULT '[]'::jsonb,
    benefits JSONB DEFAULT '[]'::jsonb,
    deadline TEXT DEFAULT 'Open until filled',
    applicants_count INT DEFAULT 0,
    contact_email TEXT,
    contact_phone TEXT,
    is_verified BOOLEAN DEFAULT true,
    is_urgent BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. GIGS & TASKS TABLE (Freelance short-term projects)
CREATE TABLE IF NOT EXISTS public.gigs (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    client_name TEXT NOT NULL,
    client_type TEXT DEFAULT 'Client',
    location TEXT DEFAULT 'Addis Ababa',
    category TEXT NOT NULL,
    budget_etb BIGINT NOT NULL,
    pricing_type TEXT NOT NULL DEFAULT 'FIXED',
    duration_text TEXT NOT NULL,
    description TEXT,
    deliverables JSONB DEFAULT '[]'::jsonb,
    required_skills JSONB DEFAULT '[]'::jsonb,
    bids_count INT DEFAULT 0,
    client_phone TEXT,
    client_telegram TEXT,
    is_urgent BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. APPLICATIONS TABLE (Job seeker CV applications)
CREATE TABLE IF NOT EXISTS public.applications (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL,
    job_title TEXT NOT NULL,
    company_name TEXT NOT NULL,
    applicant_name TEXT NOT NULL,
    applicant_email TEXT NOT NULL,
    applicant_phone TEXT NOT NULL,
    applicant_headline TEXT DEFAULT 'Candidate',
    applicant_skills JSONB DEFAULT '[]'::jsonb,
    applicant_education TEXT,
    applicant_experience TEXT,
    cover_note TEXT,
    expected_salary_etb BIGINT,
    status TEXT DEFAULT 'APPLIED',
    applied_date TEXT DEFAULT 'Today',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. BIDS & PROPOSALS TABLE (Freelancer proposals on gigs)
CREATE TABLE IF NOT EXISTS public.bids (
    id TEXT PRIMARY KEY,
    gig_id TEXT NOT NULL,
    gig_title TEXT NOT NULL,
    client_name TEXT NOT NULL,
    bidder_name TEXT NOT NULL,
    bidder_phone TEXT NOT NULL,
    bid_amount_etb BIGINT NOT NULL,
    delivery_days TEXT NOT NULL,
    proposal_pitch TEXT,
    status TEXT DEFAULT 'SUBMITTED',
    submitted_date TEXT DEFAULT 'Today',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. COMPANIES & EMPLOYERS TABLE (Verified hiring entities)
CREATE TABLE IF NOT EXISTS public.companies (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    industry TEXT NOT NULL,
    location TEXT NOT NULL,
    website TEXT,
    contact_email TEXT,
    contact_phone TEXT,
    description TEXT,
    is_verified BOOLEAN DEFAULT true,
    employees_count TEXT DEFAULT '50-100',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. USER PROFILES TABLE (Job Seekers, Companies, Gig Posters)
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT,
    account_type TEXT NOT NULL DEFAULT 'JOB_SEEKER',
    company_name TEXT,
    headline TEXT,
    city_location TEXT DEFAULT 'Addis Ababa, Ethiopia',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. ENABLE ROW LEVEL SECURITY (RLS)
ALTER TABLE public.jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.gigs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bids ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 8. RLS POLICIES (Full CRUD access for mobile app client)
DROP POLICY IF EXISTS "Public full access jobs" ON public.jobs;
CREATE POLICY "Public full access jobs" ON public.jobs FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access gigs" ON public.gigs;
CREATE POLICY "Public full access gigs" ON public.gigs FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access applications" ON public.applications;
CREATE POLICY "Public full access applications" ON public.applications FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access bids" ON public.bids;
CREATE POLICY "Public full access bids" ON public.bids FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access companies" ON public.companies;
CREATE POLICY "Public full access companies" ON public.companies FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access profiles" ON public.profiles;
CREATE POLICY "Public full access profiles" ON public.profiles FOR ALL USING (true) WITH CHECK (true);

-- 9. INITIAL SEED DATA (Ethiopian Companies, Jobs, Gigs & Applicants)
INSERT INTO public.companies (id, name, industry, location, website, contact_email, contact_phone, description, is_verified, employees_count)
VALUES
(
    'comp_ethio_1',
    'Ethio Telecom',
    'Telecommunications & Digital Finance',
    'Addis Ababa (Bole Medhanialem)',
    'https://ethiotelecom.et',
    'careers@ethiotelecom.et',
    '+251 11 550 0000',
    'National telecommunications operator and provider of Telebirr mobile payment platform.',
    true,
    '5000+'
),
(
    'comp_cbe_2',
    'Commercial Bank of Ethiopia (CBE)',
    'Banking & Financial Services',
    'Addis Ababa (Sengatera)',
    'https://cbe.com.et',
    'jobs@cbe.com.et',
    '+251 11 122 8800',
    'The premier commercial bank in Ethiopia with extensive branch and mobile banking networks.',
    true,
    '10000+'
),
(
    'comp_chapa_3',
    'Chapa Financial Technologies',
    'Financial Technology & Payment Gateways',
    'Remote / Addis Ababa',
    'https://chapa.co',
    'dev-hiring@chapa.co',
    '+251 91 144 2233',
    'Fast-growing Ethiopian payment processing startup empowering African businesses.',
    true,
    '50-100'
),
(
    'comp_default_1',
    'EthioFintech & Tech Labs',
    'Technology & Financial Services',
    'Addis Ababa (Bole Atlas)',
    'https://ethiofintech.et',
    'recruitment@ethiofintech.et',
    '+251 91 144 5566',
    'Mobile financial software development lab and enterprise payment integrator.',
    true,
    '50-100'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.jobs (id, title, company, location, category, employment_type, workplace_type, experience_level, salary_min_etb, salary_max_etb, description, responsibilities, requirements, benefits, contact_email, contact_phone, is_verified, is_urgent)
VALUES
(
    'job_ethio_1',
    'Senior Android Engineer (Fintech & Telebirr)',
    'Ethio Telecom',
    'Addis Ababa (Bole Medhanialem)',
    'Tech & Software',
    'FULL_TIME',
    'HYBRID',
    'SENIOR',
    65000,
    85000,
    'Lead development of next-generation Telebirr payment integrations and mobile banking modules.',
    '["Architect modern Android apps with Jetpack Compose & Coroutines", "Integrate telecom billing & payment APIs", "Mentor junior developers"]'::jsonb,
    '["5+ years Android development with Kotlin", "Experience with mobile fintech APIs", "Degree in Computer Science or Software Engineering"]'::jsonb,
    '["Full medical insurance", "Telecom data allowance", "Annual performance bonus"]'::jsonb,
    'careers@ethiotelecom.et',
    '+251 11 550 0000',
    true,
    false
),
(
    'job_cbe_2',
    'Digital Banking Operations Officer',
    'Commercial Bank of Ethiopia (CBE)',
    'Addis Ababa (CBE HQ Tower, Sengatera)',
    'Banking & Finance',
    'FULL_TIME',
    'ON_SITE',
    'MID',
    42000,
    58000,
    'Monitor 24/7 reliability and reconciliation across CBE Birr digital channels and mobile servers.',
    '["Monitor high-volume transaction gateways", "Resolve customer payment disputes", "Prepare regulatory audit reports"]'::jsonb,
    '["Degree in Banking, Finance, or Information Systems", "3+ years banking operations experience", "Strong analytical skills"]'::jsonb,
    '["Subsidized mortgage & staff loans", "Full health coverage", "Holiday bonuses"]'::jsonb,
    'jobs@cbe.com.et',
    '+251 11 122 8800',
    true,
    true
),
(
    'job_chapa_3',
    'Full-Stack Software Engineer (Kotlin / React)',
    'Chapa Financial Technologies',
    'Remote (Ethiopia)',
    'Tech & Software',
    'FULL_TIME',
    'REMOTE',
    'MID',
    55000,
    75000,
    'Build scalable developer payment APIs and checkout widgets empowering Ethiopian merchants.',
    '["Design microservices processing real-time card and mobile wallet transactions", "Maintain developer SDKs"]'::jsonb,
    '["Proficiency with Kotlin/Java and TypeScript/React", "Self-motivated remote work discipline"]'::jsonb,
    '["100% remote flexibility", "Home office budget (UPS, monitors)", "Stock options"]'::jsonb,
    'dev-hiring@chapa.co',
    '+251 91 144 2233',
    true,
    false
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.gigs (id, title, client_name, client_type, location, category, budget_etb, pricing_type, duration_text, description, deliverables, required_skills, client_phone, client_telegram, is_urgent)
VALUES
(
    'gig_trans_1',
    'Translate 25-Page Commercial Contract (Amharic to English)',
    'Solomon & Associates Law Office',
    'Legal Consulting Firm',
    'Addis Ababa (Kazanchis / Remote)',
    'Translation & Writing',
    14000,
    'FIXED',
    '4 Days',
    'Need certified legal translator to translate a 25-page commercial contract from Amharic to formal legal English.',
    '["Formatted Word document with matching numbering", "Glossary of legal terms", "One revision round"]'::jsonb,
    '["Amharic Translation", "Legal English", "Proofreading"]'::jsonb,
    '+251 91 144 8899',
    '@solomon_legal',
    true
),
(
    'gig_cafe_2',
    'Brand Identity & Logo for Specialty Buna (Coffee) Shop',
    'Kaffa Origin Coffee Roasters',
    'Cafe & Retail Startup',
    'Addis Ababa (Bole Atlas)',
    'Graphic Design',
    18500,
    'FIXED',
    '1 Week',
    'Complete visual identity for artisanal Ethiopian specialty coffee house in Bole blending Jebena aesthetic with modern design.',
    '["Vector logo package (.AI, .SVG, .PNG)", "Print-ready coffee pouch sticker templates", "Social media brand kit"]'::jsonb,
    '["Logo Design", "Packaging Design", "Figma/Illustrator"]'::jsonb,
    '+251 92 333 4455',
    '@kaffacoffee_et',
    false
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.applications (id, job_id, job_title, company_name, applicant_name, applicant_email, applicant_phone, applicant_headline, applicant_skills, applicant_education, applicant_experience, cover_note, expected_salary_etb, status, applied_date)
VALUES
(
    'app_cand_1',
    'job_ethio_1',
    'Senior Android Engineer (Fintech & Telebirr)',
    'Ethio Telecom',
    'Tadesse Hailu',
    'tadesse.hailu@aau.edu.et',
    '+251 92 455 7788',
    'Senior Mobile Engineer & Security Specialist',
    '["Kotlin", "Jetpack Compose", "Coroutines", "Telebirr Gateway", "CBE Birr"]'::jsonb,
    'M.Sc. in Software Engineering, Addis Ababa University (AAU)',
    '5 years developing high-throughput mobile banking apps at Dashen Bank and commercial fintechs.',
    'I have architected 3 major payment SDKs in Ethiopia with bank-grade encryption and CBE Birr APIs.',
    85000,
    'SHORTLISTED',
    '2 days ago'
),
(
    'app_cand_2',
    'job_ethio_1',
    'Senior Android Engineer (Fintech & Telebirr)',
    'Ethio Telecom',
    'Bethlehem Worku',
    'betti.worku.dev@gmail.com',
    '+251 91 322 9900',
    'Full-Stack Android & Microservices Developer',
    '["Kotlin", "Spring Boot", "Jetpack Compose", "PostgreSQL", "Docker"]'::jsonb,
    'B.Sc. in Information Technology, Hawassa University',
    '4 years building customer checkout systems and microservices in Addis Ababa.',
    'Passionate about scaling Ethiopian tech products. Strong background in both Android UI and robust backend integration.',
    78000,
    'INTERVIEW',
    'Yesterday'
)
ON CONFLICT (id) DO NOTHING;
        """.trimIndent()
    }
}
