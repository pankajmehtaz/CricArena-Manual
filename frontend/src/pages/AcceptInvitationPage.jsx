import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AlertCircle, Building2, Eye, EyeOff, Loader2, Lock, User } from 'lucide-react';
import BrandLogo from '../components/common/BrandLogo';
import { useAuth } from '../context/AuthContext';
import { invitationApi } from '../services/apiClient';

export default function AcceptInvitationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const { setSessionFromAuthResponse } = useAuth();

  const [invitation, setInvitation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [fullName, setFullName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!token) {
      setError('Invalid or missing invitation token.');
      setLoading(false);
      return;
    }

    let active = true;
    invitationApi
      .validateToken(token)
      .then((data) => {
        if (active) {
          setInvitation(data);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err.message || 'Invitation token is invalid or expired.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!fullName.trim()) {
      setError('Please enter your full name.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const authData = await invitationApi.accept(token, fullName.trim(), password);
      setSessionFromAuthResponse(authData);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || 'Failed to accept invitation.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-md flex-col justify-center py-6 sm:py-10">
      <div className="rounded-2xl border border-border bg-surface p-6 sm:p-8 shadow-[var(--shadow-card)]">
        <div className="mb-6 flex flex-col items-start gap-3">
          <BrandLogo />
          <div>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">
              Accept Admin Invitation
            </h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Set up your credentials to manage your college cricket operations.
            </p>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center gap-2 py-10 text-sm text-ink-secondary font-medium">
            <Loader2 className="h-4 w-4 animate-spin text-brand" />
            <span>Validating your invitation...</span>
          </div>
        ) : error && !invitation ? (
          <div className="text-center py-4 space-y-4">
            <div className="mx-auto inline-flex h-12 w-12 items-center justify-center rounded-full bg-live-soft text-live">
              <AlertCircle className="h-6 w-6" />
            </div>
            <div>
              <h2 className="text-base font-bold text-ink">Invitation Expired or Invalid</h2>
              <p className="mt-1 text-xs text-live">{error}</p>
            </div>
            <Link
              to="/login"
              className="inline-flex justify-center rounded-xl bg-brand px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors"
            >
              Back to Sign In
            </Link>
          </div>
        ) : (
          <form className="space-y-4" onSubmit={handleSubmit}>
            {error && (
              <div className="flex items-start gap-2.5 rounded-xl border border-live-soft bg-live-soft/60 p-3 text-xs text-live">
                <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <div className="rounded-xl border border-brand/25 bg-surface-mint p-3.5">
              <div className="flex items-center gap-1.5 text-[11px] font-bold uppercase tracking-wider text-brand-hover">
                <Building2 className="h-3.5 w-3.5" />
                <span>Invited Institution</span>
              </div>
              <p className="mt-1 text-sm font-extrabold text-ink">
                {invitation?.collegeName || 'Assigned College'}
              </p>
              <p className="mt-0.5 text-xs text-ink-secondary">
                Account Email: <span className="font-semibold text-ink">{invitation?.email}</span>
              </p>
            </div>

            <div>
              <label
                htmlFor="fullName"
                className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Full Name
              </label>
              <div className="relative">
                <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
                <input
                  id="fullName"
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="e.g. Pankaj Mehta"
                  className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-3.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Set Password
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  minLength={8}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="At least 8 characters"
                  className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-10 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-muted hover:text-ink cursor-pointer"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <div>
              <label
                htmlFor="confirmPassword"
                className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Confirm Password
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
                <input
                  id="confirmPassword"
                  type={showPassword ? 'text' : 'password'}
                  required
                  minLength={8}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter your password"
                  className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-3.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="mt-2 w-full rounded-xl bg-brand py-2.5 text-sm font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-60 transition-colors cursor-pointer"
            >
              {submitting ? 'Activating Account...' : 'Accept & Activate Account'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
