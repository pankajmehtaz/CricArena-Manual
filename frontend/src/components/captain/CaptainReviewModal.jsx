import { useState } from 'react';
import { AlertCircle, CheckCircle, Loader2, ShieldCheck, X, XCircle } from 'lucide-react';
import { captainApplicationApi } from '../../services/apiClient';

export default function CaptainReviewModal({ open, onClose, application, onReviewed }) {
  const [decision, setDecision] = useState(null); // 'APPROVE' | 'REJECT'
  const [rejectionReason, setRejectionReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (!open || !application) return null;

  const handleReview = async () => {
    if (decision === 'REJECT' && !rejectionReason.trim()) {
      setError('Please provide a reason for rejecting this application.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const isApproved = decision === 'APPROVE';
      const updated = await captainApplicationApi.review(
        application.id,
        isApproved,
        isApproved ? null : rejectionReason.trim()
      );
      onReviewed?.(updated);
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to review captain application.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl border border-border bg-surface p-6 shadow-xl animate-in fade-in-50 zoom-in-95 duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Review Captain Application</h2>
              <p className="text-xs text-ink-secondary">Applicant #{application.id}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
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

        <div className="mt-4 space-y-3 rounded-xl border border-border bg-canvas p-4 text-xs">
          <div className="flex justify-between border-b border-border/60 pb-2">
            <span className="text-ink-secondary">Applicant Name</span>
            <span className="font-semibold text-ink">
              {application.userName || application.userFullName || '—'}
            </span>
          </div>
          <div className="flex justify-between border-b border-border/60 pb-2">
            <span className="text-ink-secondary">Email</span>
            <span className="font-semibold text-ink">{application.userEmail || '—'}</span>
          </div>
          <div className="flex justify-between border-b border-border/60 pb-2">
            <span className="text-ink-secondary">Phone</span>
            <span className="font-semibold text-ink">{application.contactNumber || '—'}</span>
          </div>
          <div className="flex justify-between border-b border-border/60 pb-2">
            <span className="text-ink-secondary">Tournament</span>
            <span className="font-semibold text-brand-hover">{application.tournamentName || '—'}</span>
          </div>
          <div>
            <span className="text-ink-secondary block mb-1">Reason / Statement:</span>
            <p className="text-ink bg-surface p-2.5 rounded-lg border border-border font-normal leading-relaxed whitespace-pre-wrap">
              {application.reason || 'No statement provided.'}
            </p>
          </div>
          {application.experience && (
            <div>
              <span className="text-ink-secondary block mb-1">Cricket Experience:</span>
              <p className="text-ink bg-surface p-2.5 rounded-lg border border-border font-normal leading-relaxed whitespace-pre-wrap">
                {application.experience}
              </p>
            </div>
          )}
        </div>

        {/* Action Choice */}
        <div className="mt-4">
          <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-2">
            Decision
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => {
                setDecision('APPROVE');
                setError('');
              }}
              className={`flex items-center justify-center gap-2 rounded-xl border p-3 text-xs font-bold transition-all cursor-pointer ${
                decision === 'APPROVE'
                  ? 'border-pitch bg-pitch-soft text-pitch shadow-sm ring-2 ring-pitch/20'
                  : 'border-border bg-surface text-ink-secondary hover:bg-surface-muted'
              }`}
            >
              <CheckCircle className="h-4 w-4 text-pitch" />
              <span>Approve as Captain</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setDecision('REJECT');
                setError('');
              }}
              className={`flex items-center justify-center gap-2 rounded-xl border p-3 text-xs font-bold transition-all cursor-pointer ${
                decision === 'REJECT'
                  ? 'border-live bg-live-soft text-live shadow-sm ring-2 ring-live/20'
                  : 'border-border bg-surface text-ink-secondary hover:bg-surface-muted'
              }`}
            >
              <XCircle className="h-4 w-4 text-live" />
              <span>Reject Application</span>
            </button>
          </div>
        </div>

        {decision === 'REJECT' && (
          <div className="mt-4">
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Rejection Reason (Required)
            </label>
            <textarea
              required
              rows={2}
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="Explain to the applicant why this request was rejected..."
              className="w-full rounded-xl border border-border bg-canvas p-2.5 text-xs text-ink placeholder:text-ink-muted focus:border-live focus:bg-surface focus:outline-none"
            />
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-4 mt-4 border-t border-border">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleReview}
            disabled={!decision || submitting}
            className={`inline-flex items-center gap-1.5 rounded-xl px-5 py-2 text-xs font-bold text-white shadow-sm transition-colors cursor-pointer disabled:opacity-50 ${
              decision === 'REJECT'
                ? 'bg-live hover:bg-live-hover'
                : 'bg-pitch hover:bg-pitch-hover'
            }`}
          >
            {submitting && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
            {decision === 'APPROVE'
              ? 'Confirm Approval'
              : decision === 'REJECT'
                ? 'Confirm Rejection'
                : 'Select Decision'}
          </button>
        </div>
      </div>
    </div>
  );
}
