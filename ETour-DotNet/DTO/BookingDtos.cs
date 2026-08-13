using System.ComponentModel.DataAnnotations;

namespace ETour.Api.DTO;

public class PassengerDTO
{
    [Required(ErrorMessage = "fullName is required")]
    public string FullName { get; set; }

    [Required(ErrorMessage = "birthDate is required")]
    public DateOnly? BirthDate { get; set; }

    public string Gender { get; set; }

    [EmailAddress(ErrorMessage = "email is not a valid address")]
    public string Email { get; set; }

    public string PassportNumber { get; set; }
    public bool? WithBed { get; set; } = true;
    public string Occupancy { get; set; }
}

public class BookingRequestDTO
{
    [Required(ErrorMessage = "tourId is required")]
    public int? TourId { get; set; }

    [Required(ErrorMessage = "scheduleId is required")]
    public int? ScheduleId { get; set; }

    [MinLength(1, ErrorMessage = "at least one passenger is required")]
    public List<PassengerDTO> Passengers { get; set; } = new();

    public string PaymentMethod { get; set; }

    [StringLength(120, ErrorMessage = "contact name must be 120 characters or fewer")]
    public string ContactName { get; set; }

    [EmailAddress(ErrorMessage = "contact email is not a valid address")]
    [StringLength(150, ErrorMessage = "contact email must be 150 characters or fewer")]
    public string ContactEmail { get; set; }

    [RegularExpression(@"^$|^[0-9]{10}$", ErrorMessage = "contact phone must be exactly 10 digits")]
    public string ContactPhone { get; set; }

    public string RazorpayOrderId { get; set; }
    public string RazorpayPaymentId { get; set; }
    public string RazorpaySignature { get; set; }
}

public class PassengerResponseDTO
{
    public int? PaxId { get; set; }
    public string FullName { get; set; }
    public DateOnly? BirthDate { get; set; }
    public int? AgeAtDeparture { get; set; }
    public string PaxType { get; set; }
    public string PaxTypeLabel { get; set; }
    public decimal? PaxAmount { get; set; }
    public string Gender { get; set; }
    public string PassportNumber { get; set; }
    public string Email { get; set; }
}

public class BookingResponseDTO
{
    public int? BookingId { get; set; }
    public int? CustomerBookingNumber { get; set; }
    public DateOnly? BookingDate { get; set; }
    public string BookingStatus { get; set; }
    public int? TourId { get; set; }
    public string TourName { get; set; }
    public string Destination { get; set; }
    public int? ScheduleId { get; set; }
    public DateOnly? DepartureDate { get; set; }
    public int? CustomerId { get; set; }
    public string CustomerName { get; set; }
    public int? NoOfPax { get; set; }
    public int? RoomsRequired { get; set; }
    public int? ExtraBeds { get; set; }
    public decimal? TotalAmount { get; set; }
    public int? PaymentId { get; set; }
    public string PaymentStatus { get; set; }
    public string PaymentMethod { get; set; }
    public string ReceiptMessage { get; set; }
    public List<PassengerResponseDTO> Passengers { get; set; } = new();
}

public class CancellationRequestDTO
{
    public string Reason { get; set; }
}

public class CancellationResponseDTO
{
    public int? CancellationId { get; set; }
    public int? BookingId { get; set; }
    public string BookingStatus { get; set; }
    public DateTime? CancellationDate { get; set; }
    public string Reason { get; set; }
    public decimal? AmountPaid { get; set; }
    public decimal? RefundAmount { get; set; }
    public string RefundStatus { get; set; }
    public string Remarks { get; set; }
}
