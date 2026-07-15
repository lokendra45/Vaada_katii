/**
 * Standardized interface for all OTP Providers (Strategy/Adapter Pattern).
 * Any SMS provider across Nepal (NepalOTP, Sparrow SMS, Aakash SMS, or Twilio)
 * must implement this contract.
 */
export interface OtpSendResult {
  success: boolean;
  otpId: string;
  phone: string;
  expiresAt?: string;
  attemptsRemaining?: number;
  errorMessage?: string;
}

export interface OtpVerifyResult {
  success: boolean;
  verified: boolean;
  phone?: string;
  errorMessage?: string;
}

export interface OtpProvider {
  readonly name: string;
  sendOtp(phone: string, reference?: string): Promise<OtpSendResult>;
  verifyOtp(otpId: string, code: string, phone?: string): Promise<OtpVerifyResult>;
}
