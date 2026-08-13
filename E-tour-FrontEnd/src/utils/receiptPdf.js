const TERRACOTTA = [194, 65, 12]
const TEAL = [15, 118, 110]
const INK = [28, 25, 23]
const MUTED = [120, 113, 108]
const RULE = [214, 211, 209]
const money = (n) =>
  'INR ' + Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const dmy = (d) => {
  if (!d) return '-'
  const dt = new Date(d)
  return Number.isNaN(dt.getTime())
    ? String(d)
    : dt.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
}
export async function downloadReceipt(booking, { bandLabel } = {}) {
  const { jsPDF } = await import('jspdf')
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  const W = doc.internal.pageSize.getWidth()
  const M = 48
  let y = 0
  const ref = booking.customerBookingNumber ?? booking.bookingId
  doc.setFillColor(...TERRACOTTA)
  doc.rect(0, 0, W, 96, 'F')
  doc.setTextColor(255, 255, 255)
  doc.setFont('helvetica', 'bold').setFontSize(22)
  doc.text('e-Tour', M, 44)
  doc.setFont('helvetica', 'normal').setFontSize(10)
  doc.text('Booking receipt', M, 62)
  doc.setFont('helvetica', 'bold').setFontSize(11)
  doc.text(`Reference #${ref}`, W - M, 44, { align: 'right' })
  doc.setFont('helvetica', 'normal').setFontSize(9)
  doc.text(booking.bookingStatus || '', W - M, 62, { align: 'right' })
  y = 132

  const facts = [
    ['Tour', booking.tourName],
    ['Destination', booking.destination],
    ['Departure', dmy(booking.departureDate)],
    ['Booked on', dmy(booking.bookingDate)],
    ['Booked by', booking.customerName],
    ['Travellers', String(booking.noOfPax ?? '-')]
  ]
  const colW = (W - M * 2) / 2
  facts.forEach(([label, value], i) => {
    const col = i % 2
    const row = Math.floor(i / 2)
    const x = M + col * colW
    const ry = y + row * 44
    doc.setFont('helvetica', 'normal').setFontSize(8).setTextColor(...MUTED)
    doc.text(String(label).toUpperCase(), x, ry)
    doc.setFont('helvetica', 'bold').setFontSize(11).setTextColor(...INK)
    const lines = doc.splitTextToSize(String(value ?? '-'), colW - 16)
    doc.text(lines[0] || '-', x, ry + 15)
  })
  y += Math.ceil(facts.length / 2) * 44 + 10

  doc.setDrawColor(...RULE).setLineWidth(0.75)
  doc.line(M, y, W - M, y)
  y += 24
  doc.setFont('helvetica', 'bold').setFontSize(12).setTextColor(...INK)
  doc.text('Passengers', M, y)
  y += 18

  const cols = [M, M + 210, M + 300, W - M]
  doc.setFont('helvetica', 'bold').setFontSize(8).setTextColor(...MUTED)
  doc.text('NAME', cols[0], y)
  doc.text('AGE', cols[1], y)
  doc.text('FARE BAND', cols[2], y)
  doc.text('AMOUNT', cols[3], y, { align: 'right' })
  y += 6
  doc.setDrawColor(...RULE).line(M, y, W - M, y)
  y += 16

  doc.setFont('helvetica', 'normal').setFontSize(10).setTextColor(...INK)
  for (const p of booking.passengers || []) {
    if (y > doc.internal.pageSize.getHeight() - 140) {
      doc.addPage()
      y = M + 20
    }
    doc.text(doc.splitTextToSize(String(p.fullName || '-'), 195)[0], cols[0], y)
    doc.text(String(p.ageAtDeparture ?? '-'), cols[1], y)
    doc.text(bandLabel ? bandLabel(p.paxType) : String(p.paxType || '-'), cols[2], y)
    doc.text(money(p.paxAmount), cols[3], y, { align: 'right' })
    y += 20
  }
  y += 6
  doc.setDrawColor(...RULE).line(M, y, W - M, y)
  y += 22
  const rightRow = (label, value, bold = false) => {
    doc.setFont('helvetica', bold ? 'bold' : 'normal').setFontSize(bold ? 13 : 10)
    doc.setTextColor(...(bold ? INK : MUTED))
    doc.text(label, W - M - 170, y)
    doc.setTextColor(...INK)
    doc.text(value, W - M, y, { align: 'right' })
    y += bold ? 24 : 18
  }
  rightRow('Payment method', String(booking.paymentMethod || '-'))
  rightRow('Payment status', String(booking.paymentStatus || '-'))
  if (booking.transactionRef) rightRow('Transaction', String(booking.transactionRef))
  y += 4
  doc.setDrawColor(...TEAL).setLineWidth(1.25).line(W - M - 190, y - 12, W - M, y - 12)
  rightRow('Total paid', money(booking.totalAmount), true)

  const fy = doc.internal.pageSize.getHeight() - 54
  doc.setDrawColor(...RULE).setLineWidth(0.75).line(M, fy, W - M, fy)
  doc.setFont('helvetica', 'normal').setFontSize(8).setTextColor(...MUTED)
  doc.text('e-Tour', M, fy + 16)
  doc.text(`Generated ${dmy(new Date())}`, W - M, fy + 16, { align: 'right' })

  doc.save(`etour-receipt-${ref}.pdf`)
}
