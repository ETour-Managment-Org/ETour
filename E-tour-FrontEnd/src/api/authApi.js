import { api } from './client.js'

export const authApi = {
  changePassword: (payload) => api.post('/auth/change-password', payload, true),
  register: (payload) => api.post('/auth/register', payload),
  login: (payload) => api.post('/auth/login', payload),
  me: () => api.get('/auth/me', true),
  updateMe: (payload) => api.put('/auth/me', payload, true),
  providers: () => api.get('/auth/providers')
}
