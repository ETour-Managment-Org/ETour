using ETour.Api.Common;
using ETour.Api.DTO;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize(Roles = RoleName.Admin)]
[Route("api/admin")]
public class AdminUsersController : ControllerBase
{
    private readonly IAdminUsersService _admin;
    private readonly IFeedbackService _feedback;
    private readonly IAuthService _auth;

    public AdminUsersController(IAdminUsersService admin, IFeedbackService feedback,
                                IAuthService auth)
    {
        _admin = admin;
        _feedback = feedback;
        _auth = auth;
    }

    [HttpGet("bookings")]
    public async Task<ActionResult<List<AdminBookingDTO>>> AllBookings(
        [FromQuery] string status) =>
        Ok(string.IsNullOrWhiteSpace(status)
            ? await _admin.GetAllBookingsAsync()
            : await _admin.GetBookingsByStatusAsync(status));

    [HttpGet("users")]
    public async Task<ActionResult<List<AdminUserDTO>>> AllUsers() =>
        Ok(await _admin.GetAllUsersAsync());

    [HttpGet("users/{userId:int}")]
    public async Task<ActionResult<AdminUserDTO>> GetUser(int userId) =>
        Ok(await _admin.GetUserAsync(userId));

    [HttpGet("users/{userId:int}/bookings")]
    public async Task<ActionResult<List<AdminBookingDTO>>> UserBookings(int userId) =>
        Ok(await _admin.GetBookingsForUserAsync(userId));

    [HttpPatch("users/{userId:int}/active")]
    public async Task<ActionResult<AdminUserDTO>> SetUserActive(
        int userId, [FromQuery] bool active) =>
        Ok(await _admin.SetUserActiveAsync(userId, active));

    [HttpPatch("users/{userId:int}/password")]
    public async Task<ActionResult<Dictionary<string, string>>> ResetUserPassword(
        int userId, [FromBody] Dictionary<string, string> body)
    {
        await _auth.ResetPasswordAsync(userId, body.GetValueOrDefault("newPassword"));
        return Ok(new Dictionary<string, string> { ["message"] = "Password reset." });
    }

    [HttpGet("feedback")]
    public async Task<ActionResult<List<FeedbackResponseDTO>>> Feedback(
        [FromQuery] string status) =>
        Ok(await _feedback.GetAllAsync(status));

    [HttpPatch("feedback/{feedbackId:int}/status")]
    public async Task<ActionResult<FeedbackResponseDTO>> UpdateFeedbackStatus(
        int feedbackId, [FromQuery] string status) =>
        Ok(await _feedback.UpdateStatusAsync(feedbackId, status));

    [HttpPatch("feedback/{feedbackId:int}/publish")]
    public async Task<ActionResult<FeedbackResponseDTO>> PublishFeedback(
        int feedbackId, [FromQuery] bool published) =>
        Ok(await _feedback.SetPublishedAsync(feedbackId, published));
}
