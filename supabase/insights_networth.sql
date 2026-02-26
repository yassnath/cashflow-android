-- Insights tables
-- Run this after security_rls.sql

create table if not exists public.insight_feedback (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(id) on delete cascade,
  insight_id text not null,
  is_helpful boolean not null,
  reason text null,
  created_at text not null default to_char(
    (now() at time zone 'Asia/Jakarta'::text),
    'DD-MM-YYYY HH24:MI:SS'::text
  )
);

create index if not exists insight_feedback_user_id_idx
  on public.insight_feedback(user_id);

alter table public.insight_feedback enable row level security;

drop policy if exists insight_feedback_select_own on public.insight_feedback;
drop policy if exists insight_feedback_insert_own on public.insight_feedback;
drop policy if exists insight_feedback_update_own on public.insight_feedback;
drop policy if exists insight_feedback_delete_own on public.insight_feedback;

create policy insight_feedback_select_own
  on public.insight_feedback
  for select
  using (
    exists (
      select 1
      from public.users u
      where u.id = insight_feedback.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy insight_feedback_insert_own
  on public.insight_feedback
  for insert
  with check (
    exists (
      select 1
      from public.users u
      where u.id = insight_feedback.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy insight_feedback_update_own
  on public.insight_feedback
  for update
  using (
    exists (
      select 1
      from public.users u
      where u.id = insight_feedback.user_id
        and u.auth_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1
      from public.users u
      where u.id = insight_feedback.user_id
        and u.auth_id = auth.uid()
    )
  );

create policy insight_feedback_delete_own
  on public.insight_feedback
  for delete
  using (
    exists (
      select 1
      from public.users u
      where u.id = insight_feedback.user_id
        and u.auth_id = auth.uid()
    )
  );
