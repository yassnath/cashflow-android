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

-- Optional hardening for newly migrated accounts:
-- clear legacy password column once user is linked to auth_id.
update public.users
set password = null
where auth_id is not null;

alter table public.users enable row level security;
alter table public.money_entries enable row level security;
alter table public.dream_entries enable row level security;

drop policy if exists users_select_own on public.users;
drop policy if exists users_insert_self on public.users;
drop policy if exists users_update_own on public.users;
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
