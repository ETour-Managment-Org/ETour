import { api } from './client.js'

export const tourApi = {
  all: () => api.get('/tours'),
  byId: (id) => api.get(`/tours/${id}`),
  byCategory: (id) => api.get(`/tours/category/${id}`),
  search: (params) => {
    const q = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== '' && v !== null && v !== undefined) q.append(k, v)
    })
    const s = q.toString()
    return api.get(`/tours/search${s ? `?${s}` : ''}`)
  }
}
