import { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react'
import { OCCUPANCY } from '../utils/pricing.js'
import { useAuth } from './AuthContext.jsx'

const DRAFT_KEY = 'etour_booking_draft'
const BookingContext = createContext(null)

export const newPassenger = (occupancy = OCCUPANCY.TWIN_SHARING) => ({
  fullName: '',
  birthDate: '',
  gender: '',
  email: '',
  passportNumber: '',
  withBed: true,
  occupancy
})

const emptyDraft = {
  tour: null,
  scheduleId: null,
  schedule: null,
  cost: null,
  customer: { fullName: '', email: '', phone: '', address: '', city: '' },
  passengers: [],
  thirdPersonChoice: '',
  paymentMethod: 'UPI'
}

function readDraft() {
  try {
    const raw = sessionStorage.getItem(DRAFT_KEY)
    return raw ? { ...emptyDraft, ...JSON.parse(raw) } : emptyDraft
  } catch {
    return emptyDraft
  }
}

export function BookingProvider({ children }) {
  const [draft, setDraftState] = useState(readDraft)
  const { user } = useAuth()
  const lastUserId = useRef(user?.userId ?? null)

  const setDraft = useCallback((patch) => {
    setDraftState((prev) => {
      const next = typeof patch === 'function' ? patch(prev) : { ...prev, ...patch }
      sessionStorage.setItem(DRAFT_KEY, JSON.stringify(next))
      return next
    })
  }, [])

  const startBooking = useCallback(
    (tour, scheduleId) => {
      const schedule = (tour.schedules || []).find((s) => s.scheduleId === Number(scheduleId)) || null
      const seats = schedule?.availableSeats ?? 0
      const starting = seats >= 2 ? 2 : Math.max(seats, 1)
      setDraft({
        ...emptyDraft,
        tour,
        scheduleId: Number(scheduleId),
        schedule,
        passengers: Array.from({ length: starting }, () => newPassenger())
      })
    },
    [setDraft]
  )

  const clearDraft = useCallback(() => {
    sessionStorage.removeItem(DRAFT_KEY)
    setDraftState(emptyDraft)
  }, [])

  useEffect(() => {
    const previous = lastUserId.current
    const current = user?.userId ?? null
    if (previous === current) return

    lastUserId.current = current

    if (previous !== null) {
      clearDraft()
    }
  }, [user, clearDraft])

  const hasDraft = !!(draft.tour && draft.scheduleId)

  return (
    <BookingContext.Provider value={{ draft, setDraft, startBooking, clearDraft, hasDraft }}>
      {children}
    </BookingContext.Provider>
  )
}

export function useBooking() {
  const ctx = useContext(BookingContext)
  if (!ctx) throw new Error('useBooking must be used inside BookingProvider')
  return ctx
}
