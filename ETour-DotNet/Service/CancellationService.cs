using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using ETour.Api.Pricing;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class CancellationService : ICancellationService
{
    private readonly ETourDbContext _db;
    private readonly RefundPolicy _refundPolicy;
    private readonly IEmailService _email;
    private readonly ILogger<CancellationService> _log;

    public CancellationService(ETourDbContext db, RefundPolicy refundPolicy,
                               IEmailService email, ILogger<CancellationService> log)
    {
        _db = db;
        _refundPolicy = refundPolicy;
        _email = email;
        _log = log;
    }

    public async Task<CancellationResponseDTO> CancelBookingAsync(int bookingId,
        CancellationRequestDTO request, string username, bool isAdmin)
    {
        var booking = await _db.Bookings
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .Include(b => b.Passengers)
            .FirstOrDefaultAsync(b => b.BookingId == bookingId)
            ?? throw new ResourceNotFoundException("Booking", bookingId);

        if (!isAdmin && booking.User?.Username != username)
            throw new AccessDeniedException("This booking does not belong to you");

        if (booking.BookingStatus == BookingStatus.Cancelled)
            throw new ConflictException($"Booking {bookingId} is already cancelled");

        if (booking.BookingStatus == BookingStatus.Completed)
            throw new ConflictException(
                $"Booking {bookingId} has already travelled and cannot be cancelled");

        if (await _db.Cancellations.AnyAsync(c => c.BookingId == bookingId))
            throw new ConflictException(
                $"A cancellation record already exists for booking {bookingId}");

        var departureDate = booking.Schedule?.StartDate;
        var amountPaid = booking.TotalAmount ?? 0m;
        var refundAmount = _refundPolicy.CalculateRefund(amountPaid, departureDate);

        booking.BookingStatus = BookingStatus.Cancelled;

        var cancellation = new Cancellation
        {
            BookingId = bookingId,
            CancellationDate = DateTime.Now,
            Reason = request?.Reason ?? "Not specified",
            RefundAmount = refundAmount,
            RefundStatus = refundAmount > 0 ? RefundStatus.Processed : RefundStatus.NotApplicable,
            Remarks = _refundPolicy.Describe(departureDate)
        };

        _db.Cancellations.Add(cancellation);

        if (refundAmount > 0)
        {
            var payments = await _db.Payments
                .Where(p => p.BookingId == bookingId && p.PaymentStatus == PaymentStatus.Success)
                .ToListAsync();

            foreach (var p in payments) p.PaymentStatus = PaymentStatus.Refunded;
        }

        var schedule = booking.Schedule;
        if (schedule?.AvailableSeats is int seats)
        {
            var released = booking.Passengers?.Count ?? 0;
            var restored = seats + released;

            if (schedule.TotalSeats is int total && restored > total) restored = total;

            schedule.AvailableSeats = restored;
            _log.LogInformation("Released {Count} seat(s) back to schedule {Schedule}",
                released, schedule.ScheduleId);
        }

        await _db.SaveChangesAsync();

        _email.SendCancellationEmail(booking, cancellation.RefundAmount ?? 0m, cancellation.Remarks);

        return new CancellationResponseDTO
        {
            CancellationId = cancellation.CancellationId,
            BookingId = bookingId,
            BookingStatus = booking.BookingStatus,
            CancellationDate = cancellation.CancellationDate,
            Reason = cancellation.Reason,
            AmountPaid = amountPaid,
            RefundAmount = refundAmount,
            RefundStatus = cancellation.RefundStatus,
            Remarks = cancellation.Remarks
        };
    }
}
