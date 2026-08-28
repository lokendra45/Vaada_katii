-- 1. Update existing 'properties' bucket constraints
update storage.buckets
set file_size_limit = 5242880, -- 5MB
    allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif']
where id = 'properties';

-- 2. Create 'avatars' bucket for tenant profile images
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'avatars',
  'avatars',
  true,
  5242880, -- 5MB
  array['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif']
) on conflict (id) do nothing;

-- 3. Create 'documents' bucket for tenant lease agreements
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'documents',
  'documents',
  false, -- Private bucket! Requires signed URLs to read
  10485760, -- 10MB
  array['application/pdf', 'image/jpeg', 'image/png']
) on conflict (id) do nothing;


-- 4. Update Policies for 'properties' bucket (Folder-based RLS)
drop policy if exists "Allow Authenticated Uploads" on storage.objects;
drop policy if exists "Allow Users to Update Their Own Files" on storage.objects;
drop policy if exists "Allow Users to Delete Their Own Files" on storage.objects;

create policy "properties_insert"
on storage.objects for insert
to authenticated
with check (
    bucket_id = 'properties' and
    (storage.foldername(name))[1] = auth.uid()::text
);

create policy "properties_update"
on storage.objects for update
to authenticated
using ( bucket_id = 'properties' and auth.uid() = owner )
with check ( bucket_id = 'properties' and auth.uid() = owner );

create policy "properties_delete"
on storage.objects for delete
to authenticated
using ( bucket_id = 'properties' and auth.uid() = owner );


-- 5. Policies for 'avatars' bucket
-- No SELECT policy needed for public bucket since they use publicUrl

create policy "avatars_insert"
on storage.objects for insert
to authenticated
with check (
    bucket_id = 'avatars' and
    (storage.foldername(name))[1] = auth.uid()::text
);

create policy "avatars_update"
on storage.objects for update
to authenticated
using ( bucket_id = 'avatars' and auth.uid() = owner )
with check ( bucket_id = 'avatars' and auth.uid() = owner );

create policy "avatars_delete"
on storage.objects for delete
to authenticated
using ( bucket_id = 'avatars' and auth.uid() = owner );


-- 6. Policies for 'documents' bucket (Private, so we need explicit SELECT policy for owner)
create policy "documents_select"
on storage.objects for select
to authenticated
using ( bucket_id = 'documents' and auth.uid() = owner );

create policy "documents_insert"
on storage.objects for insert
to authenticated
with check (
    bucket_id = 'documents' and
    (storage.foldername(name))[1] = auth.uid()::text
);

create policy "documents_update"
on storage.objects for update
to authenticated
using ( bucket_id = 'documents' and auth.uid() = owner )
with check ( bucket_id = 'documents' and auth.uid() = owner );

create policy "documents_delete"
on storage.objects for delete
to authenticated
using ( bucket_id = 'documents' and auth.uid() = owner );
