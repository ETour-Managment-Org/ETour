namespace ETour.Api.Models;

public class User
{
    public int UserId { get; set; }

    public int? RoleId { get; set; }
    public Role Role { get; set; }

    public string Username { get; set; }
    public string PasswordHash { get; set; }
    public string Email { get; set; }
    public string FirstName { get; set; }
    public string LastName { get; set; }
    public string PhoneNumber { get; set; }
    public string Address { get; set; }
    public string City { get; set; }
    public string Gender { get; set; }
    public string AuthProvider { get; set; }
    public string ProviderId { get; set; }
    public DateTime? CreatedAt { get; set; }
    public bool? Isactive { get; set; }

    public ICollection<Booking> Bookings { get; set; } = new List<Booking>();
    public ICollection<Review> Reviews { get; set; } = new List<Review>();
    public ICollection<Notification> Notifications { get; set; } = new List<Notification>();

    public string DisplayName()
    {
        var full = $"{FirstName} {LastName}".Trim();
        return string.IsNullOrWhiteSpace(full) ? Username : full;
    }
}
