using ETour.Api.DTO;

namespace ETour.Api.Service;

public interface ICategoryService
{
    Task<List<CategoryDTO>> GetRootCategoriesAsync();
    Task<List<CategoryDTO>> GetChildrenAsync(int categoryId);
    Task<List<BreadcrumbDTO>> GetBreadcrumbAsync(int categoryId);
    Task<List<CategoryDTO>> GetAllCategoriesAsync();
    Task<CategoryDTO> GetCategoryAsync(int categoryId);
    Task<CategoryDTO> GetCategoryByCodeAsync(string catCode);
    Task<List<SubCategoryDTO>> GetSubCategoriesAsync(int categoryId);
    Task<List<SubCategoryDTO>> GetAllSubCategoriesAsync();
}
