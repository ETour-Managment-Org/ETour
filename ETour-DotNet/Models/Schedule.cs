namespace ETour.Api.Models;

public class Schedule
{
    public int ScheduleId { get; set; }
    public int? TourId { get; set; }
    public Tour Tour { get; set; }
    public int? AvailableSeats { get; set; }
    public int? TotalSeats { get; set; }
    public string Status { get; set; }
    public DateOnly? StartDate { get; set; }
}
