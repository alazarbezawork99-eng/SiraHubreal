-- ==============================================================================
-- SIRAHUB DEV SEED: VERIFY A COMPANY (MIGRATION 0003_verify_company_for_dev.sql)
-- ==============================================================================
--
-- WHY THIS EXISTS
-- ---------------
-- `public.enforce_job_rules()` (0002_rls.sql) refuses to insert a job with
-- status = 'open' unless `public.is_company_verified()` returns true, which requires
-- `companies.verification_status = 'verified'`. New companies are created as
-- 'unverified', so a freshly registered Company account cannot publish anything.
--
-- That is correct production behaviour: verification is meant to be a manual back-office
-- step. This migration just unblocks local/staging testing by marking ONE company verified.
--
-- HOW TO USE
-- ----------
-- 1. Replace the email below with the company account you want to verify.
-- 2. Run this file in the Supabase SQL editor, or via `supabase db push`.
--
-- NOTE ON THE GUARD TRIGGER
-- -------------------------
-- `trg_prevent_company_verification_status_change` raises an exception when
-- verification_status changes and `public.is_admin()` is false. `is_admin()` reads
-- `auth.jwt() -> 'app_metadata' ->> 'is_admin'`, which is NULL in the SQL editor (there is
-- no JWT). We therefore disable the guard for the duration of this one UPDATE and
-- re-enable it immediately afterwards. The trigger stays in force for the app itself.
--
-- DO NOT run this against production. Verification there should be performed by an
-- administrator through the back office, not by a migration.
-- ==============================================================================

do $$
declare
    v_email   text := 'linux@gmail.com';  -- <<< change to your company account email
    v_user_id uuid;
    v_company uuid;
begin
    -- Resolve the auth user by email.
    select id into v_user_id
    from auth.users
    where lower(email) = lower(v_email);

    if v_user_id is null then
        raise exception 'No auth user found for email %. Register the account in the app first.', v_email;
    end if;

    -- Find the company row owned by that user.
    select id into v_company
    from public.companies
    where owner_id = v_user_id
    order by created_at asc
    limit 1;

    if v_company is null then
        raise exception 'User % has no row in public.companies. Open the Company tab in the app once so the profile is created.', v_email;
    end if;

    -- Bypass the "only administrators" guard for this single statement.
    alter table public.companies disable trigger trg_prevent_company_verification_status_change;

    update public.companies
    set verification_status = 'verified',
        updated_at = now()
    where id = v_company;

    alter table public.companies enable trigger trg_prevent_company_verification_status_change;

    raise notice 'Verified company % (%) for user %.', v_company, v_email, v_user_id;
end;
$$;

-- Sanity check: list every company and its verification state.
select
    c.id,
    c.name,
    c.verification_status,
    u.email as owner_email
from public.companies c
join auth.users u on u.id = c.owner_id
order by c.created_at desc;
