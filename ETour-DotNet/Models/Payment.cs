namespace ETour.Api.Models;

public class Payment
{
    public int PaymentId { get; set; }
    public int? BookingId { get; set; }
    public Booking Booking { get; set; }
    public decimal? Amount { get; set; }
    public DateOnly? PaymentDate { get; set; }
    public string PaymentStatus { get; set; }
    public string PaymentMethod { get; set; }

    public string TransactionRef { get; set; }
}
