const COLUMNS = {
  tourname: 'tourName',
  name: 'tourName',
  destination: 'destination',
  days: 'days',
  nights: 'nights',
  description: 'description',
  price: 'price',
  location: 'location',
  tourtype: 'tourType',
  type: 'tourType',
  categoryid: 'categoryId',
  category: 'categoryId',
  subcategoryid: 'subCategoryId',
  subcategory: 'subCategoryId',
  stayandmeals: 'stayAndMeals',
  addons: 'addOns',
  passportandvisa: 'passportAndVisa',
  weather: 'weather',
  doanddont: 'doAndDont',
  dosanddonts: 'doAndDont',
  primaryimageurl: 'primaryImageUrl',
  image: 'primaryImageUrl',
  imageurl: 'primaryImageUrl'
}
const NUMERIC = new Set(['days', 'nights', 'categoryId', 'subCategoryId'])
const DECIMAL = new Set(['price'])
const normaliseHeader = (h) => String(h || '').toLowerCase().replace(/[^a-z]/g, '')
export const TEMPLATE_HEADERS = [
  'Tour Name', 'Destination', 'Days', 'Nights', 'Price', 'Location',
  'Tour Type', 'Category Id', 'Sub Category Id', 'Description',
  'Stay And Meals', 'Add Ons', 'Passport And Visa', 'Weather',
  'Do And Dont', 'Primary Image Url'
]
const TEMPLATE_SAMPLE = [
  'Kerala Backwaters Escape', 'Kerala', 4, 3, 12000, 'Alleppey',
  'DOMESTIC', 1, 2, 'Houseboat cruise, Kumarakom bird sanctuary and a day in Fort Kochi.',
  'Houseboat stay with all meals', 'Ayurvedic massage session',
  'Not required for Indian nationals', 'Warm and humid, 24-32C',
  'Do carry mosquito repellent. Do not swim in the backwaters.',
  '/images/kerala.jpg'
]

let xlsxPromise = null
const loadXlsx = () => (xlsxPromise ??= import('xlsx'))
export async function parseTourSheet(file) {
  const XLSX = await loadXlsx()

  const buffer = await file.arrayBuffer()
  const wb = XLSX.read(buffer, { type: 'array', cellDates: true })
  const sheetName = wb.SheetNames[0]
  if (!sheetName) throw new Error('No sheets found.')
  const sheet = wb.Sheets[sheetName]

  const grid = XLSX.utils.sheet_to_json(sheet, { header: 1, blankrows: false, defval: '' })
  if (grid.length === 0) throw new Error('That sheet is empty.')
  const rawHeaders = grid[0].map((h) => String(h ?? '').trim())
  const mapped = rawHeaders.map((h) => COLUMNS[normaliseHeader(h)] || null)
  if (!mapped.includes('tourName')) {
    throw new Error('No "Tour Name" column found.')
  }
  const rows = []
  for (let r = 1; r < grid.length; r++) {
    const line = grid[r]
    if (!line || line.every((c) => String(c ?? '').trim() === '')) continue
    const row = {}
    mapped.forEach((field, i) => {
      if (!field) return
      let v = line[i]
      if (v instanceof Date) v = v.toISOString().slice(0, 10)
      v = typeof v === 'string' ? v.trim() : v
      if (v === '' || v === null || v === undefined) return

      if (NUMERIC.has(field) || DECIMAL.has(field)) {

        const n = Number(String(v).replace(/[^0-9.-]/g, ''))
        if (Number.isNaN(n)) return
        row[field] = NUMERIC.has(field) ? Math.round(n) : n
        return
      }

      row[field] = String(v)
    })
    row.__row = r + 1
    rows.push(row)
  }
  if (rows.length === 0) throw new Error('No data rows found.')
  return { rows, headers: rawHeaders, sheetName }
}

export const toApiRows = (rows) => rows.map(({ __row, ...rest }) => rest)
export async function downloadTemplate() {
  const XLSX = await loadXlsx()
  const ws = XLSX.utils.aoa_to_sheet([TEMPLATE_HEADERS, TEMPLATE_SAMPLE])
  ws['!cols'] = TEMPLATE_HEADERS.map((h) => ({ wch: Math.max(14, h.length + 4) }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Tours')
  XLSX.writeFile(wb, 'etour-tour-template.xlsx')
}
