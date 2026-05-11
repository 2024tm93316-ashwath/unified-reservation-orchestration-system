import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Layers, User, ShieldCheck } from 'lucide-react';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [role, setRole] = useState('user');
  const [userId, setUserId] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!userId.trim() || !displayName.trim()) {
      toast.error('Please fill in all fields');
      return;
    }
    setLoading(true);
    await new Promise((r) => setTimeout(r, 600)); // Simulate auth
    login(role, userId.trim(), displayName.trim());
    toast.success(`Welcome, ${displayName}!`);
    navigate(role === 'admin' ? '/admin/dashboard' : '/user/dashboard');
    setLoading(false);
  };

  return (
    <div className="login-page">
      <div className="login-card animate-fade-in">
        {/* Logo */}
        <div className="login-logo">
          <div className="login-logo__icon"><Layers size={22} /></div>
          <div className="login-logo__brand">
            U<span>ROS</span>
          </div>
        </div>
        <p className="login-tagline">
          Unified Reservation Orchestration System
        </p>

        {/* Role tabs */}
        <div className="login-role-tabs">
          <button
            type="button"
            className={`login-role-tab${role === 'user' ? ' active' : ''}`}
            onClick={() => setRole('user')}
          >
            <User size={14} />
            End User
          </button>
          <button
            type="button"
            className={`login-role-tab${role === 'admin' ? ' active' : ''}`}
            onClick={() => setRole('admin')}
          >
            <ShieldCheck size={14} />
            Admin
          </button>
        </div>

        {/* Form */}
        <h2 className="login-form-title">
          {role === 'admin' ? 'Admin Sign In' : 'Sign In'}
        </h2>
        <p className="login-form-sub">
          {role === 'admin'
            ? 'Manage resources, reservations and policies'
            : 'Browse and book resources seamlessly'}
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-row cols-1" style={{ marginBottom: 'var(--space-4)' }}>
            <div className="form-group">
              <label className="form-label">
                Display Name <span>*</span>
              </label>
              <input
                className="form-control"
                placeholder="e.g. Ashwath Kumar"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">
                {role === 'admin' ? 'Admin ID' : 'User ID'} <span>*</span>
              </label>
              <input
                className="form-control"
                placeholder={role === 'admin' ? 'admin01' : 'user123'}
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-lg"
            style={{ width: '100%', marginTop: 'var(--space-2)' }}
            disabled={loading}
          >
            {loading ? (
              <><div className="spinner spinner-sm" />  Signing in…</>
            ) : (
              `Continue as ${role === 'admin' ? 'Admin' : 'User'}`
            )}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 'var(--space-5)', fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)' }}>
          Demo system — no password required. Enter any user ID to proceed.
        </p>
      </div>
    </div>
  );
}
