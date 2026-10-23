-- ==============================================================================
-- SIRAHUB ROW LEVEL SECURITY (MIGRATION 0002_rls.sql)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. HELPER FUNCTIONS
-- ------------------------------------------------------------------------------

-- Helper function to check if the current user is an admin
create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select coalesce((auth.jwt() -> 'app_metadata' ->> 'is_admin')::boolean, false);
$$;

-- Helper function to check if a company is verified
create or replace function public.is_company_verified(p_company_id uuid, p_posted_by uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.companies
        where (id = p_company_id or owner_id = p_posted_by)
          and verification_status = 'verified'
    );
$$;

-- ------------------------------------------------------------------------------
-- 2. PUBLIC VIEWS
-- ------------------------------------------------------------------------------

-- Public View for Companies and Gig Providers (no private phone or email exposed)
create or replace view public.public_profiles as
select
    id,
    account_type,
    full_name,
    city,
    avatar_path,
    created_at
from public.profiles
where account_type in ('company', 'gig_provider');

grant select on public.public_profiles to anon, authenticated;

-- ------------------------------------------------------------------------------
-- 3. ENABLE RLS ON ALL TABLES
-- ------------------------------------------------------------------------------
alter table public.profiles enable row level security;
alter table public.seeker_cvs enable row level security;
alter table public.education enable row level security;
alter table public.experience enable row level security;
alter table public.portfolio_links enable row level security;
alter table public.companies enable row level security;
alter table public.jobs enable row level security;
alter table public.gigs enable row level security;
alter table public.gig_contacts enable row level security;
alter table public.applications enable row level security;
alter table public.bids enable row level security;
alter table public.ratings enable row level security;
alter table public.bookmarks enable row level security;
alter table public.notifications enable row level security;
alter table public.notification_settings enable row level security;
alter table public.reports enable row level security;
alter table public.device_tokens enable row level security;
alter table public.saved_searches enable row level security;

-- ------------------------------------------------------------------------------
-- 4. POLICIES: PROFILES
-- ------------------------------------------------------------------------------
create policy "profiles_select_own"
    on public.profiles for select
    using (id = (select auth.uid()));

create policy "profiles_insert_own"
    on public.profiles for insert
    with check (id = (select auth.uid()));

create policy "profiles_update_own"
    on public.profiles for update
    using (id = (select auth.uid()))
    with check (id = (select auth.uid()));

-- Prevent users from changing account_type after creation
create or replace function public.prevent_profile_account_type_change()
returns trigger
language plpgsql
security definer
as $$
begin
    if (OLD.account_type is distinct from NEW.account_type) then
        raise exception 'account_type cannot be modified after creation.';
    end if;
    return NEW;
end;
$$;

create trigger trg_prevent_profile_account_type_change
    before update on public.profiles
    for each row execute function public.prevent_profile_account_type_change();

-- ------------------------------------------------------------------------------
-- 5. POLICIES: COMPANIES
-- ------------------------------------------------------------------------------
create policy "companies_select_public_or_owner"
    on public.companies for select
    using (verification_status = 'verified' or owner_id = (select auth.uid()));

create policy "companies_insert_owner"
    on public.companies for insert
    with check (owner_id = (select auth.uid()));

create policy "companies_update_owner"
    on public.companies for update
    using (owner_id = (select auth.uid()))
    with check (owner_id = (select auth.uid()));

create policy "companies_delete_owner"
    on public.companies for delete
    using (owner_id = (select auth.uid()));

-- Prevent non-admins from changing verification_status
create or replace function public.prevent_company_verification_status_change()
returns trigger
language plpgsql
security definer
as $$
begin
    if (OLD.verification_status is distinct from NEW.verification_status) then
        if not public.is_admin() then
            raise exception 'Only administrators can modify company verification_status.';
        end if;
    end if;
    return NEW;
end;
$$;

create trigger trg_prevent_company_verification_status_change
    before update on public.companies
    for each row execute function public.prevent_company_verification_status_change();

-- ------------------------------------------------------------------------------
-- 6. POLICIES: JOBS
-- ------------------------------------------------------------------------------
create policy "jobs_select_public_or_owner"
    on public.jobs for select
    using (status = 'open' or posted_by = (select auth.uid()));

create policy "jobs_insert_owner"
    on public.jobs for insert
    with check (posted_by = (select auth.uid()));

create policy "jobs_update_owner"
    on public.jobs for update
    using (posted_by = (select auth.uid()))
    with check (posted_by = (select auth.uid()));

create policy "jobs_delete_owner"
    on public.jobs for delete
    using (posted_by = (select auth.uid()));

-- Enforce job verification and applicants_count protection
create or replace function public.enforce_job_rules()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if (TG_OP = 'UPDATE' and OLD.applicants_count is distinct from NEW.applicants_count) then
        -- applicants_count is managed solely by application triggers
        NEW.applicants_count := OLD.applicants_count;
    end if;

    if (NEW.status = 'open') then
        if not public.is_company_verified(NEW.company_id, NEW.posted_by) then
            raise exception 'Jobs can only be set to open status by a verified company.';
        end if;
    end if;

    return NEW;
end;
$$;

create trigger trg_enforce_job_rules
    before insert or update on public.jobs
    for each row execute function public.enforce_job_rules();

-- ------------------------------------------------------------------------------
-- 7. POLICIES: GIGS & GIG CONTACTS
-- ------------------------------------------------------------------------------
create policy "gigs_select_public_or_owner"
    on public.gigs for select
    using (status = 'open' or owner_id = (select auth.uid()));

create policy "gigs_insert_owner"
    on public.gigs for insert
    with check (owner_id = (select auth.uid()));

create policy "gigs_update_owner"
    on public.gigs for update
    using (owner_id = (select auth.uid()))
    with check (owner_id = (select auth.uid()));

create policy "gigs_delete_owner"
    on public.gigs for delete
    using (owner_id = (select auth.uid()));

create or replace function public.prevent_gig_bids_count_manual_change()
returns trigger
language plpgsql
security definer
as $$
begin
    if (TG_OP = 'UPDATE' and OLD.bids_count is distinct from NEW.bids_count) then
        NEW.bids_count := OLD.bids_count;
    end if;
    return NEW;
end;
$$;

create trigger trg_prevent_gig_bids_count_manual_change
    before update on public.gigs
    for each row execute function public.prevent_gig_bids_count_manual_change();

-- GIG CONTACTS
create policy "gig_contacts_select"
    on public.gig_contacts for select
    using (
        exists (select 1 from public.gigs g where g.id = gig_contacts.gig_id and g.owner_id = (select auth.uid()))
        or
        exists (select 1 from public.bids b where b.gig_id = gig_contacts.gig_id and b.bidder_id = (select auth.uid()) and b.status = 'accepted')
    );

create policy "gig_contacts_insert_owner"
    on public.gig_contacts for insert
    with check (
        exists (select 1 from public.gigs g where g.id = gig_contacts.gig_id and g.owner_id = (select auth.uid()))
    );

create policy "gig_contacts_update_owner"
    on public.gig_contacts for update
    using (
        exists (select 1 from public.gigs g where g.id = gig_contacts.gig_id and g.owner_id = (select auth.uid()))
    );

create policy "gig_contacts_delete_owner"
    on public.gig_contacts for delete
    using (
        exists (select 1 from public.gigs g where g.id = gig_contacts.gig_id and g.owner_id = (select auth.uid()))
    );

-- ------------------------------------------------------------------------------
-- 8. POLICIES: APPLICATIONS
-- ------------------------------------------------------------------------------
create policy "applications_select"
    on public.applications for select
    using (
        applicant_id = (select auth.uid())
        or
        exists (select 1 from public.jobs j where j.id = applications.job_id and j.posted_by = (select auth.uid()))
    );

create policy "applications_insert_seeker"
    on public.applications for insert
    with check (
        applicant_id = (select auth.uid())
        and
        exists (select 1 from public.jobs j where j.id = applications.job_id and j.status = 'open')
    );

create policy "applications_update"
    on public.applications for update
    using (
        applicant_id = (select auth.uid())
        or
        exists (select 1 from public.jobs j where j.id = applications.job_id and j.posted_by = (select auth.uid()))
    );

create policy "applications_delete_seeker"
    on public.applications for delete
    using (applicant_id = (select auth.uid()));

create or replace function public.enforce_application_update_rules()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_posted_by uuid;
begin
    select posted_by into v_posted_by from public.jobs where id = OLD.job_id;

    if (auth.uid() = v_posted_by) then
        -- Company update: ONLY status column can be changed
        if (OLD.id is distinct from NEW.id or
            OLD.job_id is distinct from NEW.job_id or
            OLD.applicant_id is distinct from NEW.applicant_id or
            OLD.cover_note is distinct from NEW.cover_note or
            OLD.expected_salary_etb is distinct from NEW.expected_salary_etb or
            OLD.cv_snapshot is distinct from NEW.cv_snapshot) then
            raise exception 'Employers may only update application status.';
        end if;
    elsif (auth.uid() = OLD.applicant_id) then
        -- Applicant update: status cannot be modified by applicant
        if (OLD.status is distinct from NEW.status) then
            raise exception 'Applicants cannot modify application status.';
        end if;
    else
        raise exception 'Unauthorized application update.';
    end if;

    return NEW;
end;
$$;

create trigger trg_enforce_application_update_rules
    before update on public.applications
    for each row execute function public.enforce_application_update_rules();

-- ------------------------------------------------------------------------------
-- 9. POLICIES: BIDS
-- ------------------------------------------------------------------------------
create policy "bids_select"
    on public.bids for select
    using (
        bidder_id = (select auth.uid())
        or
        exists (select 1 from public.gigs g where g.id = bids.gig_id and g.owner_id = (select auth.uid()))
    );

create policy "bids_insert_bidder"
    on public.bids for insert
    with check (
        bidder_id = (select auth.uid())
        and
        exists (select 1 from public.gigs g where g.id = bids.gig_id and g.status = 'open')
    );

create policy "bids_update"
    on public.bids for update
    using (
        bidder_id = (select auth.uid())
        or
        exists (select 1 from public.gigs g where g.id = bids.gig_id and g.owner_id = (select auth.uid()))
    );

create policy "bids_delete_bidder"
    on public.bids for delete
    using (bidder_id = (select auth.uid()));

create or replace function public.enforce_bid_update_rules()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_owner_id uuid;
begin
    select owner_id into v_owner_id from public.gigs where id = OLD.gig_id;

    if (auth.uid() = v_owner_id) then
        -- Gig owner update: ONLY status column can be changed
        if (OLD.id is distinct from NEW.id or
            OLD.gig_id is distinct from NEW.gig_id or
            OLD.bidder_id is distinct from NEW.bidder_id or
            OLD.amount_etb is distinct from NEW.amount_etb or
            OLD.delivery_days is distinct from NEW.delivery_days or
            OLD.pitch is distinct from NEW.pitch) then
            raise exception 'Gig clients may only update bid status.';
        end if;
    elsif (auth.uid() = OLD.bidder_id) then
        -- Bidder update: status cannot be modified by bidder
        if (OLD.status is distinct from NEW.status) then
            raise exception 'Bidders cannot modify bid status.';
        end if;
    else
        raise exception 'Unauthorized bid update.';
    end if;

    return NEW;
end;
$$;

create trigger trg_enforce_bid_update_rules
    before update on public.bids
    for each row execute function public.enforce_bid_update_rules();

-- ------------------------------------------------------------------------------
-- 10. POLICIES: SEEKER CVS & RELATED TABLES
-- ------------------------------------------------------------------------------
-- SEEKER CVS
create policy "seeker_cvs_select"
    on public.seeker_cvs for select
    using (
        seeker_id = (select auth.uid())
        or
        exists (
            select 1 from public.applications a
            join public.jobs j on j.id = a.job_id
            where a.applicant_id = seeker_cvs.seeker_id
              and j.posted_by = (select auth.uid())
        )
    );

create policy "seeker_cvs_insert_owner"
    on public.seeker_cvs for insert
    with check (seeker_id = (select auth.uid()));

create policy "seeker_cvs_update_owner"
    on public.seeker_cvs for update
    using (seeker_id = (select auth.uid()))
    with check (seeker_id = (select auth.uid()));

create policy "seeker_cvs_delete_owner"
    on public.seeker_cvs for delete
    using (seeker_id = (select auth.uid()));

-- EDUCATION
create policy "education_select"
    on public.education for select
    using (
        exists (select 1 from public.seeker_cvs sc where sc.id = education.cv_id and sc.seeker_id = (select auth.uid()))
        or
        exists (
            select 1 from public.seeker_cvs sc
            join public.applications a on a.applicant_id = sc.seeker_id
            join public.jobs j on j.id = a.job_id
            where sc.id = education.cv_id
              and j.posted_by = (select auth.uid())
        )
    );

create policy "education_insert_owner"
    on public.education for insert
    with check (exists (select 1 from public.seeker_cvs sc where sc.id = education.cv_id and sc.seeker_id = (select auth.uid())));

create policy "education_update_owner"
    on public.education for update
    using (exists (select 1 from public.seeker_cvs sc where sc.id = education.cv_id and sc.seeker_id = (select auth.uid())));

create policy "education_delete_owner"
    on public.education for delete
    using (exists (select 1 from public.seeker_cvs sc where sc.id = education.cv_id and sc.seeker_id = (select auth.uid())));

-- EXPERIENCE
create policy "experience_select"
    on public.experience for select
    using (
        exists (select 1 from public.seeker_cvs sc where sc.id = experience.cv_id and sc.seeker_id = (select auth.uid()))
        or
        exists (
            select 1 from public.seeker_cvs sc
            join public.applications a on a.applicant_id = sc.seeker_id
            join public.jobs j on j.id = a.job_id
            where sc.id = experience.cv_id
              and j.posted_by = (select auth.uid())
        )
    );

create policy "experience_insert_owner"
    on public.experience for insert
    with check (exists (select 1 from public.seeker_cvs sc where sc.id = experience.cv_id and sc.seeker_id = (select auth.uid())));

create policy "experience_update_owner"
    on public.experience for update
    using (exists (select 1 from public.seeker_cvs sc where sc.id = experience.cv_id and sc.seeker_id = (select auth.uid())));

create policy "experience_delete_owner"
    on public.experience for delete
    using (exists (select 1 from public.seeker_cvs sc where sc.id = experience.cv_id and sc.seeker_id = (select auth.uid())));

-- PORTFOLIO LINKS
create policy "portfolio_links_select"
    on public.portfolio_links for select
    using (
        exists (select 1 from public.seeker_cvs sc where sc.id = portfolio_links.cv_id and sc.seeker_id = (select auth.uid()))
        or
        exists (
            select 1 from public.seeker_cvs sc
            join public.applications a on a.applicant_id = sc.seeker_id
            join public.jobs j on j.id = a.job_id
            where sc.id = portfolio_links.cv_id
              and j.posted_by = (select auth.uid())
        )
    );

create policy "portfolio_links_insert_owner"
    on public.portfolio_links for insert
    with check (exists (select 1 from public.seeker_cvs sc where sc.id = portfolio_links.cv_id and sc.seeker_id = (select auth.uid())));

create policy "portfolio_links_update_owner"
    on public.portfolio_links for update
    using (exists (select 1 from public.seeker_cvs sc where sc.id = portfolio_links.cv_id and sc.seeker_id = (select auth.uid())));

create policy "portfolio_links_delete_owner"
    on public.portfolio_links for delete
    using (exists (select 1 from public.seeker_cvs sc where sc.id = portfolio_links.cv_id and sc.seeker_id = (select auth.uid())));

-- ------------------------------------------------------------------------------
-- 11. POLICIES: BOOKMARKS, NOTIFICATIONS, SETTINGS, TOKENS, SEARCHES
-- ------------------------------------------------------------------------------
-- BOOKMARKS
create policy "bookmarks_owner_all"
    on public.bookmarks for all
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

-- NOTIFICATIONS
create policy "notifications_owner_all"
    on public.notifications for all
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

-- NOTIFICATION SETTINGS
create policy "notification_settings_owner_all"
    on public.notification_settings for all
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

-- DEVICE TOKENS
create policy "device_tokens_owner_all"
    on public.device_tokens for all
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

-- SAVED SEARCHES
create policy "saved_searches_owner_all"
    on public.saved_searches for all
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

-- ------------------------------------------------------------------------------
-- 12. POLICIES: REPORTS & RATINGS
-- ------------------------------------------------------------------------------
-- REPORTS
create policy "reports_insert_authenticated"
    on public.reports for insert
    with check (
        auth.role() = 'authenticated'
        and reporter_id = (select auth.uid())
    );

create policy "reports_select_admin"
    on public.reports for select
    using (public.is_admin());

-- RATINGS
create policy "ratings_select_public"
    on public.ratings for select
    using (true);

create policy "ratings_insert_authenticated"
    on public.ratings for insert
    with check (
        rater_id = (select auth.uid())
        and rater_id <> ratee_id
    );
