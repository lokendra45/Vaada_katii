import { Hono } from "hono";
import { getOtpProvider } from "../providers/ProviderFactory.ts";

const app = new Hono();

/**
 * Hono Router: `/send-otp`
 * Decoupled via Strategy/Adapter Pattern (`getOtpProvider()`).
 * Automatically delegates to `NepalOtpProvider`, `SparrowSmsProvider`, or `MockSandboxProvider` based on `.env`.
 */
app.post("/", async (c) => {
  try {
    const { phone, reference } = await c.req.json();

    if (!phone || typeof phone !== "string") {
      return c.json({ success: false, error: { message: "Phone number is required" } }, 400);
    }

    // Clean phone number to 10 digits
    const cleanPhone = phone.replace(/\D/g, "").slice(-10);
    if (cleanPhone.length !== 10) {
      return c.json({ success: false, error: { message: "Please enter a valid 10-digit Nepali mobile number" } }, 400);
    }

    const provider = getOtpProvider();
    const result = await provider.sendOtp(cleanPhone, reference);

    if (!result.success) {
      return c.json({
        success: false,
        provider: provider.name,
        error: { message: result.errorMessage || "Failed to dispatch verification code." },
      }, 400);
    }

    return c.json({
      success: true,
      provider: provider.name,
      data: {
        otp_id: result.otpId,
        phone: cleanPhone,
        expires_at: result.expiresAt || new Date(Date.now() + 5 * 60000).toISOString(),
        attempts_remaining: result.attemptsRemaining ?? 3,
      },
    }, 200);
  } catch (err: any) {
    return c.json({ success: false, error: { message: err.message || "Internal server error" } }, 500);
  }
});

export default app;
