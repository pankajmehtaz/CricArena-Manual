const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export const TOKEN_STORAGE_KEY = 'cricarena_access_token';
export const REFRESH_TOKEN_STORAGE_KEY = 'cricarena_refresh_token';
export const USER_STORAGE_KEY = 'cricarena_auth_session';

let unauthorizedHandler = null;
let sessionRefreshedHandler = null;
let refreshInFlight = null;

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

export function setSessionRefreshedHandler(handler) {
  sessionRefreshedHandler = handler;
}

async function attemptSilentRefresh() {
  if (refreshInFlight) return refreshInFlight;

  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) return null;

  refreshInFlight = fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
    .then(async (res) => {
      if (!res.ok) return null;
      const authResponse = await res.json().catch(() => null);
      if (!authResponse?.accessToken) return null;
      saveAuthSession(authResponse);
      sessionRefreshedHandler?.(authResponse);
      return authResponse;
    })
    .catch(() => null)
    .finally(() => {
      refreshInFlight = null;
    });

  return refreshInFlight;
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function getStoredRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
}

export function getStoredSession() {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveAuthSession(authResponse) {
  if (!authResponse?.accessToken) return;
  localStorage.setItem(TOKEN_STORAGE_KEY, authResponse.accessToken);
  if (authResponse.refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, authResponse.refreshToken);
  }
  localStorage.setItem(
    USER_STORAGE_KEY,
    JSON.stringify({
      user: authResponse.user || null,
      roleAssignment: authResponse.roleAssignment || null,
      expiresIn: authResponse.expiresIn || null,
    })
  );
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
  localStorage.removeItem('cricarena_selected_college_id');
}

export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
    this.validationErrors = data?.validationErrors || null;
  }
}

async function request(path, options = {}) {
  const { method = 'GET', body, auth = true, headers: customHeaders = {} } = options;
  const headers = { ...customHeaders };

  if (body !== undefined && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (auth) {
    const token = getStoredToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? (body instanceof FormData ? body : JSON.stringify(body)) : undefined,
  });

  if (response.status === 401 && auth && !options._retried) {
    const refreshed = await attemptSilentRefresh();
    if (refreshed?.accessToken) {
      return request(path, { ...options, _retried: true });
    }
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const data = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text().catch(() => null);

  if (!response.ok) {
    if (response.status === 401 && auth && unauthorizedHandler) {
      unauthorizedHandler();
    }
    const validationDetails =
      typeof data === 'object' &&
      data?.validationErrors &&
      typeof data.validationErrors === 'object'
        ? Object.values(data.validationErrors).filter(Boolean).join(', ')
        : '';
    const message =
      validationDetails ||
      (typeof data === 'object' && data?.message) ||
      (typeof data === 'string' && data) ||
      `Request failed with status ${response.status}`;
    throw new ApiError(message, response.status, typeof data === 'object' ? data : null);
  }

  return data;
}

export const apiClient = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
};

export const healthApi = {
  check: () => apiClient.get('/health', { auth: false }),
};

export const authApi = {
  login: (email, password) =>
    apiClient.post('/auth/login', { email, password }, { auth: false }),
  register: (fullName, email, password) =>
    apiClient.post('/auth/register', { fullName, email, password }, { auth: false }),
  refreshToken: (refreshToken) =>
    apiClient.post('/auth/refresh', { refreshToken }, { auth: false }),
  logout: (refreshToken) =>
    apiClient.post('/auth/logout', { refreshToken: refreshToken || getStoredRefreshToken() }, { auth: false })
      .finally(() => clearAuthSession()),
};

export const invitationApi = {
  validateToken: (token) =>
    apiClient.get(`/invitations/validate?token=${encodeURIComponent(token)}`, { auth: false }),
  accept: (token, fullName, password) =>
    apiClient.post('/invitations/accept', { token, fullName, password }, { auth: false }),
};

export const platformApi = {
  createInvitation: (payload) => apiClient.post('/platform/invitations', payload),
  getAllInvitations: () => apiClient.get('/platform/invitations'),
  getInvitationById: (id) => apiClient.get(`/platform/invitations/${id}`),
  revokeInvitation: (id) => apiClient.post(`/platform/invitations/${id}/revoke`),
  resendInvitation: (id) => apiClient.post(`/platform/invitations/${id}/resend`),
};

export const collegeApi = {
  getActive: () => apiClient.get('/colleges/active', { auth: false }),
  getAll: () => apiClient.get('/colleges'),
  getById: (id) => apiClient.get(`/colleges/${id}`),
  create: (payload) => apiClient.post('/colleges', payload),
  update: (id, payload) => apiClient.put(`/colleges/${id}`, payload),
  deactivate: (id) => apiClient.delete(`/colleges/${id}`),
};

export const tournamentApi = {
  getAll: () => apiClient.get('/tournaments'),
  getById: (id) => apiClient.get(`/tournaments/${id}`),
  create: (payload) => apiClient.post('/tournaments', payload),
  update: (id, payload) => apiClient.put(`/tournaments/${id}`, payload),
  publish: (id) => apiClient.post(`/tournaments/${id}/publish`),
  remove: (id) => apiClient.delete(`/tournaments/${id}`),
  getTeams: (tournamentId) => apiClient.get(`/tournaments/${tournamentId}/teams`),
  registerTeam: (tournamentId, teamId) =>
    apiClient.post(`/tournaments/${tournamentId}/teams/${teamId}`),
  removeTeam: (tournamentId, teamId) =>
    apiClient.delete(`/tournaments/${tournamentId}/teams/${teamId}`),
  getMatches: (tournamentId) => apiClient.get(`/tournaments/${tournamentId}/matches`),
};

export const squadApi = {
  getSquad: (tournamentId, teamId) =>
    apiClient.get(`/tournaments/${tournamentId}/teams/${teamId}/squad`),
  addPlayer: (tournamentId, teamId, playerId) =>
    apiClient.post(`/tournaments/${tournamentId}/teams/${teamId}/squad`, { playerId }),
  setSquad: (tournamentId, teamId, playerIds) =>
    apiClient.put(`/tournaments/${tournamentId}/teams/${teamId}/squad`, { playerIds }),
  removePlayer: (tournamentId, teamId, playerId) =>
    apiClient.delete(`/tournaments/${tournamentId}/teams/${teamId}/squad/${playerId}`),
};

export const teamApi = {
  getAll: () => apiClient.get('/teams'),
  getActive: () => apiClient.get('/teams/active'),
  getById: (id) => apiClient.get(`/teams/${id}`),
  getByCollege: (collegeId) => apiClient.get(`/teams/college/${collegeId}`),
  create: (payload) => apiClient.post('/teams', payload),
  update: (id, payload) => apiClient.put(`/teams/${id}`, payload),
  deactivate: (id) => apiClient.delete(`/teams/${id}`),
  transferCaptaincy: (teamId, newCaptainUserId) =>
    apiClient.post(`/teams/${teamId}/captain`, { newCaptainUserId }),
};

export const captainApplicationApi = {
  submit: (payload) => apiClient.post('/captain-applications', payload),
  getMyApplications: () => apiClient.get('/captain-applications/my'),
  getById: (id) => apiClient.get(`/captain-applications/${id}`),
  withdraw: (id) => apiClient.post(`/captain-applications/${id}/withdraw`),
  getByCollege: (collegeId, status) => {
    const query = status ? `?status=${encodeURIComponent(status)}` : '';
    return apiClient.get(`/colleges/${collegeId}/captain-applications${query}`);
  },
  review: (id, approved, rejectionReason) =>
    apiClient.post(`/captain-applications/${id}/review`, { approved, rejectionReason }),
};

export const playerApi = {
  getAll: () => apiClient.get('/players'),
  getActive: () => apiClient.get('/players/active'),
  getById: (id) => apiClient.get(`/players/${id}`),
  getByTeam: (teamId) => apiClient.get(`/players/team/${teamId}`),
  getActiveByTeam: (teamId) => apiClient.get(`/players/team/${teamId}/active`),
  create: (payload) => apiClient.post('/players', payload),
  update: (id, payload) => apiClient.put(`/players/${id}`, payload),
  deactivate: (id) => apiClient.delete(`/players/${id}`),
};

export const matchApi = {
  getAll: (tournamentId) =>
    apiClient.get(tournamentId ? `/matches?tournamentId=${tournamentId}` : '/matches'),
  getById: (id) => apiClient.get(`/matches/${id}`),
  create: (payload) => apiClient.post('/matches', payload),
  update: (id, payload) => apiClient.put(`/matches/${id}`, payload),
  remove: (id) => apiClient.delete(`/matches/${id}`),
  setPlayingXI: (matchId, payload) => apiClient.post(`/matches/${matchId}/playing-xi`, payload),
  getPlayingXI: (matchId) => apiClient.get(`/matches/${matchId}/playing-xi`),
  getTeamPlayingXI: (matchId, teamId) =>
    apiClient.get(`/matches/${matchId}/playing-xi/${teamId}`),
  recordToss: (matchId, payload) => apiClient.post(`/matches/${matchId}/toss`, payload),
  getToss: (matchId) => apiClient.get(`/matches/${matchId}/toss`),
  startInnings: (matchId, payload) => apiClient.post(`/matches/${matchId}/innings/start`, payload),
  getInnings: (matchId) => apiClient.get(`/matches/${matchId}/innings`),
  getInningsByNumber: (matchId, inningsNumber) =>
    apiClient.get(`/matches/${matchId}/innings/${inningsNumber}`),
  recordDelivery: (matchId, payload) => apiClient.post(`/matches/${matchId}/deliveries`, payload),
  getDeliveries: (matchId) => apiClient.get(`/matches/${matchId}/deliveries`),
  changeBowler: (matchId, payload) => apiClient.post(`/matches/${matchId}/bowler`, payload),
  getScorecard: (matchId) => apiClient.get(`/matches/${matchId}/scorecard`, { auth: false }),
};
