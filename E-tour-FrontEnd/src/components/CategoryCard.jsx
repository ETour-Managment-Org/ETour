import { useNavigate } from 'react-router-dom'
import { imageUrl, onImageError } from '../utils/media.js'

export default function CategoryCard({ node, showImage = true }) {
  const navigate = useNavigate()
  const jumps = node.nextAction === 'SHOW_TOURS'
  const img = showImage ? imageUrl(node.catImagePath) : null

  const go = () =>
    navigate(jumps ? `/tours?category=${node.categoryId}` : `/categories/${node.categoryId}`)

  return (
    <div
      className="tile"
      onClick={go}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && go()}
    >
      {img ? (
        <div className="tile-media">
          <img src={img} alt={node.categoryName} onError={onImageError} loading="lazy" />
        </div>
      ) : (
        <div className="tile-icon">{(node.catCode || node.categoryName || '?').slice(0, 2)}</div>
      )}

      <div className="between">
        <h4>{node.categoryName}</h4>
        <span className="badge badge-slate">{node.catCode}</span>
      </div>

      <div className="small muted" style={{ minHeight: 20 }}>{node.description || ''}</div>

      <div className="between">
        <span className="small muted">
          {jumps ? `${node.tourCount ?? 0} tours` : `${node.childCount ?? 0} sub-categories`}
        </span>
        <span className={jumps ? 'badge badge-primary' : 'badge badge-accent'}>
          {jumps ? 'View tours →' : 'Explore →'}
        </span>
      </div>
    </div>
  )
}
