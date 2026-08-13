using System.ComponentModel.DataAnnotations;

namespace ETour.Api.DTO;

public class CostDTO
{
    public int? CostId { get; set; }
    public decimal? AdultPrice { get; set; }
    public decimal? SinglePersonPrice { get; set; }
    public decimal? ExtraPersonPrice { get; set; }
    public decimal? ChildWithBedPrice { get; set; }
    public decimal? ChildWithoutBedPrice { get; set; }
    public DateOnly? ValidFrom { get; set; }
    public DateOnly? ValidTo { get; set; }
    public bool? IsActive { get; set; }
}

public class ItineraryDTO
{
    public int? ItineraryId { get; set; }
    public int? DayNumber { get; set; }
    public string Description { get; set; }
    public string Location { get; set; }
}

public class ScheduleDTO
{
    public int? ScheduleId { get; set; }
    public DateOnly? StartDate { get; set; }
    public int? AvailableSeats { get; set; }
    public int? TotalSeats { get; set; }
    public string Status { get; set; }
}

public class TourImageDTO
{
    public int? ImageId { get; set; }
    public string Source { get; set; }
    public string ImageTitle { get; set; }
    public bool? IsPrimary { get; set; }
    public DateTime? UploadDate { get; set; }
}

public class TourListDTO
{
    public int? TourId { get; set; }
    public string TourName { get; set; }
    public string Destination { get; set; }
    public string TourType { get; set; }
    public int? Days { get; set; }
    public int? Nights { get; set; }
    public decimal? StartingPrice { get; set; }
    public string PrimaryImageUrl { get; set; }
    public string DurationLabel { get; set; }
    public double? AverageRating { get; set; }
    public long? ReviewCount { get; set; }
}

public class TourDetailDTO
{
    public int? TourId { get; set; }
    public string TourName { get; set; }
    public string Destination { get; set; }
    public int? Days { get; set; }
    public int? Nights { get; set; }
    public string Description { get; set; }
    public float? Price { get; set; }
    public string Location { get; set; }
    public string TourType { get; set; }
    public string DurationLabel { get; set; }
    public double? AverageRating { get; set; }
    public long? ReviewCount { get; set; }

    public long? BookingCount { get; set; }

    public int? CategoryId { get; set; }
    public string CategoryName { get; set; }
    public int? SubCategoryId { get; set; }
    public string SubCategoryName { get; set; }
    public string StayAndMeals { get; set; }
    public string AddOns { get; set; }
    public string PassportAndVisa { get; set; }
    public string Weather { get; set; }
    public string DoAndDont { get; set; }
    public string PrimaryImageUrl { get; set; }
    public List<ItineraryDTO> Itineraries { get; set; } = new();
    public List<ScheduleDTO> Schedules { get; set; } = new();
    public List<CostDTO> Costs { get; set; } = new();
    public List<TourImageDTO> Images { get; set; } = new();
}

public class CostCreateDTO
{
    public decimal? AdultPrice { get; set; }
    public decimal? SinglePersonPrice { get; set; }
    public decimal? ExtraPersonPrice { get; set; }
    public decimal? ChildWithBedPrice { get; set; }
    public decimal? ChildWithoutBedPrice { get; set; }
    public DateOnly? ValidFrom { get; set; }
    public DateOnly? ValidTo { get; set; }
    public bool? IsActive { get; set; } = true;
}

public class ScheduleCreateDTO
{
    public DateOnly? StartDate { get; set; }
    public int? TotalSeats { get; set; }
    public int? AvailableSeats { get; set; }
    public string Status { get; set; } = "OPEN";
}

public class ItineraryCreateDTO
{
    public int? DayNumber { get; set; }
    public string Description { get; set; }
    public string Location { get; set; }
}

public class TourImageCreateDTO
{
    public string Source { get; set; }
    public string ImageTitle { get; set; }
    public bool? IsPrimary { get; set; } = false;
}

public class TourCreateRequestDTO
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

    public List<CostCreateDTO> Costs { get; set; } = new();
    public List<ScheduleCreateDTO> Schedules { get; set; } = new();
    public List<ItineraryCreateDTO> Itineraries { get; set; } = new();
    public List<TourImageCreateDTO> Images { get; set; } = new();
}
