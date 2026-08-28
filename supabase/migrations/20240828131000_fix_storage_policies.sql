-- Drop the overly broad policies created in the previous migration
drop policy if exists "Public Access" on storage.objects;
drop policy if exists "Allow Uploads" on storage.objects;
drop policy if exists "Allow Updates" on storage.objects;
drop policy if exists "Allow Deletes" on storage.objects;

-- 1. No SELECT policy is needed for a public bucket since files are accessed via the public URL.
--    This resolves the Supabase warning about clients being able to list all files.

-- 2. Allow authenticated users to upload files to the properties bucket
create policy "Allow Authenticated Uploads"
on storage.objects for insert
to authenticated
with check ( bucket_id = 'properties' );

-- 3. Allow users to update their own files
create policy "Allow Users to Update Their Own Files"
on storage.objects for update
to authenticated
using ( bucket_id = 'properties' and auth.uid() = owner )
with check ( bucket_id = 'properties' and auth.uid() = owner );

-- 4. Allow users to delete their own files
create policy "Allow Users to Delete Their Own Files"
on storage.objects for delete
to authenticated
using ( bucket_id = 'properties' and auth.uid() = owner );
