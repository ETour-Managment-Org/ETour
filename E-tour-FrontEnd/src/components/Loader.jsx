import LottieBox from './LottieBox.jsx'
import { ANIM } from './animations.js'

export default function Loader({ full = false, message = 'Loading…', size = 96 }) {
  if (!full) {
    return <LottieBox animation={ANIM.loading} size={size} label="Loading" />
  }
  return (
    <div className="lottie-state">
      <LottieBox animation={ANIM.loading} size={140} label="Loading" />
      <p>{message}</p>
    </div>
  )
}
export function EmptyState({ title = 'Nothing here yet', message = '', children }) {
  return (
    <div className="lottie-state">
      <LottieBox animation={ANIM.empty} size={120} />
      <h3>{title}</h3>
      {message && <p>{message}</p>}
      {children}
    </div>
  )
}
export function ErrorState({ title = 'Something went wrong', message = '', children }) {
  return (
    <div className="lottie-state">
      <LottieBox animation={ANIM.error} size={110} loop={false} />
      <h3>{title}</h3>
      {message && <p>{message}</p>}
      {children}
    </div>
  )
}
export function SuccessState({ title = 'Done', message = '', children }) {
  return (
    <div className="lottie-state">
      <LottieBox animation={ANIM.success} size={120} loop={false} />
      <h3>{title}</h3>
      {message && <p>{message}</p>}
      {children}
    </div>
  )
}
export function ButtonSpinner({ label }) {
  return (
    <span className="btn-spinner">
      <LottieBox animation={ANIM.loading} size={18} label="Working" />
      {label && <span>{label}</span>}
    </span>
  )
}
