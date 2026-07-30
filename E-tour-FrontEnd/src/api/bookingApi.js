import { api } from './client.js'

export const bookingApi = {
  place: (payload) => api.post('/bookings/place', payload, true),
  mine: () => api.get('/bookings/my', true),
  byId: (id) => api.get(`/bookings/${id}`, true),
  cancel: (id, reason) => api.post(`/bookings/${id}/cancel`, { reason }, true)
}
