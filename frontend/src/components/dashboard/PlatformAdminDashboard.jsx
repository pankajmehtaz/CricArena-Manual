import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  ArrowRight,
  Building2,
  CheckCircle2,
  Copy,
  Mail,
  Plus,
  RefreshCw,
  RotateCcw,
  ShieldCheck,
  Trash2,
  Trophy,
} from 'lucide-react';
import CollegeAdminInviteModal from '../admin/CollegeAdminInviteModal';
import CreateCollegeModal from '../admin/CreateCollegeModal';
import { useAuth } from '../../context/AuthContext';
import { useCollege } from '../../context/CollegeContext';
import { collegeApi, platformApi, tournamentApi } from '../../services/apiClient';

export default function PlatformAdminDashboard() {
  const { user } = useAuth();
  const { refreshColleges } = useCollege();

  const [colleges, setColleges] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [inviteModalOpen, setInviteModalOpen] = useState(false);
  const [createCollegeOpen, setCreateCollegeOpen] = useState(false);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [copiedId, setCopiedId] = useState(null);
  const [invitationLinks, setInvitationLinks] = useState({});

  const loadPlatformData = async () => {
    setLoading(true);
    setError('');
    try {
      const [colList, invList, tourneyList] = await Promise.all([
        collegeApi.getAll().catch(() => []),
        platformApi.getAllInvitations().catch(() => []),
        tournamentApi.getAll().catch(() => []),
      ]);

      setColleges(Array.isArray(colList) ? colList : []);
      setInvitations(Array.isArray(invList) ? invList : []);
      setTournaments(Array.isArray(tourneyList) ? tourneyList : []);
    } catch (err) {
      setError(err.message || 'Failed to load platform admin records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPlatformData();
  }, []);

  const handleRevoke = async (id) => {
    setActionLoadingId(id);
    try {
      await platformApi.revokeInvitation(id);
      setFeedback(`Invitation #${id} revoked.`);
      loadPlatformData();
      setTimeout(() => setFeedback(''), 4000);
    } catch (err) {
      setError(err.message || 'Failed to revoke invitation.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleResend = async (id) => {
    setActionLoadingId(id);
    try {
      const updated = await platformApi.resendInvitation(id);
      if (updated?.invitationUrl) {
        setInvitationLinks((prev) => ({ ...prev, [id]: updated.invitationUrl }));
      }
      setFeedback(`Invitation #${id} resent with refreshed token and expiry.`);
      loadPlatformData();
      setTimeout(() => setFeedback(''), 4000);
    } catch (err) {
      setError(err.message || 'Failed to resend invitation.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCopyLink = (inv) => {
    const url = inv.invitationUrl || invitationLinks[inv.id];
    if (!url) return;
    navigator.clipboard.writeText(url);
    setCopiedId(inv.id);
    setFeedback(`Activation link for Invitation #${inv.id} copied to clipboard!`);
    setTimeout(() => {
      setCopiedId(null);
      setFeedback('');
    }, 3000);
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 rounded-full bg-brand-soft px-3 py-0.5 text-xs font-bold text-brand-hover mb-2">
              <ShieldCheck className="h-3.5 w-3.5" />
              Platform Administration
            </div>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">
              System Overview & Governance
            </h1>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <button
              type="button"
              onClick={loadPlatformData}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            <button
              type="button"
              onClick={() => setCreateCollegeOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-xl border border-brand bg-brand-soft px-4 py-2 text-xs font-bold text-brand-hover shadow-xs hover:bg-surface-mint transition-colors cursor-pointer"
            >
              <Building2 className="h-3.5 w-3.5" />
              Add College
            </button>
            <button
              type="button"
              onClick={() => setInviteModalOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
            >
              <Mail className="h-3.5 w-3.5" />
              Invite College Admin
            </button>
          </div>
        </div>

        {/* Global Metrics */}
        <div className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">Total Colleges</span>
            <p className="mt-1 text-2xl font-black text-ink">{colleges.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">Admin Invitations</span>
            <p className="mt-1 text-2xl font-black text-brand-hover">{invitations.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">System Tournaments</span>
            <p className="mt-1 text-2xl font-black text-ink">{tournaments.length}</p>
          </div>
        </div>
      </section>

      {/* Action / Success Banner */}
      {feedback && (
        <div className="flex items-start gap-2.5 rounded-xl border border-pitch-soft bg-pitch-soft/70 p-3.5 text-xs text-pitch animate-in fade-in-50">
          <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{feedback}</span>
        </div>
      )}

      {error && (
        <div className="flex items-start gap-2.5 rounded-xl border border-live-soft bg-live-soft/70 p-3.5 text-xs text-live">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* College Admin Invitations Table */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Mail className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">College Admin Invitations</h2>
              <p className="text-xs text-ink-secondary">Dispatch and manage role invitations</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setInviteModalOpen(true)}
            className="inline-flex items-center gap-1.5 rounded-full bg-brand px-3 py-1 text-xs font-bold text-white hover:bg-brand-hover transition-colors cursor-pointer"
          >
            <Plus className="h-3.5 w-3.5" />
            New Invitation
          </button>
        </div>

        <div className="mt-4 overflow-x-auto">
          {invitations.length === 0 ? (
            <div className="py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No invitations created yet.
            </div>
          ) : (
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-border text-ink-secondary uppercase tracking-wider text-[11px]">
                  <th className="py-2.5 px-3">ID</th>
                  <th className="py-2.5 px-3">Invitee Email</th>
                  <th className="py-2.5 px-3">College</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Expires At</th>
                  <th className="py-2.5 px-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {invitations.map((inv) => (
                  <tr key={inv.id} className="hover:bg-canvas/50">
                    <td className="py-2.5 px-3 font-semibold text-ink">#{inv.id}</td>
                    <td className="py-2.5 px-3 font-medium text-ink">{inv.email}</td>
                    <td className="py-2.5 px-3 text-ink-secondary">{inv.collegeName || `College #${inv.collegeId}`}</td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${
                          inv.status === 'ACCEPTED'
                            ? 'bg-pitch-soft text-pitch'
                            : inv.status === 'PENDING'
                              ? 'bg-amber-100 text-amber-800'
                              : 'bg-live-soft text-live'
                        }`}
                      >
                        {inv.status}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-ink-muted whitespace-nowrap">
                      {inv.expiresAt
                        ? new Date(inv.expiresAt).toLocaleString('en-IN', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit',
                          })
                        : '—'}
                    </td>
                    <td className="py-2.5 px-3 text-right">
                      {inv.status === 'PENDING' && (
                        <div className="inline-flex items-center gap-1.5">
                          {(inv.invitationUrl || invitationLinks[inv.id]) && (
                            <button
                              type="button"
                              onClick={() => handleCopyLink(inv)}
                              title="Copy Activation Link"
                              className="rounded-lg border border-border px-2 py-1 text-[11px] font-semibold text-brand hover:bg-brand-soft cursor-pointer inline-flex items-center gap-1"
                            >
                              <Copy className="h-3 w-3" />
                              {copiedId === inv.id ? 'Copied' : 'Copy Link'}
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => handleResend(inv.id)}
                            disabled={actionLoadingId === inv.id}
                            title="Resend Invitation Email"
                            className="rounded-lg border border-border p-1 text-ink hover:bg-surface-muted cursor-pointer"
                          >
                            <RotateCcw className="h-3.5 w-3.5" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleRevoke(inv.id)}
                            disabled={actionLoadingId === inv.id}
                            title="Revoke Invitation"
                            className="rounded-lg border border-border p-1 text-live hover:bg-live-soft cursor-pointer"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* Colleges Oversight */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Building2 className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Colleges Register</h2>
              <p className="text-xs text-ink-secondary">Tenant institutions enrolled in CricArena</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setCreateCollegeOpen(true)}
            className="inline-flex items-center gap-1.5 rounded-full bg-brand px-3 py-1 text-xs font-bold text-white hover:bg-brand-hover transition-colors cursor-pointer"
          >
            <Plus className="h-3.5 w-3.5" />
            Add College
          </button>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2 md:grid-cols-3">
          {colleges.map((c) => (
            <div
              key={c.id}
              className="rounded-xl border border-border bg-canvas p-3.5 text-xs space-y-1.5 hover:border-brand/40 transition-colors shadow-2xs"
            >
              <div className="flex items-center justify-between">
                <span className="font-bold text-ink text-sm">{c.name}</span>
                <span className="rounded bg-surface-muted px-2 py-0.5 text-[10px] font-extrabold text-brand-hover">
                  {c.code}
                </span>
              </div>
              <p className="text-ink-secondary">{c.city ? `${c.city}, ${c.state || ''}` : 'Campus'}</p>
              {c.contactEmail && (
                <p className="text-ink-muted text-[11px] truncate">{c.contactEmail}</p>
              )}
              <div className="pt-2 flex items-center justify-between border-t border-border/60 text-[10px]">
                <span className="text-ink-muted font-medium">Tenant ID #{c.id}</span>
                <span
                  className={`font-bold inline-flex items-center gap-1 ${
                    c.active ? 'text-pitch' : 'text-live'
                  }`}
                >
                  <span className={`h-1.5 w-1.5 rounded-full ${c.active ? 'bg-pitch' : 'bg-live'}`} />
                  {c.active ? 'Active' : 'Inactive'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Add College Modal */}
      <CreateCollegeModal
        open={createCollegeOpen}
        onClose={() => setCreateCollegeOpen(false)}
        onCreated={() => {
          setFeedback('New institution onboarded successfully!');
          loadPlatformData();
          refreshColleges?.();
        }}
      />

      {/* Invite Modal */}
      <CollegeAdminInviteModal
        open={inviteModalOpen}
        onClose={() => setInviteModalOpen(false)}
        colleges={colleges}
        onInvited={(result) => {
          if (result?.id && result?.invitationUrl) {
            setInvitationLinks((prev) => ({ ...prev, [result.id]: result.invitationUrl }));
          }
          setFeedback('Invitation created and ready to share.');
          loadPlatformData();
        }}
      />
    </div>
  );
}
