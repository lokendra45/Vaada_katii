import { OtpProvider, OtpSendResult, OtpVerifyResult } from "./OtpProvider.ts";

/**
 * Concrete implementation for Sandbox / Local Development (`123456`).
 * Zero network dependencies, instant responses, complete local testing guaranteed.
 */
export class MockSandboxProvider implements OtpProvider {
  readonly name = "MockSandbox";

  async sendOtp(phone: string, reference?: string): Promise<OtpSendResult> {
    return {
      success: true,
      otpId: `mock_sandbox_${phone}_${Date.now()}`,
      phone,
      expiresAt: new Date(Date.now() + 5 * 60000).toISOString(),
      attemptsRemaining: 3,
    };
  }

  async verifyOtp(otpId: string, code: string, phone?: string): Promise<OtpVerifyResult> {
    if (code === "123456") {
      return { success: true, verified: true, phone };
    }
    return {
      success: false,
      verified: false,
      phone,
      errorMessage: "Incorrect sandbox code. Please enter exactly 123456.",
    };
  }
}
