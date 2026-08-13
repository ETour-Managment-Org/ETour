using ETour.Api.DTO;
using ETour.Api.Security;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize]
[Route("api/reviews")]
public class ReviewController : ControllerBase
{
    private readonly IReviewService _reviews;

    public ReviewController(IReviewService reviews) => _reviews = reviews;

    [HttpPost]
    public async Task<ActionResult<ReviewResponseDTO>> SubmitReview(
        [FromBody] ReviewRequestDTO request) =>
        StatusCode(StatusCodes.Status201Created,
            await _reviews.CreateReviewAsync(request, User.Username()));

    [AllowAnonymous]
    [HttpGet("tour/{tourId:int}")]
    public async Task<ActionResult<ReviewSummaryDTO>> TourReviews(int tourId) =>
        Ok(await _reviews.GetTourReviewsAsync(tourId));

    [HttpGet("my")]
    public async Task<ActionResult<List<ReviewResponseDTO>>> MyReviews() =>
        Ok(await _reviews.GetMyReviewsAsync(User.Username()));

    [HttpGet("reviewable")]
    public async Task<ActionResult<List<int>>> ReviewableBookings() =>
        Ok(await _reviews.GetReviewableBookingIdsAsync(User.Username()));

    [HttpPut("{reviewId:int}")]
    public async Task<ActionResult<ReviewResponseDTO>> UpdateReview(
        int reviewId, [FromBody] ReviewRequestDTO request) =>
        Ok(await _reviews.UpdateReviewAsync(reviewId, request, User.Username()));

    [HttpDelete("{reviewId:int}")]
    public async Task<IActionResult> DeleteReview(int reviewId)
    {
        await _reviews.DeleteReviewAsync(reviewId, User.Username());
        return NoContent();
    }
}
