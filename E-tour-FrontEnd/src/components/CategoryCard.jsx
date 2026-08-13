import { useNavigate } from 'react-router-dom'
import { imageUrl, onImageError } from '../utils/media.js'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function CategoryCard({ node, showImage = true }) {
  const { t, tc } = useI18n()
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
          <img src={img} alt={tc(node.categoryName)} onError={onImageError} loading="lazy" />
        </div>
      ) : (
        <div className="tile-icon">{(node.catCode || node.categoryName || '?').slice(0, 2)}</div>
      )}

      <div className="between">
        <h4>{tc(node.categoryName)}</h4>
        <span className="badge badge-slate">{node.catCode}</span>
      </div>

      <div className="small muted" style={{ minHeight: 20 }}>{tc(node.description) || ''}</div>

      <div className="between">
        <span className="small muted">
          {jumps
            ? `${node.tourCount ?? 0} ${t('card.tours')}`
            : `${node.childCount ?? 0} ${t('card.subCategories')}`}
        </span>
        <span className={jumps ? 'badge badge-primary' : 'badge badge-accent'}>
          {jumps ? `${t('card.viewTours')} →` : `${t('card.explore')} →`}
        </span>
      </div>
    </div>
  )
}
