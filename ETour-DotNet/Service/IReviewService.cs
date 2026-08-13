using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface IReviewService
{
    Task<ReviewResponseDTO> CreateReviewAsync(ReviewRequestDTO request, string username);
    Task<ReviewSummaryDTO> GetTourReviewsAsync(int tourId);
    Task<List<ReviewResponseDTO>> GetMyReviewsAsync(string username);
    Task<List<int>> GetReviewableBookingIdsAsync(string username);
    Task<ReviewResponseDTO> UpdateReviewAsync(int reviewId, ReviewRequestDTO request, string username);
    Task DeleteReviewAsync(int reviewId, string username);
}
