import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setUserType } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

export function ProfileTypePage() {
  const navigate = useNavigate();
  const { flow, setFlow } = useAppContext();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!flow.verificationToken) navigate('/auth/sign-in', { replace: true });
  }, [flow.verificationToken, navigate]);

  async function choose(userType: 'INDIVIDUAL' | 'BUSINESS') {
    if (!flow.verificationToken) return;
    setLoading(true);
    setError('');
    try {
      const res = await setUserType(flow.verificationToken, userType);
      setFlow({ mobileUserId: res.data.userId });
      navigate(`/auth/details?type=${userType}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not set user type');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="kk-auth">
      <div className="kk-auth__panel">
        <h1>Select your profile</h1>
        <p>Choose how you will trade on the KaratKart elite exchange.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        <div className="kk-choice">
          <button type="button" disabled={loading} onClick={() => choose('INDIVIDUAL')}>
            <strong>Individual User</strong>
            <span>Collectors and investors seeking certified stones for private vaults.</span>
          </button>
          <button type="button" disabled={loading} onClick={() => choose('BUSINESS')}>
            <strong>Business User</strong>
            <span>Retailers and wholesalers managing inventory and B2B trade.</span>
          </button>
        </div>
      </div>
    </div>
  );
}
