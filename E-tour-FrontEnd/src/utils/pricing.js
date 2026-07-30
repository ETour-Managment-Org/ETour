export const CHILD_MAX_AGE = 12

export const MAX_PER_ROOM = 2
export const MAX_EXTRA_BEDS_PER_ROOM = 1
export const MAX_OCCUPANTS_PER_ROOM = MAX_PER_ROOM + MAX_EXTRA_BEDS_PER_ROOM

export const BAND = {
  TWIN_SHARING: 'TWIN_SHARING',
  SINGLE: 'SINGLE',
  EXTRA_PERSON: 'EXTRA_PERSON',
  CHILD_WITH_BED: 'CHILD_WITH_BED',
  CHILD_WITHOUT_BED: 'CHILD_WITHOUT_BED'
}

export const OCCUPANCY = {
  TWIN_SHARING: 'TWIN_SHARING',
  SINGLE: 'SINGLE',
  EXTRA_PERSON: 'EXTRA_PERSON'
}

export function ageAtDeparture(birthDate, departureDate) {
  if (!birthDate || !departureDate) return null
  const b = new Date(birthDate)
  const d = new Date(departureDate)
  if (isNaN(b) || isNaN(d)) return null
  let age = d.getFullYear() - b.getFullYear()
  const m = d.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && d.getDate() < b.getDate())) age--
  return age < 0 ? null : age
}

export function bandForOccupancy(requestedOccupancy) {
  if (requestedOccupancy === OCCUPANCY.SINGLE) return BAND.SINGLE
  if (requestedOccupancy === OCCUPANCY.EXTRA_PERSON) return BAND.EXTRA_PERSON
  return BAND.TWIN_SHARING
}

export function resolveBand(age, withBed, requestedOccupancy) {
  if (age === null || age === undefined) return bandForOccupancy(requestedOccupancy)
  if (age <= CHILD_MAX_AGE) return withBed === false ? BAND.CHILD_WITHOUT_BED : BAND.CHILD_WITH_BED
  return bandForOccupancy(requestedOccupancy)
}

export function rateFor(cost, band) {
  if (!cost || !band) return 0
  const pick = (a, b) => (a !== null && a !== undefined ? Number(a) : Number(b || 0))
  switch (band) {
    case BAND.SINGLE: return pick(cost.singlePersonPrice, cost.adultPrice)
    case BAND.EXTRA_PERSON: return pick(cost.extraPersonPrice, cost.adultPrice)
    case BAND.CHILD_WITH_BED: return pick(cost.childWithBedPrice, cost.adultPrice)
    case BAND.CHILD_WITHOUT_BED: return pick(cost.childWithoutBedPrice, cost.adultPrice)
    default: return Number(cost.adultPrice || 0)
  }
}

export function pickCost(costs, departureDate) {
  if (!Array.isArray(costs) || costs.length === 0) return null
  const active = costs.filter((c) => c.isActive !== false)
  const pool = active.length ? active : costs
  if (!departureDate) return pool[0]
  const d = new Date(departureDate)
  return (
    pool.find((c) => {
      const from = c.validFrom ? new Date(c.validFrom) : null
      const to = c.validTo ? new Date(c.validTo) : null
      return (!from || d >= from) && (!to || d <= to)
    }) || pool[0]
  )
}

export function roomsRequired(twinSharingCount, singleCount, extraBedCount) {
  const twinRooms = Math.ceil((twinSharingCount || 0) / MAX_PER_ROOM)
  const extraBedCapacity = twinRooms * MAX_EXTRA_BEDS_PER_ROOM
  const overflowBeds = Math.max(0, (extraBedCount || 0) - extraBedCapacity)
  return twinRooms + (singleCount || 0) + overflowBeds
}

export function occupancyBreakdown(bands) {
  let twinSharing = 0
  let single = 0
  let extraBeds = 0
  bands.forEach((band) => {
    if (band === BAND.SINGLE) single += 1
    else if (band === BAND.EXTRA_PERSON || band === BAND.CHILD_WITH_BED) extraBeds += 1
    else if (band === BAND.TWIN_SHARING) twinSharing += 1
  })
  const rooms = roomsRequired(twinSharing, single, extraBeds)
  return {
    twinSharing,
    single,
    extraBeds,
    rooms,
    bedCapacity: rooms * MAX_OCCUPANTS_PER_ROOM
  }
}

export function quote(passengers, cost, departureDate) {
  const lines = passengers.map((p) => {
    const age = ageAtDeparture(p.birthDate, departureDate)
    const band = resolveBand(age, p.withBed !== false, p.occupancy)
    return {
      name: p.fullName,
      age,
      band,
      provisional: age === null || age === undefined,
      amount: rateFor(cost, band)
    }
  })
  const total = lines.reduce((s, l) => s + (l.amount || 0), 0)
  const rooming = occupancyBreakdown(lines.map((l) => l.band).filter(Boolean))
  const provisional = lines.some((l) => l.provisional)
  return { lines, total, rooming, provisional }
}

export function bandRates(cost) {
  return [
    { band: BAND.TWIN_SHARING, label: 'Adult, twin sharing', note: 'Two adults share one room', rate: rateFor(cost, BAND.TWIN_SHARING) },
    { band: BAND.SINGLE, label: 'Adult, single occupancy', note: 'A room and bed to yourself', rate: rateFor(cost, BAND.SINGLE) },
    { band: BAND.EXTRA_PERSON, label: 'Extra adult on an extra bed', note: 'Third person sharing a room', rate: rateFor(cost, BAND.EXTRA_PERSON) },
    { band: BAND.CHILD_WITH_BED, label: `Child with bed (up to ${CHILD_MAX_AGE})`, note: 'Extra bed in the parents room', rate: rateFor(cost, BAND.CHILD_WITH_BED) },
    { band: BAND.CHILD_WITHOUT_BED, label: `Child without bed (up to ${CHILD_MAX_AGE})`, note: 'Shares the existing beds', rate: rateFor(cost, BAND.CHILD_WITHOUT_BED) }
  ]
}
