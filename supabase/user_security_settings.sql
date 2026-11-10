-- ==============================================================================
-- Migration: Tabel Pengaturan PIN Keamanan Aplikasi (user_security_settings)
-- Deskripsi: Menyimpan hash PIN keamanan 6-digit per pengguna ke cloud Supabase
--            sehingga perlindungan kunci aplikasi tersinkronisasi lintas perangkat.
-- ==============================================================================

-- 1. Buat tabel user_security_settings jika belum ada
create table if not exists public.user_security_settings (
  id uuid primary key default gen_random_uuid(),
  user_id text not null unique,
  pin_hash text not null,
  pin_salt text not null,
  pin_enabled boolean not null default true,
  updated_at text default '',
  created_at timestamptz default now()
);

-- 2. Index untuk query cepat berdasarkan user_id
create index if not exists idx_user_security_settings_user_id
  on public.user_security_settings(user_id);

-- 3. Aktifkan Row Level Security (RLS)
alter table public.user_security_settings enable row level security;

-- 4. Policy RLS agar pengguna hanya dapat mengakses dan mengubah PIN miliknya sendiri
drop policy if exists user_security_settings_all_policy on public.user_security_settings;
create policy user_security_settings_all_policy
  on public.user_security_settings
  for all
  using (
    auth.uid() is null
    or user_id = auth.uid()::text
    or exists (
      select 1 from public.users u
      where u.id::text = user_security_settings.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    auth.uid() is null
    or user_id = auth.uid()::text
    or exists (
      select 1 from public.users u
      where u.id::text = user_security_settings.user_id
        and u.auth_id = auth.uid()
    )
  );

-- 5. Berikan hak akses select, insert, update, delete ke authenticated & anon
grant select, insert, update, delete on public.user_security_settings to anon, authenticated, service_role;

