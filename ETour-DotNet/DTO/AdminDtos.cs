using System.ComponentModel.DataAnnotations;

namespace ETour.Api.DTO;

public class BulkImportRequest
{
    public List<TourRequestDTO> Tours { get; set; } = new();
}

public class BulkImportRowResult
{
    public int RowNumber { get; set; }
    public string TourName { get; set; }
    public bool Success { get; set; }
    public int? TourId { get; set; }
    public string Message { get; set; }
}

public class BulkImportResponse
{
    public int Total { get; set; }
    public int Imported { get; set; }
    public int Failed { get; set; }
    public List<BulkImportRowResult> Rows { get; set; } = new();
}

public class TourRequestDTO
{
    [Required(ErrorMessage = "tourName is required")]
    public string TourName { get; set; }
    public string Destination { get; set; }
    public int? Days { get; set; }
    public int? Nights { get; set; }
    public string Description { get; set; }
    public float? Price { get; set; }
    public string Location { get; set; }
    public string TourType { get; set; }
    public int? CategoryId { get; set; }
    public int? SubCategoryId { get; set; }
    public string StayAndMeals { get; set; }
    public string AddOns { get; set; }
    public string PassportAndVisa { get; set; }
    public string Weather { get; set; }
    public string DoAndDont { get; set; }
    public string PrimaryImageUrl { get; set; }
}

public class CostRequestDTO
{
    [Required(ErrorMessage = "tourId is required")]
    public int? TourId { get; set; }
    public decimal? AdultPrice { get; set; }
    public decimal? SinglePersonPrice { get; set; }
    public decimal? ExtraPersonPrice { get; set; }
    public decimal? ChildWithBedPrice { get; set; }
    public decimal? ChildWithoutBedPrice { get; set; }
    public DateOnly? ValidFrom { get; set; }
    public DateOnly? ValidTo { get; set; }
    public bool? IsActive { get; set; } = true;
}

public class ScheduleRequestDTO
{
    [Required(ErrorMessage = "tourId is required")]
    public int? TourId { get; set; }

    [Required(ErrorMessage = "startDate is required")]
    public DateOnly? StartDate { get; set; }

    public int? TotalSeats { get; set; }
    public int? AvailableSeats { get; set; }
    public string Status { get; set; } = "OPEN";
}

public class ItineraryRequestDTO
{
    [Required(ErrorMessage = "tourId is required")]
    public int? TourId { get; set; }
    public int? DayNumber { get; set; }
    public string Description { get; set; }
    public string Location { get; set; }
}

public class AdminBookingDTO
{
    public int? BookingId { get; set; }
    public DateOnly? BookingDate { get; set; }
    public string BookingStatus { get; set; }
    public int? CustomerId { get; set; }
    public string CustomerName { get; set; }
    public string CustomerEmail { get; set; }
    public int? TourId { get; set; }
    public string TourName { get; set; }
    public int? ScheduleId { get; set; }
    public DateOnly? DepartureDate { get; set; }
    public int? NoOfPax { get; set; }
    public decimal? TotalAmount { get; set; }
    public string PaymentStatus { get; set; }
    public string PaymentMethod { get; set; }
    public decimal? RefundAmount { get; set; }
    public string RefundStatus { get; set; }
}

public class AdminUserDTO
{
    public int? UserId { get; set; }
    public string Username { get; set; }
    public string FullName { get; set; }
    public string Email { get; set; }
    public string PhoneNumber { get; set; }
    public string City { get; set; }
    public string Gender { get; set; }
    public string Role { get; set; }
    public bool? Active { get; set; }
    public DateTime? CreatedAt { get; set; }
    public long? TotalBookings { get; set; }
    public long? ActiveBookings { get; set; }
    public long? CancelledBookings { get; set; }
    public decimal? TotalSpent { get; set; }
}
