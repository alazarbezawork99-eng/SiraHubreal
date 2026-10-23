-- ==============================================================================
-- SIRAHUB PRODUCTION DATABASE SCHEMA (MIGRATION 0001_schema.sql)
-- ==============================================================================
-- TABLE OVERVIEW & EXPLANATION:
-- 1. profiles: Stores user profile information linked directly to Supabase auth users.
-- 2. seeker_cvs: Stores digital CV overview details for job seekers.
-- 3. education: Stores academic degree entries belonging to a job seeker's CV.
-- 4. experience: Stores work history entries belonging to a job seeker's CV.
-- 5. portfolio_links: Stores portfolio and project links owned by a job seeker.
-- 6. companies: Stores employer company profiles, industry details, and verification status.
-- 7. jobs: Stores formal job vacancy listings posted by verified companies and employers.
-- 8. gigs: Stores freelance project and task listings posted by gig clients.
-- 9. gig_contacts: Stores direct contact info (phone & Telegram) for gig clients separate from public gig posts.
-- 10. applications: Stores job application submissions sent by job seekers.
-- 11. bids: Stores freelance proposals and bid amounts submitted by freelancers for gigs.
-- 12. ratings: Stores star ratings and feedback reviews between clients and freelancers for completed gigs.
-- 13. bookmarks: Stores favorited job and gig listings saved by users.
-- 14. notifications: Stores in-app system, application, and bid alerts for users.
-- 15. notification_settings: Stores user notification and job alert preferences.
-- 16. reports: Stores user-submitted flag reports against posts, profiles, or applications.
-- 17. device_tokens: Stores FCM push notification tokens associated with user accounts.
-- 18. saved_searches: Stores customized job and gig search filter criteria saved by users.
-- ==============================================================================

-- Enable required Extensions
create extension if not exists "uuid-ossp";
create extension if not exists "pg_trgm";

-- Custom Domain Enums
create type public.account_type as enum ('job_seeker', 'company', 'gig_provider');
create type public.company_verification_status as enum ('unverified', 'pending', 'verified', 'rejected');
create type public.job_status as enum ('draft', 'open', 'closed', 'expired');
create type public.gig_status as enum ('open', 'under_review', 'assigned', 'completed');
create type public.application_status as enum ('applied', 'under_review', 'shortlisted', 'interview', 'offer', 'rejected');
create type public.bid_status as enum ('submitted', 'client_reviewing', 'shortlisted', 'accepted', 'completed', 'declined');
create type public.employment_type as enum ('full_time', 'part_time', 'contract', 'internship');
create type public.workplace_type as enum ('on_site', 'hybrid', 'remote');
create type public.experience_level as enum ('entry', 'mid', 'senior', 'lead');
create type public.gig_pricing_type as enum ('fixed', 'hourly', 'daily');

-- ------------------------------------------------------------------------------
-- 1. PROFILES
-- ------------------------------------------------------------------------------
create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    account_type public.account_type not null default 'job_seeker',
    full_name text not null check (char_length(trim(full_name)) >= 2 and char_length(full_name) <= 120),
    phone text check (phone is null or char_length(phone) <= 30),
    city text check (city is null or char_length(city) <= 100),
    avatar_path text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 2. SEEKER CVS
-- ------------------------------------------------------------------------------
create table public.seeker_cvs (
    id uuid primary key default gen_random_uuid(),
    seeker_id uuid not null unique references public.profiles(id) on delete cascade,
    headline text check (headline is null or char_length(headline) <= 150),
    bio text check (bio is null or char_length(bio) <= 2000),
    skills text[] not null default '{}'::text[],
    languages text[] not null default '{}'::text[],
    expected_salary_min_etb bigint check (expected_salary_min_etb is null or expected_salary_min_etb >= 0),
    availability text check (availability is null or char_length(availability) <= 100),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 3. EDUCATION
-- ------------------------------------------------------------------------------
create table public.education (
    id uuid primary key default gen_random_uuid(),
    cv_id uuid not null references public.seeker_cvs(id) on delete cascade,
    institution text not null check (char_length(trim(institution)) >= 2),
    degree text not null check (char_length(trim(degree)) >= 2),
    field_of_study text,
    start_year text,
    end_year text,
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 4. EXPERIENCE
-- ------------------------------------------------------------------------------
create table public.experience (
    id uuid primary key default gen_random_uuid(),
    cv_id uuid not null references public.seeker_cvs(id) on delete cascade,
    role_title text not null check (char_length(trim(role_title)) >= 2),
    company_name text not null check (char_length(trim(company_name)) >= 2),
    location text,
    start_date text,
    end_date text,
    description text,
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 5. PORTFOLIO LINKS
-- ------------------------------------------------------------------------------
create table public.portfolio_links (
    id uuid primary key default gen_random_uuid(),
    cv_id uuid not null references public.seeker_cvs(id) on delete cascade,
    title text not null check (char_length(trim(title)) >= 2),
    url text not null check (char_length(trim(url)) >= 5),
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 6. COMPANIES
-- ------------------------------------------------------------------------------
create table public.companies (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references public.profiles(id) on delete cascade,
    name text not null check (char_length(trim(name)) >= 2 and char_length(name) <= 150),
    industry text check (industry is null or char_length(industry) <= 100),
    location text check (location is null or char_length(location) <= 150),
    website text check (website is null or char_length(website) <= 255),
    contact_email text check (contact_email is null or char_length(contact_email) <= 255),
    contact_phone text check (contact_phone is null or char_length(contact_phone) <= 30),
    description text check (description is null or char_length(description) <= 3000),
    verification_status public.company_verification_status not null default 'unverified',
    employees_count text,
    founded_year text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 7. JOBS
-- ------------------------------------------------------------------------------
create table public.jobs (
    id uuid primary key default gen_random_uuid(),
    company_id uuid references public.companies(id) on delete cascade,
    posted_by uuid not null references public.profiles(id) on delete cascade,
    title text not null check (char_length(trim(title)) >= 3 and char_length(title) <= 200),
    company text not null check (char_length(trim(company)) >= 2),
    location text not null check (char_length(trim(location)) >= 2),
    category text not null check (char_length(trim(category)) >= 2),
    employment_type public.employment_type not null default 'full_time',
    workplace_type public.workplace_type not null default 'hybrid',
    experience_level public.experience_level not null default 'mid',
    salary_min_etb bigint default 0 check (salary_min_etb >= 0),
    salary_max_etb bigint default 0 check (salary_max_etb >= 0),
    salary_period text default 'month' check (salary_period in ('month', 'project', 'year')),
    description text not null check (char_length(trim(description)) >= 10),
    responsibilities text[] not null default '{}'::text[],
    requirements text[] not null default '{}'::text[],
    benefits text[] not null default '{}'::text[],
    is_featured boolean not null default false,
    is_urgent boolean not null default false,
    is_verified_company boolean not null default true,
    contact_email text,
    contact_phone text,
    status public.job_status not null default 'open',
    expires_at timestamptz,
    applicants_count integer not null default 0 check (applicants_count >= 0),
    fts tsvector generated always as (
        to_tsvector('english', coalesce(title, '') || ' ' || coalesce(company, '') || ' ' || coalesce(category, '') || ' ' || coalesce(description, ''))
    ) stored,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_job_salary_range check (salary_max_etb = 0 or salary_min_etb = 0 or salary_min_etb <= salary_max_etb)
);

-- ------------------------------------------------------------------------------
-- 8. GIGS
-- ------------------------------------------------------------------------------
create table public.gigs (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references public.profiles(id) on delete cascade,
    title text not null check (char_length(trim(title)) >= 3 and char_length(title) <= 200),
    client_name text not null check (char_length(trim(client_name)) >= 2),
    client_type text default 'Individual',
    location text not null default 'Addis Ababa',
    category text not null,
    budget_etb bigint not null check (budget_etb > 0),
    pricing_type public.gig_pricing_type not null default 'fixed',
    duration_text text not null,
    description text not null check (char_length(trim(description)) >= 10),
    deliverables text[] not null default '{}'::text[],
    required_skills text[] not null default '{}'::text[],
    is_urgent boolean not null default false,
    status public.gig_status not null default 'open',
    bids_count integer not null default 0 check (bids_count >= 0),
    fts tsvector generated always as (
        to_tsvector('english', coalesce(title, '') || ' ' || coalesce(client_name, '') || ' ' || coalesce(category, '') || ' ' || coalesce(description, ''))
    ) stored,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 9. GIG CONTACTS
-- ------------------------------------------------------------------------------
create table public.gig_contacts (
    id uuid primary key default gen_random_uuid(),
    gig_id uuid not null unique references public.gigs(id) on delete cascade,
    client_phone text check (client_phone is null or char_length(client_phone) <= 30),
    client_telegram text check (client_telegram is null or char_length(client_telegram) <= 50),
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 10. APPLICATIONS
-- ------------------------------------------------------------------------------
create table public.applications (
    id uuid primary key default gen_random_uuid(),
    job_id uuid not null references public.jobs(id) on delete cascade,
    applicant_id uuid not null references public.profiles(id) on delete cascade,
    cover_note text,
    expected_salary_etb bigint check (expected_salary_etb is null or expected_salary_etb >= 0),
    status public.application_status not null default 'applied',
    cv_snapshot jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (job_id, applicant_id)
);

-- ------------------------------------------------------------------------------
-- 11. BIDS
-- ------------------------------------------------------------------------------
create table public.bids (
    id uuid primary key default gen_random_uuid(),
    gig_id uuid not null references public.gigs(id) on delete cascade,
    bidder_id uuid not null references public.profiles(id) on delete cascade,
    amount_etb bigint not null check (amount_etb > 0),
    delivery_days integer not null check (delivery_days > 0),
    pitch text,
    status public.bid_status not null default 'submitted',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (gig_id, bidder_id)
);

-- ------------------------------------------------------------------------------
-- 12. RATINGS
-- ------------------------------------------------------------------------------
create table public.ratings (
    id uuid primary key default gen_random_uuid(),
    rater_id uuid not null references public.profiles(id) on delete cascade,
    ratee_id uuid not null references public.profiles(id) on delete cascade,
    gig_id uuid references public.gigs(id) on delete set null,
    stars integer not null check (stars >= 1 and stars <= 5),
    comment text check (comment is null or char_length(comment) <= 1000),
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 13. BOOKMARKS
-- ------------------------------------------------------------------------------
create table public.bookmarks (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    target_type text not null check (target_type in ('job', 'gig')),
    target_id uuid not null,
    created_at timestamptz not null default now(),
    unique (user_id, target_type, target_id)
);

-- ------------------------------------------------------------------------------
-- 14. NOTIFICATIONS
-- ------------------------------------------------------------------------------
create table public.notifications (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    title text not null check (char_length(trim(title)) >= 1),
    body text not null,
    type text not null,
    read boolean not null default false,
    data jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 15. NOTIFICATION SETTINGS
-- ------------------------------------------------------------------------------
create table public.notification_settings (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null unique references public.profiles(id) on delete cascade,
    notifications_enabled boolean not null default true,
    job_alerts_enabled boolean not null default true,
    bid_alerts_enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 16. REPORTS
-- ------------------------------------------------------------------------------
create table public.reports (
    id uuid primary key default gen_random_uuid(),
    reporter_id uuid not null references public.profiles(id) on delete cascade,
    target_type text not null check (target_type in ('job', 'gig', 'user', 'company', 'application', 'bid')),
    target_id uuid not null,
    reason text not null check (char_length(trim(reason)) >= 2),
    details text,
    status text not null default 'pending' check (status in ('pending', 'reviewed', 'resolved', 'dismissed')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 17. DEVICE TOKENS
-- ------------------------------------------------------------------------------
create table public.device_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    token text not null unique,
    device_type text not null default 'android',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- ------------------------------------------------------------------------------
-- 18. SAVED SEARCHES
-- ------------------------------------------------------------------------------
create table public.saved_searches (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    name text not null check (char_length(trim(name)) >= 1),
    target_type text not null check (target_type in ('job', 'gig')),
    query_params jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

-- ==============================================================================
-- INDEXES FOR FOREIGN KEYS, FILTERING AND FULL TEXT SEARCH
-- ==============================================================================
create index idx_profiles_account_type on public.profiles(account_type);

create index idx_seeker_cvs_seeker_id on public.seeker_cvs(seeker_id);
create index idx_education_cv_id on public.education(cv_id);
create index idx_experience_cv_id on public.experience(cv_id);
create index idx_portfolio_links_cv_id on public.portfolio_links(cv_id);

create index idx_companies_owner_id on public.companies(owner_id);

create index idx_jobs_company_id on public.jobs(company_id);
create index idx_jobs_posted_by on public.jobs(posted_by);
create index idx_jobs_status_created on public.jobs(status, created_at desc);
create index idx_jobs_fts on public.jobs using gin (fts);

create index idx_gigs_owner_id on public.gigs(owner_id);
create index idx_gigs_status_created on public.gigs(status, created_at desc);
create index idx_gigs_fts on public.gigs using gin (fts);

create index idx_gig_contacts_gig_id on public.gig_contacts(gig_id);

create index idx_applications_job_id on public.applications(job_id);
create index idx_applications_applicant_id on public.applications(applicant_id);
create index idx_applications_status_created on public.applications(status, created_at desc);

create index idx_bids_gig_id on public.bids(gig_id);
create index idx_bids_bidder_id on public.bids(bidder_id);
create index idx_bids_status_created on public.bids(status, created_at desc);

create index idx_ratings_rater_id on public.ratings(rater_id);
create index idx_ratings_ratee_id on public.ratings(ratee_id);
create index idx_ratings_gig_id on public.ratings(gig_id);

create index idx_bookmarks_user_id on public.bookmarks(user_id);
create index idx_notifications_user_id on public.notifications(user_id);
create index idx_notification_settings_user_id on public.notification_settings(user_id);

create index idx_reports_reporter_id on public.reports(reporter_id);
create index idx_reports_status_created on public.reports(status, created_at desc);

create index idx_device_tokens_user_id on public.device_tokens(user_id);
create index idx_saved_searches_user_id on public.saved_searches(user_id);

-- ==============================================================================
-- DATABASE FUNCTIONS & TRIGGERS
-- ==============================================================================

-- 1. Automatic updated_at Trigger Function
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    NEW.updated_at = now();
    return NEW;
end;
$$;

create trigger trg_profiles_updated_at before update on public.profiles for each row execute function public.set_updated_at();
create trigger trg_seeker_cvs_updated_at before update on public.seeker_cvs for each row execute function public.set_updated_at();
create trigger trg_companies_updated_at before update on public.companies for each row execute function public.set_updated_at();
create trigger trg_jobs_updated_at before update on public.jobs for each row execute function public.set_updated_at();
create trigger trg_gigs_updated_at before update on public.gigs for each row execute function public.set_updated_at();
create trigger trg_applications_updated_at before update on public.applications for each row execute function public.set_updated_at();
create trigger trg_bids_updated_at before update on public.bids for each row execute function public.set_updated_at();
create trigger trg_notification_settings_updated_at before update on public.notification_settings for each row execute function public.set_updated_at();
create trigger trg_reports_updated_at before update on public.reports for each row execute function public.set_updated_at();
create trigger trg_device_tokens_updated_at before update on public.device_tokens for each row execute function public.set_updated_at();

-- 2. Applicants Count Increment/Decrement Trigger Function
create or replace function public.update_job_applicants_count()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    if (TG_OP = 'INSERT') then
        update public.jobs
        set applicants_count = applicants_count + 1
        where id = NEW.job_id;
        return NEW;
    elsif (TG_OP = 'DELETE') then
        update public.jobs
        set applicants_count = greatest(0, applicants_count - 1)
        where id = OLD.job_id;
        return OLD;
    end if;
    return null;
end;
$$;

create trigger trg_on_application_inserted after insert on public.applications for each row execute function public.update_job_applicants_count();
create trigger trg_on_application_deleted after delete on public.applications for each row execute function public.update_job_applicants_count();

-- 3. Bids Count Increment/Decrement Trigger Function
create or replace function public.update_gig_bids_count()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    if (TG_OP = 'INSERT') then
        update public.gigs
        set bids_count = bids_count + 1
        where id = NEW.gig_id;
        return NEW;
    elsif (TG_OP = 'DELETE') then
        update public.gigs
        set bids_count = greatest(0, bids_count - 1)
        where id = OLD.gig_id;
        return OLD;
    end if;
    return null;
end;
$$;

create trigger trg_on_bid_inserted after insert on public.bids for each row execute function public.update_gig_bids_count();
create trigger trg_on_bid_deleted after delete on public.bids for each row execute function public.update_gig_bids_count();

-- 4. Automatically Create Profile on auth.users Sign Up
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
    meta_account_type text;
    valid_account_type public.account_type;
begin
    meta_account_type := NEW.raw_user_meta_data->>'account_type';

    if meta_account_type in ('job_seeker', 'company', 'gig_provider') then
        valid_account_type := meta_account_type::public.account_type;
    else
        valid_account_type := 'job_seeker'::public.account_type;
    end if;

    insert into public.profiles (
        id,
        account_type,
        full_name,
        phone,
        city,
        avatar_path
    ) values (
        NEW.id,
        valid_account_type,
        coalesce(nullif(trim(NEW.raw_user_meta_data->>'full_name'), ''), nullif(trim(NEW.raw_user_meta_data->>'name'), ''), split_part(NEW.email, '@', 1)),
        nullif(trim(NEW.raw_user_meta_data->>'phone'), ''),
        nullif(trim(NEW.raw_user_meta_data->>'city'), ''),
        nullif(trim(NEW.raw_user_meta_data->>'avatar_path'), '')
    );
    return NEW;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();
