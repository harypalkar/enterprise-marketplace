import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifyPin } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

const KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', '⌫'];

export function EnterPinPage() {
  const navigate = useNavigate();
  const { flow, setFlow } = useAppContext();
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!flow.mobileNumber) navigate('/auth/sign-in', { replace: true });
  }, [flow.mobileNumber, navigate]);

  function press(key: string) {
    if (key === '') return;
    if (key === '⌫') {
      setPin((p) => p.slice(0, -1));
      return;
    }
    if (pin.length >= 6) return;
    setPin((p) => p + key);
  }

  useEffect(() => {
    if (pin.length !== 6 || !flow.mobileNumber) return;
    void (async () => {
      setLoading(true);
      setError('');
      try {
        const res = await verifyPin(flow.mobileNumber!, pin);
        setFlow({ accessToken: res.data.accessToken, mobileUserId: res.data.userId });
        navigate('/marketplace');
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Invalid PIN');
        setPin('');
      } finally {
        setLoading(false);
      }
    })();
  }, [flow.mobileNumber, navigate, pin, setFlow]);

  return (
    <div className="kk-auth">
      <div className="kk-auth__panel" style={{ textAlign: 'center' }}>
        <h1>Enter your PIN</h1>
        <p>Unlock the KaratKart vault for +91 {flow.mobileNumber}.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        <div className="kk-pin-dots">
          {Array.from({ length: 6 }).map((_, i) => (
            <span key={i} className={i < pin.length ? 'filled' : ''} />
          ))}
        </div>
        <div className="kk-keypad">
          {KEYS.map((key, idx) => (
            <button key={`${key}-${idx}`} type="button" disabled={loading || key === ''} onClick={() => press(key)}>
              {key}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
