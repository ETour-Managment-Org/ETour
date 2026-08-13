using ETour.Api.Common;
using ETour.Api.DTO;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize(Roles = RoleName.Admin)]
[Route("api/admin")]
public class AdminCatalogueController : ControllerBase
{
    private readonly AdminCatalogueService _service;

    public AdminCatalogueController(AdminCatalogueService service) => _service = service;

    [HttpGet("tours")]
    public async Task<IActionResult> AllTours() =>
        Ok(await _service.GetAllToursAsync());

    [HttpPost("tours")]
    public async Task<IActionResult> CreateTour(TourRequestDTO request) =>
        StatusCode(201, await _service.CreateTourAsync(request));

    [HttpPost("tours/import")]
    public async Task<IActionResult> ImportTours(BulkImportRequest request) =>
        Ok(await _service.ImportToursAsync(request?.Tours ?? new List<TourRequestDTO>()));

    [HttpPut("tours/{tourId:int}")]
    public async Task<IActionResult> UpdateTour(int tourId, TourRequestDTO request) =>
        Ok(await _service.UpdateTourAsync(tourId, request));

    [HttpDelete("tours/{tourId:int}")]
    public async Task<IActionResult> DeleteTour(int tourId)
    {
        await _service.DeleteTourAsync(tourId);
        return NoContent();
    }

    [HttpGet("tours/{tourId:int}/costs")]
    public async Task<IActionResult> Costs(int tourId) =>
        Ok(await _service.GetCostsAsync(tourId));

    [HttpPost("costs")]
    public async Task<IActionResult> CreateCost(CostRequestDTO request) =>
        StatusCode(201, await _service.CreateCostAsync(request));

    [HttpPut("costs/{costId:int}")]
    public async Task<IActionResult> UpdateCost(int costId, CostRequestDTO request) =>
        Ok(await _service.UpdateCostAsync(costId, request));

    [HttpDelete("costs/{costId:int}")]
    public async Task<IActionResult> DeleteCost(int costId)
    {
        await _service.DeleteCostAsync(costId);
        return NoContent();
    }

    [HttpGet("tours/{tourId:int}/schedules")]
    public async Task<IActionResult> Schedules(int tourId) =>
        Ok(await _service.GetSchedulesAsync(tourId));

    [HttpPost("schedules")]
    public async Task<IActionResult> CreateSchedule(ScheduleRequestDTO request) =>
        StatusCode(201, await _service.CreateScheduleAsync(request));

    [HttpPut("schedules/{scheduleId:int}")]
    public async Task<IActionResult> UpdateSchedule(int scheduleId, ScheduleRequestDTO request) =>
        Ok(await _service.UpdateScheduleAsync(scheduleId, request));

    [HttpDelete("schedules/{scheduleId:int}")]
    public async Task<IActionResult> DeleteSchedule(int scheduleId)
    {
        await _service.DeleteScheduleAsync(scheduleId);
        return NoContent();
    }

    [HttpGet("tours/{tourId:int}/itineraries")]
    public async Task<IActionResult> Itineraries(int tourId) =>
        Ok(await _service.GetItinerariesAsync(tourId));

    [HttpPost("itineraries")]
    public async Task<IActionResult> CreateItinerary(ItineraryRequestDTO request) =>
        StatusCode(201, await _service.CreateItineraryAsync(request));

    [HttpPut("itineraries/{itineraryId:int}")]
    public async Task<IActionResult> UpdateItinerary(int itineraryId, ItineraryRequestDTO request) =>
        Ok(await _service.UpdateItineraryAsync(itineraryId, request));

    [HttpDelete("itineraries/{itineraryId:int}")]
    public async Task<IActionResult> DeleteItinerary(int itineraryId)
    {
        await _service.DeleteItineraryAsync(itineraryId);
        return NoContent();
    }
}
