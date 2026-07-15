import { createClient } from "@supabase/supabase-js";

export interface AuthSyncResult {
  success: boolean;
  userId?: string;
  errorMessage?: string;
}

/**
 * Service to synchronize verified mobile identities into Supabase Auth (`auth.users`)
 * via Admin Service Role (`SUPABASE_SERVICE_ROLE_KEY`).
 */
export async function syncSupabaseIdentity(phone: string, role?: string): Promise<AuthSyncResult> {
  const supabaseUrl = process.env.SUPABASE_URL || "";
  const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || "";

  if (!supabaseUrl || !supabaseServiceKey || !phone) {
    return { success: true }; // Proceed without server auth sync if Supabase env vars not set up locally yet
  }

  try {
    const supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey);
    const syntheticEmail = `${phone}@rentmanager.nepal`;
    const syntheticPassword = `npot_auth_secret_key_${phone}`;

    const { data: users } = await supabaseAdmin.auth.admin.listUsers();
    let existingUser = users?.users?.find((u) => u.email === syntheticEmail);

    if (!existingUser) {
      const { data: created, error: createError } = await supabaseAdmin.auth.admin.createUser({
        email: syntheticEmail,
        password: syntheticPassword,
        phone: `+977${phone}`,
        phone_confirm: true,
        email_confirm: true,
        user_metadata: {
          phone: `+977${phone}`,
          role: role || "LANDLORD",
          current_active_role: role || "LANDLORD",
        },
      });
      if (createError) {
        console.error("Admin user creation notice:", createError.message);
      }
      existingUser = created?.user;
    }

    return {
      success: true,
      userId: existingUser?.id,
    };
  } catch (err: any) {
    console.error("Supabase identity sync error:", err);
    return {
      success: false,
      errorMessage: err.message,
    };
  }
}
