// Date formatting
export const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
};

export const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

export const formatTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleTimeString('en-IN', {
    hour: '2-digit', minute: '2-digit',
  });
};

export const timeUntil = (expiryStr) => {
  const diff = new Date(expiryStr) - Date.now();
  if (diff <= 0) return 'Expired';
  const mins = Math.floor(diff / 60000);
  const secs = Math.floor((diff % 60000) / 1000);
  return `${mins}m ${secs}s`;
};

// Status display
export const statusClass = (status) => {
  const map = {
    HELD: 'badge-held',
    CONFIRMED: 'badge-confirmed',
    CANCELLED: 'badge-cancelled',
    EXPIRED: 'badge-expired',
  };
  return map[status] || 'badge-info';
};

// Reservation type icon mapping
export const typeIcon = (type) => {
  const map = {
    TIME_BASED: '🕐',
    RESOURCE_BASED: '🏠',
    SEAT_BASED: '🎭',
    QUOTA_BASED: '🚂',
    CAPACITY_BASED: '🎪',
  };
  return map[type] || '📋';
};

export const typeLabel = (type) => {
  const map = {
    TIME_BASED: 'Time-Based',
    RESOURCE_BASED: 'Resource-Based',
    SEAT_BASED: 'Seat-Based',
    QUOTA_BASED: 'Quota-Based',
    CAPACITY_BASED: 'Capacity-Based',
  };
  return map[type] || type;
};

export const typeColor = (type) => {
  const map = {
    TIME_BASED: '#6366f1',
    RESOURCE_BASED: '#0ea5e9',
    SEAT_BASED: '#10b981',
    QUOTA_BASED: '#f59e0b',
    CAPACITY_BASED: '#8b5cf6',
  };
  return map[type] || '#6b7280';
};

export const typeBg = (type) => {
  const map = {
    TIME_BASED: 'rgba(99,102,241,0.12)',
    RESOURCE_BASED: 'rgba(14,165,233,0.12)',
    SEAT_BASED: 'rgba(16,185,129,0.12)',
    QUOTA_BASED: 'rgba(245,158,11,0.12)',
    CAPACITY_BASED: 'rgba(139,92,246,0.12)',
  };
  return map[type] || 'rgba(107,114,128,0.12)';
};

// Truncate
export const truncate = (str, n = 30) =>
  str?.length > n ? str.slice(0, n) + '…' : str;

// Percentage bar colour
export const utilizationColor = (pct) => {
  if (pct >= 90) return '#ef4444';
  if (pct >= 70) return '#f59e0b';
  return '#10b981';
};
