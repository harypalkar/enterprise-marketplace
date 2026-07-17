import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { setUserDetails } from '../../api/services';
import { useAppContext } from '../../context/AppContext';

export function DetailsPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const userType = (params.get('type') === 'BUSINESS' ? 'BUSINESS' : 'INDIVIDUAL') as 'INDIVIDUAL' | 'BUSINESS';
  const { flow } = useAppContext();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    companyName: '',
    website: '',
    gstNumber: '',
    city: 'Mumbai',
    country: 'India',
  });

  const title = useMemo(
    () => (userType === 'BUSINESS' ? 'Enter business details' : 'Enter personal details'),
    [userType],
  );

  useEffect(() => {
    if (!flow.verificationToken) navigate('/auth/sign-in', { replace: true });
  }, [flow.verificationToken, navigate]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!flow.verificationToken) return;
    setLoading(true);
    setError('');
    try {
      await setUserDetails(flow.verificationToken, userType, form);
      navigate('/auth/create-pin');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save details');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="kk-auth">
      <form className="kk-auth__panel" onSubmit={onSubmit}>
        <h1>{title}</h1>
        <p>Provide verifiable details to access the KaratKart vault.</p>
        {error ? <p className="kk-error">{error}</p> : null}
        {userType === 'INDIVIDUAL' ? (
          <div className="kk-field">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              value={form.fullName}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
              required
            />
          </div>
        ) : (
          <>
            <div className="kk-field">
              <label htmlFor="companyName">Company name</label>
              <input
                id="companyName"
                value={form.companyName}
                onChange={(e) => setForm((f) => ({ ...f, companyName: e.target.value }))}
                required
              />
            </div>
            <div className="kk-field">
              <label htmlFor="gstNumber">GST number</label>
              <input
                id="gstNumber"
                value={form.gstNumber}
                onChange={(e) => setForm((f) => ({ ...f, gstNumber: e.target.value }))}
                required
              />
            </div>
            <div className="kk-field">
              <label htmlFor="website">Website</label>
              <input
                id="website"
                value={form.website}
                onChange={(e) => setForm((f) => ({ ...f, website: e.target.value }))}
              />
            </div>
            <div className="kk-field">
              <label htmlFor="city">City, Country</label>
              <input
                id="city"
                value={`${form.city}, ${form.country}`}
                onChange={(e) => {
                  const [city = '', country = 'India'] = e.target.value.split(',').map((s) => s.trim());
                  setForm((f) => ({ ...f, city, country: country || 'India' }));
                }}
              />
            </div>
          </>
        )}
        <div className="kk-field">
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            value={form.email}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            required
          />
        </div>
        <div className="kk-field">
          <label>Mobile</label>
          <input value={flow.mobileNumber ?? ''} readOnly />
        </div>
        <button className="kk-btn kk-btn--gold" type="submit" disabled={loading}>
          {loading ? 'Saving…' : 'Continue'}
        </button>
      </form>
    </div>
  );
}
