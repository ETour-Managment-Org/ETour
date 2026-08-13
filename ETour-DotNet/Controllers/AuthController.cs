using ETour.Api.DTO;
using ETour.Api.Options;
using ETour.Api.Security;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;

namespace ETour.Api.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _auth;
    private readonly OAuthOptions _oauth;

    public AuthController(IAuthService auth, IOptions<OAuthOptions> oauth)
    {
        _auth = auth;
        _oauth = oauth.Value;
    }

    [HttpPost("register")]
    public async Task<ActionResult<AuthResponse>> Register([FromBody] RegisterRequest request) =>
        StatusCode(StatusCodes.Status201Created, await _auth.RegisterAsync(request));

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login([FromBody] LoginRequest request) =>
        Ok(await _auth.LoginAsync(request));

    [Authorize]
    [HttpGet("me")]
    public async Task<ActionResult<UserProfileDTO>> Me() =>
        Ok(await _auth.GetProfileAsync(User.Username()));

    [Authorize]
    [HttpPut("me")]
    public async Task<ActionResult<UserProfileDTO>> UpdateMe(
        [FromBody] UpdateProfileRequest request) =>
        Ok(await _auth.UpdateProfileAsync(User.Username(), request));

    [Authorize]
    [HttpPost("change-password")]
    public async Task<ActionResult<Dictionary<string, string>>> ChangePassword(
        [FromBody] ChangePasswordRequest request)
    {
        await _auth.ChangePasswordAsync(User.Username(), request);
        return Ok(new Dictionary<string, string>
        {
            ["message"] = "Your password has been changed."
        });
    }

    [AllowAnonymous]
    [HttpGet("providers")]
    public ActionResult<Dictionary<string, object>> Providers() =>
        Ok(new Dictionary<string, object>
        {
            ["google"] = _oauth.IsConfigured(),
            ["googleAuthUrl"] = _oauth.IsConfigured() ? "/oauth2/authorization/google" : null
        });
}
