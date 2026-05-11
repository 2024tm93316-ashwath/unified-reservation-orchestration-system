export default function TopBar({ title, subtitle, actions }) {
  return (
    <header className="topbar">
      <div className="topbar__left">
        <div>
          <div className="topbar__title">{title}</div>
          {subtitle && (
            <div className="topbar__breadcrumb">{subtitle}</div>
          )}
        </div>
      </div>
      {actions && <div className="topbar__right">{actions}</div>}
    </header>
  );
}
