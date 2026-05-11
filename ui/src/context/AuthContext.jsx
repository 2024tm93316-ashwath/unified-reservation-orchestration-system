import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(sessionStorage.getItem('uros_user')) || null;
    } catch {
      return null;
    }
  });

  const login = (role, userId, displayName) => {
    const u = { role, userId, displayName };
    sessionStorage.setItem('uros_user', JSON.stringify(u));
    setUser(u);
  };

  const logout = () => {
    sessionStorage.removeItem('uros_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin: user?.role === 'admin' }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
