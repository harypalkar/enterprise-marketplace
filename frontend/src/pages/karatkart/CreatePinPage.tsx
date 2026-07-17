import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPin } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

const KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', '⌫'];

export function CreatePinPage() {
  const navigate = useNavigate();
  const { flow, setFlow } = useAppContext();
  const [step, setStep] = useState<'create' | 'confirm'>('create');
  const [pin, setPin] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!flow.verificationToken) navigate('/auth/sign-in', { replace: true });
  }, [flow.verificationToken, navigate]);

  const active = step === 'create' ? pin : confirm;

  function press(key: string) {
    if (key === '') return;
    if (key === '⌫') {
      if (step === 'create') setPin((p) => p.slice(0, -1));
      else setConfirm((p) => p.slice(0, -1));
      return;
    }
    if (active.length >= 6) return;
    if (step === 'create') setPin((p) => p + key);
    else setConfirm((p) => p + key);
  }

  useEffect(() => {
    if (step === 'create' && pin.length === 6) {
      setStep('confirm');
    }
  }, [pin, step]);

  useEffect(() => {
    if (step !== 'confirm' || confirm.length !== 6 || !flow.verificationToken) return;
    if (confirm !== pin) {
      setError('PIN and confirm PIN must match');
      setConfirm('');
      return;
    }
    void (async () => {
      setLoading(true);
      setError('');
      try {
        const res = await createPin(flow.verificationToken!, pin);
        setFlow({ accessToken: res.data.accessToken, mobileUserId: res.data.userId });
        navigate('/marketplace');
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Could not create PIN');
        setPin('');
        setConfirm('');
        setStep('create');
      } finally {
        setLoading(false);
      }
    })();
  }, [confirm, flow.verificationToken, navigate, pin, setFlow, step]);

  return (
    <div className="kk-auth">
      <div className="kk-auth__panel" style={{ textAlign: 'center' }}>
        <h1>{step === 'create' ? 'Create secure PIN' : 'Confirm PIN'}</h1>
        <p>Set a 6-digit access code to authorize transactions and secure your assets.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        <div className="kk-pin-dots">
          {Array.from({ length: 6 }).map((_, i) => (
            <span key={i} className={i < active.length ? 'filled' : ''} />
          ))}
        </div>
        <div className="kk-keypad">
          {KEYS.map((key, idx) => (
            <button key={`${key}-${idx}`} type="button" disabled={loading || key === ''} onClick={() => press(key)}>
              {key}
            </button>
          ))}
        </div>
        <p className="kk-hint">Avoid sequential patterns like 123456.</p>
      </div>
    </div>
  );
}
