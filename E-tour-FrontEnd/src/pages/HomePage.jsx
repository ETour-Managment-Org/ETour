import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { categoryApi } from '../api/categoryApi.js'
import { tourApi } from '../api/tourApi.js'
import CategoryCard from '../components/CategoryCard.jsx'
import TourCard from '../components/TourCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function HomePage() {
  const { t } = useI18n()
  const [roots, setRoots] = useState([])
  const [tours, setTours] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([categoryApi.roots(), tourApi.all()])
      .then(([r, t]) => {
        setRoots(r || [])
        setTours((t || []).slice(0, 8))
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="container page">
      <Breadcrumb items={[{ label: t('crumb.home'), to: '/' }, { label: t('nav.browse') }]} />

      <div className="section-head">
        <span className="eyebrow">{t('home.step1')}</span>
        <h1>{t('home.browseTitle')}</h1>
        <p className="muted">
          {t('home.browseSub')}
        </p>
      </div>

      <ErrorBox error={error} />
      {loading ? <Loader /> : (
        <>
          <div className="grid grid-4 mb32">
            {roots.map((c) => <CategoryCard key={c.categoryId} node={c} />)}
          </div>

          <div className="between mb16">
            <h2>{t('home.popular')}</h2>
            <Link to="/tours" className="small strong" style={{ color: 'var(--primary)' }}>{t('home.viewAll')} →</Link>
          </div>
          <div className="grid grid-4">
            {tours.map((t) => <TourCard key={t.tourId} tour={t} />)}
          </div>
        </>
      )}
    </div>
  )
}
