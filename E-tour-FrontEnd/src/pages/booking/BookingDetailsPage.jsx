import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useI18n } from '../../i18n/I18nContext.jsx'
import { useBooking, newPassenger } from '../../context/BookingContext.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { authApi } from '../../api/authApi.js'
import Stepper from '../../components/Stepper.jsx'
import Breadcrumb from '../../components/Breadcrumb.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import { inr, prettyDate, bandLabel } from '../../utils/format.js'
import {
  pickCost,
  quote,
  ageAtDeparture,
  bandRates,
  occupancyBreakdown,
  isDomestic,
  oddTravellerChoiceNeeded,
  OCCUPANCY,
  BAND,
  CHILD_MAX_AGE,
  MAX_PER_ROOM,
  MAX_EXTRA_BEDS_PER_ROOM,
  MAX_OCCUPANTS_PER_ROOM
} from '../../utils/pricing.js'

const GROUPS = [
  {
    occupancy: OCCUPANCY.TWIN_SHARING,
    band: BAND.TWIN_SHARING,
    label: 'Twin sharing',
    note: `Two travellers share one room (default). Up to ${MAX_PER_ROOM} per room.`
  },
  {
    occupancy: OCCUPANCY.SINGLE,
    band: BAND.SINGLE,
    label: 'Single occupancy',
    note: 'A separate room with a single bed, charged at the single rate.'
  },
  {
    occupancy: OCCUPANCY.EXTRA_PERSON,
    band: BAND.EXTRA_PERSON,
    label: 'Extra bed in a room',
    note: `A third traveller on an extra bed. Maximum ${MAX_EXTRA_BEDS_PER_ROOM} extra bed per room.`
  }
]

const ODD_EXTRA_BED = 'EXTRA_BED_SAME_ROOM'
const ODD_NEW_ROOM = 'DIFFERENT_ROOM'

const onlyDigits = (v) => String(v || '').replace(/\D/g, '').slice(0, 10)

export default function BookingDetailsPage() {
  const { tc, locale, lang } = useI18n()
  const { draft, setDraft, hasDraft } = useBooking()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!hasDraft) navigate('/tours', { replace: true })
  }, [hasDraft, navigate])

  const [prefilled, setPrefilled] = useState(false)

  useEffect(() => {
    let cancelled = false

    authApi
      .me()
      .then((profile) => {
        if (cancelled || !profile) return

        setDraft((prev) => ({
          ...prev,
          customer: {
            fullName: prev.customer.fullName || profile.fullName || '',
            email: prev.customer.email || profile.email || '',
            phone: prev.customer.phone || profile.phoneNumber || '',
            address: prev.customer.address || profile.address || '',
            city: prev.customer.city || profile.city || ''
          }
        }))
        setPrefilled(String(profile.authProvider).toUpperCase() === 'GOOGLE' ? 'google' : 'account')
      })
      .catch(() => {})

    return () => {
      cancelled = true
    }
  }, [])

  const departure = draft.schedule?.startDate
  const cost = useMemo(() => pickCost(draft.tour?.costs, departure), [draft.tour, departure])
  const rates = useMemo(() => bandRates(cost), [cost])
  const { lines, total, rooming, provisional } = useMemo(
    () => quote(draft.passengers, cost, departure),
    [draft.passengers, cost, departure]
  )

  const passengers = draft.passengers
  const paxCount = passengers.length
  const seats = draft.schedule?.availableSeats ?? 0

  const counts = useMemo(() => {
    const c = { [OCCUPANCY.TWIN_SHARING]: 0, [OCCUPANCY.SINGLE]: 0, [OCCUPANCY.EXTRA_PERSON]: 0 }
    passengers.forEach((p) => {
      const key = p.occupancy || OCCUPANCY.TWIN_SHARING
      if (c[key] !== undefined) c[key] += 1
    })
    return c
  }, [passengers])

  const plannedRooming = useMemo(
    () =>
      occupancyBreakdown(
        passengers.map((p) => {
          const key = p.occupancy || OCCUPANCY.TWIN_SHARING
          if (key === OCCUPANCY.SINGLE) return BAND.SINGLE
          if (key === OCCUPANCY.EXTRA_PERSON) return BAND.EXTRA_PERSON
          return BAND.TWIN_SHARING
        })
      ),
    [passengers]
  )

  if (!hasDraft) return null

  const setCustomer = (k) => (e) => setDraft({ customer: { ...draft.customer, [k]: e.target.value } })
  const setCustomerPhone = (e) =>
    setDraft({ customer: { ...draft.customer, phone: onlyDigits(e.target.value) } })

  const setPax = (i, k, v) =>
    setDraft((prev) => ({
      ...prev,
      passengers: prev.passengers.map((p, idx) => (idx === i ? { ...p, [k]: v } : p))
    }))

  const changeGroup = (occupancy, delta) => {
    setError(null)
    setDraft((prev) => {
      const list = [...prev.passengers]
      if (delta > 0) {
        if (list.length >= seats) return prev
        list.push(newPassenger(occupancy))
      } else {
        const idx = [...list].reverse().findIndex((p) => (p.occupancy || OCCUPANCY.TWIN_SHARING) === occupancy)
        if (idx === -1 || list.length <= 1) return prev
        list.splice(list.length - 1 - idx, 1)
      }
      const stillOdd = list.length >= 3 && list.length % 2 === 1
      return { ...prev, passengers: list, thirdPersonChoice: stillOdd ? prev.thirdPersonChoice : '' }
    })
  }

  const chooseOddTraveller = (choice) => {
    setError(null)
    setDraft((prev) => {
      const last = prev.passengers.length - 1
      const list = prev.passengers.map((p, i) => ({
        ...p,
        occupancy:
          i < last
            ? OCCUPANCY.TWIN_SHARING
            : choice === ODD_EXTRA_BED
              ? OCCUPANCY.EXTRA_PERSON
              : OCCUPANCY.SINGLE
      }))
      return { ...prev, passengers: list, thirdPersonChoice: choice }
    })
  }

  const submit = (e) => {
    e.preventDefault()
    setError(null)

    if (paxCount === 0) return setError('Add at least one traveller.')
    if (paxCount > seats) return setError(`Only ${seats} seats remain on this departure.`)

    if (needsOddChoice && !draft.thirdPersonChoice) {
      return setError(
        `You have ${paxCount} travellers. Please choose a new room or an extra bed for the odd traveller.`
      )
    }

    for (const p of passengers) {
      if (!p.fullName.trim()) return setError('Every traveller needs a full name.')
      if (!p.birthDate) return setError('Every traveller needs a date of birth.')
      if (ageAtDeparture(p.birthDate, departure) === null) {
        return setError(`The date of birth for ${p.fullName || 'a traveller'} is not valid for this departure.`)
      }
    }

    if (paxCount > rooming.bedCapacity) {
      return setError(
        `${paxCount} travellers do not fit in ${rooming.rooms} room(s). A room holds ${MAX_PER_ROOM} people plus ${MAX_EXTRA_BEDS_PER_ROOM} extra bed.`
      )
    }

    if (!draft.customer.fullName || !draft.customer.email || !draft.customer.phone) {
      return setError('Please complete your contact details.')
    }

    navigate('/booking/review')
  }

  const canAdd = paxCount < seats
  const needsOddChoice = oddTravellerChoiceNeeded(paxCount)
  const domestic = isDomestic(draft.tour)

  return (
    <div className="container page">
      <Breadcrumb
        items={[
          { label: 'Home', to: '/' },
          { label: 'Browse', to: '/home' },
          { label: tc(draft.tour.tourName), to: `/tours/${draft.tour.tourId}` },
          { label: 'Travellers' }
        ]}
      />
      <Stepper current={1} />

      <div className="section-head">
        <span className="eyebrow">Step 2 of 4</span>
        <h1>Travellers and rooming</h1>
        <p className="muted">
          Prices are quoted per person on twin sharing. The fare band for each traveller is derived
          from their age on the departure date, {prettyDate(departure, locale)}.
        </p>
      </div>

      <form onSubmit={submit}>
        <div className="grid grid-side">
          <div>
            <div className="card card-pad mb24">
              <h3 className="mb16">Published fare bands</h3>
              <table className="table">
                <thead>
                  <tr>
                    <th>Band</th>
                    <th>What it means</th>
                    <th className="right">Per person</th>
                  </tr>
                </thead>
                <tbody>
                  {rates.map((r) => (
                    <tr key={r.band} className={r.band === BAND.TWIN_SHARING ? 'is-default' : undefined}>
                      <td className="strong">
                        {r.label}
                        {r.band === BAND.TWIN_SHARING && <span className="badge badge-primary" style={{ marginLeft: 8 }}>Default</span>}
                      </td>
                      <td className="small muted">{r.note}</td>
                      <td className="right strong">{inr(r.rate, locale)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="card card-pad mb24">
              <h3 className="mb16">Your contact details</h3>
              {prefilled && (
                <div className="hint mb16">
                  {prefilled === 'google'
                    ? 'Filled in from your Google account. Change anything you like.'
                    : 'Filled in from your profile. Change anything you like.'}
                </div>
              )}
              <div className="grid grid-2">
                <div className="field">
                  <label className="label">Full name</label>
                  <input className="input" value={draft.customer.fullName} onChange={setCustomer('fullName')} required />
                </div>
                <div className="field">
                  <label className="label">Email</label>
                  <input type="email" className="input" value={draft.customer.email} onChange={setCustomer('email')} required />
                </div>
                <div className="field">
                  <label className="label">Phone</label>
                  <input className="input" value={draft.customer.phone} onChange={setCustomerPhone} required
                         inputMode="numeric" maxLength={10} pattern="[6-9][0-9]{9}"
                         title="10 digits, starting 6 to 9" placeholder="10 digits" />
                </div>
                <div className="field">
                  <label className="label">City</label>
                  <input className="input" value={draft.customer.city} onChange={setCustomer('city')} />
                </div>
              </div>
              <div className="field">
                <label className="label">Address</label>
                <input className="input" value={draft.customer.address} onChange={setCustomer('address')} />
              </div>
            </div>

            <div className="card card-pad mb24">
              <div className="between mb8">
                <h3>Select travellers</h3>
                <span className="small muted">{seats} seats left</span>
              </div>
              <p className="small muted mb16">
                Twin sharing is the default. Add single occupancy or an extra bed only if you need it.
              </p>

              {GROUPS.map((g) => (
                <div key={g.occupancy} className="counter-row">
                  <div>
                    <div className="strong small">{g.label}</div>
                    <div className="tiny muted">{g.note}</div>
                    <div className="tiny strong mt8" style={{ color: 'var(--primary)' }}>
                      {inr(rates.find((r) => r.band === g.band)?.rate)} per person
                    </div>
                  </div>
                  <div className="counter">
                    <button
                      type="button"
                      className="counter-btn"
                      aria-label={`Remove one ${g.label}`}
                      onClick={() => changeGroup(g.occupancy, -1)}
                      disabled={counts[g.occupancy] === 0 || paxCount <= 1}
                    >
                      −
                    </button>
                    <span className="counter-val">{counts[g.occupancy]}</span>
                    <button
                      type="button"
                      className="counter-btn"
                      aria-label={`Add one ${g.label}`}
                      onClick={() => changeGroup(g.occupancy, 1)}
                      disabled={!canAdd}
                    >
                      +
                    </button>
                  </div>
                </div>
              ))}

              <div className="between mt16" style={{ paddingTop: 14, borderTop: '1px solid var(--border)' }}>
                <span className="strong">
                  {paxCount} traveller{paxCount === 1 ? '' : 's'}
                </span>
                <span className="small muted">
                  {plannedRooming.rooms} room{plannedRooming.rooms === 1 ? '' : 's'}
                  {plannedRooming.extraBeds > 0 && `, ${plannedRooming.extraBeds} extra bed${plannedRooming.extraBeds === 1 ? '' : 's'}`}
                </span>
              </div>

              {!canAdd && (
                <div className="hint">This departure has {seats} seats left, so no more travellers can be added.</div>
              )}
            </div>

            {needsOddChoice && (
              <div className="card card-pad mb24" style={{ borderColor: 'var(--primary)' }}>
                <h3 className="mb8">You have {paxCount} travellers</h3>
                <p className="small muted mb16">
                  {paxCount - 1} travellers pair up into {(paxCount - 1) / 2} room
                  {(paxCount - 1) / 2 === 1 ? '' : 's'} on twin sharing. A room holds{' '}
                  {MAX_PER_ROOM} people in {MAX_PER_ROOM} beds plus {MAX_EXTRA_BEDS_PER_ROOM} extra
                  bed. Do you want a new room or an extra bed?
                </p>

                <div className="choice">
                  <label className={draft.thirdPersonChoice === ODD_NEW_ROOM ? 'choice-opt sel' : 'choice-opt'}>
                    <input
                      type="radio"
                      name="oddTraveller"
                      checked={draft.thirdPersonChoice === ODD_NEW_ROOM}
                      onChange={() => chooseOddTraveller(ODD_NEW_ROOM)}
                    />
                    <span className="grow">
                      <span className="choice-title">A new room</span>
                      <br />
                      <span className="choice-desc">
                        A separate room for the odd traveller, charged at the single occupancy rate.
                      </span>
                    </span>
                    <span className="choice-cost">{inr(rates.find((r) => r.band === BAND.SINGLE)?.rate)}</span>
                  </label>

                  <label className={draft.thirdPersonChoice === ODD_EXTRA_BED ? 'choice-opt sel' : 'choice-opt'}>
                    <input
                      type="radio"
                      name="oddTraveller"
                      checked={draft.thirdPersonChoice === ODD_EXTRA_BED}
                      onChange={() => chooseOddTraveller(ODD_EXTRA_BED)}
                    />
                    <span className="grow">
                      <span className="choice-title">An extra bed</span>
                      <br />
                      <span className="choice-desc">
                        The odd traveller shares an existing room on an extra bed, charged at the
                        extra person rate.
                      </span>
                    </span>
                    <span className="choice-cost">{inr(rates.find((r) => r.band === BAND.EXTRA_PERSON)?.rate)}</span>
                  </label>
                </div>

                {!draft.thirdPersonChoice && (
                  <div className="err-text">Please pick one of the two options to continue.</div>
                )}
              </div>
            )}

            <div className="card card-pad">
              <h3 className="mb16">Traveller details</h3>

              {passengers.map((p, i) => {
                const age = ageAtDeparture(p.birthDate, departure)
                const line = lines[i]
                const isChild = age !== null && age <= CHILD_MAX_AGE
                return (
                  <div key={i} className="card-soft mb16">
                    <div className="between mb16">
                      <span className="strong small">Traveller {i + 1}</span>
                      <span className="center" style={{ gap: 8 }}>
                        {line?.band && (
                          <span className={line.provisional ? 'badge badge-slate' : 'badge badge-primary'}>
                            {bandLabel(line.band, lang)}
                          </span>
                        )}
                        {passengers.length > 1 && (
                          <button
                            type="button"
                            className="btn btn-ghost btn-sm"
                            onClick={() => changeGroup(p.occupancy || OCCUPANCY.TWIN_SHARING, -1)}
                          >
                            Remove
                          </button>
                        )}
                      </span>
                    </div>

                    <div className="grid grid-2">
                      <div className="field">
                        <label className="label">Full name</label>
                        <input
                          className="input"
                          value={p.fullName}
                          onChange={(e) => setPax(i, 'fullName', e.target.value)}
                          required
                        />
                      </div>
                      <div className="field">
                        <label className="label">Date of birth</label>
                        <input
                          type="date"
                          className="input"
                          value={p.birthDate}
                          onChange={(e) => setPax(i, 'birthDate', e.target.value)}
                          required
                        />
                        {age !== null ? (
                          <div className="hint">
                            Age at departure: <strong>{age}</strong> — {bandLabel(line?.band, lang)} at {inr(line?.amount, locale)}
                          </div>
                        ) : (
                          <div className="hint">
                            Needed to set the fare band. Shown at the adult rate until entered.
                          </div>
                        )}
                      </div>
                      <div className="field">
                        <label className="label">Gender</label>
                        <select className="select" value={p.gender} onChange={(e) => setPax(i, 'gender', e.target.value)}>
                          <option value="">Select</option>
                          <option>Male</option>
                          <option>Female</option>
                          <option>Other</option>
                        </select>
                      </div>
                      <div className="field">
                        <label className="label">Occupancy</label>
                        <select
                          className="select"
                          value={p.occupancy || OCCUPANCY.TWIN_SHARING}
                          onChange={(e) => setPax(i, 'occupancy', e.target.value)}
                          disabled={isChild}
                        >
                          <option value={OCCUPANCY.TWIN_SHARING}>Twin sharing</option>
                          <option value={OCCUPANCY.SINGLE}>Single occupancy</option>
                          <option value={OCCUPANCY.EXTRA_PERSON}>Extra bed in a room</option>
                        </select>
                        {isChild && (
                          <div className="hint">
                            Under {CHILD_MAX_AGE + 1}, so a child fare applies instead of an adult occupancy.
                          </div>
                        )}
                      </div>
                      {!domestic && (
                        <div className="field">
                          <label className="label">Passport number</label>
                          <input
                            className="input"
                            value={p.passportNumber}
                            onChange={(e) => setPax(i, 'passportNumber', e.target.value)}
                            placeholder="As printed on the passport"
                          />
                          <div className="hint">Required for this international tour.</div>
                        </div>
                      )}
                    </div>

                    {isChild && (
                      <label className="center small">
                        <input
                          type="checkbox"
                          checked={p.withBed !== false}
                          onChange={(e) => setPax(i, 'withBed', e.target.checked)}
                        />
                        Requires a separate bed
                      </label>
                    )}
                  </div>
                )
              })}
            </div>

          </div>

          <aside className="card card-pad sticky-side">
            <h3 className="mb8">{tc(draft.tour.tourName)}</h3>
            <div className="small muted mb16">{tc(draft.tour.destination)}</div>

            <div className="inv-row"><span className="muted">Departure</span><span>{prettyDate(departure, locale)}</span></div>
            <div className="inv-row"><span className="muted">Duration</span><span>{draft.tour.durationLabel}</span></div>
            <div className="inv-row"><span className="muted">Travellers</span><span>{paxCount}</span></div>
            <div className="inv-row"><span className="muted">Rooms</span><span>{rooming.rooms}</span></div>
            <div className="inv-row"><span className="muted">Extra beds</span><span>{rooming.extraBeds}</span></div>

            <div className="mt16 mb8 strong small">Live price estimate</div>
            {lines.map((l, i) => (
              <div key={i} className="inv-row small">
                <span className="muted">
                  {l.name || `Traveller ${i + 1}`}
                  <br />
                  <span className="tiny">
                    {bandLabel(l.band, lang)}
                    {l.provisional && ' (before date of birth)'}
                  </span>
                </span>
                <span>{inr(l.amount, locale)}</span>
              </div>
            ))}
            <div className="inv-total"><span>Total</span><span>{inr(total, locale)}</span></div>

            {provisional && (
              <div className="hint mb8">
                Adult rates shown. Enter each date of birth and any traveller aged{' '}
                {CHILD_MAX_AGE} or under will reprice to the child band.
              </div>
            )}

            <div className="hint mb16">
              A room takes {MAX_PER_ROOM} people plus {MAX_EXTRA_BEDS_PER_ROOM} extra bed, so{' '}
              {rooming.rooms} room{rooming.rooms === 1 ? '' : 's'} hold
              {rooming.rooms === 1 ? 's' : ''} up to {MAX_OCCUPANTS_PER_ROOM * rooming.rooms}{' '}
              traveller{MAX_OCCUPANTS_PER_ROOM * rooming.rooms === 1 ? '' : 's'}.
            </div>

            <ErrorBox error={error} />
            <button className="btn btn-block btn-lg">Continue to review</button>
            <div className="hint" style={{ textAlign: 'center' }}>You will not be charged yet</div>
          </aside>
        </div>
      </form>
    </div>
  )
}
