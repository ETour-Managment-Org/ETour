namespace ETour.Api.Models;

public class Notification
{
    public int NotificationId { get; set; }
    public int? UserId { get; set; }
    public User User { get; set; }
    public string Title { get; set; }
    public string Message { get; set; }
    public bool? IsRead { get; set; }
    public DateTime? CreatedDate { get; set; }
}
