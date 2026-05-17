import TopBar from '../../components/common/TopBar';
import { getResourceUtilization } from '../../api/dashboardApi';
import { useApi } from '../../hooks/useApi';
import { utilizationColor } from '../../utils/formatters';
import { RadialBarChart, RadialBar, Tooltip, ResponsiveContainer, Legend } from 'recharts';

export default function UtilizationPage() {
  const { data, loading } = useApi(getResourceUtilization);
  const list = Array.isArray(data) ? data : [];

  return (
    <div className="page-wrapper">
      <TopBar title="Utilization" subtitle="Resource usage and capacity analytics" />
      <div className="app-content animate-fade-in">
        {loading ? (
          <div className="loading-screen"><div className="spinner spinner-lg" /></div>
        ) : list.length === 0 ? (
          <div className="card">
            <div className="empty-state">
              <div className="empty-state__title">No utilization data</div>
              <div className="empty-state__desc">Create resources and reservations to view utilization metrics</div>
            </div>
          </div>
        ) : (
          <div className="grid-2">
            {list.map((u) => {
              const pct = Math.round(u.utilizationPercentage ?? 0);
              const color = utilizationColor(pct);
              return (
                <div className="card" key={u.resourceId}>
                  <div className="card__header">
                    <div>
                      <div className="card__title">{u.resourceName}</div>
                      <div className="card__subtitle">
                        {u.activeReservations ?? 0} active · {u.totalCapacity} total capacity
                      </div>
                    </div>
                    <span style={{
                      fontSize: '1.5rem', fontWeight: 800, color,
                    }}>{pct}%</span>
                  </div>
                  <div className="util-bar">
                    <div className="util-bar__fill" style={{ width: `${pct}%`, background: color }} />
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 'var(--space-3)', fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)' }}>
                    <span>{u.activeReservations} active reservations</span>
                    <span>{u.totalCapacity - (u.activeReservations ?? 0)} remaining</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
