const BASE = import.meta.env.VITE_API_BASE_URL || '/api'
const TOKEN_KEY = 'etour_token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message)
    this.status = status
    this.body = body
  }
}

async function request(path, { method = 'GET', body, auth = false } = {}) {
  const headers = {}
  const isForm = typeof FormData !== 'undefined' && body instanceof FormData
  if (body !== undefined && !isForm) headers['Content-Type'] = 'application/json'

  if (auth) {
    const token = tokenStore.get()
    if (token) headers['Authorization'] = `Bearer ${token}`
  }

  let res
  try {
    res = await fetch(`${BASE}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : (isForm ? body : JSON.stringify(body))
    })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the API container is running.')
  }

  if (res.status === 204) return null

  const text = await res.text()
  let data = null
  if (text) {
    try { data = JSON.parse(text) } catch { data = text }
  }

  if (!res.ok) {
    if (res.status === 401) tokenStore.clear()
    const msg = (data && data.message) || (typeof data === 'string' && data) || `Request failed (${res.status})`
    throw new ApiError(res.status, msg, data)
  }
  return data
}

export const api = {
  get: (p, auth = false) => request(p, { auth }),
  post: (p, body, auth = false) => request(p, { method: 'POST', body, auth }),
  put: (p, body, auth = false) => request(p, { method: 'PUT', body, auth }),
  patch: (p, body, auth = false) => request(p, { method: 'PATCH', body, auth }),
  del: (p, auth = false) => request(p, { method: 'DELETE', auth }),
  upload: (p, formData, auth = true) => request(p, { method: 'POST', body: formData, auth })
}
