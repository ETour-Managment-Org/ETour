using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface ICancellationService
{
    Task<CancellationResponseDTO> CancelBookingAsync(int bookingId,
        CancellationRequestDTO request, string username, bool isAdmin);
}
