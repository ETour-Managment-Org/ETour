using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using ETour.Api.Security;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class AuthService : IAuthService
{
    private readonly ETourDbContext _db;
    private readonly JwtTokenService _jwt;
    private readonly IEmailService _email;
    private readonly ILogger<AuthService> _log;

    public AuthService(ETourDbContext db, JwtTokenService jwt,
                       IEmailService email, ILogger<AuthService> log)
    {
        _db = db;
        _jwt = jwt;
        _email = email;
        _log = log;
    }

    public async Task<AuthResponse> RegisterAsync(RegisterRequest r)
    {
        if (await _db.Users.AnyAsync(u => u.Username == r.Username))
            throw new ArgumentException($"Username '{r.Username}' is already taken");

        if (await _db.Users.AnyAsync(u => u.Email == r.Email))
            throw new ArgumentException($"An account already exists for {r.Email}");

        if (!Gender.IsValid(r.Gender))
            throw new ArgumentException(
                $"gender must be one of {string.Join(", ", Gender.All)} or left empty");

        var role = await _db.Roles.FirstOrDefaultAsync(x => x.RoleName == RoleName.Customer)
            ?? throw new ResourceNotFoundException($"Role {RoleName.Customer} is missing from the database");

        var user = new User
        {
            RoleId = role.RoleId,
            Username = r.Username,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(r.Password),
            Email = r.Email,
            FirstName = r.FirstName,
            LastName = r.LastName,
            PhoneNumber = r.PhoneNumber,
            Address = r.Address,
            City = r.City,
            Gender = Gender.Normalise(r.Gender),
            AuthProvider = "LOCAL",
            CreatedAt = DateTime.Now,
            Isactive = true
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        _email.SendWelcomeEmail(user);

        var token = _jwt.GenerateToken(user.Username, RoleName.Customer);
        return BuildResponse(token, user, RoleName.Customer);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest r)
    {
        var user = await _db.Users.Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.Username == r.Username);

        if (user is null
            || string.IsNullOrWhiteSpace(user.PasswordHash)
            || !BCrypt.Net.BCrypt.Verify(r.Password, user.PasswordHash))
        {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.Isactive == false)
        {
            throw new BadCredentialsException("This account has been deactivated");
        }

        var roleName = user.Role?.RoleName ?? RoleName.Customer;
        var token = _jwt.GenerateToken(user.Username, roleName);
        return BuildResponse(token, user, roleName);
    }

    private AuthResponse BuildResponse(string token, User user, string roleName) => new()
    {
        Token = token,
        TokenType = "Bearer",
        ExpiresInMs = _jwt.ExpirationMs,
        UserId = user.UserId,
        Username = user.Username,
        Email = user.Email,
        FullName = user.DisplayName(),
        Role = roleName
    };

    public async Task<UserProfileDTO> GetProfileAsync(string username)
    {
        var user = await _db.Users.AsNoTracking().Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException("User", username);

        return new UserProfileDTO
        {
            UserId = user.UserId,
            Username = user.Username,
            FullName = user.DisplayName(),
            FirstName = user.FirstName,
            LastName = user.LastName,
            Email = user.Email,
            PhoneNumber = user.PhoneNumber,
            Address = user.Address,
            City = user.City,
            Gender = user.Gender,
            Role = user.Role?.RoleName,
            AuthProvider = user.AuthProvider
        };
    }

    public async Task<UserProfileDTO> UpdateProfileAsync(string username, UpdateProfileRequest r)
    {
        var user = await _db.Users.Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException("User", username);

        var newEmail = r.Email;
        if (newEmail is not null
            && !string.Equals(newEmail, user.Email, StringComparison.OrdinalIgnoreCase)
            && await _db.Users.AnyAsync(u => u.Email == newEmail))
        {
            throw new ArgumentException($"An account already exists for {newEmail}");
        }

        if (!Gender.IsValid(r.Gender))
            throw new ArgumentException(
                $"gender must be one of {string.Join(", ", Gender.All)} or left empty");

        user.FirstName = r.FirstName;
        user.LastName = r.LastName;
        user.Email = newEmail;
        user.PhoneNumber = r.PhoneNumber;
        user.Address = r.Address;
        user.City = r.City;
        user.Gender = Gender.Normalise(r.Gender);

        await _db.SaveChangesAsync();
        return await GetProfileAsync(user.Username);
    }

    public async Task ChangePasswordAsync(string username, ChangePasswordRequest r)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException("User", username);

        if (string.IsNullOrWhiteSpace(user.PasswordHash))
            throw new ConflictException(
                "This account signs in with Google and has no password to change.");

        if (!BCrypt.Net.BCrypt.Verify(r.CurrentPassword, user.PasswordHash))
            throw new BadCredentialsException("Your current password is not correct");

        if (BCrypt.Net.BCrypt.Verify(r.NewPassword, user.PasswordHash))
            throw new ArgumentException("The new password must be different from the current one");

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(r.NewPassword);
        await _db.SaveChangesAsync();

        _email.SendPasswordChangedEmail(user, false);
        _log.LogInformation("Password changed for {Username}", username);
    }

    public async Task ResetPasswordAsync(int userId, string newPassword)
    {
        if (newPassword is null || newPassword.Length < 8)
            throw new ArgumentException("new password must be at least 8 characters");

        var user = await _db.Users.FindAsync(userId)
            ?? throw new ResourceNotFoundException("User", userId);

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(newPassword);
        await _db.SaveChangesAsync();

        _email.SendPasswordChangedEmail(user, true);
        _log.LogInformation("Password reset by an administrator for {Username}", user.Username);
    }
}
