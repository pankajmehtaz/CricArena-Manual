import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import {
  authApi,
  clearAuthSession,
  getStoredRefreshToken,
  getStoredSession,
  getStoredToken,
  saveAuthSession,
  setSessionRefreshedHandler,
  setUnauthorizedHandler,
} from '../services/apiClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredToken());
  const [session, setSession] = useState(() => getStoredSession());

  const logout = useCallback(() => {
    clearAuthSession();
    setToken(null);
    setSession(null);
  }, []);

  const setSessionFromAuthResponse = useCallback((authResponse) => {
    saveAuthSession(authResponse);
    setToken(authResponse.accessToken);
    setSession({
      user: authResponse.user || null,
      roleAssignment: authResponse.roleAssignment || null,
      expiresIn: authResponse.expiresIn || null,
    });
    return authResponse;
  }, []);

  const refreshSession = useCallback(async () => {
    const refreshToken = getStoredRefreshToken();
    if (!refreshToken) return null;
    try {
      const authResponse = await authApi.refreshToken(refreshToken);
      return setSessionFromAuthResponse(authResponse);
    } catch {
      return null;
    }
  }, [setSessionFromAuthResponse]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
    });
    setSessionRefreshedHandler((authResponse) => {
      setSessionFromAuthResponse(authResponse);
    });
    return () => {
      setUnauthorizedHandler(null);
      setSessionRefreshedHandler(null);
    };
  }, [logout, setSessionFromAuthResponse]);

  const login = useCallback(async (email, password) => {
    const authResponse = await authApi.login(email, password);
    return setSessionFromAuthResponse(authResponse);
  }, [setSessionFromAuthResponse]);

  const value = {
    token,
    user: session?.user || null,
    roleAssignment: session?.roleAssignment || null,
    isAuthenticated: Boolean(token),
    login,
    setSessionFromAuthResponse,
    logout,
    refreshSession,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return ctx;
}
