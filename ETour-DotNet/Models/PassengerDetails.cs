namespace ETour.Api.Models;

public class PassengerDetails
{
    public int PaxId { get; set; }
    public int? BookingId { get; set; }
    public Booking Booking { get; set; }
    public string FullName { get; set; }
    public string Email { get; set; }
    public string Gender { get; set; }
    public DateOnly? BirthDate { get; set; }
    public int? Age { get; set; }
    public string PassportNumber { get; set; }
    public string PaxType { get; set; }
}
