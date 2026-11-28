const MONOGRAM_PALETTES = [
  'bg-teal-600 text-white',
  'bg-emerald-600 text-white',
  'bg-sky-600 text-white',
  'bg-amber-600 text-white',
  'bg-indigo-600 text-white',
  'bg-rose-600 text-white',
];

export function getTeamInitials(name = '') {
  const words = name.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return 'TM';
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return (words[0][0] + words[1][0]).toUpperCase();
}

export function getTeamBadgeClass(name = '') {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
  }
  return MONOGRAM_PALETTES[hash % MONOGRAM_PALETTES.length];
}

export function formatMatchDateTime(isoString) {
  if (!isoString) return 'Date TBD';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return isoString;
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatMatchShortDate(isoString) {
  if (!isoString) return 'Date TBD';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return isoString;
  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: '2-digit',
  });
}

export function canManageMatches(roleAssignment) {
  const role = roleAssignment?.role;
  return role === 'PLATFORM_ADMIN' || role === 'COLLEGE_ADMIN';
}

export function getTeamRowsForCard(match, scorecard) {
  const inn1 = scorecard?.innings1 || null;
  const inn2 = scorecard?.innings2 || null;

  if (inn1) {
    const firstTeamId = inn1.battingTeamId;
    const firstTeamName = inn1.battingTeamName;
    const secondTeamId = inn2?.battingTeamId ?? inn1.bowlingTeamId;
    const secondTeamName = inn2?.battingTeamName ?? inn1.bowlingTeamName;

    return [
      {
        teamId: firstTeamId,
        teamName: firstTeamName,
        innings: inn1,
      },
      {
        teamId: secondTeamId,
        teamName: secondTeamName,
        innings: inn2,
      },
    ];
  }

  return [
    { teamId: match.teamAId, teamName: match.teamAName, innings: null },
    { teamId: match.teamBId, teamName: match.teamBName, innings: null },
  ];
}

export function getMatchCardFootnote(match, scorecard) {
  const resultSummary = scorecard?.result?.resultSummary || match.resultSummary;
  if (match.status === 'COMPLETED' && resultSummary) {
    return resultSummary;
  }
  if (match.status === 'ABANDONED') {
    return resultSummary || 'Match abandoned / no result';
  }
  if (match.status === 'CANCELLED') {
    return resultSummary || 'Match cancelled';
  }

  const inn2 = scorecard?.innings2;
  if (match.status === 'IN_PROGRESS' && inn2 && inn2.targetRuns != null) {
    const runsNeeded = Math.max(0, inn2.targetRuns - (inn2.totalRuns ?? 0));
    const maxBalls = (inn2.maxOvers ?? match.maxOvers ?? 0) * 6;
    const ballsRemaining = Math.max(0, maxBalls - (inn2.legalBalls ?? 0));
    return `${inn2.battingTeamName} needs ${runsNeeded} run${runsNeeded === 1 ? '' : 's'} in ${ballsRemaining} ball${ballsRemaining === 1 ? '' : 's'}`;
  }

  if (match.tossWinnerTeamName && match.tossDecision) {
    const decisionText = match.tossDecision === 'BAT' ? 'bat' : 'bowl';
    return `${match.tossWinnerTeamName} won the toss and elected to ${decisionText}`;
  }

  if (match.status === 'IN_PROGRESS') {
    return 'Match in progress';
  }

  return 'Scheduled · Awaiting toss & Playing XI';
}
