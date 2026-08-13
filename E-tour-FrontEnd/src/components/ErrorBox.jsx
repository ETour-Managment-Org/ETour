export default function ErrorBox({ error }) {
  if (!error) return null
  const msg = typeof error === 'string' ? error : error.message || 'Something went wrong'
  return <div className="alert alert-err">{msg}</div>
}
