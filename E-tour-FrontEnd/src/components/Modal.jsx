export default function Modal({ open, children }) {
  if (!open) return null
  return (
    <div className="overlay">
      <div className="modal">{children}</div>
    </div>
  )
}
