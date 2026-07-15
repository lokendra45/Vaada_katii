import { Hono } from "hono";
import { getOtpProvider } from "../providers/ProviderFactory.ts";
import { syncSupabaseIdentity } from "../services/AuthSyncService.ts";

const app = new Hono();

/**
 * Hono Router: `/verify-otp`
 * Decoupled via Strategy/Adapter Pattern (`getOtpProvider()`).
 * Automatically delegates verification and then synchronizes identity via `syncSupabaseIdentity()`.
 */
app.post("/", async (c) => {
  try {
    const { otp_id, code, phone, role } = await c.req.json();

    if (!otp_id || !code) {
      return c.json({ success: false, error: { message: "otp_id and verification code are required" } }, 400);
    }

    const cleanPhone = phone ? phone.replace(/\D/g, "").slice(-10) : "";
    const provider = getOtpProvider();
    const result = await provider.verifyOtp(otp_id, code, cleanPhone);

    if (!result.success || !result.verified) {
      return c.json({
        success: false,
        provider: provider.name,
        error: { message: result.errorMessage || "Incorrect verification code. Please check and try again." },
      }, 400);
    }

    // Automatically synchronize identity into Supabase Admin (`auth.users`)
    const authResult = await syncSupabaseIdentity(cleanPhone, role);

    return c.json({
      success: true,
      provider: provider.name,
      data: {
        verified: true,
        phone: cleanPhone,
        user_id: authResult.userId || null,
      },
    }, 200);
  } catch (err: any) {
    return c.json({ success: false, error: { message: err.message || "Internal server error" } }, 500);
  }
});

export default app;
