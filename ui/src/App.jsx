import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Sidebar from './components/common/Sidebar';

// Auth
import LoginPage from './pages/auth/LoginPage';

// Admin
import AdminDashboard from './pages/admin/AdminDashboard';
import ResourcesPage from './pages/admin/ResourcesPage';
import ReservationsPage from './pages/admin/ReservationsPage';
import PoliciesPage from './pages/admin/PoliciesPage';
import UtilizationPage from './pages/admin/UtilizationPage';

// User
import UserDashboard from './pages/user/UserDashboard';
import BrowseCatalogue from './pages/user/BrowseCatalogue';
import BookingFlow from './pages/user/BookingFlow';
import MyReservations from './pages/user/MyReservations';

function ProtectedRoute({ children, requiredRole }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to={user.role === 'admin' ? '/admin/dashboard' : '/user/dashboard'} replace />;
  }
  return children;
}

function AppLayout({ children }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="app-main">
        {children}
      </main>
    </div>
  );
}

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      {/* Login */}
      <Route path="/login" element={
        user ? <Navigate to={user.role === 'admin' ? '/admin/dashboard' : '/user/dashboard'} replace /> : <LoginPage />
      } />

      {/* Admin Routes */}
      <Route path="/admin/*" element={
        <ProtectedRoute requiredRole="admin">
          <AppLayout>
            <Routes>
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="resources" element={<ResourcesPage />} />
              <Route path="reservations" element={<ReservationsPage />} />
              <Route path="policies" element={<PoliciesPage />} />
              <Route path="utilization" element={<UtilizationPage />} />
              <Route path="*" element={<Navigate to="dashboard" replace />} />
            </Routes>
          </AppLayout>
        </ProtectedRoute>
      } />

      {/* User Routes */}
      <Route path="/user/*" element={
        <ProtectedRoute requiredRole="user">
          <AppLayout>
            <Routes>
              <Route path="dashboard" element={<UserDashboard />} />
              <Route path="catalogue" element={<BrowseCatalogue />} />
              <Route path="book" element={<BookingFlow />} />
              <Route path="reservations" element={<MyReservations />} />
              <Route path="*" element={<Navigate to="dashboard" replace />} />
            </Routes>
          </AppLayout>
        </ProtectedRoute>
      } />

      {/* Default redirect */}
      <Route path="/" element={
        user
          ? <Navigate to={user.role === 'admin' ? '/admin/dashboard' : '/user/dashboard'} replace />
          : <Navigate to="/login" replace />
      } />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
