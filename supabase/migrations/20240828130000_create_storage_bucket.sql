-- Create the properties bucket if it doesn't exist
insert into storage.buckets (id, name, public)
values ('properties', 'properties', true)
on conflict (id) do nothing;

-- Allow public read access to the properties bucket
create policy "Public Access"
on storage.objects for select
using ( bucket_id = 'properties' );

-- Allow insert access to the properties bucket for anon/authenticated users
create policy "Allow Uploads"
on storage.objects for insert
with check ( bucket_id = 'properties' );

-- Allow update access to the properties bucket
create policy "Allow Updates"
on storage.objects for update
using ( bucket_id = 'properties' );

-- Allow delete access to the properties bucket
create policy "Allow Deletes"
on storage.objects for delete
using ( bucket_id = 'properties' );
