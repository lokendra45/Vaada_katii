import { Hono } from "hono";
import { createClient } from "npm:@supabase/supabase-js";
import { getOtpProvider } from "../providers/ProviderFactory.ts";

const app = new Hono();

app.post("/", async (c) => {
  try {
    const { owner_id } = await c.req.json();
    if (!owner_id) {
      return c.json({ success: false, error: "Missing owner_id" }, 400);
    }

    // Initialize Supabase admin client to bypass RLS
    const supabaseUrl = process.env.SUPABASE_URL || "";
    const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || "";
    
    if (!supabaseUrl || !supabaseServiceKey) {
        return c.json({ success: false, error: "Missing Supabase admin config" }, 500);
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Get overdue tenants for this owner
    const { data: overdueTenants, error } = await supabase
      .from('tenant')
      .select('name, phone, rent_amount, room_number')
      .eq('owner_id', owner_id)
      .eq('status', 'Overdue');

    if (error) {
      return c.json({ success: false, error: error.message }, 500);
    }

    if (!overdueTenants || overdueTenants.length === 0) {
      return c.json({ success: true, message: "No overdue tenants found", sent_count: 0 });
    }

    const provider = getOtpProvider(); // Reusing the OTP provider for SMS dispatch
    let sentCount = 0;
    const failures = [];

    for (const tenant of overdueTenants) {
      if (tenant.phone) {
        const cleanPhone = tenant.phone.replace(/\D/g, "").slice(-10);
        if (cleanPhone.length === 10) {
          const message = `Namaste ${tenant.name}, your rent of NPR ${tenant.rent_amount} for room ${tenant.room_number || ''} is overdue. Please pay at your earliest convenience.`;
          
          // Using sendOtp here as a generic SMS sender since SparrowSMS supports it
          // In a real app we'd add a dedicated `sendSms(phone, message)` method to the Provider interface
          const res = await provider.sendOtp(cleanPhone, message);
          if (res.success) {
            sentCount++;
          } else {
            failures.push({ phone: cleanPhone, error: res.errorMessage });
          }
        }
      }
    }

    return c.json({
      success: true,
      message: `Reminders sent to ${sentCount} tenants`,
      sent_count: sentCount,
      failures
    });
  } catch (err: any) {
    return c.json({ success: false, error: err.message }, 500);
  }
});

export default app;
