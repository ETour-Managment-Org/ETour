import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { categoryApi } from '../api/categoryApi.js'
import CategoryCard from '../components/CategoryCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'

export default function CategoryPage() {
  const { id } = useParams()
  const [children, setChildren] = useState([])
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

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: 'Browse', to: '/home' },
          ...parents.map((c) => ({ label: c.categoryName, to: `/categories/${c.categoryId}` })),
          { label: current?.categoryName || 'Categories' }
        ]}
      />
      <h1 className="mb8">{current?.categoryName || 'Categories'}</h1>
      <p className="muted mb24">Choose a sub-category to continue.</p>

      <ErrorBox error={error} />
      {loading ? (
        <Loader />
      ) : children.length === 0 ? (
        <div className="empty">No sub-categories here.</div>
      ) : (
        <div className="grid grid-3">
          {children.map((c) => <CategoryCard key={c.categoryId} node={c} />)}
        </div>
      )}
    </div>
  )
}
