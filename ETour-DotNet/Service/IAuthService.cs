using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface IAuthService
{
    Task<AuthResponse> RegisterAsync(RegisterRequest request);
    Task<AuthResponse> LoginAsync(LoginRequest request);
    Task<UserProfileDTO> GetProfileAsync(string username);
    Task<UserProfileDTO> UpdateProfileAsync(string username, UpdateProfileRequest request);
    Task ChangePasswordAsync(string username, ChangePasswordRequest request);
    Task ResetPasswordAsync(int userId, string newPassword);
}
