-- Security baseline for production:
-- 1) Attach app profiles to Supabase Auth via auth_id
-- 2) Enable strict per-user RLS on profile + finance tables

alter table public.users
  add column if not exists auth_id uuid;

create unique index if not exists users_auth_id_uidx
  on public.users(auth_id)
  where auth_id is not null;

create or replace function public.lookup_user_by_username(p_username text)
returns table(
  id uuid,
  name text,
  email text,
  country text,
  bio text,
  birthdate text,
  created_at text,
  auth_id uuid,
  username text,
  password text
)
language sql
security definer
set search_path = public
as $$
  select
    u.id,
    u.name,
    u.email,
    u.country,
    u.bio,
    u.birthdate,
    u.created_at,
    u.auth_id,
    u.username,
    u.password
  from public.users u
  where lower(u.username) = lower(trim(p_username))
  limit 1;
$$;

revoke all on function public.lookup_user_by_username(text) from public;
grant execute on function public.lookup_user_by_username(text) to anon, authenticated;

create or replace function public.lookup_user_by_email(p_email text)
returns table(
  id uuid,
  name text,
  email text,
  country text,
  bio text,
  birthdate text,
  created_at text,
  auth_id uuid,
  username text,
  password text
)
language sql
security definer
set search_path = public
as $$
  select
    u.id,
    u.name,
    u.email,
    u.country,
    u.bio,
    u.birthdate,
    u.created_at,
    u.auth_id,
    u.username,
    u.password
  from public.users u
  where lower(u.email) = lower(trim(p_email))
  limit 1;
$$;

revoke all on function public.lookup_user_by_email(text) from public;
grant execute on function public.lookup_user_by_email(text) to anon, authenticated;

-- One-time backfill for profiles that already have a matching Supabase Auth
-- identity. Rows that still do not have an Auth identity can be linked on the
-- next successful legacy login by the users_link_legacy_auth policy below.
update public.users u
set auth_id = au.id
from auth.users au
where u.auth_id is null
  and nullif(trim(u.email), '') is not null
  and lower(trim(u.email)) = lower(trim(au.email));

-- Optional hardening after migration is verified:
-- update public.users
-- set password = null
-- where auth_id is not null;

alter table public.users enable row level security;
alter table public.money_entries enable row level security;
alter table public.dream_entries enable row level security;

drop policy if exists users_select_own on public.users;
drop policy if exists users_insert_self on public.users;
drop policy if exists users_update_own on public.users;
drop policy if exists users_link_legacy_auth on public.users;
drop policy if exists users_delete_own on public.users;

create policy users_select_own
  on public.users
  for select
  using (auth.uid() = auth_id);

create policy users_insert_self
  on public.users
  for insert
  with check (auth.uid() = auth_id);

create policy users_update_own
  on public.users
  for update
  using (auth.uid() = auth_id)
  with check (auth.uid() = auth_id);

-- Legacy rows created before Supabase Auth have auth_id = null. Allow the
-- authenticated owner to attach the row only when the Auth email matches.
create policy users_link_legacy_auth
  on public.users
  for update
  to authenticated
  using (
    auth_id is null
    and lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
  )
  with check (
    auth_id = auth.uid()
    and lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
  );

create policy users_delete_own
  on public.users
  for delete
  using (auth.uid() = auth_id);

drop policy if exists money_entries_select_own on public.money_entries;
drop policy if exists money_entries_insert_own on public.money_entries;
drop policy if exists money_entries_update_own on public.money_entries;
drop policy if exists money_entries_delete_own on public.money_entries;

create policy money_entries_select_own
  on public.money_entries
  for select
  using (
    exists (
      select 1
      from public.users u
      where u.id = money_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy money_entries_insert_own
  on public.money_entries
  for insert
  with check (
    exists (
      select 1
      from public.users u
      where u.id = money_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy money_entries_update_own
  on public.money_entries
  for update
  using (
    exists (
      select 1
      from public.users u
      where u.id = money_entries.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1
      from public.users u
      where u.id = money_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy money_entries_delete_own
  on public.money_entries
  for delete
  using (
    exists (
      select 1
      from public.users u
      where u.id = money_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

drop policy if exists dream_entries_select_own on public.dream_entries;
drop policy if exists dream_entries_insert_own on public.dream_entries;
drop policy if exists dream_entries_update_own on public.dream_entries;
drop policy if exists dream_entries_delete_own on public.dream_entries;

create policy dream_entries_select_own
  on public.dream_entries
  for select
  using (
    exists (
      select 1
      from public.users u
      where u.id = dream_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy dream_entries_insert_own
  on public.dream_entries
  for insert
  with check (
    exists (
      select 1
      from public.users u
      where u.id = dream_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy dream_entries_update_own
  on public.dream_entries
  for update
  using (
    exists (
      select 1
      from public.users u
      where u.id = dream_entries.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1
      from public.users u
      where u.id = dream_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy dream_entries_delete_own
  on public.dream_entries
  for delete
  using (
    exists (
      select 1
      from public.users u
      where u.id = dream_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

-- RPC helper functions to fetch money entries and dream entries safely
create or replace function public.fetch_user_money_entries(p_user_id text)
returns table(
  id uuid,
  user_id uuid,
  type text,
  amount numeric,
  date text,
  created_at text,
  category text,
  note text,
  source_method text,
  channel_bank text
)
language sql
security definer
set search_path = public
as $$
  select
    m.id,
    m.user_id,
    m.type,
    m.amount,
    m.date,
    m.created_at,
    m.category,
    m.note,
    m.source_method,
    m.channel_bank
  from public.money_entries m
  where m.user_id::text = p_user_id
     or exists (
          select 1 from public.users u
          where u.id::text = p_user_id
            and (m.user_id = u.id or m.user_id = u.auth_id)
        );
$$;

revoke all on function public.fetch_user_money_entries(text) from public;
grant execute on function public.fetch_user_money_entries(text) to anon, authenticated;

create or replace function public.fetch_user_dream_entries(p_user_id text)
returns table(
  id uuid,
  user_id uuid,
  title text,
  target numeric,
  current numeric,
  deadline text,
  note text,
  source_type text
)
language sql
security definer
set search_path = public
as $$
  select
    d.id,
    d.user_id,
    d.title,
    d.target,
    d.current,
    d.deadline,
    d.note,
    d.source_type
  from public.dream_entries d
  where d.user_id::text = p_user_id
     or exists (
          select 1 from public.users u
          where u.id::text = p_user_id
            and (d.user_id = u.id or d.user_id = u.auth_id)
        );
$$;

revoke all on function public.fetch_user_dream_entries(text) from public;
grant execute on function public.fetch_user_dream_entries(text) to anon, authenticated;
