import { Hono } from "hono";
import { cors } from "hono/cors";
import { logger } from "hono/logger";
import { networkInterfaces } from "os";
import sendOtpHandler from "./send-otp/index.ts";
import verifyOtpHandler from "./verify-otp/index.ts";
import { getOtpProvider } from "./providers/ProviderFactory.ts";

const app = new Hono();

// Global Hono Middleware
app.use("*", logger());
app.use("*", cors({
  origin: "*",
  allowHeaders: ["authorization", "x-client-info", "apikey", "content-type"],
  allowMethods: ["POST", "GET", "OPTIONS"],
}));

// Mount modular sub-routers
app.route("/send-otp", sendOtpHandler);
app.route("/verify-otp", verifyOtpHandler);

const port = Number(process.env.PORT) || 8787;

app.get("/", (c) => {
  const activeProvider = getOtpProvider();
  return c.json({
    status: "online",
    engine: `Bun v${Bun.version}`,
    framework: "Hono v4",
    activeProvider: activeProvider.name,
    network: `0.0.0.0:${port} (Local Network Ready)`,
    endpoints: ["POST /send-otp", "POST /verify-otp"],
  });
});

// Auto-detect local Wi-Fi / Ethernet IPv4 address for physical phone testing
function getLocalIp(): string {
  try {
    const nets = networkInterfaces();
    for (const name of Object.keys(nets)) {
      for (const net of nets[name] || []) {
        if (net.family === "IPv4" && !net.internal) {
          return net.address;
        }
      }
    }
  } catch (e) {
    // Ignore network query errors
  }
  return "localhost";
}

const localIp = getLocalIp();

console.log(`\n=============================================================`);
console.log(`🔥 Rent Manager Nepal OTP Server running on Bun + Hono (v${Bun.version})`);
console.log(`=============================================================`);
console.log(`🔌 Active OTP Provider : ${getOtpProvider().name}`);
console.log(`📡 Network Binding     : 0.0.0.0:${port} (Accessible across Wi-Fi/LAN)`);
console.log(`📱 Physical Phone      : http://${localIp}:${port}/`);
console.log(`🤖 Android Emulator    : http://10.0.2.2:${port}/`);
console.log(`💻 Local Machine       : http://localhost:${port}/`);
console.log(`=============================================================\n`);

export default {
  port,
  hostname: "0.0.0.0",
  fetch: app.fetch,
};
