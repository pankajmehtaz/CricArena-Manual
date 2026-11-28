import { useEffect, useState } from 'react';
import {
  AlertCircle,
  Check,
  CheckCircle2,
  Copy,
  ExternalLink,
  Loader2,
  Mail,
  Send,
  X,
} from 'lucide-react';
import { platformApi } from '../../services/apiClient';

export default function CollegeAdminInviteModal({ open, onClose, colleges = [], onInvited }) {
  const [collegeId, setCollegeId] = useState(
    colleges.length > 0 ? String(colleges[0].id) : ''
  );
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [createdInvitation, setCreatedInvitation] = useState(null);
  const [copied, setCopied] = useState(false);

  // Sync default collegeId when modal opens or colleges list loads
  useEffect(() => {
    if (open && colleges.length > 0) {
      setCollegeId((prev) => {
        const exists = colleges.some((c) => String(c.id) === String(prev));
        return exists ? prev : String(colleges[colleges.length - 1].id);
      });
    }
  }, [open, colleges]);

  if (!open) return null;

  const handleClose = () => {
    setEmail('');
    setError('');
    setCreatedInvitation(null);
    setCopied(false);
    onClose();
  };

  const handleCopy = () => {
    if (!createdInvitation?.invitationUrl) return;
    navigator.clipboard.writeText(createdInvitation.invitationUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setCreatedInvitation(null);

    if (!collegeId) {
      setError('Please select a college.');
      return;
    }
    if (!email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError('Please provide a valid email address.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await platformApi.createInvitation({
        collegeId: Number(collegeId),
        email: email.trim(),
        role: 'COLLEGE_ADMIN',
      });
      setCreatedInvitation(result);
      onInvited?.(result);
    } catch (err) {
      setError(err.message || 'Failed to dispatch College Admin invitation.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
      <div className="w-full max-w-md rounded-2xl border border-border bg-surface p-6 shadow-xl animate-in fade-in-50 zoom-in-95 duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Mail className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Invite College Admin</h2>
              <p className="text-xs text-ink-secondary">
                Generate secure onboarding activation link
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleClose}
            className="rounded-lg p-1.5 text-ink-muted hover:bg-surface-muted hover:text-ink cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/60 p-3 text-xs text-live">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {createdInvitation ? (
          <div className="mt-4 space-y-4">
            <div className="flex items-start gap-2.5 rounded-xl border border-pitch-soft bg-pitch-soft/60 p-3.5 text-xs text-pitch">
              <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold">Invitation created for {createdInvitation.email}</p>
                <p className="mt-0.5 text-ink-secondary">
                  Share this activation link with the College Administrator (valid 24 hours).
                </p>
              </div>
            </div>

            {createdInvitation.invitationUrl && (
              <div className="rounded-xl border border-border bg-canvas p-3 space-y-2">
                <span className="block text-[10px] font-bold uppercase tracking-wider text-ink-secondary">
                  Activation Link
                </span>
                <p className="text-[11px] font-mono text-ink break-all select-all bg-surface rounded-lg border border-border p-2">
                  {createdInvitation.invitationUrl}
                </p>
                <div className="flex items-center gap-2 pt-1">
                  <button
                    type="button"
                    onClick={handleCopy}
                    className="flex-1 inline-flex items-center justify-center gap-1.5 rounded-xl bg-brand px-3 py-2 text-xs font-bold text-white hover:bg-brand-hover transition-colors cursor-pointer"
                  >
                    {copied ? <Check className="h-3.5 w-3.5" /> : <Copy className="h-3.5 w-3.5" />}
                    {copied ? 'Copied!' : 'Copy Link'}
                  </button>
                  <a
                    href={createdInvitation.invitationUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors"
                  >
                    <ExternalLink className="h-3.5 w-3.5" />
                    Open
                  </a>
                </div>
              </div>
            )}

            <div className="flex justify-end pt-2 border-t border-border">
              <button
                type="button"
                onClick={handleClose}
                className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted cursor-pointer"
              >
                Done
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="mt-4 space-y-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Select College *
              </label>
              <select
                required
                value={collegeId}
                onChange={(e) => setCollegeId(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {colleges.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.code})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Admin Email Address *
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="sports.admin@jcboseust.ac.in"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-border">
              <button
                type="button"
                onClick={handleClose}
                className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-60 transition-colors cursor-pointer"
              >
                {submitting ? (
                  <Loader2 className="h-3.5 w-3.5 animate-spin" />
                ) : (
                  <Send className="h-3.5 w-3.5" />
                )}
                {submitting ? 'Sending...' : 'Send Invitation'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
