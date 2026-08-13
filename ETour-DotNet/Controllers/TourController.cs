using ETour.Api.DTO;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Route("api/tours")]
public class TourController : ControllerBase
{
    private readonly ITourService _tours;

    public TourController(ITourService tours) => _tours = tours;

    [Authorize(Roles = Common.RoleName.Admin)]
    [HttpPost]
    public async Task<ActionResult<TourDetailDTO>> CreateTour(
        [FromBody] TourCreateRequestDTO request) =>
        StatusCode(StatusCodes.Status201Created, await _tours.CreateTourAsync(request));

    [AllowAnonymous]
    [HttpGet]
    public async Task<ActionResult<List<TourListDTO>>> GetAllTours() =>
        Ok(await _tours.GetAllToursAsync());

    [AllowAnonymous]
    [HttpGet("search")]
    public async Task<ActionResult<List<TourListDTO>>> SearchTours(
        [FromQuery] DateOnly? startDate, [FromQuery] DateOnly? endDate,
        [FromQuery] double? minPrice, [FromQuery] double? maxPrice,
        [FromQuery] int? minDuration, [FromQuery] int? maxDuration,
        [FromQuery] string city) =>
        Ok(await _tours.SearchToursAsync(startDate, endDate, minPrice, maxPrice,
                                         minDuration, maxDuration, city));

    [AllowAnonymous]
    [HttpGet("cities")]
    public async Task<ActionResult<List<string>>> Cities() =>
        Ok(await _tours.GetAllCitiesAsync());

    [AllowAnonymous]
    [HttpGet("city/{city}")]
    public async Task<ActionResult<List<TourListDTO>>> ToursByCity(string city) =>
        Ok(await _tours.GetToursByCityAsync(city));

    [AllowAnonymous]
    [HttpGet("category/{categoryId:int}")]
    public async Task<ActionResult<List<TourListDTO>>> GetToursByCategory(int categoryId) =>
        Ok(await _tours.GetToursByCategoryAsync(categoryId));

    [AllowAnonymous]
    [HttpGet("subcategory/{subCategoryId:int}")]
    public async Task<ActionResult<List<TourListDTO>>> GetToursBySubCategory(int subCategoryId) =>
        Ok(await _tours.GetToursBySubCategoryAsync(subCategoryId));

    [AllowAnonymous]
    [HttpGet("{id:int}")]
    public async Task<ActionResult<TourDetailDTO>> GetTourById(int id) =>
        Ok(await _tours.GetTourDetailsAsync(id));
}
