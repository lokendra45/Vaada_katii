# Rent Manager Nepal — Blazing Fast Bun + Hono Serverless Backend (`0.0.0.0:8787` Ready)

This folder contains high-performance **Bun + Hono / TypeScript** serverless endpoints (`send-otp` and `verify-otp`) for OTP dispatch, verification, and automated Supabase identity synchronization across Nepal.

---

## Why Bun + Hono on Port `8787`?
1. **No Port 3000 Collisions**: By defaulting to **port `8787`** (`process.env.PORT || 8787`), we avoid collisions with React, Next.js, Vite, or system background services.
2. **Local Network & Emulator Accessibility**: Binds to `hostname: "0.0.0.0"`, allowing physical phones (`192.168.x.x:8787`) and Android Emulators (`10.0.2.2:8787`) to connect cleanly!
3. **Ultra-Fast & Modular**: Powered by [Hono](https://hono.dev/) (`app.route("/send-otp", ...)`), providing clean 100% web-standard `Request`/`Response` execution running on [Bun](https://bun.sh/).
4. **Zero Client-Side Secret Exposure**: Your live `NEPAL_OTP_TOKEN` resides strictly on the server (`process.env.NEPAL_OTP_TOKEN`), keeping your credentials safe from APK decompilation.
5. **Automated Supabase Identity Sync**: On verification (`/verify-otp`), Hono uses `@supabase/supabase-js` Admin (`SUPABASE_SERVICE_ROLE_KEY`) to provision or verify the user inside `supabase.auth` (`auth.users`) with their exact active `role` (`LANDLORD`/`TENANT`).

---

## Running Across Your Local Network (`bun run dev`)

### 1. Install Dependencies
Navigate to `supabase/functions/` and install Hono + Supabase SDK:
```bash
bun install
```

### 2. Start the Server on Local Network (`0.0.0.0:8787`)
```bash
bun run dev
```
On startup, `server.ts` will automatically discover your Wi-Fi/LAN IPv4 address and display exact connection URLs:
```
=============================================================
🔥 Rent Manager Nepal OTP Server running on Bun + Hono (v1.x)
=============================================================
📡 Network Binding : 0.0.0.0:8787 (Accessible across Wi-Fi/LAN)
📱 Physical Phone  : http://192.168.x.x:8787/
🤖 Android Emulator: http://10.0.2.2:8787/
💻 Local Machine   : http://localhost:8787/
=============================================================
```

---

## Sandbox Mode (`123456`)
Both endpoints check if your `NEPAL_OTP_TOKEN` begins with `npot_test_` or `notp_sandbox_`. In Sandbox Mode:
* Any 10-digit Nepali mobile number (`9841234567`) is accepted without deducting balance.
* Verification succeeds instantly when entering code **`123456`**.
