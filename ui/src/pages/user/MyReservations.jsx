import { useState } from 'react';
import TopBar from '../../components/common/TopBar';
import StatusBadge from '../../components/common/StatusBadge';
import { useAuth } from '../../context/AuthContext';
import { getReservationsByUser, confirmReservation, cancelReservation } from '../../api/reservationApi';
import { useApi } from '../../hooks/useApi';
import { formatDateTime, typeLabel, typeIcon } from '../../utils/formatters';
import { CheckCircle2, XCircle, RefreshCw, CalendarCheck, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

const FILTERS = ['ALL', 'HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED'];

export default function MyReservations() {
  const { user } = useAuth();
  const { data, loading, refetch } = useApi(
    () => getReservationsByUser(user.userId),
    [user.userId]
  );
  const [filter, setFilter] = useState('ALL');

  const reservations = Array.isArray(data) ? data : [];
  const filtered = reservations.filter((r) => filter === 'ALL' || r.status === filter);

  const handleConfirm = async (id) => {
    try {
      await confirmReservation(id);
      toast.success(`Reservation #${id} confirmed!`);
      refetch();
    } catch (e) { toast.error(e.message); }
  };

  const handleCancel = async (id) => {
    if (!confirm(`Cancel reservation #${id}?`)) return;
    try {
      await cancelReservation(id);
      toast.success(`Reservation #${id} cancelled`);
      refetch();
    } catch (e) { toast.error(e.message); }
  };

  return (
    <div className="page-wrapper">
      <TopBar
        title="My Reservations"
        subtitle={`All reservations for ${user.displayName}`}
        actions={
          <button className="btn btn-secondary btn-sm" onClick={refetch}>
            <RefreshCw size={14} /> Refresh
          </button>
        }
      />
      <div className="app-content animate-fade-in">
        <div className="pill-filters" style={{ marginBottom: 'var(--space-5)' }}>
          {FILTERS.map((f) => (
            <button key={f} className={`pill-filter${filter === f ? ' active' : ''}`}
              onClick={() => setFilter(f)}>{f}</button>
          ))}
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner spinner-lg" /><span>Loading reservations…</span></div>
        ) : filtered.length === 0 ? (
          <div className="card">
            <div className="empty-state">
              <div className="empty-state__icon"><CalendarCheck size={24} /></div>
              <div className="empty-state__title">No reservations found</div>
              <div className="empty-state__desc">
                {filter === 'ALL'
                  ? 'You haven\'t made any reservations yet.'
                  : `No ${filter.toLowerCase()} reservations.`}
              </div>
            </div>
          </div>
        ) : (
          <div className="reservation-list">
            {filtered.map((r) => (
              <div className="reservation-item" key={r.id}>
                <div>
                  <div className="flex items-center gap-3">
                    <span style={{ fontSize: '1.5rem' }}>{typeIcon(r.reservationType)}</span>
                    <div>
                      <div className="font-semibold" style={{ marginBottom: 2 }}>
                        {r.resource?.name ?? `Reservation #${r.id}`}
                      </div>
                      <div className="reservation-item__meta">
                        <span className="reservation-item__meta-item">
                          <CalendarCheck size={11} /> {typeLabel(r.reservationType)}
                        </span>
                        {r.quantity && (
                          <span className="reservation-item__meta-item">Qty: {r.quantity}</span>
                        )}
                        <span className="reservation-item__meta-item">
                          <Clock size={11} /> {formatDateTime(r.createdAt)}
                        </span>
                        {r.holdExpiresAt && r.status === 'HELD' && (
                          <span className="reservation-item__meta-item" style={{ color: 'var(--color-warning)' }}>
                            ⏱ Expires: {formatDateTime(r.holdExpiresAt)}
                          </span>
                        )}
                        {r.startTime && (
                          <span className="reservation-item__meta-item">
                            From: {formatDateTime(r.startTime)} → {formatDateTime(r.endTime)}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="flex flex-col items-center gap-2">
                  <StatusBadge status={r.status} />
                  <span style={{ fontSize: '10px', color: 'var(--color-text-muted)' }}>#{r.id}</span>
                  <div className="flex gap-2">
                    {r.status === 'HELD' && (
                      <button className="btn btn-primary btn-sm" onClick={() => handleConfirm(r.id)}>
                        <CheckCircle2 size={12} /> Confirm
                      </button>
                    )}
                    {(r.status === 'HELD' || r.status === 'CONFIRMED') && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleCancel(r.id)}>
                        <XCircle size={12} /> Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
