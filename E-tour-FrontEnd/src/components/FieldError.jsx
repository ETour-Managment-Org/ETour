export default function FieldError({ message }) {
  if (!message) return null
  return <div className="field-error" role="alert">{message}</div>
}
