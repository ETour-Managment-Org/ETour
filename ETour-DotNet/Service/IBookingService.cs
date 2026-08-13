using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface IBookingService
{
    Task<BookingResponseDTO> PlaceBookingAsync(BookingRequestDTO request, string username);
    Task<BookingResponseDTO> GetBookingAsync(int bookingId, string username, bool isAdmin);
    Task<List<BookingResponseDTO>> GetMyBookingsAsync(string username);
    Task<int> CompleteElapsedBookingsAsync();
}
