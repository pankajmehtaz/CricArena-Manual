import { Building2, ChevronDown } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useCollege } from '../../context/CollegeContext';

export default function CollegeSelector() {
  const { roleAssignment } = useAuth();
  const { colleges, selectedCollegeId, setSelectedCollegeId, isCollegeLocked, loadingColleges } =
    useCollege();
  const isPlatformAdmin = roleAssignment?.role === 'PLATFORM_ADMIN';

  if (loadingColleges && colleges.length === 0) {
    return (
      <div className="hidden sm:flex items-center gap-1.5 rounded-full border border-border bg-surface px-3 py-1 text-xs text-ink-muted">
        <Building2 className="h-3.5 w-3.5" />
        <span>Loading Colleges...</span>
      </div>
    );
  }

  if (colleges.length === 0) {
    return null;
  }

  const currentCollege = colleges.find((c) => String(c.id) === String(selectedCollegeId));

  if (isCollegeLocked && currentCollege) {
    return (
      <div className="relative inline-flex items-center">
        <div
          className="flex items-center gap-1.5 rounded-full border border-border bg-surface px-3 py-1 shadow-xs text-xs font-semibold text-ink"
          title={`Locked to ${currentCollege.name} (${currentCollege.code})`}
        >
          <Building2 className="h-3.5 w-3.5 text-brand shrink-0" />
          <span className="max-w-[140px] sm:max-w-[180px] truncate">
            {currentCollege.name} ({currentCollege.code})
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="relative inline-flex items-center">
      <div className="flex items-center gap-1.5 rounded-full border border-border bg-surface px-3 py-1 shadow-sm text-xs font-semibold text-ink">
        <Building2 className="h-3.5 w-3.5 text-brand shrink-0" />
        <select
          value={selectedCollegeId || ''}
          onChange={(e) => setSelectedCollegeId(e.target.value)}
          disabled={isCollegeLocked}
          title={isCollegeLocked ? `Assigned to ${currentCollege?.name || 'your college'}` : 'Switch active college'}
          className="appearance-none bg-transparent min-w-0 font-semibold text-ink focus:outline-none cursor-pointer disabled:cursor-default disabled:opacity-90 max-w-[130px] sm:max-w-[175px] truncate"
        >
          {!isCollegeLocked && (
            <option value="">{isPlatformAdmin ? 'All Colleges (Global)' : 'Select your college'}</option>
          )}
          {colleges.map((col) => (
            <option key={col.id} value={col.id}>
              {col.name} ({col.code})
            </option>
          ))}
        </select>
        {!isCollegeLocked && (
          <ChevronDown className="pointer-events-none h-3.5 w-3.5 shrink-0 text-ink-muted" />
        )}
      </div>
    </div>
  );
}
