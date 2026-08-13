export const GENDERS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' }
]

export default function GenderField({ value, onChange, required = false }) {
  return (
    <div className="field">
      <label className="label">Gender</label>
      <div className="center wrap" style={{ gap: 18 }}>
        {GENDERS.map((g) => (
          <label key={g.value} className="center small" style={{ gap: 6, cursor: 'pointer' }}>
            <input
              type="radio"
              name="gender"
              value={g.value}
              checked={value === g.value}
              onChange={(e) => onChange(e.target.value)}
              required={required}
              style={{ accentColor: 'var(--primary)' }}
            />
            {g.label}
          </label>
        ))}
        {value && !required && (
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => onChange('')}>
            Clear
          </button>
        )}
      </div>
    </div>
  )
}
