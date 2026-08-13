import { api } from './client.js'

export const categoryApi = {
  all: () => api.get('/categories'),
  roots: () => api.get('/categories/roots'),
  children: (id) => api.get(`/categories/${id}/children`),
  breadcrumb: (id) => api.get(`/categories/${id}/breadcrumb`),
  one: (id) => api.get(`/categories/${id}`)
}
