using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface IAdminUsersService
{
    Task<List<AdminBookingDTO>> GetAllBookingsAsync();
    Task<List<AdminBookingDTO>> GetBookingsByStatusAsync(string status);
    Task<List<AdminBookingDTO>> GetBookingsForUserAsync(int userId);

    Task<List<AdminUserDTO>> GetAllUsersAsync();
    Task<AdminUserDTO> GetUserAsync(int userId);
    Task<AdminUserDTO> SetUserActiveAsync(int userId, bool active);
}
