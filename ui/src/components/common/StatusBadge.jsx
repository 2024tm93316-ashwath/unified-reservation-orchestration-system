import { statusClass } from '../../utils/formatters';

export default function StatusBadge({ status }) {
  if (!status) return null;
  return (
    <span className={`badge ${statusClass(status)}`}>
      {status}
    </span>
  );
}
