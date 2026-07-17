import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createQrSession, getQrSession } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

export function QrLoginPage() {
  const navigate = useNavigate();
  const { setFlow } = useAppContext();
  const [payload, setPayload] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [status, setStatus] = useState('PENDING');
  const [error, setError] = useState('');

  useEffect(() => {
    void (async () => {
      try {
        const res = await createQrSession('karatkart-web');
        setPayload(res.data.qrPayload);
        setSessionId(res.data.qrSessionId);
        setStatus(res.data.status);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Could not create QR session');
      }
    })();
  }, []);

  useEffect(() => {
    if (!sessionId || status === 'CONFIRMED') return;
    const timer = window.setInterval(() => {
      void (async () => {
        try {
          const res = await getQrSession(sessionId);
          setStatus(res.data.status);
          if (res.data.status === 'CONFIRMED' && res.data.accessToken) {
            setFlow({ accessToken: res.data.accessToken });
            navigate('/marketplace');
          }
        } catch {
          // keep polling while session is alive
        }
      })();
    }, 2500);
    return () => window.clearInterval(timer);
  }, [navigate, sessionId, setFlow, status]);

  return (
    <div className="kk-auth">
      <div className="kk-auth__panel kk-qr">
        <h1>Scan to sign in</h1>
        <p>Use the KaratKart mobile application to scan the QR code and securely access your diamond trading account.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        <div className="kk-qr__frame">{payload || 'Creating session…'}</div>
        <p className="kk-hint">Status: {status}</p>
        {status === 'CONFIRMED' ? <p className="kk-dev-otp">QR confirmed. Vault access granted.</p> : null}
        <p className="kk-hint">
          Prefer phone OTP? <Link to="/auth/sign-in">Sign in with mobile</Link>
        </p>
      </div>
    </div>
  );
}
