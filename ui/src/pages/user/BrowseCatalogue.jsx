import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../../components/common/TopBar';
import { useApi } from '../../hooks/useApi';
import { getResources } from '../../api/resourceApi';
import { typeLabel, typeIcon, typeColor, typeBg } from '../../utils/formatters';
import { Search, Users, ArrowRight } from 'lucide-react';

const ALL_TYPES = ['ALL', 'TIME_BASED', 'RESOURCE_BASED', 'SEAT_BASED', 'QUOTA_BASED', 'CAPACITY_BASED'];

export default function BrowseCatalogue() {
  const { data, loading } = useApi(getResources);
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');

  const resources = Array.isArray(data) ? data.filter((r) => r.isActive) : [];
  const filtered = resources.filter((r) => {
    const matchType = typeFilter === 'ALL' || r.resourceType?.reservationType === typeFilter;
    const q = search.toLowerCase();
    const matchSearch = !q || r.name?.toLowerCase().includes(q) || r.resourceType?.name?.toLowerCase().includes(q);
    return matchType && matchSearch;
  });

  return (
    <div>
      <TopBar title="Resource Catalogue" subtitle="Browse all available resources" />
      <div className="app-content animate-fade-in">
        {/* Search + filters */}
        <div className="card" style={{ marginBottom: 'var(--space-5)', padding: 'var(--space-4) var(--space-5)' }}>
          <div className="flex items-center gap-4 flex-wrap">
            <div className="search-box flex-1" style={{ minWidth: 200 }}>
              <Search size={15} className="search-box__icon" />
              <input className="form-control" placeholder="Search resources…"
                value={search} onChange={(e) => setSearch(e.target.value)} />
            </div>
            <div className="pill-filters">
              {ALL_TYPES.map((t) => (
                <button key={t} className={`pill-filter${typeFilter === t ? ' active' : ''}`}
                  onClick={() => setTypeFilter(t)}>
                  {t === 'ALL' ? 'All Types' : `${typeIcon(t)} ${typeLabel(t)}`}
                </button>
              ))}
            </div>
          </div>
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner spinner-lg" /><span>Loading resources…</span></div>
        ) : filtered.length === 0 ? (
          <div className="card">
            <div className="empty-state">
              <div className="empty-state__icon"><Search size={22} /></div>
              <div className="empty-state__title">No resources found</div>
              <div className="empty-state__desc">Try adjusting your search or filters</div>
            </div>
          </div>
        ) : (
          <>
            <div style={{ marginBottom: 'var(--space-3)', fontSize: 'var(--font-size-sm)', color: 'var(--color-text-secondary)' }}>
              {filtered.length} resource{filtered.length !== 1 ? 's' : ''} found
            </div>
            <div className="grid-3">
              {filtered.map((res) => {
                const t = res.resourceType?.reservationType;
                return (
                  <div className="resource-card" key={res.id}
                    onClick={() => navigate('/user/book', { state: { resource: res } })}>
                    <div className="resource-card__icon"
                      style={{ background: typeBg(t), color: typeColor(t) }}>
                      <span style={{ fontSize: '1.4rem' }}>{typeIcon(t)}</span>
                    </div>
                    <div className="resource-card__name">{res.name}</div>
                    <div className="resource-card__type">
                      {typeLabel(t)}
                    </div>
                    {res.description && (
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)', marginTop: 'var(--space-2)' }}>
                        {res.description}
                      </div>
                    )}
                    <div className="resource-card__meta">
                      <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)' }}>
                        <Users size={11} /> Capacity: {res.totalCapacity}
                      </span>
                    </div>
                    <div style={{ marginTop: 'var(--space-4)', display: 'flex', justifyContent: 'flex-end' }}>
                      <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: 600, color: 'var(--color-accent)', display: 'flex', alignItems: 'center', gap: 4 }}>
                        Book Now <ArrowRight size={12} />
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
