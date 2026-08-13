namespace ETour.Api.Models;

public class Itinerary
{
    public int ItineraryId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public int? DayNumber { get; set; }
    public string Description { get; set; }
    public string Location { get; set; }
}
