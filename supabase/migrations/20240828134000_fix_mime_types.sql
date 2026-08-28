-- Update all buckets to allow application/octet-stream since KMP ByteArray uploads default to it
update storage.buckets
set allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif', 'application/octet-stream']
where id in ('properties', 'avatars');

update storage.buckets
set allowed_mime_types = array['application/pdf', 'image/jpeg', 'image/png', 'application/octet-stream']
where id = 'documents';
