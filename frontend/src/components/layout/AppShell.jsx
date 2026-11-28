import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  Activity,
  ChevronDown,
  Home,
  LogIn,
  LogOut,
  ShieldCheck,
  Trophy,
  User,
  Users,
} from 'lucide-react';
import BrandLogo from '../common/BrandLogo';
import CollegeSelector from '../common/CollegeSelector';
import { useAuth } from '../../context/AuthContext';

export default function AppShell() {
  const { isAuthenticated, user, roleAssignment, logout } = useAuth();
  const location = useLocation();
  const isLoginPage =
    location.pathname === '/login' || location.pathname === '/accept-invitation';

  const [profileOpen, setProfileOpen] = useState(false);
  const profileRef = useRef(null);

  // Close profile dropdown on outside click
  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setProfileOpen(false);
      }
    };
    if (profileOpen) {
      document.addEventListener('mousedown', handleOutsideClick);
    }
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, [profileOpen]);

  const navLinkClass = ({ isActive }) =>
    `inline-flex items-center gap-2 rounded-full px-3.5 py-2 text-sm font-semibold transition-colors ${
      isActive
        ? 'bg-brand-soft text-brand-hover'
        : 'text-ink-secondary hover:bg-surface-muted hover:text-ink'
    }`;

  const mobileLinkClass = ({ isActive }) =>
    `flex flex-col items-center justify-center gap-0.5 rounded-full px-3 py-1.5 text-[11px] font-semibold transition-colors ${
      isActive ? 'bg-brand-soft text-brand-hover' : 'text-ink-secondary hover:text-ink'
    }`;

  const role = roleAssignment?.role;

  return (
    <div className="min-h-screen bg-canvas text-ink flex flex-col">
      {/* Sticky Light Top Bar */}
      <header className="sticky top-0 z-30 border-b border-border bg-surface/95 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
          <div className="flex items-center gap-4 sm:gap-6">
            <Link to="/" className="focus:outline-none shrink-0">
              <BrandLogo />
            </Link>

            {/* Desktop Primary Navigation */}
            {!isLoginPage && (
              <nav className="hidden md:flex items-center gap-1" aria-label="Primary Navigation">
                <NavLink to="/" end className={navLinkClass}>
                  <Home className="h-4 w-4" />
                  Home
                </NavLink>
                <NavLink to="/matches" className={navLinkClass}>
                  <Activity className="h-4 w-4" />
                  Matches
                </NavLink>
                <NavLink to="/tournaments" className={navLinkClass}>
                  <Trophy className="h-4 w-4" />
                  Tournaments
                </NavLink>
                <NavLink to="/teams" className={navLinkClass}>
                  <Users className="h-4 w-4" />
                  Teams
                </NavLink>
              </nav>
            )}
          </div>

          {/* Right Actions */}
          <div className="flex items-center gap-2 sm:gap-3">
            {/* College Selector (only when signed in) */}
            {isAuthenticated && !isLoginPage && <CollegeSelector />}

            {/* Authenticated User Profile Dropdown */}
            {isAuthenticated ? (
              <div className="relative" ref={profileRef}>
                <button
                  type="button"
                  onClick={() => setProfileOpen((prev) => !prev)}
                  className="inline-flex shrink-0 items-center gap-2 rounded-full border border-border bg-surface px-3 py-1.5 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer shadow-xs"
                >
                  <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-soft text-brand-hover font-extrabold text-xs">
                    {(user?.fullName || user?.email || 'U')[0].toUpperCase()}
                  </div>
                  <span className="max-w-[140px] truncate text-ink font-bold whitespace-nowrap">
                    {user?.fullName || user?.email || 'User'}
                  </span>
                  {role ? (
                    <span className="hidden sm:inline-block whitespace-nowrap rounded-full bg-surface-mint px-2.5 py-0.5 text-[10px] font-extrabold text-brand-hover tracking-wider">
                      {role.replace('_', ' ')}
                    </span>
                  ) : (
                    <span className="hidden sm:inline-block whitespace-nowrap rounded-full bg-surface-muted px-2 py-0.5 text-[10px] font-bold text-ink-secondary">
                      Member
                    </span>
                  )}
                  <ChevronDown
                    className={`h-3.5 w-3.5 text-ink-muted transition-transform duration-150 ${
                      profileOpen ? 'rotate-180' : ''
                    }`}
                  />
                </button>

                {/* Profile Dropdown Card */}
                {profileOpen && (
                  <div className="absolute right-0 mt-2 w-64 rounded-2xl border border-border bg-surface p-2 shadow-xl z-50 animate-in fade-in-50 zoom-in-95 duration-150">
                    <div className="px-3 py-2.5 border-b border-border/60">
                      <p className="text-xs font-extrabold text-ink truncate">
                        {user?.fullName || 'User Account'}
                      </p>
                      <p className="text-[11px] text-ink-secondary truncate mt-0.5">
                        {user?.email}
                      </p>
                      <div className="mt-2 flex items-center gap-1.5">
                        <span className="inline-flex items-center gap-1 rounded-full bg-brand-soft px-2 py-0.5 text-[10px] font-bold text-brand-hover">
                          <ShieldCheck className="h-3 w-3" />
                          {role ? role.replace('_', ' ') : 'MEMBER'}
                        </span>
                        {roleAssignment?.collegeName && (
                          <span className="text-[10px] text-ink-muted truncate">
                            • {roleAssignment.collegeName}
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="pt-1.5">
                      <button
                        type="button"
                        onClick={() => {
                          setProfileOpen(false);
                          logout();
                        }}
                        className="w-full flex items-center gap-2 rounded-xl px-3 py-2 text-xs font-semibold text-live hover:bg-live-soft transition-colors cursor-pointer"
                      >
                        <LogOut className="h-4 w-4" />
                        <span>Log Out</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : !isLoginPage ? (
              <Link
                to="/login"
                className="inline-flex h-9 items-center gap-1.5 rounded-full border border-border bg-surface px-3.5 text-sm font-semibold text-ink hover:bg-surface-muted transition-colors"
              >
                <LogIn className="h-4 w-4 text-brand" />
                <span>Sign In</span>
              </Link>
            ) : null}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 mx-auto w-full max-w-6xl px-4 py-5 pb-24 sm:px-6 md:pb-8">
        <Outlet />
      </main>

      {/* Mobile Floating Light Bottom Navigation Bar */}
      {!isLoginPage && (
        <nav
          aria-label="Mobile Navigation"
          className="md:hidden fixed bottom-3 left-3 right-3 z-30 mx-auto max-w-md rounded-full border border-border bg-surface/95 px-2 py-1.5 shadow-[var(--shadow-nav)] backdrop-blur-md"
        >
          <div className="grid grid-cols-5 items-center gap-1">
            <NavLink to="/" end className={mobileLinkClass}>
              <Home className="h-4 w-4" />
              <span>Home</span>
            </NavLink>
            <NavLink to="/matches" className={mobileLinkClass}>
              <Activity className="h-4 w-4" />
              <span>Matches</span>
            </NavLink>
            <NavLink to="/tournaments" className={mobileLinkClass}>
              <Trophy className="h-4 w-4" />
              <span>Tournaments</span>
            </NavLink>
            <NavLink to="/teams" className={mobileLinkClass}>
              <Users className="h-4 w-4" />
              <span>Teams</span>
            </NavLink>
            <NavLink to="/login" className={mobileLinkClass}>
              <User className="h-4 w-4" />
              <span>{isAuthenticated ? 'Account' : 'Sign In'}</span>
            </NavLink>
          </div>
        </nav>
      )}
    </div>
  );
}
