namespace ETour.Api.Models;

public class Booking
{
    public int BookingId { get; set; }

    public int? UserId { get; set; }
    public User User { get; set; }

    public int? TourId { get; set; }
    public Tour Tour { get; set; }

    public int? ScheduleId { get; set; }
    public Schedule Schedule { get; set; }

    public DateOnly? BookingDate { get; set; }
    public decimal? TotalAmount { get; set; }
    public string BookingStatus { get; set; }

    public string ContactName { get; set; }
    public string ContactEmail { get; set; }
    public string ContactPhone { get; set; }

    public ICollection<PassengerDetails> Passengers { get; set; } = new List<PassengerDetails>();
    public ICollection<Payment> Payments { get; set; } = new List<Payment>();
    public Cancellation Cancellation { get; set; }
    public ICollection<Review> Reviews { get; set; } = new List<Review>();

    public string ResolveNotificationEmail()
    {
        if (!string.IsNullOrWhiteSpace(ContactEmail))
        {
            return ContactEmail.Trim();
        }
        return User?.Email;
    }
}
