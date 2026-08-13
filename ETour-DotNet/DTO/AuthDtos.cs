using System.ComponentModel.DataAnnotations;

namespace ETour.Api.DTO;

public class LoginRequest
{
    [Required(ErrorMessage = "username is required")]
    public string Username { get; set; }

    [Required(ErrorMessage = "password is required")]
    public string Password { get; set; }
}

public class RegisterRequest
{
    [Required(ErrorMessage = "username is required")]
    [StringLength(50, MinimumLength = 3, ErrorMessage = "username must be 3 to 50 characters")]
    public string Username { get; set; }

    [Required(ErrorMessage = "password is required")]
    [MinLength(8, ErrorMessage = "password must be at least 8 characters")]
    public string Password { get; set; }

    [Required(ErrorMessage = "email is required")]
    [EmailAddress(ErrorMessage = "email is not a valid address")]
    public string Email { get; set; }

    public string FirstName { get; set; }
    public string LastName { get; set; }

    [RegularExpression(@"^$|^[0-9]{10}$", ErrorMessage = "phone number must be exactly 10 digits")]
    public string PhoneNumber { get; set; }

    public string Address { get; set; }
    public string Gender { get; set; }
    public string City { get; set; }
    public string State { get; set; }
    public string PinCode { get; set; }
}

public class AuthResponse
{
    public string Token { get; set; }
    public string TokenType { get; set; } = "Bearer";
    public long? ExpiresInMs { get; set; }
    public int? UserId { get; set; }
    public string Username { get; set; }
    public string Email { get; set; }
    public string FullName { get; set; }
    public string Role { get; set; }
}

public class UserProfileDTO
{
    public int? UserId { get; set; }
    public string Username { get; set; }
    public string FullName { get; set; }
    public string FirstName { get; set; }
    public string LastName { get; set; }
    public string Email { get; set; }
    public string PhoneNumber { get; set; }
    public string Address { get; set; }
    public string City { get; set; }
    public string Gender { get; set; }
    public string Role { get; set; }
    public string AuthProvider { get; set; }
}

public class UpdateProfileRequest
{
    public string FirstName { get; set; }
    public string LastName { get; set; }

    [EmailAddress(ErrorMessage = "email is not a valid address")]
    public string Email { get; set; }

    [RegularExpression(@"^$|^[0-9]{10}$", ErrorMessage = "phone number must be exactly 10 digits")]
    public string PhoneNumber { get; set; }

    public string Address { get; set; }
    public string City { get; set; }
    public string Gender { get; set; }
}

public class ChangePasswordRequest
{
    [Required(ErrorMessage = "current password is required")]
    public string CurrentPassword { get; set; }

    [Required(ErrorMessage = "new password is required")]
    [MinLength(8, ErrorMessage = "new password must be at least 8 characters")]
    public string NewPassword { get; set; }
}
