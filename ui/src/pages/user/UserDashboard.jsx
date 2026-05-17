import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../../components/common/TopBar';
import StatusBadge from '../../components/common/StatusBadge';
import { useAuth } from '../../context/AuthContext';
import { getReservationsByUser } from '../../api/reservationApi';
import { formatDateTime, typeLabel, typeIcon } from '../../utils/formatters';
import { CalendarCheck, Clock, CheckCircle, XCircle, ArrowRight } from 'lucide-react';

export default function UserDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getReservationsByUser(user.userId)
      .then((r) => setReservations(r.data?.data ?? r.data ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user.userId]);

  const active = reservations.filter((r) => r.status === 'HELD' || r.status === 'CONFIRMED');
  const counts = {
    held: reservations.filter((r) => r.status === 'HELD').length,
    confirmed: reservations.filter((r) => r.status === 'CONFIRMED').length,
    cancelled: reservations.filter((r) => r.status === 'CANCELLED').length,
    total: reservations.length,
  };

  return (
    <div className="page-wrapper">
      <TopBar title={`Welcome back, ${user.displayName}!`} subtitle="Here is a summary of your activity" />
      <div className="app-content animate-fade-in">
        {/* Quick Action Banner */}
        <div className="card" style={{
          background: 'linear-gradient(135deg, #6366f1 0%, #818cf8 100%)',
          border: 'none', marginBottom: 'var(--space-6)', cursor: 'pointer',
        }} onClick={() => navigate('/user/book')}>
          <div className="flex items-center justify-between">
            <div>
              <div style={{ color: 'rgba(255,255,255,0.8)', fontSize: 'var(--font-size-sm)', marginBottom: 4 }}>
                Ready to reserve?
              </div>
              <div style={{ color: '#fff', fontSize: 'var(--font-size-xl)', fontWeight: 700 }}>
                Book a Resource Now
              </div>
              <div style={{ color: 'rgba(255,255,255,0.7)', fontSize: 'var(--font-size-sm)', marginTop: 6 }}>
                Browse catalogue → Check availability → Confirm booking
              </div>
            </div>
            <div style={{
              width: 50, height: 50, background: 'rgba(255,255,255,0.2)', borderRadius: '50%',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <ArrowRight size={22} color="white" />
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="grid-4" style={{ marginBottom: 'var(--space-6)' }}>
          {[
            { label: 'Total Reservations', value: counts.total, icon: CalendarCheck, color: '#6366f1', bg: 'rgba(99,102,241,0.12)' },
            { label: 'Active Holds', value: counts.held, icon: Clock, color: '#f59e0b', bg: 'rgba(245,158,11,0.12)' },
            { label: 'Confirmed', value: counts.confirmed, icon: CheckCircle, color: '#10b981', bg: 'rgba(16,185,129,0.12)' },
            { label: 'Cancelled', value: counts.cancelled, icon: XCircle, color: '#ef4444', bg: 'rgba(239,68,68,0.12)' },
          ].map(({ label, value, icon: Icon, color, bg }) => (
            <div className="stat-card" key={label}>
              <div className="flex items-center justify-between">
                <div className="stat-card__icon" style={{ background: bg, color }}><Icon size={20} /></div>
              </div>
              <div>
                <div className="stat-card__label">{label}</div>
                <div className="stat-card__value">{value}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Active bookings */}
        <div className="card">
          <div className="card__header">
            <div>
              <div className="card__title">Active Reservations</div>
              <div className="card__subtitle">Holds and confirmed bookings</div>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/user/reservations')}>
              View All
            </button>
          </div>
          {loading ? (
            <div className="loading-screen" style={{ height: 100 }}><div className="spinner" /></div>
          ) : active.length === 0 ? (
            <div className="empty-state" style={{ padding: 'var(--space-8)' }}>
              <div className="empty-state__title">No active reservations</div>
              <div className="empty-state__desc">
                <button className="btn btn-primary btn-sm" onClick={() => navigate('/user/book')}>
                  Book your first resource
                </button>
              </div>
            </div>
          ) : (
            <div className="reservation-list">
              {active.slice(0, 5).map((r) => (
                <div className="reservation-item" key={r.id}>
                  <div>
                    <div className="flex items-center gap-3">
                      <span style={{ fontSize: '1.25rem' }}>{typeIcon(r.reservationType)}</span>
                      <div>
                        <div className="font-semibold">
                          {r.resource?.name ?? `Reservation #${r.id}`}
                        </div>
                        <div className="reservation-item__meta">
                          <span className="reservation-item__meta-item">
                            <CalendarCheck size={11} /> {typeLabel(r.reservationType)}
                          </span>
                          {r.quantity && (
                            <span className="reservation-item__meta-item">
                              Qty: {r.quantity}
                            </span>
                          )}
                          <span className="reservation-item__meta-item">
                            <Clock size={11} /> {formatDateTime(r.createdAt)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col items-center gap-2">
                    <StatusBadge status={r.status} />
                    <span style={{ fontSize: '10px', color: 'var(--color-text-muted)' }}>#{r.id}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
