using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface ITourService
{
    Task<List<TourListDTO>> GetAllToursAsync();
    Task<List<TourListDTO>> SearchToursAsync(DateOnly? startDate, DateOnly? endDate,
        double? minPrice, double? maxPrice, int? minDuration, int? maxDuration, string city);
    Task<List<TourListDTO>> GetToursByCityAsync(string city);
    Task<List<string>> GetAllCitiesAsync();
    Task<TourDetailDTO> GetTourDetailsAsync(int tourId);
    Task<TourDetailDTO> CreateTourAsync(TourCreateRequestDTO request);
    Task<List<TourListDTO>> GetToursByCategoryAsync(int categoryId);
    Task<List<TourListDTO>> GetToursBySubCategoryAsync(int subCategoryId);
}
