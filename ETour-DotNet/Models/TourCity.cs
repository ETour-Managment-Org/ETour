namespace ETour.Api.Models;

public class TourCity
{
    public int CityId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public string CityName { get; set; }
    public int? StopOrder { get; set; }
}
