-- Tabel sinkronisasi hutang & piutang pengguna (Loan Tracking) ke database Supabase
create table if not exists public.loan_entries (
  id text primary key,
  user_id uuid not null references public.users(id) on delete cascade,
  type text not null default '',
  title text not null default '',
  principal bigint not null default 0,
  paid bigint not null default 0,
  annual_interest_rate double precision not null default 0.0,
  monthly_payment bigint not null default 0,
  due_date text not null default '',
  note text not null default '',
  linked_expense_id text,
  is_receivable boolean not null default false,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Index untuk performa query per pengguna
create index if not exists loan_entries_user_id_idx on public.loan_entries(user_id);
create index if not exists loan_entries_due_date_idx on public.loan_entries(due_date);

-- Row Level Security (RLS)
alter table public.loan_entries enable row level security;

drop policy if exists loan_entries_select_own on public.loan_entries;
drop policy if exists loan_entries_insert_own on public.loan_entries;
drop policy if exists loan_entries_update_own on public.loan_entries;
drop policy if exists loan_entries_delete_own on public.loan_entries;

create policy loan_entries_select_own
  on public.loan_entries
  for select
  using (
    exists (
      select 1
      from public.users u
      where u.id = loan_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy loan_entries_insert_own
  on public.loan_entries
  for insert
  with check (
    exists (
      select 1
      from public.users u
      where u.id = loan_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy loan_entries_update_own
  on public.loan_entries
  for update
  using (
    exists (
      select 1
      from public.users u
      where u.id = loan_entries.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1
      from public.users u
      where u.id = loan_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy loan_entries_delete_own
  on public.loan_entries
  for delete
  using (
    exists (
      select 1
      from public.users u
      where u.id = loan_entries.user_id
        and u.auth_id = auth.uid()
    )
  );

