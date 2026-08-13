import { api } from './client.js'
import { tokenStore } from './client.js'

export const feedbackApi = {
  submit: (payload) => api.post('/feedback', payload, !!tokenStore.get()),
  categories: () => api.get('/feedback/categories'),

  published: () => api.get('/feedback/published'),

  adminAll: (status) =>
    api.get(`/admin/feedback${status && status !== 'ALL' ? `?status=${status}` : ''}`, true),
  adminSetStatus: (id, status) =>
    api.patch(`/admin/feedback/${id}/status?status=${status}`, undefined, true),
  adminSetPublished: (id, published) =>
    api.patch(`/admin/feedback/${id}/publish?published=${published}`, undefined, true)
}
