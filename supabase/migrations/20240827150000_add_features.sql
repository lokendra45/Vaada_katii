-- 1. Update `property` table
ALTER TABLE public.property
ADD COLUMN IF NOT EXISTS units text[] DEFAULT '{}'::text[],
ADD COLUMN IF NOT EXISTS wifi_charge bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS water_charge bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS electricity_charge bigint DEFAULT 0,
ADD COLUMN IF NOT EXISTS waste_charge bigint DEFAULT 0;

-- 2. Update `tenant` table
ALTER TABLE public.tenant
ADD COLUMN IF NOT EXISTS profile_image_url text,
ADD COLUMN IF NOT EXISTS document_type text,
ADD COLUMN IF NOT EXISTS document_url text,
ADD COLUMN IF NOT EXISTS has_wifi boolean DEFAULT false,
ADD COLUMN IF NOT EXISTS has_water boolean DEFAULT false,
ADD COLUMN IF NOT EXISTS has_electricity boolean DEFAULT false,
ADD COLUMN IF NOT EXISTS has_waste boolean DEFAULT false,
ADD COLUMN IF NOT EXISTS lease_duration text,
ADD COLUMN IF NOT EXISTS move_in_date text,
ADD COLUMN IF NOT EXISTS payment_due_date text,
ADD COLUMN IF NOT EXISTS security_deposit bigint DEFAULT 0;
