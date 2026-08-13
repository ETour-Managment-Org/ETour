import { Link } from 'react-router-dom'

export default function Breadcrumb({ items = [], trail = [], current }) {
  const resolved = items.length
    ? items
    : [
        { label: 'Home', to: '/' },
        ...trail.map((t) => ({ label: t.categoryName, to: `/categories/${t.categoryId}` })),
        ...(current ? [{ label: current }] : [])
      ]

  return (
    <nav className="breadcrumb no-print" aria-label="Breadcrumb">
      {resolved.map((item, i) => {
        const last = i === resolved.length - 1
        return (
          <span key={`${item.label}-${i}`} className="center" style={{ gap: 8 }}>
            {i > 0 && <span className="sep">/</span>}
            {last || !item.to ? (
              <span className="crumb-current" aria-current={last ? 'page' : undefined}>{item.label}</span>
            ) : (
              <Link to={item.to}>{item.label}</Link>
            )}
          </span>
        )
      })}
    </nav>
  )
}
