-- ==============================================================================
-- SIRAHUB STAGING DEV SEED DATA (supabase/seed/dev_seed.sql)
-- FOR STAGING / DEVELOPMENT ENVIRONMENT ONLY
-- ==============================================================================

-- 1. DEV PROFILES & COMPANIES
INSERT INTO public.profiles (id, account_type, full_name, phone, city)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'company', 'Ethio Telecom HR', '+251 11 550 0000', 'Addis Ababa'),
    ('44444444-4444-4444-4444-444444444444', 'gig_provider', 'SiraHub Local Client', '+251 91 144 2233', 'Addis Ababa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.companies (id, owner_id, name, industry, location, website, contact_email, contact_phone, description, verification_status, employees_count)
VALUES
(
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '33333333-3333-3333-3333-333333333333',
    'Ethio Telecom',
    'Telecommunications & Digital Finance',
    'Addis Ababa (Bole Medhanialem)',
    'https://ethiotelecom.et',
    'careers@ethiotelecom.et',
    '+251 11 550 0000',
    'National telecommunications operator and provider of Telebirr mobile payment platform.',
    'verified',
    '5000+'
),
(
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '33333333-3333-3333-3333-333333333333',
    'Chapa Financial Technologies',
    'Financial Technology & Payment Gateways',
    'Addis Ababa (Bole Atlas)',
    'https://chapa.co',
    'dev-hiring@chapa.co',
    '+251 91 144 2233',
    'Fast-growing Ethiopian payment processing startup empowering African businesses.',
    'verified',
    '50-100'
)
ON CONFLICT (id) DO NOTHING;

-- 2. DEV JOBS
INSERT INTO public.jobs (
    id, company_id, posted_by, title, company, location, category, employment_type, workplace_type, experience_level,
    salary_min_etb, salary_max_etb, salary_period, description, responsibilities, requirements, benefits,
    contact_email, contact_phone, status, is_featured, is_urgent
)
VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '33333333-3333-3333-3333-333333333333',
    'Senior Android Engineer (Telebirr Integration)',
    'Ethio Telecom',
    'Addis Ababa (Bole Medhanialem)',
    'Tech & Software',
    'full_time',
    'hybrid',
    'senior',
    65000,
    85000,
    'month',
    'Lead development of next-generation Telebirr payment integrations and mobile banking modules.',
    ARRAY['Architect modern Android apps with Jetpack Compose & Coroutines', 'Integrate telecom billing & payment APIs', 'Mentor junior developers'],
    ARRAY['5+ years Android development with Kotlin', 'Experience with mobile fintech APIs', 'Degree in CS or Software Engineering'],
    ARRAY['Full medical insurance', 'Telecom data allowance', 'Annual performance bonus'],
    'careers@ethiotelecom.et',
    '+251 11 550 0000',
    'open',
    true,
    true
),
(
    '22222222-2222-2222-2222-222222222222',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '33333333-3333-3333-3333-333333333333',
    'Full Stack Kotlin & React Developer',
    'Chapa Financial Technologies',
    'Addis Ababa (Bole Atlas)',
    'Tech & Software',
    'full_time',
    'remote',
    'mid',
    45000,
    65000,
    'month',
    'Build reliable payment gateway dashboards and merchant SDKs for Ethiopian e-commerce platforms.',
    ARRAY['Develop RESTful services in Kotlin', 'Maintain merchant analytics web dashboard', 'Integrate bank settlement APIs'],
    ARRAY['2+ years Kotlin or Java backend experience', 'Proficiency in React/TypeScript', 'Understanding of web security'],
    ARRAY['Remote flexibility', 'Competitive ETB salary', 'Equity options'],
    'dev-hiring@chapa.co',
    '+251 91 144 2233',
    'open',
    false,
    false
)
ON CONFLICT (id) DO NOTHING;

-- 3. DEV GIGS
INSERT INTO public.gigs (
    id, owner_id, title, client_name, client_type, location, category, budget_etb, pricing_type, duration_text,
    description, deliverables, required_skills, status, is_urgent
)
VALUES
(
    '55555555-5555-5555-5555-555555555555',
    '44444444-4444-4444-4444-444444444444',
    'E-Commerce Mobile App UI Design (Figma)',
    'SiraHub Local Client',
    'Local Startup',
    'Addis Ababa',
    'Graphic Design',
    15000,
    'fixed',
    '1 Week',
    'Design 10 clean, modern mobile screens for an Ethiopian online retail grocery shop in Figma.',
    ARRAY['Figma design file with components', 'Clickable prototype', 'Amharic & English typography scale'],
    ARRAY['Figma', 'UI/UX Design', 'Mobile Prototyping'],
    'open',
    true
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.gig_contacts (gig_id, client_phone, client_telegram)
VALUES
(
    '55555555-5555-5555-5555-555555555555',
    '+251 91 144 2233',
    '@sirahub_client'
)
ON CONFLICT (gig_id) DO NOTHING;
