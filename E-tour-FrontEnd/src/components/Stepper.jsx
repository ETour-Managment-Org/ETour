const STEPS = ['Traveller details', 'Review booking', 'Payment', 'Confirmation']

export default function Stepper({ current = 1 }) {
  return (
    <div className="stepper no-print">
      {STEPS.map((label, i) => {
        const n = i + 1
        const cls = n === current ? 'step active' : n < current ? 'step done' : 'step'
        return (
          <div key={label} className="center" style={{ gap: 6 }}>
            <div className={cls}>
              <div className="step-num">{n < current ? '✓' : n}</div>
              <div className="step-label">{label}</div>
            </div>
            {n < STEPS.length && <div className="step-bar" />}
          </div>
        )
      })}
    </div>
  )
}
