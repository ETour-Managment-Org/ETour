using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using ETour.Api.Pricing;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class BookingService : IBookingService
{
    private readonly ETourDbContext _db;
    private readonly FareBandPolicy _fareBandPolicy;
    private readonly TourCostCalculator _costCalculator;
    private readonly IPaymentGatewayService _paymentGateway;
    private readonly IReceiptService _receiptService;
    private readonly IEmailService _email;
    private readonly ILogger<BookingService> _log;

    public BookingService(ETourDbContext db, FareBandPolicy fareBandPolicy,
                          TourCostCalculator costCalculator,
                          IPaymentGatewayService paymentGateway,
                          IReceiptService receiptService,
                          IEmailService email,
                          ILogger<BookingService> log)
    {
        _db = db;
        _fareBandPolicy = fareBandPolicy;
        _costCalculator = costCalculator;
        _paymentGateway = paymentGateway;
        _receiptService = receiptService;
        _email = email;
        _log = log;
    }

    public async Task<BookingResponseDTO> PlaceBookingAsync(BookingRequestDTO request, string username)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException($"No account found for {username}");

        var tour = await _db.Tours.FindAsync(request.TourId)
            ?? throw new ResourceNotFoundException("Tour", request.TourId);

        var schedule = await _db.Schedules.FindAsync(request.ScheduleId)
            ?? throw new ResourceNotFoundException("Schedule", request.ScheduleId);

        if (schedule.TourId != tour.TourId)
            throw new ConflictException(
                $"Schedule {schedule.ScheduleId} does not belong to tour {tour.TourId}");

        var departureDate = schedule.StartDate;
        if (departureDate is null)
            throw new ConflictException($"Schedule {schedule.ScheduleId} has no start date, "
                + "so the age-based fare (BRD-065) cannot be calculated.");

        if (departureDate < DateOnly.FromDateTime(DateTime.Today))
            throw new ArgumentException($"Departure {departureDate} is in the past");

        var paxCount = request.Passengers.Count;

        var available = schedule.AvailableSeats
            ?? throw new ConflictException(
                $"Schedule {schedule.ScheduleId} has no seat count configured");

        if (available < paxCount)
            throw new ConflictException(
                $"Only {available} seat(s) remain on this departure, {paxCount} requested");

        var cost = (await _db.Costs.Where(c => c.TourId == tour.TourId && c.IsActive == true)
                        .ToListAsync())
                   .FirstOrDefault(c => _costCalculator.IsValidOn(c, departureDate))
            ?? throw new ResourceNotFoundException(
                $"No active cost configured for tour {tour.TourId} covering {departureDate}");

        var booking = new Booking
        {
            UserId = user.UserId,
            TourId = tour.TourId,
            ScheduleId = schedule.ScheduleId,
            BookingDate = DateOnly.FromDateTime(DateTime.Today),
            BookingStatus = BookingStatus.Pending,
            ContactName = TrimOrNull(request.ContactName),
            ContactEmail = TrimOrNull(request.ContactEmail),
            ContactPhone = TrimOrNull(request.ContactPhone)
        };

        var total = 0m;
        var twinSharingCount = 0;
        var singleCount = 0;
        var extraBedCount = 0;

        foreach (var dto in request.Passengers)
        {
            var age = _fareBandPolicy.CalculateAgeAtDeparture(dto.BirthDate, departureDate);
            var withBed = dto.WithBed != false;
            var band = _fareBandPolicy.Resolve(age, withBed, dto.Occupancy);
            var rate = _costCalculator.RateFor(cost, band);

            booking.Passengers.Add(new PassengerDetails
            {
                FullName = dto.FullName,
                BirthDate = dto.BirthDate,
                Age = age,
                Gender = dto.Gender,
                Email = dto.Email,
                PassportNumber = dto.PassportNumber,
                PaxType = band.ToString()
            });

            switch (band)
            {
                case FareBand.SINGLE: singleCount++; break;
                case FareBand.EXTRA_PERSON:
                case FareBand.CHILD_WITH_BED: extraBedCount++; break;
                case FareBand.TWIN_SHARING: twinSharingCount++; break;
            }

            total += rate;
            _log.LogDebug("Passenger {Name} age {Age} -> band {Band} rate {Rate}",
                dto.FullName, age, band, rate);
        }

        var rooms = Occupancy.RoomsRequired(twinSharingCount, singleCount, extraBedCount);
        var bedCapacity = rooms * Occupancy.MaxOccupantsPerRoom;

        if (paxCount > bedCapacity)
            throw new ArgumentException(
                $"Occupancy is not valid. A room holds {Occupancy.MaxPerRoom} people plus "
              + $"{Occupancy.MaxExtraBedsPerRoom} extra bed. Adjust the occupancy choices "
              + $"for {paxCount} passengers.");

        booking.TotalAmount = total;

        _db.Bookings.Add(booking);
        await _db.SaveChangesAsync();

        var method = request.PaymentMethod ?? "ONLINE";

        var gatewayCheckout = !string.IsNullOrWhiteSpace(request.RazorpayPaymentId)
                           || !string.IsNullOrWhiteSpace(request.RazorpayOrderId)
                           || !string.IsNullOrWhiteSpace(request.RazorpaySignature);

        PaymentResult result;
        if (gatewayCheckout)
        {
            if (string.IsNullOrWhiteSpace(request.RazorpayPaymentId)
                || string.IsNullOrWhiteSpace(request.RazorpayOrderId)
                || string.IsNullOrWhiteSpace(request.RazorpaySignature))
                throw new ConflictException(
                    "Payment details are incomplete. The booking was not confirmed.");

            method = "RAZORPAY";
            result = await _paymentGateway.VerifyAsync(
                new VerificationRequest(request.RazorpayOrderId,
                                        request.RazorpayPaymentId,
                                        request.RazorpaySignature),
                total);
        }
        else
        {
            result = _paymentGateway.Charge(total, method, booking.BookingId);
        }

        var payment = new Payment
        {
            BookingId = booking.BookingId,
            Amount = total,
            PaymentDate = DateOnly.FromDateTime(DateTime.Today),
            PaymentStatus = result.Success ? PaymentStatus.Success : PaymentStatus.Failed,
            PaymentMethod = method,
            TransactionRef = result.Reference
        };

        _db.Payments.Add(payment);
        await _db.SaveChangesAsync();

        if (!result.Success)
            throw new ConflictException($"Payment failed: {result.Message}");

        booking.BookingStatus = BookingStatus.Confirmed;
        schedule.AvailableSeats = available - paxCount;
        await _db.SaveChangesAsync();

        booking.User = user;
        booking.Tour = tour;
        booking.Schedule = schedule;

        var receiptMessage = _receiptService.GenerateAndSendReceipt(booking);

        return await ToResponseAsync(booking, payment, receiptMessage, rooms, extraBedCount);
    }

    public async Task<BookingResponseDTO> GetBookingAsync(int bookingId, string username, bool isAdmin)
    {
        var booking = await LoadFullAsync(bookingId)
            ?? throw new ResourceNotFoundException("Booking", bookingId);

        if (!isAdmin && booking.User?.Username != username)
            throw new AccessDeniedException("This booking does not belong to you");

        await ApplyElapsedCompletionAsync(booking);

        var payment = await LatestPaymentAsync(bookingId);
        var dto = await ToResponseAsync(booking, payment, null, null, null);

        if (booking.UserId is int uid)
        {
            dto.CustomerBookingNumber = await CustomerBookingNumberAsync(uid, bookingId);
        }
        return dto;
    }

    public async Task<List<BookingResponseDTO>> GetMyBookingsAsync(string username)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException($"No account found for {username}");

        var bookings = await _db.Bookings
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .Include(b => b.Passengers)
            .Where(b => b.UserId == user.UserId)
            .OrderByDescending(b => b.BookingDate).ThenByDescending(b => b.BookingId)
            .ToListAsync();

        var ordered = bookings.OrderBy(b => b.BookingId).ToList();
        var numbers = new Dictionary<int, int>();
        for (var i = 0; i < ordered.Count; i++) numbers[ordered[i].BookingId] = i + 1;

        var result = new List<BookingResponseDTO>();
        foreach (var b in bookings)
        {
            await ApplyElapsedCompletionAsync(b);
            var dto = await ToResponseAsync(b, await LatestPaymentAsync(b.BookingId), null, null, null);
            dto.CustomerBookingNumber = numbers.GetValueOrDefault(b.BookingId);
            result.Add(dto);
        }
        return result;
    }

    public async Task<int> CompleteElapsedBookingsAsync()
    {
        var confirmed = await _db.Bookings
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .Where(b => b.BookingStatus == BookingStatus.Confirmed)
            .ToListAsync();

        var completed = 0;
        foreach (var booking in confirmed)
        {
            var before = booking.BookingStatus;
            await ApplyElapsedCompletionAsync(booking);
            if (before != booking.BookingStatus) completed++;
        }

        if (completed > 0)
        {
            _log.LogInformation(
                "Marked {Count} booking(s) as COMPLETED and sent the review invitation", completed);
        }
        return completed;
    }

    private async Task ApplyElapsedCompletionAsync(Booking booking)
    {
        if (booking.BookingStatus != BookingStatus.Confirmed) return;
        if (!TourDates.HasFinished(booking, DateOnly.FromDateTime(DateTime.Today))) return;

        booking.BookingStatus = BookingStatus.Completed;
        await _db.SaveChangesAsync();
        _email.SendTourCompletedEmail(booking);
    }

    private async Task<Booking> LoadFullAsync(int bookingId) =>
        await _db.Bookings
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .Include(b => b.Passengers)
            .FirstOrDefaultAsync(b => b.BookingId == bookingId);

    private async Task<Payment> LatestPaymentAsync(int bookingId) =>
        await _db.Payments.AsNoTracking()
            .Where(p => p.BookingId == bookingId)
            .OrderByDescending(p => p.PaymentId)
            .FirstOrDefaultAsync();

    private async Task<int> CustomerBookingNumberAsync(int userId, int bookingId)
    {
        var ids = await _db.Bookings.AsNoTracking()
            .Where(b => b.UserId == userId).OrderBy(b => b.BookingId)
            .Select(b => b.BookingId).ToListAsync();
        return ids.IndexOf(bookingId) + 1;
    }

    private async Task<BookingResponseDTO> ToResponseAsync(Booking b, Payment payment,
        string receiptMessage, int? rooms, int? extraBeds)
    {
        var departureDate = b.Schedule?.StartDate;
        var paxCount = b.Passengers?.Count ?? 0;

        Cost costRow = null;
        if (b.TourId is int tid && departureDate is not null)
        {
            costRow = (await _db.Costs.AsNoTracking()
                    .Where(c => c.TourId == tid && c.IsActive == true).ToListAsync())
                .FirstOrDefault(c => _costCalculator.IsValidOn(c, departureDate));
        }

        var pax = new List<PassengerResponseDTO>();
        var twinSharingCount = 0;
        var singleCount = 0;
        var extraBedCount = 0;

        foreach (var p in b.Passengers ?? new List<PassengerDetails>())
        {
            var band = FareBandExtensions.FromOccupancy(p.PaxType);
            if (band is null && p.Age is int a)
            {
                band = _fareBandPolicy.Resolve(a, true, null);
            }

            switch (band)
            {
                case FareBand.SINGLE: singleCount++; break;
                case FareBand.EXTRA_PERSON:
                case FareBand.CHILD_WITH_BED: extraBedCount++; break;
                case FareBand.TWIN_SHARING: twinSharingCount++; break;
            }

            pax.Add(new PassengerResponseDTO
            {
                PaxId = p.PaxId,
                FullName = p.FullName,
                BirthDate = p.BirthDate,
                AgeAtDeparture = p.Age,
                PaxType = band?.ToString(),
                PaxTypeLabel = band is null ? null : band.Value.Label(),
                PaxAmount = band is null ? null : _costCalculator.RateFor(costRow, band),
                Gender = p.Gender,
                PassportNumber = p.PassportNumber,
                Email = p.Email
            });
        }

        return new BookingResponseDTO
        {
            BookingId = b.BookingId,
            BookingDate = b.BookingDate,
            BookingStatus = b.BookingStatus,
            TourId = b.TourId,
            TourName = b.Tour?.TourName,
            Destination = b.Tour?.Destination,
            ScheduleId = b.ScheduleId,
            DepartureDate = departureDate,
            CustomerId = b.UserId,
            CustomerName = b.User?.DisplayName(),
            NoOfPax = paxCount,
            RoomsRequired = rooms
                ?? Occupancy.RoomsRequired(twinSharingCount, singleCount, extraBedCount),
            ExtraBeds = extraBeds ?? extraBedCount,
            TotalAmount = b.TotalAmount,
            PaymentId = payment?.PaymentId,
            PaymentStatus = payment?.PaymentStatus,
            PaymentMethod = payment?.PaymentMethod,
            ReceiptMessage = receiptMessage,
            Passengers = pax
        };
    }

    private static string TrimOrNull(string value)
    {
        if (value is null) return null;
        var trimmed = value.Trim();
        return trimmed.Length == 0 ? null : trimmed;
    }
}
