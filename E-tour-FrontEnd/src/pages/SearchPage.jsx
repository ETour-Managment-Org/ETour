import { useEffect, useState } from 'react'
import { tourApi } from '../api/tourApi.js'
import TourCard from '../components/TourCard.jsx'
import Breadcrumb from '../components/Breadcrumb.jsx'
import Loader, { EmptyState } from '../components/Loader.jsx'
import ErrorBox from '../components/ErrorBox.jsx'

const blank = { city: '', startDate: '', endDate: '', minPrice: '', maxPrice: '', minDuration: '', maxDuration: '' }

const POPULAR = ['Manali', 'Shimla', 'Leh', 'Goa', 'Munnar', 'Jaipur', 'Srinagar', 'Dubai']

export default function SearchPage() {
  const [form, setForm] = useState(blank)
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [cities, setCities] = useState([])

  useEffect(() => {
    tourApi.cities().then(setCities).catch(() => setCities([]))
  }, [])

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const quickCity = (name) => {
    const next = { ...blank, city: name }
    setForm(next)
    setLoading(true)
    setError(null)
    tourApi.search(next).then(setResults).catch((err) => { setError(err); setResults([]) })
      .finally(() => setLoading(false))
  }

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
      <p className="muted mb24">Search by city, travel period, price or duration. All fields are optional.</p>

      <form className="card card-pad mb24" onSubmit={run}>
        <div className="field">
          <label className="label">City or place</label>
          <input
            className="input"
            list="city-options"
            placeholder="Manali, Goa, Munnar..."
            value={form.city}
            onChange={set('city')}
          />
          <datalist id="city-options">
            {cities.map((c) => <option key={c} value={c} />)}
          </datalist>
          <div className="hint">
            Matches any stop on the route. Searching Manali finds both
            "Manali - Shimla" and "Leh - Manali - Chandigarh".
          </div>
        </div>

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

      {cities.length > 0 && (
        <div className="mb24">
          <div className="small muted mb8">Popular places</div>
          <div className="center wrap" style={{ gap: 8 }}>
            {POPULAR.filter((c) => cities.includes(c)).map((c) => (
              <button key={c} type="button" className="btn btn-ghost btn-sm"
                      onClick={() => quickCity(c)}>
                {c}
              </button>
            ))}
          </div>
        </div>
      )}

      <ErrorBox error={error} />
      {loading && <Loader full message="Searching…" />}

      {results !== null && !loading && (
        <>
          <h3 className="mb16">{results.length} tour{results.length === 1 ? '' : 's'} found</h3>
          {results.length === 0 ? (
            <EmptyState title="Nothing matched those filters" />
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
