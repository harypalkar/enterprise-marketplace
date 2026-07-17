import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { sendOtp, verifyPin } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

export function SignInPage() {
  const navigate = useNavigate();
  const { setFlow } = useAppContext();
  const [mobile, setMobile] = useState('7506426501');
  const [pin, setPin] = useState('');
  const [mode, setMode] = useState<'otp' | 'pin'>('otp');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (mode === 'otp') {
        const res = await sendOtp(mobile);
        setFlow({
          mobileNumber: res.data.mobileNumber,
          otpSessionId: res.data.sessionId,
          otpCode: res.data.otp,
        });
        navigate('/auth/otp');
      } else {
        const res = await verifyPin(mobile, pin);
        setFlow({
          mobileNumber: mobile,
          accessToken: res.data.accessToken,
          mobileUserId: res.data.userId,
        });
        navigate('/marketplace');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign-in failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="kk-auth">
      <form className="kk-auth__panel" onSubmit={onSubmit}>
        <h1>KaratKart secure login</h1>
        <p>Access your vault with OTP onboarding or your 6-digit PIN.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        <div className="kk-field">
          <label htmlFor="mobile">Mobile number</label>
          <input
            id="mobile"
            inputMode="numeric"
            pattern="[6-9][0-9]{9}"
            maxLength={10}
            value={mobile}
            onChange={(e) => setMobile(e.target.value.replace(/\D/g, '').slice(0, 10))}
            required
          />
        </div>
        {mode === 'pin' ? (
          <div className="kk-field">
            <label htmlFor="pin">PIN</label>
            <input
              id="pin"
              type="password"
              inputMode="numeric"
              maxLength={6}
              value={pin}
              onChange={(e) => setPin(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
            />
          </div>
        ) : null}
        <button className="kk-btn kk-btn--gold" type="submit" disabled={loading || mobile.length !== 10}>
          {loading ? 'Please wait…' : mode === 'otp' ? 'Continue with OTP' : 'Unlock vault'}
        </button>
        <p className="kk-hint">
          {mode === 'otp' ? (
            <>
              Returning user?{' '}
              <button type="button" className="kk-btn kk-btn--ghost" style={{ padding: '0.2rem 0.6rem' }} onClick={() => setMode('pin')}>
                Enter PIN
              </button>
            </>
          ) : (
            <button type="button" className="kk-btn kk-btn--ghost" style={{ padding: '0.2rem 0.6rem' }} onClick={() => setMode('otp')}>
              Use OTP instead
            </button>
          )}
        </p>
        <p className="kk-hint">
          Prefer desktop QR? <Link to="/auth/qr">Scan to sign in</Link>
        </p>
      </form>
    </div>
  );
}
