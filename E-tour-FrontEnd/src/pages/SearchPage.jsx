import { useState } from 'react'
import { tourApi } from '../api/tourApi.js'
import TourCard from '../components/TourCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'

const blank = { startDate: '', endDate: '', minPrice: '', maxPrice: '', minDuration: '', maxDuration: '' }

export default function SearchPage() {
  const [form, setForm] = useState(blank)
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const run = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      setResults(await tourApi.search(form))
    } catch (err) {
      setError(err)
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  const reset = () => {
    setForm(blank)
    setResults(null)
    setError(null)
  }

  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Search' }]} />
      <h1 className="mb8">Search tours</h1>
      <p className="muted mb24">Filter by travel period, price band or duration. All fields optional.</p>

      <form className="card card-pad mb24" onSubmit={run}>
        <div className="grid grid-3">
          <div className="field">
            <label className="label">Departure from</label>
            <input type="date" className="input" value={form.startDate} onChange={set('startDate')} />
          </div>
          <div className="field">
            <label className="label">Departure to</label>
            <input type="date" className="input" value={form.endDate} onChange={set('endDate')} />
          </div>
          <div className="field">
            <label className="label">Min price</label>
            <input type="number" className="input" placeholder="20000" value={form.minPrice} onChange={set('minPrice')} />
          </div>
          <div className="field">
            <label className="label">Max price</label>
            <input type="number" className="input" placeholder="50000" value={form.maxPrice} onChange={set('maxPrice')} />
          </div>
          <div className="field">
            <label className="label">Min days</label>
            <input type="number" className="input" placeholder="4" value={form.minDuration} onChange={set('minDuration')} />
          </div>
          <div className="field">
            <label className="label">Max days</label>
            <input type="number" className="input" placeholder="10" value={form.maxDuration} onChange={set('maxDuration')} />
          </div>
        </div>
        <div className="row mt8">
          <button className="btn" type="submit" disabled={loading}>Search</button>
          <button className="btn btn-ghost" type="button" onClick={reset}>Reset</button>
        </div>
      </form>

      <ErrorBox error={error} />
      {loading && <Loader />}

      {results !== null && !loading && (
        <>
          <h3 className="mb16">{results.length} tour{results.length === 1 ? '' : 's'} found</h3>
          {results.length === 0 ? (
            <div className="empty">Nothing matched those filters.</div>
          ) : (
            <div className="grid grid-4">
              {results.map((t) => <TourCard key={t.tourId} tour={t} />)}
            </div>
          )}
        </>
      )}
    </div>
  )
}
