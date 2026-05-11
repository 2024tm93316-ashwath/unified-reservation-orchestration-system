import { Search } from 'lucide-react';

export default function DataTable({
  columns, data, loading, emptyMessage = 'No data found',
  onSearch, searchPlaceholder = 'Search…',
}) {
  return (
    <div>
      {onSearch && (
        <div className="search-box" style={{ marginBottom: 'var(--space-4)', maxWidth: 320 }}>
          <Search size={15} className="search-box__icon" />
          <input
            className="form-control"
            placeholder={searchPlaceholder}
            onChange={(e) => onSearch(e.target.value)}
          />
        </div>
      )}
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              {columns.map((col) => (
                <th key={col.key} style={{ width: col.width }}>
                  {col.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={columns.length} style={{ textAlign: 'center', padding: '2rem' }}>
                  <div className="loading-screen" style={{ height: 'auto' }}>
                    <div className="spinner" />
                    <span>Loading…</span>
                  </div>
                </td>
              </tr>
            ) : !data?.length ? (
              <tr>
                <td colSpan={columns.length}>
                  <div className="empty-state">
                    <div className="empty-state__title">{emptyMessage}</div>
                  </div>
                </td>
              </tr>
            ) : (
              data.map((row, i) => (
                <tr key={row.id ?? i}>
                  {columns.map((col) => (
                    <td key={col.key}>
                      {col.render ? col.render(row[col.key], row) : row[col.key] ?? '—'}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
