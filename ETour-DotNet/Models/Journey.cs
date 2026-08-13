namespace ETour.Api.Models;

public class Journey
{
    public int JourneyId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public string Source { get; set; }
    public string Destination { get; set; }
    public string Transport { get; set; }
    public string Details { get; set; }
}
