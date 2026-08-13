export const required = (label = 'This field') => (v) =>
  v === undefined || v === null || String(v).trim() === '' ? `${label} is required` : null
export const minLength = (n, label = 'This field') => (v) =>
  v && String(v).length < n ? `${label} must be at least ${n} characters` : null
export const maxLength = (n, label = 'This field') => (v) =>
  v && String(v).length > n ? `${label} must be ${n} characters or fewer` : null

export const email = () => (v) =>
  v && !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v).trim())
    ? 'Enter a valid email address'
    : null
export const phone10 = () => (v) => {
  if (!v) return null
  const digits = String(v).replace(/\D/g, '')
  if (digits.length !== 10) return 'Phone number must be exactly 10 digits'
  if (!/^[6-9]/.test(digits)) return 'Enter a valid mobile number'
  return null
}
export const password = () => (v) => {
  if (!v) return null
  if (v.length < 8) return 'Password must be at least 8 characters'
  if (!/[A-Za-z]/.test(v)) return 'Password must contain at least one letter'
  if (!/[0-9]/.test(v)) return 'Password must contain at least one number'
  return null
}
export const matches = (otherValue, label = 'Values') => (v) =>
  v && v !== otherValue ? `${label} do not match` : null
export const number = (label = 'This field') => (v) =>
  v !== '' && v !== null && v !== undefined && Number.isNaN(Number(v))
    ? `${label} must be a number` : null
export const min = (n, label = 'This field') => (v) =>
  v !== '' && v !== null && Number(v) < n ? `${label} must be at least ${n}` : null
export const max = (n, label = 'This field') => (v) =>
  v !== '' && v !== null && Number(v) > n ? `${label} must be ${n} or less` : null
export const integer = (label = 'This field') => (v) =>
  v !== '' && v !== null && !Number.isInteger(Number(v))
    ? `${label} must be a whole number` : null
export const notInPast = (label = 'Date') => (v) => {
  if (!v) return null
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const d = new Date(v); d.setHours(0, 0, 0, 0)
  return d < today ? `${label} cannot be in the past` : null
}
export const notInFuture = (label = 'Date') => (v) => {
  if (!v) return null
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const d = new Date(v); d.setHours(0, 0, 0, 0)
  return d > today ? `${label} cannot be in the future` : null
}
export const oneOf = (values, label = 'This field') => (v) =>
  v && !values.includes(v) ? `${label} must be one of: ${values.join(', ')}` : null
export const firstError = (value, rules = []) => {
  for (const rule of rules) {
    const err = rule(value)
    if (err) return err
  }
  return null
}

export const validate = (values, schema) => {
  const errors = {}
  for (const [field, rules] of Object.entries(schema)) {
    const err = firstError(values[field], rules)
    if (err) errors[field] = err
  }
  return errors
}
