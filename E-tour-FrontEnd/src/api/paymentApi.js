import { api } from './client.js'

export const paymentApi = {
  config: () => api.get('/payments/config'),

  createOrder: (amount, tourId) =>
    api.post('/payments/order', { amount, tourId }, true)
}
