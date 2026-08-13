namespace ETour.Api.Models;

public class Feedback
{
    public int FeedbackId { get; set; }
    public int? UserId { get; set; }
    public User User { get; set; }
    public string Name { get; set; }
    public string Email { get; set; }
    public string Category { get; set; }
    public int? Rating { get; set; }
    public string Message { get; set; }
    public string Status { get; set; }
    public string PageUrl { get; set; }
    public bool? Published { get; set; }
    public DateTime? CreatedAt { get; set; }
}
