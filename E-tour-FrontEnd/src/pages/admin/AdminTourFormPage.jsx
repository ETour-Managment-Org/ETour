import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { adminApi } from '../../api/adminApi.js'
import { categoryApi } from '../../api/categoryApi.js'
import Loader from '../../components/Loader.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { imageUrl, onImageError, FALLBACK_IMAGE } from '../../utils/media.js'

const emptyForm = {
  tourName: '', destination: '', days: '', nights: '', description: '',
  price: '', location: '', tourType: 'DOMESTIC', categoryId: '',
  stayAndMeals: '', addOns: '', passportAndVisa: '', weather: '', doAndDont: '',
  primaryImageUrl: ''
}

const emptyFares = {
  costId: null,
  adultPrice: '', singlePersonPrice: '', extraPersonPrice: '',
  childWithBedPrice: '', childWithoutBedPrice: '',
  validFrom: '', validTo: ''
}

const FARE_BANDS = [
  { key: 'adultPrice', label: 'Twin sharing', ratio: 1,
    hint: 'The headline per-person rate. Two adults to a room.' },
  { key: 'singlePersonPrice', label: 'Single occupancy', ratio: 1.45,
    hint: 'One adult with the room to themselves.' },
  { key: 'extraPersonPrice', label: 'Extra person', ratio: 0.85,
    hint: 'A third adult on an extra bed in an existing room.' },
  { key: 'childWithBedPrice', label: 'Child with bed', ratio: 0.75,
    hint: 'Under 12 on the departure date, with their own bed.' },
  { key: 'childWithoutBedPrice', label: 'Child without bed', ratio: 0.55,
    hint: 'Under 12 on the departure date, sharing the parents bed.' }
]

const uniqueKey = (prefix) =>
  `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`

const newDay = (n) => ({
  key: `new-${Date.now()}-${n}`,
  itineraryId: null,
  dayNumber: n,
  description: '',
  location: ''
})

const newDeparture = () => ({
  key: uniqueKey('new'),
  scheduleId: null,
  startDate: '',
  totalSeats: 30,
  availableSeats: '',
  status: 'OPEN'
})

export default function AdminTourFormPage() {
  const { tourId } = useParams()
  const navigate = useNavigate()
  const editing = Boolean(tourId)

  const [form, setForm] = useState(emptyForm)
  const [days, setDays] = useState([])
  const [openDay, setOpenDay] = useState(null)
  const [removedDayIds, setRemovedDayIds] = useState([])

  const [fares, setFares] = useState(emptyFares)
  const [departures, setDepartures] = useState([])
  const [removedScheduleIds, setRemovedScheduleIds] = useState([])

  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  useEffect(() => {
    let alive = true

    const jobs = [categoryApi.all()]
    if (editing) {
      jobs.push(
        adminApi.tours(),
        adminApi.itineraries(tourId),
        adminApi.costs(tourId),
        adminApi.schedules(tourId)
      )
    }

    Promise.all(jobs)
      .then(([allCats, tours, itins, costs, scheds]) => {
        if (!alive) return

        setCategories((allCats || []).filter((c) => (c.childCount ?? 0) === 0))

        if (editing) {
          const t = (tours || []).find((x) => String(x.tourId) === String(tourId))
          if (t) {
            setForm({
              tourName: t.tourName || '', destination: t.destination || '',
              days: t.days ?? '', nights: t.nights ?? '',
              description: t.description || '', price: t.price ?? '',
              location: t.location || '', tourType: t.tourType || 'DOMESTIC',
              categoryId: t.categoryId ?? '',
              stayAndMeals: t.stayAndMeals || '', addOns: t.addOns || '',
              passportAndVisa: t.passportAndVisa || '', weather: t.weather || '',
              doAndDont: t.doAndDont || '',
              primaryImageUrl:
                t.primaryImageUrl ||
                (t.images && t.images.find((i) => i.isPrimary)?.source) ||
                (t.images && t.images[0] && t.images[0].source) || ''
            })
          }
          setDays(
            (itins || [])
              .slice()
              .sort((a, b) => (a.dayNumber ?? 0) - (b.dayNumber ?? 0))
              .map((i) => ({
                key: `db-${i.itineraryId}`,
                itineraryId: i.itineraryId,
                dayNumber: i.dayNumber,
                description: i.description || '',
                location: i.location || ''
              }))
          )

          const active = (costs || []).find((c) => c.isActive !== false) || (costs || [])[0]
          if (active) {
            setFares({
              costId: active.costId ?? null,
              adultPrice: active.adultPrice ?? '',
              singlePersonPrice: active.singlePersonPrice ?? '',
              extraPersonPrice: active.extraPersonPrice ?? '',
              childWithBedPrice: active.childWithBedPrice ?? '',
              childWithoutBedPrice: active.childWithoutBedPrice ?? '',
              validFrom: active.validFrom ?? '',
              validTo: active.validTo ?? ''
            })
          }

          setDepartures(
            (scheds || [])
              .slice()
              .sort((a, b) => String(a.startDate ?? '').localeCompare(String(b.startDate ?? '')))
              .map((s) => ({
                key: `db-${s.scheduleId}`,
                scheduleId: s.scheduleId,
                startDate: s.startDate ?? '',
                totalSeats: s.totalSeats ?? '',
                availableSeats: s.availableSeats ?? '',
                status: s.status || 'OPEN'
              }))
          )
        }
      })
      .catch(setError)
      .finally(() => { if (alive) setLoading(false) })

    return () => { alive = false }
  }, [tourId, editing])

  const pickImage = async (e) => {
    const file = e.target.files && e.target.files[0]
    e.target.value = ''
    if (!file) return
    setUploading(true)
    setError(null)
    try {
      const res = await adminApi.uploadImage(file)
      setForm((f) => ({ ...f, primaryImageUrl: res.url }))
      setNotice(`Image uploaded: ${res.originalName}`)
    } catch (err) {
      setError(err)
    } finally {
      setUploading(false)
    }
  }

  const addDay = () => {
    const next = days.length ? Math.max(...days.map((d) => d.dayNumber || 0)) + 1 : 1
    const row = newDay(next)
    setDays([...days, row])
    setOpenDay(row.key)
  }

  const setDayField = (key, field) => (e) =>
    setDays(days.map((d) => (d.key === key ? { ...d, [field]: e.target.value } : d)))

  const removeDay = (key) => {
    const row = days.find((d) => d.key === key)
    if (row && row.itineraryId) setRemovedDayIds([...removedDayIds, row.itineraryId])

    const left = days
      .filter((d) => d.key !== key)
      .map((d, i) => ({ ...d, dayNumber: i + 1 }))

    setDays(left)
    if (openDay === key) setOpenDay(null)
  }

  const setFare = (k) => (e) => setFares({ ...fares, [k]: e.target.value })

  const suggestFares = () => {
    const base = Number(form.price)
    if (!base || base <= 0) return
    const next = { ...fares }
    FARE_BANDS.forEach((b) => { next[b.key] = String(Math.round(base * b.ratio)) })
    setFares(next)
    setNotice('Fare bands filled from the base price. Adjust any of them before saving.')
  }

  const addDeparture = () => setDepartures([...departures, newDeparture()])

  const setDepartureField = (key, field) => (e) =>
    setDepartures(departures.map((d) => (d.key === key ? { ...d, [field]: e.target.value } : d)))

  const removeDeparture = (key) => {
    const row = departures.find((d) => d.key === key)
    if (row && row.scheduleId) setRemovedScheduleIds([...removedScheduleIds, row.scheduleId])
    setDepartures(departures.filter((d) => d.key !== key))
  }

  const dayMismatch = useMemo(() => {
    const declared = form.days === '' ? null : Number(form.days)
    if (!declared || days.length === 0) return null
    if (days.length === declared) return null
    return `This tour is ${declared} days but the itinerary has ${days.length} entr${days.length === 1 ? 'y' : 'ies'}.`
  }, [form.days, days.length])

  const save = async (e) => {
    e.preventDefault()

    if (fares.adultPrice === '' || Number(fares.adultPrice) <= 0) {
      setError(new Error(
        'Set at least the twin sharing fare. A tour with no fare bands cannot be booked.'
      ))
      window.scrollTo({ top: 0, behavior: 'smooth' })
      return
    }

    setBusy(true)
    setError(null)

    try {
      const payload = {
        ...form,
        days: form.days === '' ? null : Number(form.days),
        nights: form.nights === '' ? null : Number(form.nights),
        price: form.price === '' ? null : Number(form.price),
        categoryId: form.categoryId === '' ? null : Number(form.categoryId)
      }

      const saved = editing
        ? await adminApi.updateTour(tourId, payload)
        : await adminApi.createTour(payload)

      const id = editing ? Number(tourId) : saved.tourId

      const num = (v) => (v === '' || v === null || v === undefined ? null : Number(v))

      const costBody = {
        tourId: id,
        adultPrice: num(fares.adultPrice),
        singlePersonPrice: num(fares.singlePersonPrice),
        extraPersonPrice: num(fares.extraPersonPrice),
        childWithBedPrice: num(fares.childWithBedPrice),
        childWithoutBedPrice: num(fares.childWithoutBedPrice),
        validFrom: fares.validFrom || null,
        validTo: fares.validTo || null,
        isActive: true
      }

      if (fares.costId) await adminApi.updateCost(fares.costId, costBody)
      else await adminApi.createCost(costBody)

      for (const scheduleId of removedScheduleIds) {
        await adminApi.deleteSchedule(scheduleId)
      }

      for (const d of departures) {
        if (!d.startDate) continue
        const body = {
          tourId: id,
          startDate: d.startDate,
          totalSeats: num(d.totalSeats),
          availableSeats: num(d.availableSeats),
          status: d.status || 'OPEN'
        }
        if (d.scheduleId) await adminApi.updateSchedule(d.scheduleId, body)
        else await adminApi.createSchedule(body)
      }

      for (const dayId of removedDayIds) {
        await adminApi.deleteItinerary(dayId)
      }

      for (const d of days) {
        const body = {
          tourId: id,
          dayNumber: d.dayNumber,
          description: d.description,
          location: d.location
        }
        if (d.itineraryId) await adminApi.updateItinerary(d.itineraryId, body)
        else await adminApi.createItinerary(body)
      }

      navigate('/admin/tours', {
        state: { notice: `Tour "${form.tourName}" ${editing ? 'updated' : 'added'}.` }
      })
    } catch (err) {
      setError(err)
      setBusy(false)
    }
  }

  if (loading) return <Loader />

  return (
    <div>
      <div className="between wrap mb16">
        <div>
          <span className="eyebrow">Admin</span>
          <h2>{editing ? 'Edit tour' : 'Add a new tour'}</h2>
          <div className="small muted">
            {editing
              ? 'Update the details, image and day-wise itinerary.'
              : 'Fill in the details, upload an image and build the itinerary day by day.'}
          </div>
        </div>
        <Link to="/admin/tours" className="btn btn-ghost btn-sm">Back to tours</Link>
      </div>

      <ErrorBox error={error} />
      {notice && <div className="alert alert-ok mb16">{notice}</div>}

      <form onSubmit={save}>

        <div className="card card-pad mb24">
          <h3 className="mb16">Basic details</h3>

          <div className="field">
            <label className="label">Tour name</label>
            <input className="input" value={form.tourName} onChange={set('tourName')} required />
          </div>

          <div className="field">
            <label className="label">Destination</label>
            <input className="input" value={form.destination} onChange={set('destination')}
                   placeholder="Leh - Manali - Chandigarh" required />
            <div className="hint">
              Separate stops with " - ", a comma or "to". Each becomes a searchable city.
            </div>
          </div>

          <div className="grid grid-4">
            <div className="field">
              <label className="label">Days</label>
              <input type="number" min={1} className="input" value={form.days}
                     onChange={set('days')} required />
            </div>
            <div className="field">
              <label className="label">Nights</label>
              <input type="number" min={0} className="input" value={form.nights}
                     onChange={set('nights')} required />
            </div>
            <div className="field">
              <label className="label">Price</label>
              <input type="number" min={0} className="input" value={form.price}
                     onChange={set('price')} required />
            </div>
            <div className="field">
              <label className="label">Tour type</label>
              <select className="select" value={form.tourType} onChange={set('tourType')}>
                <option value="DOMESTIC">Domestic</option>
                <option value="INTERNATIONAL">International</option>
              </select>
            </div>
          </div>

          <div className="grid grid-2">
            <div className="field">
              <label className="label">Location</label>
              <input className="input" value={form.location} onChange={set('location')} />
            </div>
            <div className="field">
              <label className="label">Category</label>
              <select className="select" value={form.categoryId} onChange={set('categoryId')} required>
                <option value="">Select a category</option>
                {categories.map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.parentName ? `${c.parentName} → ${c.categoryName}` : c.categoryName}
                  </option>
                ))}
              </select>
              <div className="hint">
                Only leaf categories are listed, because tours attach to leaves.
              </div>
            </div>
          </div>

          <div className="field">
            <label className="label">Description</label>
            <textarea className="textarea" rows={3} value={form.description}
                      onChange={set('description')} />
          </div>
        </div>

        <div className="card card-pad mb24">
          <h3 className="mb16">Tour image</h3>
          <div className="center wrap" style={{ gap: 16, alignItems: 'flex-start' }}>
            <img
              src={imageUrl(form.primaryImageUrl || FALLBACK_IMAGE)}
              onError={onImageError}
              alt="Tour preview"
              style={{ width: 180, height: 120, objectFit: 'cover', borderRadius: 10,
                       border: '1px solid var(--border)' }}
            />
            <div className="grow">
              <input type="file" className="input"
                     accept="image/png,image/jpeg,image/webp,image/gif"
                     onChange={pickImage} disabled={uploading} />
              <div className="hint">
                {uploading
                  ? 'Uploading…'
                  : form.primaryImageUrl || 'PNG, JPG, WEBP or GIF. Up to 5 MB.'}
              </div>
              {form.primaryImageUrl && !uploading && (
                <button type="button" className="btn btn-ghost btn-sm mt8"
                        onClick={() => setForm({ ...form, primaryImageUrl: '' })}>
                  Remove image
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="card card-pad mb24">
          <div className="between wrap mb16">
            <div>
              <h3>Fare bands</h3>
              <div className="small muted">
                Every passenger is charged from one of these five bands, chosen by their
                age on the departure date and their room choice.
              </div>
            </div>
            <button type="button" className="btn btn-ghost btn-sm"
                    onClick={suggestFares} disabled={!form.price}>
              Fill from base price
            </button>
          </div>

          <div className="grid grid-2">
            {FARE_BANDS.map((b) => (
              <div className="field" key={b.key}>
                <label className="label">
                  {b.label}{b.key === 'adultPrice' ? ' *' : ''}
                </label>
                <input type="number" min={0} className="input"
                       value={fares[b.key]} onChange={setFare(b.key)} />
                <div className="hint">{b.hint}</div>
              </div>
            ))}
          </div>

          <div className="grid grid-2 mt8">
            <div className="field">
              <label className="label">Valid from</label>
              <input type="date" className="input"
                     value={fares.validFrom} onChange={setFare('validFrom')} />
            </div>
            <div className="field">
              <label className="label">Valid to</label>
              <input type="date" className="input"
                     value={fares.validTo} onChange={setFare('validTo')} />
            </div>
          </div>
          <div className="hint">
            Leave both dates blank for a fare that never expires. A departure can only be
            booked if its date falls inside this window.
          </div>
        </div>

        <div className="card card-pad mb24">
          <div className="between wrap mb16">
            <div>
              <h3>Departures</h3>
              <div className="small muted">
                The dates customers can actually book. Past dates are hidden from the site
                automatically.
              </div>
            </div>
            <button type="button" className="btn btn-sm" onClick={addDeparture}>
              + Add departure
            </button>
          </div>

          {departures.length === 0 ? (
            <div className="empty">
              No departures yet. Click <strong>Add departure</strong> to set the first date.
            </div>
          ) : (
            departures.map((d) => (
              <div key={d.key} className="card card-pad mb8">
                <div className="grid grid-4">
                  <div className="field">
                    <label className="label">Departure date</label>
                    <input type="date" className="input" value={d.startDate}
                           onChange={setDepartureField(d.key, 'startDate')} />
                  </div>
                  <div className="field">
                    <label className="label">Total seats</label>
                    <input type="number" min={1} className="input" value={d.totalSeats}
                           onChange={setDepartureField(d.key, 'totalSeats')} />
                  </div>
                  <div className="field">
                    <label className="label">Seats left</label>
                    <input type="number" min={0} className="input" value={d.availableSeats}
                           onChange={setDepartureField(d.key, 'availableSeats')}
                           placeholder="same as total" />
                  </div>
                  <div className="field">
                    <label className="label">Status</label>
                    <select className="select" value={d.status}
                            onChange={setDepartureField(d.key, 'status')}>
                      <option value="OPEN">OPEN</option>
                      <option value="FULL">FULL</option>
                      <option value="CLOSED">CLOSED</option>
                    </select>
                  </div>
                </div>
                <button type="button" className="btn btn-ghost btn-sm"
                        onClick={() => removeDeparture(d.key)}>
                  Remove this departure
                </button>
              </div>
            ))
          )}
        </div>

        <div className="card card-pad mb24">
          <div className="between wrap mb16">
            <div>
              <h3>Day-wise itinerary</h3>
              <div className="small muted">
                Add one entry per day. Click a day to open it.
              </div>
            </div>
            <button type="button" className="btn btn-sm" onClick={addDay}>
              + Add day {days.length ? days.length + 1 : 1}
            </button>
          </div>

          {dayMismatch && <div className="alert alert-warn mb16">{dayMismatch}</div>}

          {days.length === 0 ? (
            <div className="empty">
              No itinerary yet. Click <strong>Add day 1</strong> to start.
            </div>
          ) : (
            days.map((d) => {
              const open = openDay === d.key
              return (
                <div key={d.key} className="card mb8" style={{ overflow: 'hidden' }}>
                  <div
                    className="between card-pad"
                    style={{ cursor: 'pointer', padding: '12px 16px' }}
                    onClick={() => setOpenDay(open ? null : d.key)}
                  >
                    <div className="center" style={{ gap: 10 }}>
                      <span className="badge badge-primary">Day {d.dayNumber}</span>
                      <span className="small muted">
                        {d.location || d.description
                          ? `${d.location ? d.location + ' — ' : ''}${(d.description || '').slice(0, 60)}${(d.description || '').length > 60 ? '…' : ''}`
                          : 'Not filled in yet'}
                      </span>
                    </div>
                    <div className="center" style={{ gap: 8 }}>
                      <button type="button" className="btn btn-danger btn-sm"
                              onClick={(e) => { e.stopPropagation(); removeDay(d.key) }}>
                        Remove
                      </button>
                      <span className="small muted">{open ? '▲' : '▼'}</span>
                    </div>
                  </div>

                  {open && (
                    <div style={{ padding: '0 16px 16px' }}>
                      <div className="field">
                        <label className="label">Location on this day</label>
                        <input className="input" value={d.location}
                               onChange={setDayField(d.key, 'location')}
                               placeholder="Srinagar" />
                      </div>
                      <div className="field" style={{ marginBottom: 0 }}>
                        <label className="label">What happens on day {d.dayNumber}</label>
                        <textarea className="textarea" rows={3} value={d.description}
                                  onChange={setDayField(d.key, 'description')}
                                  placeholder="Arrive Srinagar, transfer to the houseboat, evening shikara ride on Dal Lake." />
                      </div>
                    </div>
                  )}
                </div>
              )
            })
          )}
        </div>

        <div className="card card-pad mb24">
          <h3 className="mb16">Travel information</h3>

          <div className="field">
            <label className="label">Stay and meals</label>
            <textarea className="textarea" rows={2} value={form.stayAndMeals}
                      onChange={set('stayAndMeals')} />
          </div>
          <div className="field">
            <label className="label">Add-ons</label>
            <textarea className="textarea" rows={2} value={form.addOns} onChange={set('addOns')} />
          </div>
          <div className="field">
            <label className="label">Passport and visa</label>
            <textarea className="textarea" rows={2} value={form.passportAndVisa}
                      onChange={set('passportAndVisa')} />
          </div>
          <div className="field">
            <label className="label">Weather</label>
            <textarea className="textarea" rows={2} value={form.weather} onChange={set('weather')} />
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label className="label">Do's and don'ts</label>
            <textarea className="textarea" rows={2} value={form.doAndDont}
                      onChange={set('doAndDont')} />
          </div>
        </div>

        <div className="card card-pad">
          <div className="between wrap">
            <div className="small muted">
              {departures.length === 0
                ? 'This tour has no departures yet, so nobody can book it. Add at least one date above.'
                : `Saving writes the tour, its fare bands, ${departures.length} departure${
                    departures.length === 1 ? '' : 's'} and the itinerary together.`}
            </div>
            <div className="row">
              <button className="btn btn-lg" disabled={busy || uploading}>
                {busy ? 'Saving…' : editing ? 'Save changes' : 'Create tour'}
              </button>
              <button type="button" className="btn btn-ghost btn-lg" disabled={busy}
                      onClick={() => navigate('/admin/tours')}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}
