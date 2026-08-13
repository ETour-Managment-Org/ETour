using System.Security.Claims;
using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.Models;
using ETour.Api.Options;
using ETour.Api.Security;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.Google;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;

namespace ETour.Api.Controllers;

[ApiController]
[AllowAnonymous]
public class OAuthController : ControllerBase
{
    private readonly ETourDbContext _db;
    private readonly JwtTokenService _jwt;
    private readonly OAuthOptions _oauth;
    private readonly ILogger<OAuthController> _log;

    public OAuthController(ETourDbContext db, JwtTokenService jwt,
                           IOptions<OAuthOptions> oauth, ILogger<OAuthController> log)
    {
        _db = db;
        _jwt = jwt;
        _oauth = oauth.Value;
        _log = log;
    }

    [HttpGet("/oauth2/authorization/google")]
    public IActionResult StartGoogle()
    {
        if (!_oauth.IsConfigured())
        {
            return BadRequest(new { message = "Google sign-in is not configured on this server." });
        }

        return Challenge(
            new AuthenticationProperties { RedirectUri = "/login/oauth2/code/google" },
            GoogleDefaults.AuthenticationScheme);
    }

    [HttpGet("/login/oauth2/code/google")]
    public async Task<IActionResult> GoogleCallback()
    {
        var result = await HttpContext.AuthenticateAsync(
                CookieAuthenticationDefaults.AuthenticationScheme);

        if (!result.Succeeded)
        {
            return Redirect($"{_oauth.RedirectUri}?error=google-sign-in-failed");
        }

        var email = result.Principal.FindFirstValue(ClaimTypes.Email);
        var providerId = result.Principal.FindFirstValue(ClaimTypes.NameIdentifier);
        var firstName = result.Principal.FindFirstValue(ClaimTypes.GivenName);
        var lastName = result.Principal.FindFirstValue(ClaimTypes.Surname);

        if (string.IsNullOrWhiteSpace(email))
        {
            return Redirect($"{_oauth.RedirectUri}?error=no-email-from-google");
        }

        var user = await _db.Users.Include(u => u.Role)
            .FirstOrDefaultAsync(u => u.Email == email);

        if (user is null)
        {
            var role = await _db.Roles.FirstOrDefaultAsync(r => r.RoleName == RoleName.Customer);

            user = new User
            {
                RoleId = role?.RoleId,
                Username = await UniqueUsernameAsync(email),
                Email = email,
                FirstName = firstName,
                LastName = lastName,
                PasswordHash = null,
                AuthProvider = "GOOGLE",
                ProviderId = providerId,
                CreatedAt = DateTime.Now,
                Isactive = true
            };

            _db.Users.Add(user);
            await _db.SaveChangesAsync();
            _log.LogInformation("Created Google account for {Email}", email);

            user.Role = role;
        }
        else if (user.AuthProvider is null || user.AuthProvider == "LOCAL")
        {
            user.AuthProvider = "GOOGLE";
            user.ProviderId = providerId;
            await _db.SaveChangesAsync();
        }

        var token = _jwt.GenerateToken(user.Username, user.Role?.RoleName ?? RoleName.Customer);
        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);

        return Redirect($"{_oauth.RedirectUri}?token={Uri.EscapeDataString(token)}");
    }

    private async Task<string> UniqueUsernameAsync(string email)
    {
        var baseName = email.Split('@')[0];
        if (baseName.Length > 40) baseName = baseName[..40];

        var candidate = baseName;
        var suffix = 1;

        while (await _db.Users.AnyAsync(u => u.Username == candidate))
        {
            candidate = $"{baseName}{suffix++}";
        }
        return candidate;
    }
}
