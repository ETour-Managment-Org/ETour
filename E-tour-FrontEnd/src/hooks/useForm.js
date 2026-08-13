import { useState, useCallback } from 'react'
import { validate } from '../validation/rules.js'

export function useForm(initialValues, schemaFor) {
  const [values, setValues] = useState(initialValues)
  const [touched, setTouched] = useState({})
  const [submitted, setSubmitted] = useState(false)
  const schema = typeof schemaFor === 'function' ? schemaFor(values) : schemaFor
  const errors = validate(values, schema)
  const setValue = useCallback((name, value) => {
    setValues((prev) => ({ ...prev, [name]: value }))
  }, [])
  const handleChange = useCallback((e) => {
    const { name, type, checked, value } = e.target
    setValues((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }, [])
  const handleBlur = useCallback((e) => {
    setTouched((prev) => ({ ...prev, [e.target.name]: true }))
  }, [])
  const error = useCallback(
    (name) => ((touched[name] || submitted) ? errors[name] || null : null),
    [errors, touched, submitted]
  )

  const field = useCallback((name) => ({
    name,
    value: values[name] ?? '',
    onChange: handleChange,
    onBlur: handleBlur,
    'aria-invalid': (touched[name] || submitted) && errors[name] ? 'true' : undefined
  }), [values, handleChange, handleBlur, touched, submitted, errors])
  const isValid = Object.keys(errors).length === 0

  const onSubmit = useCallback((handler) => (e) => {
    if (e?.preventDefault) e.preventDefault()
    setSubmitted(true)
    setTouched(Object.fromEntries(Object.keys(schema).map((k) => [k, true])))
    if (Object.keys(validate(values, schema)).length > 0) {

      const firstBad = Object.keys(schema).find((k) => validate(values, schema)[k])
      const el = document.querySelector(`[name="${firstBad}"]`)
      if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'center' }); el.focus?.() }
      return
    }
    handler(values)
  }, [values, schema])
  const reset = useCallback((next = initialValues) => {
    setValues(next); setTouched({}); setSubmitted(false)
  }, [initialValues])
  return { values, setValues, setValue, errors, error, field,
           isValid, onSubmit, reset, touched, submitted,
           handleChange, handleBlur }
}
