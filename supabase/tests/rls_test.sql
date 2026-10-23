-- ==============================================================================
-- SIRAHUB RLS PGTAP TEST SUITE (supabase/tests/rls_test.sql)
-- ==============================================================================

begin;

-- Enable pgTAP extension first
create extension if not exists pgtap;

-- Declare test plan
select plan(18);

-- ------------------------------------------------------------------------------
-- SETUP: CREATE TEST USERS & RECORDS
-- ------------------------------------------------------------------------------
-- Test UUIDs:
-- Seeker 1:    11111111-1111-1111-1111-111111111111
-- Seeker 2:    22222222-2222-2222-2222-222222222222
-- Company 1:   33333333-3333-3333-3333-333333333333
-- Gig Client 1: 44444444-4444-4444-4444-444444444444

insert into auth.users (id, email, raw_user_meta_data)
values
    ('11111111-1111-1111-1111-111111111111', 'seeker1@sirahub.et', '{"account_type": "job_seeker", "full_name": "Abebe Seeker One"}'::jsonb),
    ('22222222-2222-2222-2222-222222222222', 'seeker2@sirahub.et', '{"account_type": "job_seeker", "full_name": "Kebede Seeker Two"}'::jsonb),
    ('33333333-3333-3333-3333-333333333333', 'company1@sirahub.et', '{"account_type": "company", "full_name": "Ethio Corp Owner"}'::jsonb),
    ('44444444-4444-4444-4444-444444444444', 'gigclient1@sirahub.et', '{"account_type": "gig_provider", "full_name": "Gig Client One"}'::jsonb)
on conflict (id) do nothing;

-- Setup Verified Company, Job, Gig, and Gig Contacts as superuser/postgres
set local role postgres;

insert into public.companies (id, owner_id, name, verification_status)
values ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'Ethio Fintech Corp', 'verified')
on conflict (id) do nothing;

insert into public.jobs (id, company_id, posted_by, title, company, location, category, description, status)
values ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'Android Developer', 'Ethio Fintech Corp', 'Addis Ababa', 'Tech', 'Build Android app with Compose', 'open')
on conflict (id) do nothing;

insert into public.gigs (id, owner_id, title, client_name, location, category, budget_etb, duration_text, description, status)
values ('cccccccc-cccc-cccc-cccc-cccccccccccc', '44444444-4444-4444-4444-444444444444', 'Logo Design', 'Gig Client One', 'Remote', 'Design', 5000, '2 Days', 'Design brand logo', 'open')
on conflict (id) do nothing;

insert into public.gig_contacts (gig_id, client_phone, client_telegram)
values ('cccccccc-cccc-cccc-cccc-cccccccccccc', '+251911001122', '@gigclient_eth')
on conflict (gig_id) do nothing;

-- ------------------------------------------------------------------------------
-- 1. ANONYMOUS USER (ANON ROLE) RESTRICTION TESTS
-- ------------------------------------------------------------------------------
set local role anon;
set local "request.jwt.claims" = '{"role": "anon"}';

select results_eq('select count(*)::int from public.applications', array[0], 'Anon user cannot read any application rows');
select results_eq('select count(*)::int from public.bids', array[0], 'Anon user cannot read any bid rows');
select results_eq('select count(*)::int from public.seeker_cvs', array[0], 'Anon user cannot read any seeker CV rows');
select results_eq('select count(*)::int from public.gig_contacts', array[0], 'Anon user cannot read any gig contact rows');
select results_eq('select count(*)::int from public.jobs where status = ''open''', array[1], 'Anon user can read open job postings');
select results_eq('select count(*)::int from public.gigs where status = ''open''', array[1], 'Anon user can read open gig postings');

-- ------------------------------------------------------------------------------
-- 2. SEEKER 1 ACTIONS (JOB SEEKER)
-- ------------------------------------------------------------------------------
set local role authenticated;
set local "request.jwt.claims" = '{"sub": "11111111-1111-1111-1111-111111111111", "role": "authenticated"}';

select results_eq('select count(*)::int from public.profiles where id = ''11111111-1111-1111-1111-111111111111''', array[1], 'Seeker 1 can read own profile');
select results_eq('select count(*)::int from public.profiles where id = ''22222222-2222-2222-2222-222222222222''', array[0], 'Seeker 1 cannot read Seeker 2 private profile');

-- Seeker 1 creates CV
insert into public.seeker_cvs (seeker_id, headline, bio)
values ('11111111-1111-1111-1111-111111111111', 'Senior Android Developer', '4 years Android exp');

-- Seeker 1 applies to open job
insert into public.applications (job_id, applicant_id, cover_note)
values ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'Excited to apply!');

-- Seeker 1 bids on open gig
insert into public.bids (gig_id, bidder_id, amount_etb, delivery_days, pitch)
values ('cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 4500, 2, 'Experienced designer');

select results_eq('select count(*)::int from public.applications where applicant_id = ''11111111-1111-1111-1111-111111111111''', array[1], 'Seeker 1 can read own submitted application');
select results_eq('select count(*)::int from public.gig_contacts', array[0], 'Seeker 1 cannot read gig contact details before bid is accepted');

-- ------------------------------------------------------------------------------
-- 3. SEEKER 2 ISOLATION TESTS (UNINVOLVED USER)
-- ------------------------------------------------------------------------------
set local role authenticated;
set local "request.jwt.claims" = '{"sub": "22222222-2222-2222-2222-222222222222", "role": "authenticated"}';

select results_eq('select count(*)::int from public.applications', array[0], 'Seeker 2 cannot read Seeker 1 applications');
select results_eq('select count(*)::int from public.bids', array[0], 'Seeker 2 cannot read Seeker 1 bids');
select results_eq('select count(*)::int from public.seeker_cvs', array[0], 'Seeker 2 cannot read Seeker 1 CV');

-- ------------------------------------------------------------------------------
-- 4. COMPANY 1 ACCESS & STATUS UPDATE
-- ------------------------------------------------------------------------------
set local role authenticated;
set local "request.jwt.claims" = '{"sub": "33333333-3333-3333-3333-333333333333", "role": "authenticated"}';

select results_eq('select count(*)::int from public.applications where job_id = ''bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb''', array[1], 'Company 1 can read applications to its job');
select results_eq('select count(*)::int from public.seeker_cvs where seeker_id = ''11111111-1111-1111-1111-111111111111''', array[1], 'Company 1 can read Seeker 1 CV via job application');

-- Company 1 updates application status
update public.applications set status = 'shortlisted' where job_id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
select results_eq('select status::text from public.applications where job_id = ''bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb''', array['shortlisted'], 'Company 1 can update application status to shortlisted');

-- ------------------------------------------------------------------------------
-- 5. GIG CLIENT 1 ACCEPTS BID & UNLOCKS CONTACT DETAILS
-- ------------------------------------------------------------------------------
set local role authenticated;
set local "request.jwt.claims" = '{"sub": "44444444-4444-4444-4444-444444444444", "role": "authenticated"}';

select results_eq('select count(*)::int from public.bids where gig_id = ''cccccccc-cccc-cccc-cccc-cccccccccccc''', array[1], 'Gig Client 1 can read bids on its gig');

-- Gig Client 1 accepts Seeker 1 bid
update public.bids set status = 'accepted' where gig_id = 'cccccccc-cccc-cccc-cccc-cccccccccccc';

-- Switch back to Seeker 1 to verify gig contact access after bid acceptance
set local role authenticated;
set local "request.jwt.claims" = '{"sub": "11111111-1111-1111-1111-111111111111", "role": "authenticated"}';

select results_eq('select count(*)::int from public.gig_contacts where gig_id = ''cccccccc-cccc-cccc-cccc-cccccccccccc''', array[1], 'Seeker 1 can read gig contact details after bid is accepted');

-- Finish pgTAP tests
select * from finish();
rollback;
