namespace ETour.Api.Models;

public class Cancellation
{
    public int CancellationId { get; set; }
    public int? BookingId { get; set; }
    public Booking Booking { get; set; }
    public DateTime? CancellationDate { get; set; }
    public string Reason { get; set; }
    public decimal? RefundAmount { get; set; }
    public string RefundStatus { get; set; }
    public string Remarks { get; set; }
}
