export default function StarRating({ value = 0, count, size = 14 }) {
  const rounded = Math.round(value || 0)
  return (
    <span className="center" style={{ gap: 6 }}>
      <span className="stars" style={{ fontSize: size }}>
        {'★'.repeat(rounded)}
        <span style={{ color: 'var(--border)' }}>{'★'.repeat(5 - rounded)}</span>
      </span>
      {value ? <span className="small strong">{Number(value).toFixed(1)}</span> : null}
      {count !== undefined && count !== null ? <span className="small muted">({count})</span> : null}
    </span>
  )
}
