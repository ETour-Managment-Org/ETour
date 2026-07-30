import { api } from './client.js'

export const reviewApi = {
  forTour: (tourId) => api.get(`/reviews/tour/${tourId}`),
  submit: (payload) => api.post('/reviews', payload, true),
  mine: () => api.get('/reviews/my', true),
  reviewable: () => api.get('/reviews/reviewable', true),
  remove: (id) => api.del(`/reviews/${id}`, true)
}
