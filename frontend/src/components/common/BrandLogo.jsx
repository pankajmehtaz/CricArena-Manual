export default function BrandLogo({ compact = false }) {
  return (
    <div className="inline-flex items-center gap-2.5 select-none">
      <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand text-white shadow-sm">
        <svg
          viewBox="0 0 24 24"
          className="h-5 w-5"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          {/* Three stumps + cricket ball seam */}
          <path d="M7 20V7" />
          <path d="M12 20V5" />
          <path d="M17 20V7" />
          <path d="M6.5 6.5h11" />
          <circle cx="17.5" cy="14.5" r="3" fill="currentColor" fillOpacity="0.22" />
        </svg>
      </div>
      {!compact && (
        <div className="flex flex-col leading-none">
          <span className="text-lg font-extrabold tracking-tight text-ink">
            Cric<span className="text-brand">Arena</span>
          </span>
          <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-muted">
            Match Center
          </span>
        </div>
      )}
    </div>
  );
}
