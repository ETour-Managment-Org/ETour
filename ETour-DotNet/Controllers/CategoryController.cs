using ETour.Api.DTO;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[AllowAnonymous]
[Route("api/categories")]
public class CategoryController : ControllerBase
{
    private readonly ICategoryService _categories;

    public CategoryController(ICategoryService categories) => _categories = categories;

    [HttpGet("roots")]
    public async Task<ActionResult<List<CategoryDTO>>> GetRootCategories() =>
        Ok(await _categories.GetRootCategoriesAsync());

    [HttpGet]
    public async Task<ActionResult<List<CategoryDTO>>> GetAllCategories() =>
        Ok(await _categories.GetAllCategoriesAsync());

    [HttpGet("subcategories")]
    public async Task<ActionResult<List<SubCategoryDTO>>> GetAllSubCategories() =>
        Ok(await _categories.GetAllSubCategoriesAsync());

    [HttpGet("code/{catCode}")]
    public async Task<ActionResult<CategoryDTO>> GetCategoryByCode(string catCode) =>
        Ok(await _categories.GetCategoryByCodeAsync(catCode));

    [HttpGet("{categoryId:int}/children")]
    public async Task<ActionResult<List<CategoryDTO>>> GetChildren(int categoryId) =>
        Ok(await _categories.GetChildrenAsync(categoryId));

    [HttpGet("{categoryId:int}/breadcrumb")]
    public async Task<ActionResult<List<BreadcrumbDTO>>> GetBreadcrumb(int categoryId) =>
        Ok(await _categories.GetBreadcrumbAsync(categoryId));

    [HttpGet("{categoryId:int}/subcategories")]
    public async Task<ActionResult<List<SubCategoryDTO>>> GetSubCategories(int categoryId) =>
        Ok(await _categories.GetSubCategoriesAsync(categoryId));

    [HttpGet("{categoryId:int}")]
    public async Task<ActionResult<CategoryDTO>> GetCategory(int categoryId) =>
        Ok(await _categories.GetCategoryAsync(categoryId));
}
