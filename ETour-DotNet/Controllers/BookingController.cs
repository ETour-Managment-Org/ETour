using ETour.Api.DTO;
using ETour.Api.Security;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize]
[Route("api/bookings")]
public class BookingController : ControllerBase
{
    private readonly IBookingService _bookings;
    private readonly ICancellationService _cancellations;

    public BookingController(IBookingService bookings, ICancellationService cancellations)
    {
        _bookings = bookings;
        _cancellations = cancellations;
    }

    [HttpPost("place")]
    public async Task<ActionResult<BookingResponseDTO>> PlaceBooking(
        [FromBody] BookingRequestDTO request) =>
        StatusCode(StatusCodes.Status201Created,
            await _bookings.PlaceBookingAsync(request, User.Username()));

    [HttpGet("my")]
    public async Task<ActionResult<List<BookingResponseDTO>>> MyBookings() =>
        Ok(await _bookings.GetMyBookingsAsync(User.Username()));

    [HttpGet("{bookingId:int}")]
    public async Task<ActionResult<BookingResponseDTO>> GetBooking(int bookingId) =>
        Ok(await _bookings.GetBookingAsync(bookingId, User.Username(), User.IsAdmin()));

    [HttpPost("{bookingId:int}/cancel")]
    public async Task<ActionResult<CancellationResponseDTO>> CancelBooking(
        int bookingId, [FromBody] CancellationRequestDTO request) =>
        Ok(await _cancellations.CancelBookingAsync(
            bookingId, request, User.Username(), User.IsAdmin()));
}
