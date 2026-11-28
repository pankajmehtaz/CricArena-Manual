// Live external APIs with local caching & graceful fallbacks for Indian States, Cities, and Colleges
const COUNTRIES_NOW_BASE = 'https://countriesnow.space/api/v0.1/countries';
const COLLEGES_CDN_BASE = 'https://cdn.jsdelivr.net/gh/anburocky3/indian-colleges-data@main/data/states';
const COLLEGES_RAW_BASE = 'https://raw.githubusercontent.com/anburocky3/indian-colleges-data/main/data/states';
const HIPO_UNIVERSITIES_URL =
  'https://cdn.jsdelivr.net/gh/Hipo/university-domains-list@master/world_universities_and_domains.json';

const FALLBACK_STATES = [
  'Andhra Pradesh',
  'Arunachal Pradesh',
  'Assam',
  'Bihar',
  'Chandigarh',
  'Chhattisgarh',
  'Delhi',
  'Goa',
  'Gujarat',
  'Haryana',
  'Himachal Pradesh',
  'Jammu and Kashmir',
  'Jharkhand',
  'Karnataka',
  'Kerala',
  'Madhya Pradesh',
  'Maharashtra',
  'Manipur',
  'Meghalaya',
  'Mizoram',
  'Nagaland',
  'Odisha',
  'Puducherry',
  'Punjab',
  'Rajasthan',
  'Sikkim',
  'Tamil Nadu',
  'Telangana',
  'Tripura',
  'Uttar Pradesh',
  'Uttarakhand',
  'West Bengal',
];

const FALLBACK_CITIES_BY_STATE = {
  Haryana: ['Ambala', 'Bhiwani', 'Faridabad', 'Gurugram', 'Hisar', 'Jhajjar', 'Karnal', 'Kurukshetra', 'Palwal', 'Panchkula', 'Panipat', 'Rewari', 'Rohtak', 'Sirsa', 'Sonipat', 'Yamunanagar'],
  Delhi: ['Central Delhi', 'Dwarka', 'East Delhi', 'New Delhi', 'North Delhi', 'Rohini', 'South Delhi', 'West Delhi'],
  'Uttar Pradesh': ['Agra', 'Aligarh', 'Bareilly', 'Ghaziabad', 'Gorakhpur', 'Greater Noida', 'Kanpur', 'Lucknow', 'Mathura', 'Meerut', 'Noida', 'Prayagraj', 'Varanasi'],
  Rajasthan: ['Ajmer', 'Alwar', 'Bikaner', 'Jaipur', 'Jodhpur', 'Kota', 'Pilani', 'Sikar', 'Udaipur'],
  Punjab: ['Amritsar', 'Bathinda', 'Chandigarh', 'Jalandhar', 'Ludhiana', 'Mohali', 'Patiala'],
  Maharashtra: ['Aurangabad', 'Kolhapur', 'Mumbai', 'Nagpur', 'Nashik', 'Navi Mumbai', 'Pune', 'Thane'],
  Karnataka: ['Belagavi', 'Bengaluru', 'Hubballi', 'Mangaluru', 'Mysuru'],
  'Tamil Nadu': ['Chennai', 'Coimbatore', 'Madurai', 'Salem', 'Tiruchirappalli', 'Vellore'],
};

const ACRONYMS = new Set(['IIT', 'NIT', 'IIIT', 'IIM', 'AIIMS', 'YMCA', 'DAV', 'DPS', 'NCR', 'BITS', 'DTU', 'NSUT', 'VIT', 'SRM', 'MIT', 'NPTI', 'XLRI', 'JC', 'DR', 'B.TECH', 'PGDM']);
const SMALL_WORDS = new Set(['of', 'and', 'in', 'for', 'the', 'at', 'to', 'on', 'by']);

export function toTitleCase(input) {
  if (!input || typeof input !== 'string') return '';
  const trimmed = input.trim().replace(/\s+/g, ' ');
  if (!trimmed) return '';
  // If already mixed-case (not all caps and not all lower), preserve original nicely
  if (trimmed !== trimmed.toUpperCase() && trimmed !== trimmed.toLowerCase()) {
    return trimmed;
  }
  return trimmed
    .split(' ')
    .map((word, idx) => {
      const upper = word.toUpperCase();
      if (ACRONYMS.has(upper)) return upper;
      const lower = word.toLowerCase();
      if (idx > 0 && SMALL_WORDS.has(lower)) return lower;
      return lower.charAt(0).toUpperCase() + lower.slice(1);
    })
    .join(' ');
}

export function suggestCollegeCode(name) {
  if (!name) return '';
  const words = name
    .replace(/[^a-zA-Z0-9\s]/g, ' ')
    .split(/\s+/)
    .filter((w) => w && !SMALL_WORDS.has(w.toLowerCase()));
  if (words.length === 0) return '';
  if (words.length === 1) return words[0].slice(0, 8).toUpperCase();
  const initials = words.map((w) => w[0].toUpperCase()).join('');
  return initials.slice(0, 12);
}

function normalizeCityName(raw) {
  if (!raw || typeof raw !== 'string') return '';
  return raw
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+District$/i, '')
    .trim();
}

// In-memory session caches
let statesCache = null;
const citiesCache = new Map();
const stateCollegesCache = new Map();
let hipoUniversitiesCache = null;

export async function fetchIndianStates() {
  if (statesCache) return statesCache;
  try {
    const res = await fetch(`${COUNTRIES_NOW_BASE}/states/q?country=India`);
    if (!res.ok) throw new Error('Failed to fetch states');
    const json = await res.json();
    const list = json?.data?.states?.map((s) => s.name).filter(Boolean) || [];
    if (list.length > 0) {
      statesCache = Array.from(new Set(list)).sort((a, b) => a.localeCompare(b));
      return statesCache;
    }
  } catch {
    // fallback below
  }
  statesCache = [...FALLBACK_STATES];
  return statesCache;
}

export async function fetchCitiesByState(stateName) {
  const key = (stateName || '').trim();
  if (!key) return [];
  if (citiesCache.has(key)) return citiesCache.get(key);

  const fallback = FALLBACK_CITIES_BY_STATE[key] || [];
  try {
    const res = await fetch(
      `${COUNTRIES_NOW_BASE}/state/cities/q?country=India&state=${encodeURIComponent(key)}`
    );
    if (!res.ok) throw new Error('Failed to fetch cities');
    const json = await res.json();
    const rawList = Array.isArray(json?.data) ? json.data : [];
    const cleaned = rawList.map(normalizeCityName).filter(Boolean);
    const merged = Array.from(new Set([...cleaned, ...fallback])).sort((a, b) =>
      a.localeCompare(b)
    );
    if (merged.length > 0) {
      citiesCache.set(key, merged);
      return merged;
    }
  } catch {
    // fallback below
  }
  citiesCache.set(key, fallback);
  return fallback;
}

export async function fetchMajorIndianUniversities() {
  if (hipoUniversitiesCache) return hipoUniversitiesCache;
  try {
    const res = await fetch(HIPO_UNIVERSITIES_URL);
    if (!res.ok) throw new Error('Failed to fetch universities');
    const all = await res.json();
    const indiaOnly = (Array.isArray(all) ? all : [])
      .filter((u) => u.country === 'India' && u.name)
      .map((u) => ({
        name: u.name.trim(),
        city: '',
        state: u['state-province'] ? u['state-province'].trim() : '',
        address: '',
        website: Array.isArray(u.web_pages) && u.web_pages[0] ? u.web_pages[0] : '',
        subtitle: [u['state-province'], u.domains?.[0]].filter(Boolean).join(' • '),
      }));
    hipoUniversitiesCache = indiaOnly;
    return indiaOnly;
  } catch {
    return [];
  }
}

export async function fetchCollegesByState(stateName) {
  const cleanState = (stateName || '').trim();
  if (!cleanState) return [];
  if (stateCollegesCache.has(cleanState)) return stateCollegesCache.get(cleanState);

  const slug = cleanState.toLowerCase().replace(/\s+/g, '-');
  let rawInstitutions = [];

  for (const baseUrl of [COLLEGES_CDN_BASE, COLLEGES_RAW_BASE]) {
    try {
      const res = await fetch(`${baseUrl}/${slug}.json`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          rawInstitutions = data;
          break;
        }
      }
    } catch {
      // try next mirror
    }
  }

  const seen = new Set();
  const normalized = [];

  for (const item of rawInstitutions) {
    const rawName = (item.institute_name || '').trim();
    if (!rawName) continue;
    const dedupKey = rawName.toUpperCase();
    if (seen.has(dedupKey)) continue;
    seen.add(dedupKey);

    const formattedName = toTitleCase(rawName);
    const formattedCity = toTitleCase(normalizeCityName(item.district || ''));
    const formattedAddress = toTitleCase(item.address || '');
    const uni =
      item.university && item.university !== 'NOT APPLICABLE'
        ? toTitleCase(item.university)
        : item.institution_type || '';

    normalized.push({
      name: formattedName,
      city: formattedCity,
      state: cleanState,
      address: formattedAddress,
      website: '',
      subtitle: [formattedCity, uni].filter(Boolean).join(' • '),
    });
  }

  // Also merge matching Hipo universities if already loaded or load quickly
  const hipo = await fetchMajorIndianUniversities();
  for (const u of hipo) {
    if (
      u.state &&
      u.state.toLowerCase().includes(cleanState.toLowerCase()) &&
      !seen.has(u.name.toUpperCase())
    ) {
      seen.add(u.name.toUpperCase());
      normalized.push({
        ...u,
        state: cleanState,
      });
    }
  }

  normalized.sort((a, b) => a.name.localeCompare(b.name));
  stateCollegesCache.set(cleanState, normalized);
  return normalized;
}
