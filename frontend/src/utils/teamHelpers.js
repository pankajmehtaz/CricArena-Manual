export const TEAM_MONOGRAM_PALETTES = [
  'bg-teal-600 text-white',
  'bg-emerald-600 text-white',
  'bg-blue-600 text-white',
  'bg-sky-600 text-white',
  'bg-indigo-600 text-white',
  'bg-purple-600 text-white',
  'bg-rose-600 text-white',
  'bg-amber-600 text-white',
];

export function getTeamInitials(name = '', shortName = '') {
  if (shortName && shortName.trim()) {
    return shortName.trim().toUpperCase().slice(0, 4);
  }
  const words = name.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return 'TM';
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return (words[0][0] + words[1][0]).toUpperCase();
}

export function getTeamMonogramClass(name = '') {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
  }
  return TEAM_MONOGRAM_PALETTES[hash % TEAM_MONOGRAM_PALETTES.length];
}

export function canManageTeams(roleAssignment, team = null) {
  if (!roleAssignment?.role) return false;
  if (roleAssignment.role === 'PLATFORM_ADMIN') return true;
  if (roleAssignment.role === 'COLLEGE_ADMIN') {
    if (!team) return true;
    return String(team.collegeId) === String(roleAssignment.collegeId);
  }
  if (roleAssignment.role === 'TEAM_CAPTAIN') {
    if (!team) return false;
    return String(team.id) === String(roleAssignment.teamId);
  }
  return false;
}

export function formatTeamDate(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  if (Number.isNaN(d.getTime())) return isoString;
  return d.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}
