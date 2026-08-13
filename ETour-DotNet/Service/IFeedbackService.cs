using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface IFeedbackService
{
    Task<FeedbackResponseDTO> SubmitAsync(FeedbackRequestDTO request, string username);
    Task<List<FeedbackResponseDTO>> GetAllAsync(string status);
    Task<FeedbackResponseDTO> UpdateStatusAsync(int feedbackId, string status);
    Task<FeedbackResponseDTO> SetPublishedAsync(int feedbackId, bool published);
    Task<List<PublicFeedbackDTO>> GetPublishedAsync();
    Task<long> CountNewAsync();
}
