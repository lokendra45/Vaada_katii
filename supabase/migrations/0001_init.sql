-- ============================================================================
-- 0001_init.sql
-- Initial schema for RentManagerApp (GharBhada) — Supabase-only architecture.
--
-- Tables mirror the domain DTOs (tenant, property, payments). RLS is enabled on
-- all three tables; ownership is enforced via auth.uid() so that every row's
-- owner_id equals the authenticated (or anonymous) user id.
-- ============================================================================

-- Required for the GIN trigram indexes used to accelerate ILIKE '%term%' search.
create extension if not exists pg_trgm;

-- ----------------------------------------------------------------------------
-- 1. property
-- ----------------------------------------------------------------------------
create table if not exists public.property (
    id            text primary key,
    owner_id      text not null,
    name          text not null,
    address       text not null,
    image_url     text,
    property_type text not null default 'HOUSE',
    total_units   integer not null default 1,
    monthly_rent  bigint not null default 0,
    description   text not null default '',
    billing_cycle text not null default '1st of the month',
    amenities     jsonb not null default '[]'::jsonb,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

create index if not exists property_owner_name_idx
    on public.property (owner_id, name);

-- Supports the common `where owner_id = ? order by created_at desc` list query.
create index if not exists property_owner_created_idx
    on public.property (owner_id, created_at desc);

-- Enables index-backed ILIKE '%term%' search on name and address (pg_trgm).
create index if not exists property_name_trgm_idx
    on public.property using gin (name gin_trgm_ops);

create index if not exists property_address_trgm_idx
    on public.property using gin (address gin_trgm_ops);

alter table public.property enable row level security;

create policy property_select on public.property
    for select using (owner_id = auth.uid()::text);

create policy property_insert on public.property
    for insert with check (owner_id = auth.uid()::text);

create policy property_update on public.property
    for update using (owner_id = auth.uid()::text)
    with check (owner_id = auth.uid()::text);

create policy property_delete on public.property
    for delete using (owner_id = auth.uid()::text);

-- ----------------------------------------------------------------------------
-- 2. tenant
-- ----------------------------------------------------------------------------
create table if not exists public.tenant (
    id          text primary key,
    owner_id    text not null,
    name        text not null,
    email       text,
    phone       text,
    property_id text references public.property (id) on delete set null,
    room_number text,
    rent_amount bigint not null default 0,
    status      text not null default 'Active',
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now()
);

create index if not exists tenant_owner_name_idx
    on public.tenant (owner_id, name);

create index if not exists tenant_owner_status_idx
    on public.tenant (owner_id, status);

create index if not exists tenant_property_idx
    on public.tenant (property_id, owner_id, status);

-- Supports the common `where owner_id = ? order by created_at desc` list query.
create index if not exists tenant_owner_created_idx
    on public.tenant (owner_id, created_at desc);

-- Supports the `getTenantsByProperty` server-side filter (owner_id + property_id).
create index if not exists tenant_owner_property_idx
    on public.tenant (owner_id, property_id, created_at desc);

-- Enables index-backed ILIKE '%term%' search on tenant name (pg_trgm).
create index if not exists tenant_name_trgm_idx
    on public.tenant using gin (name gin_trgm_ops);

alter table public.tenant enable row level security;

create policy tenant_select on public.tenant
    for select using (owner_id = auth.uid()::text);

create policy tenant_insert on public.tenant
    for insert with check (owner_id = auth.uid()::text);

create policy tenant_update on public.tenant
    for update using (owner_id = auth.uid()::text)
    with check (owner_id = auth.uid()::text);

create policy tenant_delete on public.tenant
    for delete using (owner_id = auth.uid()::text);

-- ----------------------------------------------------------------------------
-- 3. payments
-- ----------------------------------------------------------------------------
create table if not exists public.payments (
    id              text primary key,
    owner_id        text not null,
    tenant_id       text references public.tenant (id) on delete set null,
    property_id     text references public.property (id) on delete set null,
    amount          bigint not null,
    date            date not null,
    status          text not null default 'Paid',
    payment_method  text,
    notes           text,
    idempotency_key text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create unique index if not exists payments_idempotency_key_uidx
    on public.payments (idempotency_key)
    where idempotency_key is not null;

create index if not exists payments_owner_date_idx
    on public.payments (owner_id, date);

create index if not exists payments_owner_status_date_idx
    on public.payments (owner_id, status, date);

create index if not exists payments_tenant_date_idx
    on public.payments (tenant_id, date);

create index if not exists payments_property_date_idx
    on public.payments (owner_id, property_id, date);

-- Payments search uses `tenant(name)` join with ILIKE — the tenant name trigram
-- index above already accelerates that, but this covers owner-scoped payment
-- lists that join tenant for display names.
create index if not exists payments_owner_tenant_idx
    on public.payments (owner_id, tenant_id, date);

alter table public.payments enable row level security;

create policy payments_select on public.payments
    for select using (owner_id = auth.uid()::text);

create policy payments_insert on public.payments
    for insert with check (owner_id = auth.uid()::text);

create policy payments_update on public.payments
    for update using (owner_id = auth.uid()::text)
    with check (owner_id = auth.uid()::text);

create policy payments_delete on public.payments
    for delete using (owner_id = auth.uid()::text);

-- ----------------------------------------------------------------------------
-- 4. Grants — required so the PostgREST anon/authenticated roles can actually
--    reach the tables. RLS still enforces per-row ownership; grants only control
--    table-level access.
-- ----------------------------------------------------------------------------
grant select, insert, update, delete on public.property to anon, authenticated;
grant select, insert, update, delete on public.tenant to anon, authenticated;
grant select, insert, update, delete on public.payments to anon, authenticated;

-- ----------------------------------------------------------------------------
-- 6. get_dashboard_summary
--    Single server-side aggregate used by the Home dashboard. Computes all the
--    summary numbers + the 5 most recent payments in one round-trip, avoiding
--    client-side full-table scans. Runs as the calling role (security invoker)
--    so RLS still scopes every query to the caller's owner_id.
-- ----------------------------------------------------------------------------
create or replace function public.get_dashboard_summary(p_owner_id text)
returns jsonb
language sql
stable
security invoker
as $$
    select jsonb_build_object(
        'total_rent', coalesce(
            (select sum(rent_amount) from public.tenant
             where owner_id = p_owner_id and status = 'Active'), 0),
        'collected_rent', coalesce(
            (select sum(amount) from public.payments
             where owner_id = p_owner_id and status = 'Paid'), 0),
        'outstanding_rent', coalesce(
            (select sum(amount) from public.payments
             where owner_id = p_owner_id and status = 'Unpaid'), 0),
        'properties_count', (select count(*) from public.property
             where owner_id = p_owner_id),
        'tenants_count', (select count(*) from public.tenant
             where owner_id = p_owner_id and status = 'Active'),
        'overdue_tenants_count', (select count(*) from public.tenant
             where owner_id = p_owner_id and status = 'Overdue'),
        'recent_payments', coalesce((
            select jsonb_agg(x order by x->>'date' desc, x->>'tenant_name' desc)
            from (
                select jsonb_build_object(
                    'tenant_id', p.tenant_id,
                    'tenant_name', t.name,
                    'unit_number', t.room_number,
                    'date', p.date,
                    'amount', p.amount,
                    'is_paid', p.status = 'Paid'
                ) as x
                from public.payments p
                left join public.tenant t on t.id = p.tenant_id
                where p.owner_id = p_owner_id
                order by p.date desc, p.id desc
                limit 5
            ) s
        ), '[]'::jsonb)
    );
$$;

grant execute on function public.get_dashboard_summary(text) to anon, authenticated;

-- ----------------------------------------------------------------------------
-- 7. Anon JWT claim support
--    Supabase's bundled auth.uid() helper returns the `sub` claim. For anonymous
--    sign-ins that claim is the anonymous user id, so no extra claims are needed.
-- ----------------------------------------------------------------------------