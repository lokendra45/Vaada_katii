-- 1. Add receipt_number to payments
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS receipt_number text;

-- 2. Fix Storage RLS: Add missing SELECT policies for 'avatars' and 'properties' buckets.
-- Without these, upsert=true fails because the Storage API cannot SELECT the row to check if it exists.
CREATE POLICY "properties_select"
ON storage.objects FOR SELECT
TO authenticated
USING ( bucket_id = 'properties' );

CREATE POLICY "avatars_select"
ON storage.objects FOR SELECT
TO authenticated
USING ( bucket_id = 'avatars' );
