-- ==============================================================================
-- Migration: Tabel Saldo Awal Akun / Rekening (account_initial_balances)
-- Deskripsi: Menyimpan saldo awal per akun/rekening/dompet ke cloud Supabase
--            agar data saldo awal tidak hilang saat ganti perangkat.
-- ==============================================================================

-- 1. Buat tabel account_initial_balances jika belum ada
create table if not exists public.account_initial_balances (
  id uuid primary key default gen_random_uuid(),
  user_id text not null,
  account_name text not null,
  balance bigint not null default 0,
  updated_at text default '',
  created_at timestamptz default now(),
  constraint account_initial_balances_user_acc_key unique (user_id, account_name)
);

-- 2. Index untuk query cepat berdasarkan user_id
create index if not exists idx_initial_balances_user_id
  on public.account_initial_balances(user_id);

-- 3. Aktifkan Row Level Security (RLS)
alter table public.account_initial_balances enable row level security;

-- 4. Policy RLS agar setiap pengguna hanya dapat mengelola saldo awalnya sendiri
drop policy if exists initial_balances_all_policy on public.account_initial_balances;
create policy initial_balances_all_policy
  on public.account_initial_balances
  for all
  using (
    auth.uid() is null
    or user_id = auth.uid()::text
    or exists (
      select 1 from public.users u
      where u.id::text = account_initial_balances.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    auth.uid() is null
    or user_id = auth.uid()::text
    or exists (
      select 1 from public.users u
      where u.id::text = account_initial_balances.user_id
        and u.auth_id = auth.uid()
    )
  );

-- 5. Berikan hak akses select, insert, update, delete ke authenticated & anon
grant select, insert, update, delete on public.account_initial_balances to anon, authenticated, service_role;
