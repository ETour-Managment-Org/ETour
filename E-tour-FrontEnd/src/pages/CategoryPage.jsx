import { useEffect, useState } from 'react'
import { useParams, Navigate } from 'react-router-dom'
import { categoryApi } from '../api/categoryApi.js'
import CategoryCard from '../components/CategoryCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function CategoryPage() {
  const { t, tc } = useI18n()
  const { id } = useParams()
  const [children, setChildren] = useState(null)
  const [trail, setTrail] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    Promise.all([categoryApi.children(id), categoryApi.breadcrumb(id)])
      .then(([kids, bc]) => {
        setChildren(kids || [])
        setTrail(bc || [])
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [id])

  const current = trail.length ? trail[trail.length - 1] : null
  const parents = trail.slice(0, -1)

  if (!loading && !error && children && children.length === 0) {
    return <Navigate to={`/tours?category=${id}`} replace />
  }

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: 'Browse', to: '/home' },
          ...parents.map((c) => ({ label: tc(c.categoryName), to: `/categories/${c.categoryId}` })),
          { label: tc(current?.categoryName) || t('nav.browse') }
        ]}
      />
      <h1 className="mb8">{tc(current?.categoryName) || t('nav.browse')}</h1>
      <p className="muted mb24">Choose a sub-category to continue.</p>

      <ErrorBox error={error} />
      {loading ? (
        <Loader full message="Loading categories…" />
      ) : (
        <div className="grid grid-3">
          {(children || []).map((c) => <CategoryCard key={c.categoryId} node={c} />)}
        </div>
      )}
    </div>
  )
}
