import { useEffect, useState } from 'react';
import TopBar from '../../components/common/TopBar';
import StatCard from '../../components/common/StatCard';
import StatusBadge from '../../components/common/StatusBadge';
import { getDashboardStats, getResourceUtilization } from '../../api/dashboardApi';
import { getAllReservations } from '../../api/reservationApi';
import {
  CalendarCheck, Clock, XCircle, AlertCircle, BarChart3, TrendingUp
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';
import { formatDateTime, utilizationColor, typeLabel } from '../../utils/formatters';

const PIE_COLORS = ['#f59e0b', '#10b981', '#ef4444', '#9ca3af'];

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [util, setUtil] = useState([]);
  const [recent, setRecent] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getDashboardStats(),
      getResourceUtilization(),
      getAllReservations(),
    ]).then(([s, u, r]) => {
      setStats(s.data?.data ?? s.data);
      setUtil(u.data?.data ?? u.data ?? []);
      const all = r.data?.data ?? r.data ?? [];
      setRecent(Array.isArray(all) ? all.slice(0, 6) : []);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const pieData = stats ? [
    { name: 'Held', value: stats.activeHolds || 0 },
    { name: 'Confirmed', value: stats.confirmedBookings || 0 },
    { name: 'Cancelled', value: stats.cancelledBookings || 0 },
    { name: 'Expired', value: stats.expiredReservations || 0 },
  ] : [];

  const barData = stats?.last7DaysCounts
    ? Object.entries(stats.last7DaysCounts).map(([day, count]) => ({ day, count }))
    : [];

  return (
    <div className="page-wrapper">
      <TopBar title="Dashboard" subtitle="System overview and analytics" />
      <div className="app-content animate-fade-in">
        {loading ? (
          <div className="loading-screen"><div className="spinner spinner-lg" /><span>Loading analytics…</span></div>
        ) : (
          <>
            {/* KPI Cards */}
            <div className="stats-grid">
              <StatCard label="Total Reservations" value={stats?.totalReservations ?? 0}
                icon={CalendarCheck} color="#6366f1" bg="rgba(99,102,241,0.12)" />
              <StatCard label="Active Holds" value={stats?.activeHolds ?? 0}
                icon={Clock} color="#f59e0b" bg="rgba(245,158,11,0.12)"
                change="Temporary holds pending confirmation" />
              <StatCard label="Confirmed Bookings" value={stats?.confirmedBookings ?? 0}
                icon={TrendingUp} color="#10b981" bg="rgba(16,185,129,0.12)" />
              <StatCard label="Cancellations" value={stats?.cancelledBookings ?? 0}
                icon={XCircle} color="#ef4444" bg="rgba(239,68,68,0.12)" />
            </div>

            {/* Charts Row */}
            <div className="dashboard-grid">
              {/* Bar Chart */}
              <div className="card">
                <div className="card__header">
                  <div>
                    <div className="card__title">Reservations — Last 7 Days</div>
                    <div className="card__subtitle">Daily booking activity</div>
                  </div>
                  <BarChart3 size={18} style={{ color: 'var(--color-text-muted)' }} />
                </div>
                <div className="chart-container">
                  {barData.length ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={barData} barSize={28}>
                        <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border-light)" />
                        <XAxis dataKey="day" tick={{ fontSize: 11 }} />
                        <YAxis tick={{ fontSize: 11 }} />
                        <Tooltip />
                        <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} name="Reservations" />
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="empty-state"><div className="empty-state__title">No data yet</div></div>
                  )}
                </div>
              </div>

              {/* Pie Chart */}
              <div className="card">
                <div className="card__header">
                  <div>
                    <div className="card__title">Status Breakdown</div>
                    <div className="card__subtitle">Reservation distribution</div>
                  </div>
                </div>
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={pieData} cx="50%" cy="45%" innerRadius={55} outerRadius={90}
                        paddingAngle={3} dataKey="value">
                        {pieData.map((_, i) => (
                          <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize: 12 }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            {/* Utilization + Recent Reservations */}
            <div className="dashboard-grid mt-6">
              <div className="card">
                <div className="card__header">
                  <div className="card__title">Recent Reservations</div>
                </div>
                {recent.length === 0 ? (
                  <div className="empty-state"><div className="empty-state__title">No reservations yet</div></div>
                ) : (
                  <div className="table-wrap">
                    <table className="table">
                      <thead>
                        <tr>
                          <th>ID</th><th>User</th><th>Type</th><th>Status</th><th>Created</th>
                        </tr>
                      </thead>
                      <tbody>
                        {recent.map((r) => (
                          <tr key={r.id}>
                            <td>#{r.id}</td>
                            <td>{r.userId}</td>
                            <td><span className="badge badge-info">{typeLabel(r.reservationType)}</span></td>
                            <td><StatusBadge status={r.status} /></td>
                            <td style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-xs)' }}>
                              {formatDateTime(r.createdAt)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              <div className="card">
                <div className="card__header">
                  <div className="card__title">Resource Utilization</div>
                  <div className="card__subtitle">Current capacity usage</div>
                </div>
                {util.length === 0 ? (
                  <div className="empty-state"><div className="empty-state__title">No utilization data</div></div>
                ) : (
                  util.slice(0, 6).map((u) => {
                    const pct = Math.round(u.utilizationPercentage ?? 0);
                    return (
                      <div className="util-bar-wrap" key={u.resourceId}>
                        <div className="util-bar-label">
                          <span className="font-medium text-sm">{u.resourceName}</span>
                          <span className="text-sm text-muted">{pct}%</span>
                        </div>
                        <div className="util-bar">
                          <div className="util-bar__fill"
                            style={{ width: `${pct}%`, background: utilizationColor(pct) }} />
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
