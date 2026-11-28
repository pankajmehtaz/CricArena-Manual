import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  AlertCircle,
  Building2,
  CheckCircle2,
  Eye,
  EyeOff,
  Lock,
  Mail,
  ShieldCheck,
  User,
  UserPlus,
} from 'lucide-react';
import BrandLogo from '../components/common/BrandLogo';
import { useAuth } from '../context/AuthContext';
import { useCollege } from '../context/CollegeContext';
import { authApi } from '../services/apiClient';

export default function LoginPage() {
  const { isAuthenticated, user, roleAssignment, login, logout } = useAuth();
  const { colleges, loadingColleges, setSelectedCollegeId } = useCollege();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from || '/';

  const [mode, setMode] = useState('login'); // 'login' | 'signup'
  const [fullName, setFullName] = useState('');
  const [selectedCollegeChoice, setSelectedCollegeChoice] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFeedback('');

    if (!selectedCollegeChoice) {
      setError('Please select your college or institution before continuing.');
      return;
    }

    if (mode === 'signup') {
      if (selectedCollegeChoice === 'GLOBAL') {
        setError('Please select your college to create an account.');
        return;
      }
      if (!fullName.trim()) {
        setError('Please enter your full name.');
        return;
      }
      if (!email.trim() || !password) {
        setError('Please enter email and password.');
        return;
      }
      if (password.length < 8) {
        setError('Password must be at least 8 characters.');
        return;
      }
      if (password !== confirmPassword) {
        setError('Passwords do not match.');
        return;
      }

      setSubmitting(true);
      try {
        await authApi.register(fullName.trim(), email.trim(), password);
        setFeedback('Account created successfully! Signing you in...');
        // Automatically login and lock chosen college for the signed-in user
        const authRes = await login(email.trim(), password);
        const collegeToLock = authRes?.roleAssignment?.collegeId
          ? String(authRes.roleAssignment.collegeId)
          : selectedCollegeChoice;
        setSelectedCollegeId(collegeToLock, authRes?.user?.id);
        navigate(redirectTo, { replace: true });
      } catch (err) {
        setError(err.message || 'Signup failed. Please try again.');
      } finally {
        setSubmitting(false);
      }
    } else {
      if (!email.trim() || !password) {
        setError('Please enter both email and password.');
        return;
      }

      setSubmitting(true);
      try {
        const authRes = await login(email.trim(), password);
        const collegeToLock = authRes?.roleAssignment?.collegeId
          ? String(authRes.roleAssignment.collegeId)
          : selectedCollegeChoice === 'GLOBAL'
            ? ''
            : selectedCollegeChoice;
        setSelectedCollegeId(collegeToLock, authRes?.user?.id);
        navigate(redirectTo, { replace: true });
      } catch (err) {
        setError(err.message || 'Unable to sign in. Please verify your credentials.');
      } finally {
        setSubmitting(false);
      }
    }
  };

  if (isAuthenticated) {
    return (
      <div className="mx-auto max-w-md py-6">
        <div className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)]">
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-11 w-11 items-center justify-center rounded-full bg-pitch-soft text-pitch">
              <CheckCircle2 className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-ink">Signed in to CricArena</h1>
              <p className="text-xs text-ink-secondary">Active JWT session ready</p>
            </div>
          </div>

          <div className="space-y-3 rounded-xl border border-border bg-canvas p-4 text-sm">
            <div className="flex items-center justify-between gap-2">
              <span className="text-ink-secondary">Name</span>
              <span className="font-semibold text-ink">{user?.fullName || '—'}</span>
            </div>
            <div className="flex items-center justify-between gap-2">
              <span className="text-ink-secondary">Email</span>
              <span className="font-semibold text-ink">{user?.email || '—'}</span>
            </div>
            <div className="flex items-center justify-between gap-2">
              <span className="text-ink-secondary">Role</span>
              <span className="inline-flex items-center gap-1 rounded-full bg-brand-soft px-2.5 py-0.5 text-xs font-bold text-brand-hover">
                <ShieldCheck className="h-3.5 w-3.5" />
                {roleAssignment?.role || 'REGISTERED_USER'}
              </span>
            </div>
            {roleAssignment?.collegeId && (
              <div className="flex items-center justify-between gap-2">
                <span className="text-ink-secondary">College ID</span>
                <span className="font-semibold text-ink">#{roleAssignment.collegeId}</span>
              </div>
            )}
            {roleAssignment?.teamId && (
              <div className="flex items-center justify-between gap-2">
                <span className="text-ink-secondary">Team ID</span>
                <span className="font-semibold text-ink">#{roleAssignment.teamId}</span>
              </div>
            )}
          </div>

          <div className="mt-5 flex gap-3">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="flex-1 rounded-xl bg-brand px-4 py-2.5 text-sm font-semibold text-white hover:bg-brand-hover transition-colors cursor-pointer"
            >
              Go to Home
            </button>
            <button
              type="button"
              onClick={logout}
              className="rounded-xl border border-border bg-surface px-4 py-2.5 text-sm font-semibold text-live hover:bg-live-soft transition-colors cursor-pointer"
            >
              Sign Out
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto flex max-w-md flex-col justify-center py-6 sm:py-10">
      <div className="rounded-2xl border border-border bg-surface p-6 sm:p-8 shadow-[var(--shadow-card)]">
        <div className="mb-6 flex flex-col items-start gap-3">
          <BrandLogo />
          <div>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">
              {mode === 'signup' ? 'Create CricArena Account' : 'Sign in to CricArena'}
            </h1>
            <p className="mt-1 text-sm text-ink-secondary">
              {mode === 'signup'
                ? 'Join as a member to apply for captaincy and participate in tournaments.'
                : 'Manage tournaments, squads, and live ball-by-ball scoring.'}
            </p>
          </div>
        </div>

        {/* Tab switch */}
        <div className="mb-5 flex rounded-xl border border-border bg-canvas p-1">
          <button
            type="button"
            onClick={() => {
              setMode('login');
              setError('');
              setFeedback('');
            }}
            className={`flex-1 rounded-lg py-1.5 text-xs font-bold transition-all cursor-pointer ${
              mode === 'login'
                ? 'bg-surface text-ink shadow-xs'
                : 'text-ink-secondary hover:text-ink'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => {
              setMode('signup');
              if (selectedCollegeChoice === 'GLOBAL') setSelectedCollegeChoice('');
              setError('');
              setFeedback('');
            }}
            className={`flex-1 rounded-lg py-1.5 text-xs font-bold transition-all cursor-pointer ${
              mode === 'signup'
                ? 'bg-surface text-ink shadow-xs'
                : 'text-ink-secondary hover:text-ink'
            }`}
          >
            Create Account
          </button>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-5 flex items-start gap-2.5 rounded-xl border border-live-soft bg-live-soft/70 p-3.5 text-sm text-live"
          >
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {feedback && (
          <div className="mb-5 flex items-start gap-2.5 rounded-xl border border-pitch-soft bg-pitch-soft/70 p-3.5 text-sm text-pitch">
            <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{feedback}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === 'signup' && (
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
                  placeholder="Rahul Dravid"
                  className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-3.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
                />
              </div>
            </div>
          )}

          <div>
            <label
              htmlFor="loginCollege"
              className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
            >
              College / Institution
            </label>
            <div className="flex items-center gap-2.5 rounded-xl border border-border bg-canvas px-3.5 py-2.5 transition-colors focus-within:border-brand focus-within:bg-surface focus-within:ring-2 focus-within:ring-brand/15">
              <Building2 className="h-4 w-4 shrink-0 text-ink-muted" />
              <select
                id="loginCollege"
                required
                disabled={loadingColleges}
                value={selectedCollegeChoice}
                onChange={(e) => setSelectedCollegeChoice(e.target.value)}
                className={`w-full bg-transparent text-sm focus:outline-none cursor-pointer ${
                  selectedCollegeChoice ? 'text-ink' : 'text-ink-muted'
                }`}
              >
                <option value="">
                  {loadingColleges ? 'Loading colleges...' : 'Select your college...'}
                </option>
                {colleges.map((college) => (
                  <option key={college.id} value={String(college.id)}>
                    {college.name} ({college.code})
                  </option>
                ))}
                {mode === 'login' && (
                  <option value="GLOBAL">Platform Administration (All Colleges)</option>
                )}
              </select>
            </div>
            <p className="mt-1 text-[11px] text-ink-muted">
              Your campus scope stays locked to the selected college after login.
            </p>
          </div>

          <div>
            <label
              htmlFor="email"
              className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
            >
              Email Address
            </label>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
              <input
                id="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="user@example.com"
                className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-3.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="password"
              className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
            >
              Password
            </label>
            <div className="relative">
              <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder={mode === 'signup' ? 'At least 8 characters' : 'Enter your password'}
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

          {mode === 'signup' && (
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
                  autoComplete="new-password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter your password"
                  className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-3.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
                />
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 w-full rounded-xl bg-brand py-2.5 text-sm font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-60 transition-colors cursor-pointer"
          >
            {submitting
              ? mode === 'signup'
                ? 'Creating account...'
                : 'Signing in...'
              : mode === 'signup'
                ? 'Create Account'
                : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}
