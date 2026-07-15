import { OtpProvider } from "./OtpProvider.ts";
import { NepalOtpProvider } from "./NepalOtpProvider.ts";
import { SparrowSmsProvider } from "./SparrowSmsProvider.ts";
import { MockSandboxProvider } from "./MockSandboxProvider.ts";

/**
 * Strategy/Adapter Factory:
 * Selects active provider via `process.env.OTP_PROVIDER`.
 *
 * Available Options:
 * - `"nepalotp"` (Default): Uses NepalOTP (`https://api.nepalotp.com`)
 * - `"sparrow"` : Uses Sparrow SMS Nepal (`http://api.sparrowsms.com`)
 * - `"mock"`    : Uses Mock Sandbox Provider (`123456`)
 */
export function getOtpProvider(): OtpProvider {
  const providerKey = (process.env.OTP_PROVIDER || "mock").toLowerCase().trim();

  switch (providerKey) {
    case "sparrow":
    case "sparrowsms":
      return new SparrowSmsProvider();

    case "mock":
    case "sandbox":
    case "test":
      return new MockSandboxProvider();

    case "nepalotp":
    default:
      return new NepalOtpProvider();
  }
}
