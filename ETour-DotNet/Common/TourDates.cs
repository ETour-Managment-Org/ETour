using ETour.Api.Models;

namespace ETour.Api.Common;

public static class TourDates
{
    public static DateOnly? ReturnDate(Schedule schedule, Tour tour)
    {
        if (schedule?.StartDate is null)
        {
            return null;
        }

        var departure = schedule.StartDate.Value;
        var days = tour?.Days;

        if (days is null || days < 1)
        {
            return departure;
        }
        return departure.AddDays(days.Value - 1);
    }

    public static DateOnly? ReturnDate(Booking booking) =>
        booking is null ? null : ReturnDate(booking.Schedule, booking.Tour);

    public static bool HasFinished(Booking booking, DateOnly today)
    {
        var end = ReturnDate(booking);
        return end is not null && end < today;
    }
}
