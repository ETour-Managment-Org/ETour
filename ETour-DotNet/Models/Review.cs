namespace ETour.Api.Models;

public class Review
{
    public int ReviewId { get; set; }

    public int? BookingId { get; set; }
    public Booking Booking { get; set; }

    public int? CustomerId { get; set; }
    public User Customer { get; set; }

    public int? TourId { get; set; }
    public Tour Tour { get; set; }

    public int? Rating { get; set; }
    public string ReviewTitle { get; set; }
    public string ReviewDescription { get; set; }
    public DateOnly? ReviewDate { get; set; }
    public string VerificationStatus { get; set; }
}
