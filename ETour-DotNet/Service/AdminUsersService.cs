using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class AdminUsersService : IAdminUsersService
{
    private readonly ETourDbContext _db;

    public AdminUsersService(ETourDbContext db) => _db = db;

    public async Task<List<AdminBookingDTO>> GetAllBookingsAsync() =>
        await BookingQuery().OrderByDescending(b => b.BookingDate)
            .ThenByDescending(b => b.BookingId)
            .Select(b => ToAdminBookingDto(b)).ToListAsync();

    public async Task<List<AdminBookingDTO>> GetBookingsByStatusAsync(string status) =>
        await BookingQuery().Where(b => b.BookingStatus == status)
            .OrderByDescending(b => b.BookingId)
            .Select(b => ToAdminBookingDto(b)).ToListAsync();

    public async Task<List<AdminBookingDTO>> GetBookingsForUserAsync(int userId)
    {
        if (!await _db.Users.AnyAsync(u => u.UserId == userId))
            throw new ResourceNotFoundException("User", userId);

        return await BookingQuery().Where(b => b.UserId == userId)
            .OrderBy(b => b.BookingId)
            .Select(b => ToAdminBookingDto(b)).ToListAsync();
    }

    private IQueryable<Booking> BookingQuery() =>
        _db.Bookings.AsNoTracking()
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .Include(b => b.Passengers).Include(b => b.Payments)
            .Include(b => b.Cancellation);

    private static AdminBookingDTO ToAdminBookingDto(Booking b) => new()
    {
        BookingId = b.BookingId,
        BookingDate = b.BookingDate,
        BookingStatus = b.BookingStatus,
        CustomerId = b.UserId,
        CustomerName = b.User == null ? null : b.User.DisplayName(),
        CustomerEmail = b.User == null ? null : b.User.Email,
        TourId = b.TourId,
        TourName = b.Tour == null ? null : b.Tour.TourName,
        ScheduleId = b.ScheduleId,
        DepartureDate = b.Schedule == null ? null : b.Schedule.StartDate,
        NoOfPax = b.Passengers.Count,
        TotalAmount = b.TotalAmount,
        PaymentStatus = b.Payments.OrderByDescending(p => p.PaymentId)
            .Select(p => p.PaymentStatus).FirstOrDefault(),
        PaymentMethod = b.Payments.OrderByDescending(p => p.PaymentId)
            .Select(p => p.PaymentMethod).FirstOrDefault(),
        RefundAmount = b.Cancellation == null ? null : b.Cancellation.RefundAmount,
        RefundStatus = b.Cancellation == null ? null : b.Cancellation.RefundStatus
    };

    public async Task<List<AdminUserDTO>> GetAllUsersAsync()
    {
        var users = await _db.Users.AsNoTracking().Include(u => u.Role)
            .OrderBy(u => u.UserId).ToListAsync();

        var bookings = await _db.Bookings.AsNoTracking()
            .Select(b => new { b.UserId, b.BookingStatus, b.TotalAmount }).ToListAsync();

        return users.Select(u => ToAdminUserDto(u,
            bookings.Where(b => b.UserId == u.UserId)
                    .Select(b => (b.BookingStatus, b.TotalAmount)).ToList())).ToList();
    }

    public async Task<AdminUserDTO> GetUserAsync(int userId)
    {
        var user = await _db.Users.AsNoTracking().Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.UserId == userId)
            ?? throw new ResourceNotFoundException("User", userId);

        var bookings = await _db.Bookings.AsNoTracking()
            .Where(b => b.UserId == userId)
            .Select(b => new { b.BookingStatus, b.TotalAmount }).ToListAsync();

        return ToAdminUserDto(user,
            bookings.Select(b => (b.BookingStatus, b.TotalAmount)).ToList());
    }

    public async Task<AdminUserDTO> SetUserActiveAsync(int userId, bool active)
    {
        var user = await _db.Users.Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.UserId == userId)
            ?? throw new ResourceNotFoundException("User", userId);

        if (user.Role?.RoleName == RoleName.Admin && !active)
            throw new ArgumentException("An administrator account cannot be deactivated");

        user.Isactive = active;
        await _db.SaveChangesAsync();
        return await GetUserAsync(userId);
    }

    private static AdminUserDTO ToAdminUserDto(User user,
        List<(string Status, decimal? Amount)> bookings)
    {
        var cancelled = bookings.Count(b => b.Status == BookingStatus.Cancelled);
        var active = bookings.Count(b => b.Status != BookingStatus.Cancelled
                                      && b.Status != BookingStatus.Completed);
        var spent = bookings.Where(b => b.Status != BookingStatus.Cancelled)
                            .Sum(b => b.Amount ?? 0m);

        return new AdminUserDTO
        {
            UserId = user.UserId,
            Username = user.Username,
            FullName = user.DisplayName(),
            Email = user.Email,
            PhoneNumber = user.PhoneNumber,
            City = user.City,
            Gender = user.Gender,
            Role = user.Role?.RoleName,
            Active = user.Isactive,
            CreatedAt = user.CreatedAt,
            TotalBookings = bookings.Count,
            ActiveBookings = active,
            CancelledBookings = cancelled,
            TotalSpent = spent
        };
    }
}
