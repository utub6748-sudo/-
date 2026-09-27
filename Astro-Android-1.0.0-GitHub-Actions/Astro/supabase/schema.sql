-- Optional cloud schema for the production sync milestone.
-- The current Android build is local-first and does not write these tables yet.
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  nickname text not null default 'Astro User',
  avatar_url text,
  base_currency text not null default 'EUR',
  created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;
create policy "profiles_own" on public.profiles for all using (auth.uid() = id) with check (auth.uid() = id);
