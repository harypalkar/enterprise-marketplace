import { FormEvent, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { resendOtp, verifyOtp } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

export function OtpPage() {
  const navigate = useNavigate();
  const { flow, setFlow } = useAppContext();
  const [digits, setDigits] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [devOtp, setDevOtp] = useState(flow.otpCode ?? '');
  const inputs = useRef<Array<HTMLInputElement | null>>([]);

  useEffect(() => {
    if (!flow.otpSessionId) {
      navigate('/auth/sign-in', { replace: true });
    }
  }, [flow.otpSessionId, navigate]);

  function setDigit(index: number, value: string) {
    const char = value.replace(/\D/g, '').slice(-1);
    const next = [...digits];
    next[index] = char;
    setDigits(next);
    if (char && index < 5) {
      inputs.current[index + 1]?.focus();
    }
  }

  async function onVerify(e: FormEvent) {
    e.preventDefault();
    const otp = digits.join('');
    if (otp.length !== 6 || !flow.otpSessionId) {
      setError('Enter the full 6-digit code');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await verifyOtp(flow.otpSessionId, otp);
      setFlow({
        verificationToken: res.data.verificationToken,
        mobileUserId: res.data.userId,
        mobileNumber: res.data.mobileNumber,
      });
      navigate(res.data.isNewUser ? '/auth/profile-type' : '/auth/enter-pin');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'OTP verification failed');
    } finally {
      setLoading(false);
    }
  }

  async function onResend() {
    if (!flow.otpSessionId) return;
    setLoading(true);
    setError('');
    try {
      const res = await resendOtp(flow.otpSessionId);
      setFlow({ otpSessionId: res.data.sessionId, otpCode: res.data.otp });
      setDevOtp(res.data.otp ?? '');
      setDigits(['', '', '', '', '', '']);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Resend failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="kk-auth">
      <form className="kk-auth__panel" onSubmit={onVerify}>
        <h1>Verify your identity</h1>
        <p>We sent a secure 6-digit code to +91 {flow.mobileNumber ?? '••••••••••'}.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        {devOtp ? (
          <p className="kk-dev-otp">
            Local mode: SMS is off. Use OTP <strong>{devOtp}</strong>
          </p>
        ) : null}
        <div className="kk-otp">
          {digits.map((d, i) => (
            <input
              key={i}
              ref={(el) => {
                inputs.current[i] = el;
              }}
              inputMode="numeric"
              maxLength={1}
              value={d}
              onChange={(e) => setDigit(i, e.target.value)}
              aria-label={`Digit ${i + 1}`}
            />
          ))}
        </div>
        <button className="kk-btn kk-btn--gold" type="submit" disabled={loading}>
          {loading ? 'Verifying…' : 'Verify mobile number'}
        </button>
        <p className="kk-hint">
          Didn’t get a code?{' '}
          <button type="button" className="kk-btn kk-btn--ghost" style={{ padding: '0.2rem 0.6rem' }} onClick={onResend}>
            Resend OTP
          </button>
        </p>
      </form>
    </div>
  );
}
