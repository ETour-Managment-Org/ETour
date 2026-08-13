namespace ETour.Api.Models;

public class Cost
{
    public int CostId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public decimal? AdultPrice { get; set; }
    public decimal? SinglePersonPrice { get; set; }
    public decimal? ExtraPersonPrice { get; set; }
    public decimal? ChildWithBedPrice { get; set; }
    public decimal? ChildWithoutBedPrice { get; set; }
    public DateOnly? ValidFrom { get; set; }
    public DateOnly? ValidTo { get; set; }
    public bool? IsActive { get; set; }
}
