const STATUS_STYLES = {
  IN_PROGRESS: {
    label: 'LIVE',
    classes: 'bg-live text-white',
    dot: true,
  },
  LIVE: {
    label: 'LIVE',
    classes: 'bg-live text-white',
    dot: true,
  },
  COMPLETED: {
    label: 'RESULT',
    classes: 'bg-ink text-white',
    dot: false,
  },
  RESULT: {
    label: 'RESULT',
    classes: 'bg-ink text-white',
    dot: false,
  },
  SCHEDULED: {
    label: 'UPCOMING',
    classes: 'bg-brand-soft text-brand-hover',
    dot: false,
  },
  DRAFT: {
    label: 'DRAFT',
    classes: 'bg-surface-muted text-ink-secondary',
    dot: false,
  },
  PUBLISHED: {
    label: 'PUBLISHED',
    classes: 'bg-pitch-soft text-pitch',
    dot: false,
  },
  ABANDONED: {
    label: 'ABANDONED',
    classes: 'bg-boundary-soft text-boundary',
    dot: false,
  },
  CANCELLED: {
    label: 'CANCELLED',
    classes: 'bg-surface-muted text-ink-secondary',
    dot: false,
  },
  ACTIVE: {
    label: 'ACTIVE',
    classes: 'bg-pitch-soft text-pitch border border-pitch/20',
    dot: true,
  },
  INACTIVE: {
    label: 'INACTIVE',
    classes: 'bg-surface-muted text-ink-muted border border-border',
    dot: false,
  },
};

export default function StatusBadge({ status, label }) {
  const cfg = STATUS_STYLES[status] || {
    label: status || 'UNKNOWN',
    classes: 'bg-surface-muted text-ink-secondary',
    dot: false,
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-bold tracking-wide ${cfg.classes}`}
    >
      {cfg.dot && <span className="h-1.5 w-1.5 rounded-full bg-white animate-pulse" />}
      {label || cfg.label}
    </span>
  );
}
