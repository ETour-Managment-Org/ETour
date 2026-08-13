import { Suspense, lazy, useMemo } from 'react'

const Lottie = lazy(() => import('lottie-react'))
const prefersReducedMotion = () =>
  typeof window !== 'undefined' &&
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
export default function LottieBox({
  animation,
  size = 120,
  loop = true,
  autoplay = true,
  className = '',
  label = ''
}) {
  const reduced = useMemo(prefersReducedMotion, [])
  const style = { width: size, height: size, margin: '0 auto' }

  return (
    <div className={`lottie-box ${className}`} style={style}
         role={label ? 'img' : 'presentation'} aria-label={label || undefined}>
      <Suspense fallback={<div style={style} />}>
        <Lottie
          animationData={animation}
          loop={reduced ? false : loop}
          autoplay={reduced ? false : autoplay}
          style={style}
        />
      </Suspense>
    </div>
  )
}
