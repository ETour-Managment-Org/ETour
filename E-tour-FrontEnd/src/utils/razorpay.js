const SCRIPT_SRC = 'https://checkout.razorpay.com/v1/checkout.js'
let scriptPromise = null
export function loadRazorpay() {
  if (window.Razorpay) return Promise.resolve(true)
  if (scriptPromise) return scriptPromise
  scriptPromise = new Promise((resolve, reject) => {
    const el = document.createElement('script')
    el.src = SCRIPT_SRC
    el.async = true
    el.onload = () => resolve(true)
    el.onerror = () => {

      scriptPromise = null
      reject(new Error('Could not load the payment gateway. Check your internet connection.'))
    }
    document.body.appendChild(el)
  })
  return scriptPromise
}
export async function openCheckout({ order, customer = {}, tourName = 'e-Tour booking' }) {
  await loadRazorpay()

  return new Promise((resolve) => {
    let settled = false
    const settle = (value) => {
      if (settled) return
      settled = true
      resolve(value)
    }
    const rzp = new window.Razorpay({
      key: order.keyId,
      amount: order.amountPaise,
      currency: order.currency || 'INR',
      order_id: order.orderId,
      name: 'e-Tour',
      description: tourName,
      prefill: {
        name: customer.name || '',
        email: customer.email || '',
        contact: customer.phone || ''
      },
      theme: { color: '#c2410c' },
      notes: { receipt: order.receipt || '' },

      handler: (response) => settle({
        ok: true,
        razorpayOrderId: response.razorpay_order_id,
        razorpayPaymentId: response.razorpay_payment_id,
        razorpaySignature: response.razorpay_signature
      }),
      modal: {
        ondismiss: () => settle({
          ok: false,
          reason: 'dismissed',
          message: 'Payment was cancelled. Your booking has not been made.'
        })
      }
    })
    rzp.on('payment.failed', (response) => {
      const d = response?.error || {}
      settle({
        ok: false,
        reason: 'failed',
        message: d.description || 'The payment did not go through. You have not been charged.',
        code: d.code,
        paymentId: d.metadata?.payment_id
      })
    })
    rzp.open()
  })
}

export const TEST_CARDS = {
  success: '4111 1111 1111 1111',
  failure: '5104 0600 0000 0008',
  upiSuccess: 'success@razorpay',
  upiFailure: 'failure@razorpay'
}
