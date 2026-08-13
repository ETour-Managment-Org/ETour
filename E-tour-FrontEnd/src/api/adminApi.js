import { api } from './client.js'

export const adminApi = {
  uploadImage: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return api.upload('/admin/uploads/image', fd, true)
  },
  resetPassword: (userId, newPassword) =>
    api.patch(`/admin/users/${userId}/password`, { newPassword }, true),
  users: () => api.get('/admin/users', true),
  user: (id) => api.get(`/admin/users/${id}`, true),
  userBookings: (id) => api.get(`/admin/users/${id}/bookings`, true),
  setUserActive: (id, active) =>
    api.patch(`/admin/users/${id}/active?active=${active}`, undefined, true),

  bookings: () => api.get('/admin/bookings', true),
  tours: () => api.get('/admin/tours', true),
  createTour: (payload) => api.post('/admin/tours', payload, true),
  importTours: (tours) => api.post('/admin/tours/import', { tours }, true),
  updateTour: (id, payload) => api.put(`/admin/tours/${id}`, payload, true),
  deleteTour: (id) => api.del(`/admin/tours/${id}`, true),

  itineraries: (tourId) => api.get(`/admin/tours/${tourId}/itineraries`, true),
  createItinerary: (payload) => api.post('/admin/itineraries', payload, true),
  updateItinerary: (id, payload) => api.put(`/admin/itineraries/${id}`, payload, true),
  deleteItinerary: (id) => api.del(`/admin/itineraries/${id}`, true),

  costs: (tourId) => api.get(`/admin/tours/${tourId}/costs`, true),
  createCost: (payload) => api.post('/admin/costs', payload, true),
  updateCost: (id, payload) => api.put(`/admin/costs/${id}`, payload, true),
  deleteCost: (id) => api.del(`/admin/costs/${id}`, true),

  schedules: (tourId) => api.get(`/admin/tours/${tourId}/schedules`, true),
  createSchedule: (payload) => api.post('/admin/schedules', payload, true),
  updateSchedule: (id, payload) => api.put(`/admin/schedules/${id}`, payload, true),
  deleteSchedule: (id) => api.del(`/admin/schedules/${id}`, true)
}
