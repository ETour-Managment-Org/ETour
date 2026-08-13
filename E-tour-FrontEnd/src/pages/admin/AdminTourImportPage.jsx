import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { adminApi } from '../../api/adminApi.js'
import Breadcrumb from '../../components/Breadcrumb.jsx'
import ErrorBox from '../../components/ErrorBox.jsx'
import Loader, { EmptyState, ButtonSpinner } from '../../components/Loader.jsx'
import { parseTourSheet, toApiRows } from '../../utils/tourSheet.js'

const PREVIEW_COLS = [
  { key: 'tourName', label: 'Tour name' },
  { key: 'destination', label: 'Destination' },
  { key: 'days', label: 'Days' },
  { key: 'nights', label: 'Nights' },
  { key: 'price', label: 'Price' },
  { key: 'location', label: 'Location' },
  { key: 'tourType', label: 'Type' },
  { key: 'categoryId', label: 'Cat' },
  { key: 'subCategoryId', label: 'Sub' }
]
export default function AdminTourImportPage() {
  const navigate = useNavigate()
  const [stage, setStage] = useState('upload')
  const [fileName, setFileName] = useState('')
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [result, setResult] = useState(null)
  const [dragging, setDragging] = useState(false)
  const badRows = useMemo(
    () => rows.filter((r) => !r.tourName || !String(r.tourName).trim()).length,
    [rows]
  )
  const readFile = async (file) => {
    if (!file) return
    setError(null)
    setBusy(true)
    try {
      const parsed = await parseTourSheet(file)
      setFileName(file.name)
      setRows(parsed.rows)
      setStage('preview')
    } catch (err) {
      setError(err)
      setStage('upload')
    } finally {
      setBusy(false)
    }
  }
  const onDrop = (e) => {
    e.preventDefault()
    setDragging(false)
    readFile(e.dataTransfer.files?.[0])
  }
  const confirmImport = async () => {
    setBusy(true)
    setError(null)
    try {
      const res = await adminApi.importTours(toApiRows(rows))
      setResult(res)
      setStage('done')
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }
  const startOver = () => {
    setStage('upload'); setRows([])
    setResult(null); setError(null); setFileName('')
  }
  return (
    <div className="container page">
      <Breadcrumb items={[
        { label: 'Home', to: '/' },
        { label: 'Admin', to: '/admin' },
        { label: 'Tours', to: '/admin/tours' },
        { label: 'Import from Excel' }
      ]} />
      <h1 className="mb8">Import tours from a spreadsheet</h1>
      <ErrorBox error={error} />

      {stage === 'upload' && (
        <div className="card card-pad">
          <label
            className={dragging ? 'dropzone drag' : 'dropzone'}
            onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
            onDragLeave={() => setDragging(false)}
            onDrop={onDrop}
          >
            <input
              type="file"
              accept=".xlsx,.xls,.csv"
              hidden
              onChange={(e) => readFile(e.target.files?.[0])}
            />
            {busy ? <Loader full message="Reading the sheet…" /> : (
              <>
                <div className="dropzone-icon" aria-hidden="true">⬆</div>
                <strong>Drop a spreadsheet here, or click to choose one</strong>
              </>
            )}
          </label>
        </div>
      )}
      {stage === 'preview' && (
        <>
          <div className="card card-pad mb16">
            <div className="between">
              <div>
                <strong>{fileName}</strong>
                <div className="small muted">
                  {rows.length} row{rows.length === 1 ? '' : 's'} read
                  {badRows > 0 && <> · <span className="text-err">{badRows} without a name</span></>}
                </div>
              </div>
              <button className="btn btn-ghost" onClick={startOver} disabled={busy}>
                Choose a different file
              </button>
            </div>
          </div>
          <div className="card mb16" style={{ overflowX: 'auto' }}>
            <table className="table">
              <thead>
                <tr>
                  <th style={{ width: 56 }}>Row</th>
                  {PREVIEW_COLS.map((c) => <th key={c.key}>{c.label}</th>)}
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.__row} className={r.tourName ? '' : 'row-bad'}>
                    <td className="muted small">{r.__row}</td>
                    {PREVIEW_COLS.map((c) => (
                      <td key={c.key}>
                        {r[c.key] ?? <span className="muted tiny">—</span>}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="card card-pad">
            <div className="between">
              <span className="small muted">
                {rows.length - badRows} tour{rows.length - badRows === 1 ? '' : 's'} will be created.
              </span>
              <button className="btn btn-lg" onClick={confirmImport} disabled={busy}>
                {busy
                  ? <ButtonSpinner label="Importing…" />
                  : `Import ${rows.length - badRows} tours`}
              </button>
            </div>
          </div>
        </>
      )}
      {stage === 'done' && result && (
        <>
          <div className="card card-pad mb16">
            <h2 className="mb8">
              {result.imported} of {result.total} imported
            </h2>
            <p className="muted mb0">
              {result.failed === 0
                ? ''
                : `${result.failed} row${result.failed === 1 ? '' : 's'} could not be imported.`}
            </p>
          </div>
          <div className="card mb16" style={{ overflowX: 'auto' }}>
            <table className="table">
              <thead>
                <tr><th style={{ width: 56 }}>Row</th><th>Tour</th><th>Result</th></tr>
              </thead>
              <tbody>
                {result.rows.map((r) => (
                  <tr key={r.rowNumber} className={r.success ? '' : 'row-bad'}>
                    <td className="muted small">{r.rowNumber}</td>
                    <td>{r.tourName || <span className="muted tiny">—</span>}</td>
                    <td>
                      <span className={r.success ? 'pill pill-ok' : 'pill pill-err'}>
                        {r.success ? 'Imported' : 'Not imported'}
                      </span>{' '}
                      {!r.success && <span className="small muted">{r.message}</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="row" style={{ gap: 12 }}>
            <button className="btn" onClick={() => navigate('/admin/tours')}>
              Go to the tour list
            </button>
            <button className="btn btn-ghost" onClick={startOver}>
              Import another sheet
            </button>
          </div>
        </>
      )}
      {stage === 'preview' && rows.length === 0 && (
        <EmptyState title="No rows found">
          <Link className="btn btn-ghost mt8" to="/admin/tours">Back to tours</Link>
        </EmptyState>
      )}
    </div>
  )
}
