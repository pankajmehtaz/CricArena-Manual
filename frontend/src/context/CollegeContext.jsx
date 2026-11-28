import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import { collegeApi } from '../services/apiClient';

const SELECTED_COLLEGE_STORAGE_KEY = 'cricarena_selected_college_id';

const CollegeContext = createContext(null);

export function CollegeProvider({ children }) {
  const { user, roleAssignment } = useAuth();
  const [colleges, setColleges] = useState([]);
  const [loadingColleges, setLoadingColleges] = useState(true);
  const [selectedCollegeId, setSelectedCollegeIdState] = useState(() => {
    return localStorage.getItem(SELECTED_COLLEGE_STORAGE_KEY) || '';
  });

  // Reset stored college choice whenever authenticated user changes (unless role locks it)
  useEffect(() => {
    if (!user?.id) {
      setSelectedCollegeIdState('');
      localStorage.removeItem(SELECTED_COLLEGE_STORAGE_KEY);
      return;
    }

    if (!roleAssignment?.collegeId) {
      const savedForUserKey = `${SELECTED_COLLEGE_STORAGE_KEY}_u${user.id}`;
      const saved = localStorage.getItem(savedForUserKey) || '';
      setSelectedCollegeIdState(saved);
      if (saved) {
        localStorage.setItem(SELECTED_COLLEGE_STORAGE_KEY, saved);
      } else {
        localStorage.removeItem(SELECTED_COLLEGE_STORAGE_KEY);
      }
    }
  }, [user?.id, roleAssignment?.collegeId]);

  const loadColleges = useCallback(async () => {
    setLoadingColleges(true);
    try {
      const list = await collegeApi.getActive();
      const safeList = Array.isArray(list) ? list : [];
      setColleges(safeList);

      // Auto-select and lock college ONLY if user has a role tied to a specific college
      if (roleAssignment?.collegeId) {
        const roleCollegeStr = String(roleAssignment.collegeId);
        setSelectedCollegeIdState(roleCollegeStr);
        localStorage.setItem(SELECTED_COLLEGE_STORAGE_KEY, roleCollegeStr);
      }
    } catch {
      setColleges([]);
    } finally {
      setLoadingColleges(false);
    }
  }, [roleAssignment?.collegeId]);

  useEffect(() => {
    loadColleges();
  }, [loadColleges]);

  // Keep selectedCollegeId locked to roleAssignment.collegeId if college admin / captain
  useEffect(() => {
    if (roleAssignment?.collegeId) {
      const lockedCollege = String(roleAssignment.collegeId);
      if (selectedCollegeId !== lockedCollege) {
        setSelectedCollegeIdState(lockedCollege);
        localStorage.setItem(SELECTED_COLLEGE_STORAGE_KEY, lockedCollege);
      }
    }
  }, [roleAssignment, selectedCollegeId]);

  const setSelectedCollegeId = useCallback(
    (id, explicitUserId = null) => {
      const str = id ? String(id) : '';
      setSelectedCollegeIdState(str);
      const targetUserId = explicitUserId || user?.id;
      const userKey = targetUserId ? `${SELECTED_COLLEGE_STORAGE_KEY}_u${targetUserId}` : null;
      if (str) {
        localStorage.setItem(SELECTED_COLLEGE_STORAGE_KEY, str);
        if (userKey) localStorage.setItem(userKey, str);
      } else {
        localStorage.removeItem(SELECTED_COLLEGE_STORAGE_KEY);
        if (userKey) localStorage.removeItem(userKey);
      }
    },
    [user?.id]
  );

  const selectedCollege = useMemo(() => {
    if (!selectedCollegeId) return null;
    return colleges.find((c) => String(c.id) === String(selectedCollegeId)) || null;
  }, [colleges, selectedCollegeId]);

  const isPlatformAdmin = roleAssignment?.role === 'PLATFORM_ADMIN';
  const isCollegeLocked =
    Boolean(roleAssignment?.collegeId) ||
    (!isPlatformAdmin && Boolean(user?.id) && Boolean(selectedCollegeId));

  const value = {
    colleges,
    selectedCollegeId,
    selectedCollege,
    setSelectedCollegeId,
    loadingColleges,
    isCollegeLocked,
    refreshColleges: loadColleges,
  };

  return <CollegeContext.Provider value={value}>{children}</CollegeContext.Provider>;
}

export function useCollege() {
  const ctx = useContext(CollegeContext);
  if (!ctx) {
    throw new Error('useCollege must be used inside CollegeProvider');
  }
  return ctx;
}
