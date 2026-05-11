import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard, Package, CalendarCheck, ClipboardList,
  Settings, BarChart3, LogOut, Layers, Users,
} from 'lucide-react';

const adminNav = [
  { label: 'Overview', to: '/admin/dashboard', icon: LayoutDashboard },
  { label: 'Resources', to: '/admin/resources', icon: Package },
  { label: 'Reservations', to: '/admin/reservations', icon: CalendarCheck },
  { label: 'Policies', to: '/admin/policies', icon: Settings },
  { label: 'Utilization', to: '/admin/utilization', icon: BarChart3 },
];

const userNav = [
  { label: 'Dashboard', to: '/user/dashboard', icon: LayoutDashboard },
  { label: 'Browse Catalogue', to: '/user/catalogue', icon: Layers },
  { label: 'Book Resource', to: '/user/book', icon: CalendarCheck },
  { label: 'My Reservations', to: '/user/reservations', icon: ClipboardList },
];

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const navItems = isAdmin ? adminNav : userNav;

  const handleLogout = () => { logout(); navigate('/login'); };

  const initials = user?.displayName
    ?.split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || 'U';

  return (
    <aside className="sidebar">
      <div className="sidebar__logo">
        <div className="sidebar__logo-icon">
          <Layers size={18} />
        </div>
        <div>
          <div className="sidebar__logo-text">UROS</div>
          <div className="sidebar__logo-sub">
            {isAdmin ? 'Admin Portal' : 'User Portal'}
          </div>
        </div>
      </div>

      <nav className="sidebar__section">
        <div className="sidebar__section-label">
          {isAdmin ? 'Administration' : 'Navigation'}
        </div>
        {navItems.map(({ label, to, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `sidebar__item${isActive ? ' active' : ''}`
            }
          >
            <Icon size={17} className="sidebar__item-icon" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar__footer">
        <div className="sidebar__user">
          <div
            className="avatar"
            style={{
              background: isAdmin
                ? 'rgba(99,102,241,0.2)'
                : 'rgba(14,165,233,0.2)',
              color: isAdmin ? '#6366f1' : '#0ea5e9',
            }}
          >
            {initials}
          </div>
          <div className="sidebar__user-info">
            <div className="sidebar__user-name">{user?.displayName}</div>
            <div className="sidebar__user-role">
              {isAdmin ? 'Administrator' : `ID: ${user?.userId}`}
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="btn btn-ghost btn-icon"
            title="Logout"
            style={{ color: 'var(--color-sidebar-text)' }}
          >
            <LogOut size={15} />
          </button>
        </div>
      </div>
    </aside>
  );
}
