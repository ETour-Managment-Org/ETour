namespace ETour.Api.Models;

public class TourImages
{
    public int ImageId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public string Source { get; set; }
    public string ImageTitle { get; set; }
    public bool? IsPrimary { get; set; }
    public DateTime? UploadDate { get; set; }
}
