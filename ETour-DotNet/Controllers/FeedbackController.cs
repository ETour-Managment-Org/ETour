using ETour.Api.Common;
using ETour.Api.DTO;
using ETour.Api.Security;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[AllowAnonymous]
[Route("api/feedback")]
public class FeedbackController : ControllerBase
{
    private readonly IFeedbackService _feedback;

    public FeedbackController(IFeedbackService feedback) => _feedback = feedback;

    [HttpPost]
    public async Task<ActionResult<FeedbackResponseDTO>> Submit(
        [FromBody] FeedbackRequestDTO request) =>
        StatusCode(StatusCodes.Status201Created,
            await _feedback.SubmitAsync(request, User.Username()));

    [HttpGet("published")]
    public async Task<ActionResult<List<PublicFeedbackDTO>>> Published() =>
        Ok(await _feedback.GetPublishedAsync());

    [HttpGet("categories")]
    public ActionResult<Dictionary<string, object>> Categories() =>
        Ok(new Dictionary<string, object> { ["categories"] = FeedbackCategory.All });
}
