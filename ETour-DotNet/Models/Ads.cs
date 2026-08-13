namespace ETour.Api.Models;

public class Ads
{
    public int AdId { get; set; }
    public string Title { get; set; }
    public string ImagePath { get; set; }
    public string LinkUrl { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public int? DisplayOrder { get; set; }
    public bool? Active { get; set; }
}
