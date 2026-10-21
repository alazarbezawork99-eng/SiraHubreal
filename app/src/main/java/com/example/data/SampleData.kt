package com.example.data

import com.example.model.EmploymentType
import com.example.model.ExperienceLevel
import com.example.model.GigPost
import com.example.model.GigPricingType
import com.example.model.GigStatus
import com.example.model.JobPost
import com.example.model.WorkplaceType

object SampleData {
    val initialJobs: List<JobPost> = listOf(
        JobPost(
            id = "job_1",
            title = "Senior Android Engineer (Fintech & Telebirr)",
            company = "Ethio Telecom",
            location = "Addis Ababa (Bole Medhanialem)",
            category = "Tech & Software",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.HYBRID,
            experienceLevel = ExperienceLevel.SENIOR,
            salaryMinETB = 65000,
            salaryMaxETB = 85000,
            salaryPeriod = "month",
            description = "Lead the design and development of next-generation merchant and consumer payment features. You will collaborate with cross-functional engineering teams to build secure, ultra-responsive digital transaction experiences across Ethiopia.",
            responsibilities = listOf(
                "Architect modern Android applications utilizing Jetpack Compose, Kotlin Coroutines, and Clean Architecture.",
                "Integrate telecom billing APIs, biometric authentication, and QR-based offline-capable transactions.",
                "Mentor junior and mid-level engineers and conduct comprehensive code reviews."
            ),
            requirements = listOf(
                "5+ years of production Android development with Kotlin.",
                "Proven track record with mobile payment SDKs, encryption, and secure storage on Android.",
                "Bachelor's degree in Computer Science, Software Engineering, or equivalent experience.",
                "Fluency in Amharic and English."
            ),
            benefits = listOf(
                "Comprehensive medical insurance for employee and dependents.",
                "Annual telecom & high-speed data allowance.",
                "Performance-based annual bonus and transport stipend."
            ),
            postedTimeAgo = "2 days ago",
            deadline = "Nov 15, 2026",
            applicantsCount = 28,
            isFeatured = true,
            isUrgent = false,
            isVerifiedCompany = true,
            contactEmail = "careers@ethiotelecom.et",
            contactPhone = "+251 11 550 0000"
        ),
        JobPost(
            id = "job_2",
            title = "Digital Banking Operations Officer",
            company = "Commercial Bank of Ethiopia (CBE)",
            location = "Addis Ababa (CBE HQ Tower, Sengatera)",
            category = "Banking & Finance",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.ON_SITE,
            experienceLevel = ExperienceLevel.MID,
            salaryMinETB = 42000,
            salaryMaxETB = 58000,
            salaryPeriod = "month",
            description = "Ensure 24/7 reliability and reconciliation across CBE Birr digital channels, automated teller machines, and mobile banking servers.",
            responsibilities = listOf(
                "Monitor high-volume transaction gateways and resolve customer payment disputes.",
                "Coordinate with backend network engineers during core banking maintenance windows.",
                "Generate daily compliance audit reports for National Bank of Ethiopia (NBE) regulatory standards."
            ),
            requirements = listOf(
                "Degree in Banking, Finance, Information Systems, or Economics.",
                "At least 3 years working in banking operations or fintech systems.",
                "Strong analytical troubleshooting mindset with Excel and SQL."
            ),
            benefits = listOf(
                "Subsidized mortgage and staff interest-free loans.",
                "Full medical coverage and continuous professional banking training.",
                "Attractive annual holiday bonuses."
            ),
            postedTimeAgo = "Just now",
            deadline = "Nov 20, 2026",
            applicantsCount = 45,
            isFeatured = true,
            isUrgent = true,
            isVerifiedCompany = true,
            contactEmail = "jobs@cbe.com.et"
        ),
        JobPost(
            id = "job_3",
            title = "Enterprise B2B Account Manager",
            company = "Safaricom Ethiopia",
            location = "Addis Ababa (Kazanchis)",
            category = "Sales & Marketing",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.HYBRID,
            experienceLevel = ExperienceLevel.MID,
            salaryMinETB = 50000,
            salaryMaxETB = 72000,
            salaryPeriod = "month",
            description = "Drive corporate connectivity, cloud services, and M-PESA enterprise adoption among top manufacturing, banking, and retail clients in Ethiopia.",
            responsibilities = listOf(
                "Cultivate relationships with C-level executives in Addis Ababa, Hawassa, and Dire Dawa.",
                "Negotiate bespoke enterprise connectivity contracts and dedicated fiber SLA agreements.",
                "Exceed quarterly enterprise revenue quotas."
            ),
            requirements = listOf(
                "B.A. in Business Administration, Marketing, or Telecommunications.",
                "3+ years enterprise sales experience in tech or telecommunications in Ethiopia.",
                "Exceptional presentation and communication skills in Amharic and English."
            ),
            benefits = listOf(
                "Competitive sales commission structure in addition to base salary.",
                "Company car / fuel allowance.",
                "Global training certifications."
            ),
            postedTimeAgo = "3 days ago",
            deadline = "Nov 10, 2026",
            applicantsCount = 19,
            isFeatured = false,
            isUrgent = false,
            isVerifiedCompany = true,
            contactEmail = "recruitment@safaricom.et"
        ),
        JobPost(
            id = "job_4",
            title = "Full-Stack Software Engineer (Kotlin / React)",
            company = "Chapa Financial Technologies",
            location = "Remote (Ethiopia)",
            category = "Tech & Software",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.REMOTE,
            experienceLevel = ExperienceLevel.MID,
            salaryMinETB = 55000,
            salaryMaxETB = 75000,
            salaryPeriod = "month",
            description = "Build scalable payment APIs, developer portals, and checkout widgets empowering thousands of Ethiopian online merchants.",
            responsibilities = listOf(
                "Design RESTful microservices processing real-time card and mobile wallet transactions.",
                "Maintain developer documentation and client SDKs.",
                "Optimize database query performance and maintain 99.99% uptime."
            ),
            requirements = listOf(
                "Proficiency with Kotlin/Java, TypeScript, React, and PostgreSQL.",
                "Familiarity with financial compliance, webhook dispatching, and security standards.",
                "Self-motivated remote work discipline."
            ),
            benefits = listOf(
                "100% remote work flexibility from any city in Ethiopia.",
                "Home office setup budget (laptop, monitor, UPS power backup).",
                "Equity stock options in rapidly growing startup."
            ),
            postedTimeAgo = "1 day ago",
            deadline = "Dec 1, 2026",
            applicantsCount = 33,
            isFeatured = true,
            isUrgent = false,
            isVerifiedCompany = true,
            contactEmail = "dev-hiring@chapa.co"
        ),
        JobPost(
            id = "job_5",
            title = "Humanitarian Logistics Coordinator",
            company = "Ethiopian Red Cross Society",
            location = "Hawassa (Sidama Region)",
            category = "NGO & Development",
            employmentType = EmploymentType.CONTRACT,
            workplaceType = WorkplaceType.ON_SITE,
            experienceLevel = ExperienceLevel.MID,
            salaryMinETB = 38000,
            salaryMaxETB = 48000,
            salaryPeriod = "month",
            description = "Coordinate supply chain, relief aid warehouse distribution, and emergency response inventory across southern Ethiopia.",
            responsibilities = listOf(
                "Supervise warehouse logistics and dispatch of medical kits and dry rations.",
                "Ensure compliance with international humanitarian relief standards and fleet tracking.",
                "Liaise with local community elders, zonal administrative offices, and UN agencies."
            ),
            requirements = listOf(
                "Bachelor's degree in Supply Chain Management, Logistics, or Disaster Preparedness.",
                "2+ years experience in humanitarian field logistics.",
                "Fluency in Amharic, Sidaamu Afoo or Oromo is a strong advantage."
            ),
            benefits = listOf(
                "Field per diem allowance and hardship bonus.",
                "Full medical coverage and hazard insurance.",
                "Opportunities for international deployment."
            ),
            postedTimeAgo = "4 days ago",
            deadline = "Nov 12, 2026",
            applicantsCount = 14,
            isFeatured = false,
            isUrgent = true,
            isVerifiedCompany = true,
            contactEmail = "hr@redcrosseth.org"
        ),
        JobPost(
            id = "job_6",
            title = "Civil Site Engineer & Construction Supervisor",
            company = "Sunshine Construction & Real Estate",
            location = "Addis Ababa (CMC & Bole Ayat)",
            category = "Engineering & Construction",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.ON_SITE,
            experienceLevel = ExperienceLevel.MID,
            salaryMinETB = 35000,
            salaryMaxETB = 52000,
            salaryPeriod = "month",
            description = "Direct site progress for luxury multi-story residential towers, inspect structural concrete pours, and manage subcontractor schedules.",
            responsibilities = listOf(
                "Oversee day-to-day site operations ensuring adherence to architectural blueprints and structural specs.",
                "Conduct quality checks on steel reinforcement, cement grading, and masonry work.",
                "Enforce strict on-site Occupational Health & Safety (OHS) rules."
            ),
            requirements = listOf(
                "B.Sc. in Civil Engineering or Construction Management.",
                "3+ years of high-rise building site experience.",
                "Proficiency with AutoCAD, Revit, and project scheduling tools."
            ),
            benefits = listOf(
                "Site transport and communication allowance.",
                "Completion milestone bonuses.",
                "Professional engineer licensing sponsorship."
            ),
            postedTimeAgo = "5 days ago",
            deadline = "Nov 25, 2026",
            applicantsCount = 22,
            isFeatured = false,
            isUrgent = false,
            isVerifiedCompany = true,
            contactEmail = "jobs@sunshineconstruction.net"
        ),
        JobPost(
            id = "job_7",
            title = "Junior Graphic Designer & Social Media Creator",
            company = "YenePay Financial Services",
            location = "Addis Ababa (Piassa / Churchil Ave)",
            category = "Design & Creative",
            employmentType = EmploymentType.FULL_TIME,
            workplaceType = WorkplaceType.ON_SITE,
            experienceLevel = ExperienceLevel.ENTRY,
            salaryMinETB = 25000,
            salaryMaxETB = 35000,
            salaryPeriod = "month",
            description = "Create engaging social media graphics, product infographics, and digital billboard designs for Ethiopian consumer fintech campaigns.",
            responsibilities = listOf(
                "Design weekly promotional banners for Telegram, Instagram, and LinkedIn.",
                "Collaborate with the marketing team on seasonal holiday promotions (Enkutatash, Ganna, Eid).",
                "Create localized illustrations with Ethiopian visual motifs."
            ),
            requirements = listOf(
                "Strong portfolio of graphic design work (Figma, Photoshop, Illustrator).",
                "Good eye for typography, Ethiopian cultural aesthetics, and color balance.",
                "Creative, enthusiastic team player."
            ),
            benefits = listOf(
                "Modern creative studio workspace.",
                "Daily catered lunch and coffee.",
                "Mentorship from experienced creative directors."
            ),
            postedTimeAgo = "Just now",
            deadline = "Nov 18, 2026",
            applicantsCount = 12,
            isFeatured = false,
            isUrgent = true,
            isVerifiedCompany = true,
            contactEmail = "design@yenepay.com"
        )
    )

    val initialGigs: List<GigPost> = listOf(
        GigPost(
            id = "gig_1",
            title = "Translate 25-Page Commercial Contract (Amharic to English)",
            clientName = "Solomon & Associates Law Office",
            clientType = "Legal Consulting Firm",
            location = "Addis Ababa (Kazanchis / Remote)",
            category = "Translation & Writing",
            budgetETB = 14000,
            pricingType = GigPricingType.FIXED,
            durationText = "4 Days",
            description = "Need a certified or experienced legal translator to accurately translate a 25-page commercial joint venture agreement between a foreign investor and an Ethiopian manufacturing firm from Amharic to standard legal English.",
            deliverables = listOf(
                "Formatted Word Document with matching paragraph numbering.",
                "Glossary of translated Ethiopian legal & trade terminology.",
                "One revision round if clarification needed."
            ),
            requiredSkills = listOf("Amharic Translation", "Legal English", "Proofreading", "Contract Law"),
            bidsCount = 7,
            postedTimeAgo = "3 hours ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 91 144 8899",
            clientTelegram = "@solomon_legal",
            isUrgent = true
        ),
        GigPost(
            id = "gig_2",
            title = "Brand Identity & Logo for Specialty Buna (Coffee) Shop",
            clientName = "Kaffa Origin Coffee Roasters",
            clientType = "Cafe & Retail Startup",
            location = "Addis Ababa (Bole Atlas)",
            category = "Graphic Design",
            budgetETB = 18500,
            pricingType = GigPricingType.FIXED,
            durationText = "1 Week",
            description = "We are launching an artisanal Ethiopian specialty coffee house in Bole. We need a complete visual identity: logo badge, coffee pouch sticker label, coffee cup print design, and social media brand kit blending modern minimalism with traditional clay Jebena aesthetic.",
            deliverables = listOf(
                "Vector Logo package (.AI, .SVG, .PNG, .PDF).",
                "Color palette & typography guidelines.",
                "Print-ready packaging templates for 250g and 500g roasted coffee beans.",
                "Instagram profile branding kit."
            ),
            requiredSkills = listOf("Logo Design", "Packaging Design", "Figma/Illustrator", "Brand Identity"),
            bidsCount = 12,
            postedTimeAgo = "6 hours ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 92 333 4455",
            clientTelegram = "@kaffacoffee_et",
            isUrgent = false
        ),
        GigPost(
            id = "gig_3",
            title = "Telebirr & CBE Birr Payment Gateway Integration for WooCommerce",
            clientName = "HabeshaCraft Online Market",
            clientType = "E-Commerce Merchant",
            location = "Remote (Anywhere in Ethiopia)",
            category = "Web & Mobile Dev",
            budgetETB = 32000,
            pricingType = GigPricingType.FIXED,
            durationText = "10 Days",
            description = "Our WordPress/WooCommerce handicraft store needs seamless integration with Telebirr merchant gateway and Commercial Bank of Ethiopia (CBE Birr). Must support real-time IPN callback verification and automatic order status update upon payment receipt.",
            deliverables = listOf(
                "Tested WordPress plugin / custom integration module.",
                "Staging server demonstration with sandbox test payments.",
                "Full deployment to production and brief handover guide."
            ),
            requiredSkills = listOf("PHP/WordPress", "WooCommerce", "Telebirr API", "CBE Birr Gateway", "cURL/REST"),
            bidsCount = 9,
            postedTimeAgo = "1 day ago",
            status = GigStatus.OPEN,
            clientTelegram = "@habeshacraft_admin",
            isUrgent = true
        ),
        GigPost(
            id = "gig_4",
            title = "Event Photography & Quick Video Reels for 2-Day Conference",
            clientName = "Horn of Africa Tech Summit",
            clientType = "Event Organizer",
            location = "Addis Ababa (Sheraton / UNECA)",
            category = "Photography & Video",
            budgetETB = 28000,
            pricingType = GigPricingType.FIXED,
            durationText = "2 Days (Weekend)",
            description = "Seeking a professional photographer with their own high-end mirrorless gear to cover keynote speeches, panel discussions, and networking cocktail sessions. Same-day delivery of top 25 edited photos for press release, and 3 vertical 30-sec reels for Instagram/LinkedIn.",
            deliverables = listOf(
                "200+ high-res color graded event photos.",
                "3 vertical edited reels with background sound & speaker subtitles.",
                "RAW files delivered on portable drive."
            ),
            requiredSkills = listOf("Event Photography", "Lightroom Color Grading", "Video Editing", "Reels/Shorts"),
            bidsCount = 5,
            postedTimeAgo = "1 day ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 91 299 1122",
            isUrgent = false
        ),
        GigPost(
            id = "gig_5",
            title = "QuickBooks Setup & Monthly VAT/TOT Tax Reconciliation",
            clientName = "Nile Valley Trading PLC",
            clientType = "Import & Distribution",
            location = "Addis Ababa (Merkato / Autobus Tera)",
            category = "Accounting & Finance",
            budgetETB = 16000,
            pricingType = GigPricingType.FIXED,
            durationText = "5 Days",
            description = "Setup chart of accounts in QuickBooks Desktop for our wholesale consumer goods business. Reconcile past 3 months of bank statements (CBE, Awash, Dashen) and prepare monthly Ethiopian Ministry of Revenues VAT declaration worksheets.",
            deliverables = listOf(
                "Properly configured Chart of Accounts & Customer/Vendor lists.",
                "Bank reconciliation reports for Q3.",
                "Standard operating procedure document for daily cash sales entry."
            ),
            requiredSkills = listOf("QuickBooks", "Ethiopian Tax Law (VAT/TOT)", "Bank Reconciliation", "Accounting"),
            bidsCount = 8,
            postedTimeAgo = "2 days ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 91 188 7766",
            isUrgent = false
        ),
        GigPost(
            id = "gig_6",
            title = "Private Math & Physics Tutoring for Grade 12 National Exam",
            clientName = "Aster M. (Parent)",
            clientType = "Individual",
            location = "Addis Ababa (Old Airport / Bisrate Gabriel)",
            category = "Tutoring & Education",
            budgetETB = 7500,
            pricingType = GigPricingType.FIXED,
            durationText = "3 Weeks (3 sessions/week)",
            description = "Looking for an energetic engineering or science university graduate to provide one-on-one exam preparation tutoring in Grade 11 & 12 Physics and College Mathematics for a student preparing for university entrance examination.",
            deliverables = listOf(
                "3 in-person 1.5 hour sessions per week at family residence.",
                "Weekly practice tests with problem walkthroughs."
            ),
            requiredSkills = listOf("Physics Tutoring", "Mathematics", "Ethiopian Curriculum", "Patience"),
            bidsCount = 4,
            postedTimeAgo = "2 days ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 93 455 6677",
            isUrgent = false
        ),
        GigPost(
            id = "gig_7",
            title = "Urgent Delivery & Handling of Sample Coffee Bags",
            clientName = "Abyssinia Export Group",
            clientType = "Coffee Exporter",
            location = "Addis Ababa (Bole Airport Cargo to Piassa)",
            category = "Errands & Logistics",
            budgetETB = 4000,
            pricingType = GigPricingType.FIXED,
            durationText = "Urgent - Today",
            description = "Need a trusted courier with motorbike or small car to pick up 4 sealed coffee cupping sample boxes from Bole Airport Cargo terminal and securely deliver them to our testing lab in Piassa before 4:00 PM.",
            deliverables = listOf(
                "Signed delivery confirmation receipt from lab technician.",
                "Safe transit with no moisture or damage."
            ),
            requiredSkills = listOf("City Courier", "Punctuality", "Motorbike / Vehicle", "Addis Ababa Navigation"),
            bidsCount = 6,
            postedTimeAgo = "1 hour ago",
            status = GigStatus.OPEN,
            clientPhone = "+251 91 100 2233",
            isUrgent = true
        )
    )
}
