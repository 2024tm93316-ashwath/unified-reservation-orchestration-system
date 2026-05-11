export default function StatCard({ label, value, icon: Icon, color, bg, change }) {
  return (
    <div className="stat-card">
      <div className="flex items-center justify-between">
        <div
          className="stat-card__icon"
          style={{ background: bg || 'var(--color-accent-light)', color: color || 'var(--color-accent)' }}
        >
          <Icon size={20} />
        </div>
      </div>
      <div>
        <div className="stat-card__label">{label}</div>
        <div className="stat-card__value">{value ?? '—'}</div>
        {change && <div className="stat-card__change">{change}</div>}
      </div>
      <div
        className="stat-card__bg"
        style={{ background: color || 'var(--color-accent)' }}
      />
    </div>
  );
}
