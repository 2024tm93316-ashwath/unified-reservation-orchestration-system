import { useState } from 'react';
import TopBar from '../../components/common/TopBar';
import DataTable from '../../components/common/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import { getAllReservations, cancelReservation } from '../../api/reservationApi';
import { useApi } from '../../hooks/useApi';
import { formatDateTime, typeLabel, typeIcon } from '../../utils/formatters';
import { XCircle, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';

const STATUS_FILTERS = ['ALL', 'HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED'];

export default function ReservationsPage() {
  const { data, loading, refetch } = useApi(getAllReservations);
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  const reservations = Array.isArray(data) ? data : [];
  const filtered = reservations.filter((r) => {
    const matchStatus = filter === 'ALL' || r.status === filter;
    const q = search.toLowerCase();
    const matchSearch = !q || r.userId?.toLowerCase().includes(q) || String(r.id).includes(q) || r.reservationType?.toLowerCase().includes(q);
    return matchStatus && matchSearch;
  });

  const handleCancel = async (id) => {
    if (!confirm(`Cancel reservation #${id}?`)) return;
    try {
      await cancelReservation(id);
      toast.success(`Reservation #${id} cancelled`);
      refetch();
    } catch (e) { toast.error(e.message); }
  };

  const columns = [
    { key: 'id', label: 'ID', width: 60, render: (v) => <span style={{ fontWeight: 600 }}>#{v}</span> },
    { key: 'userId', label: 'User' },
    { key: 'reservationType', label: 'Type', render: (v) => (
      <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: 500 }}>{typeIcon(v)} {typeLabel(v)}</span>
    )},
    { key: 'status', label: 'Status', render: (v) => <StatusBadge status={v} /> },
    { key: 'quantity', label: 'Qty', render: (v) => v ?? '—' },
    { key: 'createdAt', label: 'Created', render: (v) => (
      <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)' }}>{formatDateTime(v)}</span>
    )},
    { key: 'holdExpiresAt', label: 'Hold Expires', render: (v) => (
      <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)' }}>{formatDateTime(v)}</span>
    )},
    { key: 'id', label: 'Actions', render: (id, row) => (
      row.status === 'HELD' || row.status === 'CONFIRMED'
        ? <button className="btn btn-ghost btn-sm" style={{ color: 'var(--color-danger)' }}
            onClick={() => handleCancel(id)}>
            <XCircle size={13} /> Cancel
          </button>
        : null
    )},
  ];

  return (
    <div className="page-wrapper">
      <TopBar
        title="Reservations"
        subtitle="Monitor and manage all system reservations"
        actions={
          <button className="btn btn-secondary btn-sm" onClick={refetch}>
            <RefreshCw size={14} /> Refresh
          </button>
        }
      />
      <div className="app-content animate-fade-in">
        <div className="pill-filters" style={{ marginBottom: 'var(--space-5)' }}>
          {STATUS_FILTERS.map((s) => (
            <button key={s} className={`pill-filter${filter === s ? ' active' : ''}`}
              onClick={() => setFilter(s)}>{s}</button>
          ))}
        </div>
        <div className="card">
          <DataTable
            columns={columns}
            data={filtered}
            loading={loading}
            onSearch={setSearch}
            searchPlaceholder="Search by user, ID or type…"
            emptyMessage="No reservations match the current filter"
          />
        </div>
      </div>
    </div>
  );
}
