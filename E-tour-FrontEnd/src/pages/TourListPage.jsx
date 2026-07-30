import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { tourApi } from '../api/tourApi.js'
import { categoryApi } from '../api/categoryApi.js'
import TourCard from '../components/TourCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'

export default function TourListPage() {
  const [params] = useSearchParams()
  const categoryId = params.get('category')

  const [tours, setTours] = useState([])
  const [trail, setTrail] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    const jobs = categoryId
      ? [tourApi.byCategory(categoryId), categoryApi.breadcrumb(categoryId)]
      : [tourApi.all(), Promise.resolve([])]

    Promise.all(jobs)
      .then(([t, bc]) => {
        setTours(t || [])
        setTrail(bc || [])
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [categoryId])

  const current = trail.length ? trail[trail.length - 1] : null

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: 'Browse', to: '/home' },
          ...trail.slice(0, -1).map((c) => ({ label: c.categoryName, to: `/categories/${c.categoryId}` })),
          { label: current ? current.categoryName : 'All tours' }
        ]}
      />
      <h1 className="mb8">{current ? current.categoryName : 'All tours'}</h1>
      <p className="muted mb24">{tours.length} tour{tours.length === 1 ? '' : 's'} available</p>

      <ErrorBox error={error} />
      {loading ? (
        <Loader />
      ) : tours.length === 0 ? (
        <div className="empty">No tours here yet.</div>
      ) : (
        <div className="grid grid-4">
          {tours.map((t) => <TourCard key={t.tourId} tour={t} />)}
        </div>
      )}
    </div>
  )
}
