import { useEffect, useMemo, useRef, useState } from 'react';
import { ChevronDown, Loader2 } from 'lucide-react';

/**
 * Reusable searchable combobox input that supports both picking from an API-driven
 * option list and typing a custom value freely.
 * Each option can be a string or an object { value, label, subtitle?, badge? }.
 */
export default function SearchableDropdownInput({
  value,
  onChange,
  onSelectOption,
  options = [],
  placeholder = '',
  required = false,
  loading = false,
  emptyText = 'No matching options found. You can keep typing your own.',
  maxVisible = 80,
  className = '',
}) {
  const [open, setOpen] = useState(false);
  const [isFiltering, setIsFiltering] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    const handleOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
        setIsFiltering(false);
      }
    };
    if (open) {
      document.addEventListener('mousedown', handleOutside);
    }
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [open]);

  const normalizedOptions = useMemo(
    () =>
      options.map((opt) =>
        typeof opt === 'string'
          ? { value: opt, label: opt, subtitle: '', badge: '', raw: opt }
          : {
              value: opt.value ?? opt.name ?? '',
              label: opt.label ?? opt.name ?? '',
              subtitle: opt.subtitle ?? '',
              badge: opt.badge ?? '',
              raw: opt,
            }
      ),
    [options]
  );

  const filteredOptions = useMemo(() => {
    const q = isFiltering ? (value || '').trim().toLowerCase() : '';
    const matches = q
      ? normalizedOptions.filter(
          (opt) =>
            opt.label.toLowerCase().includes(q) ||
            opt.subtitle.toLowerCase().includes(q)
        )
      : normalizedOptions;
    return matches.slice(0, maxVisible);
  }, [normalizedOptions, value, isFiltering, maxVisible]);

  const handleInputChange = (e) => {
    setIsFiltering(true);
    setOpen(true);
    onChange?.(e.target.value);
  };

  const handleOptionClick = (opt) => {
    setOpen(false);
    setIsFiltering(false);
    onChange?.(opt.value);
    onSelectOption?.(opt.raw);
  };

  return (
    <div className="relative" ref={containerRef}>
      <div className="relative">
        <input
          type="text"
          required={required}
          value={value}
          onFocus={() => {
            setIsFiltering(false);
            setOpen(true);
          }}
          onChange={handleInputChange}
          placeholder={placeholder}
          autoComplete="off"
          className={`w-full rounded-xl border border-border bg-canvas pl-3.5 pr-9 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none ${className}`}
        />
        <button
          type="button"
          tabIndex={-1}
          onClick={() => {
            setIsFiltering(false);
            setOpen((prev) => !prev);
          }}
          aria-label="Toggle options"
          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-ink-muted hover:text-ink cursor-pointer"
        >
          {loading ? (
            <Loader2 className="h-4 w-4 animate-spin text-brand" />
          ) : (
            <ChevronDown
              className={`h-4 w-4 transition-transform duration-150 ${
                open ? 'rotate-180' : ''
              }`}
            />
          )}
        </button>
      </div>

      {open && (
        <div className="absolute left-0 right-0 mt-1.5 max-h-56 overflow-y-auto rounded-xl border border-border bg-surface p-1 shadow-xl z-50">
          {loading && normalizedOptions.length === 0 ? (
            <div className="flex items-center gap-2 px-3 py-3 text-xs text-ink-secondary">
              <Loader2 className="h-3.5 w-3.5 animate-spin text-brand" />
              <span>Loading from directory API...</span>
            </div>
          ) : filteredOptions.length === 0 ? (
            <div className="px-3 py-2.5 text-xs text-ink-muted">{emptyText}</div>
          ) : (
            filteredOptions.map((opt, idx) => (
              <button
                key={`${opt.value}-${idx}`}
                type="button"
                onClick={() => handleOptionClick(opt)}
                className="w-full text-left rounded-lg px-3 py-2 hover:bg-brand-soft/60 transition-colors cursor-pointer flex items-start justify-between gap-2"
              >
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-semibold text-ink truncate">{opt.label}</p>
                  {opt.subtitle && (
                    <p className="text-[11px] text-ink-secondary truncate mt-0.5">
                      {opt.subtitle}
                    </p>
                  )}
                </div>
                {opt.badge && (
                  <span className="shrink-0 rounded-full bg-surface-mint px-2 py-0.5 text-[10px] font-bold text-brand-hover">
                    {opt.badge}
                  </span>
                )}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
