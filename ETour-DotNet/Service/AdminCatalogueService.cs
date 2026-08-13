using AutoMapper;
using ETour.Api.Common;
using ETour.Api.DTO;
using ETour.Api.Exceptions;
using ETour.Api.Models;
using ETour.Api.Repository;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class AdminCatalogueService
{
    private readonly IGenericRepository<Tour> _tours;
    private readonly IGenericRepository<Cost> _costs;
    private readonly IGenericRepository<Schedule> _schedules;
    private readonly IGenericRepository<Itinerary> _itineraries;
    private readonly IGenericRepository<TourImages> _images;
    private readonly IGenericRepository<Booking> _bookings;
    private readonly IGenericRepository<Review> _reviews;
    private readonly IGenericRepository<Journey> _journeys;
    private readonly IGenericRepository<TourCity> _cities;
    private readonly ITourService _tourDetails;
    private readonly IMapper _mapper;
    private readonly ILogger<AdminCatalogueService> _log;

    public AdminCatalogueService(
        IGenericRepository<Tour> tours,
        IGenericRepository<Cost> costs,
        IGenericRepository<Schedule> schedules,
        IGenericRepository<Itinerary> itineraries,
        IGenericRepository<TourImages> images,
        IGenericRepository<Booking> bookings,
        IGenericRepository<Review> reviews,
        IGenericRepository<Journey> journeys,
        IGenericRepository<TourCity> cities,
        ITourService tourDetails,
        IMapper mapper,
        ILogger<AdminCatalogueService> log)
    {
        _tours = tours;
        _costs = costs;
        _schedules = schedules;
        _itineraries = itineraries;
        _images = images;
        _bookings = bookings;
        _reviews = reviews;
        _journeys = journeys;
        _cities = cities;
        _tourDetails = tourDetails;
        _mapper = mapper;
        _log = log;
    }

    public async Task<List<TourDetailDTO>> GetAllToursAsync()
    {
        var ids = await _tours.Query().OrderBy(t => t.TourId)
            .Select(t => t.TourId).ToListAsync();

        var result = new List<TourDetailDTO>();
        foreach (var id in ids)
        {
            var dto = await _tourDetails.GetTourDetailsAsync(id);
            dto.BookingCount = await _bookings.CountAsync(b => b.TourId == id);
            result.Add(dto);
        }
        return result;
    }

    public async Task<BulkImportResponse> ImportToursAsync(List<TourRequestDTO> rows)
    {
        var results = new List<BulkImportRowResult>();
        var imported = 0;

        for (var i = 0; i < rows.Count; i++)
        {
            var r = rows[i];
            var rowNumber = i + 2;
            var name = r?.TourName;

            try
            {
                if (string.IsNullOrWhiteSpace(name))
                    throw new ArgumentException("Tour name is empty");

                var needle = name.Trim().ToLower();
                var exists = await _tours.ExistsAsync(t => t.TourName.ToLower() == needle);

                if (exists)
                {
                    results.Add(new BulkImportRowResult
                    {
                        RowNumber = rowNumber, TourName = name, Success = false,
                        Message = "Skipped - a tour with this name already exists"
                    });
                    continue;
                }

                var created = await CreateTourAsync(r);
                imported++;
                results.Add(new BulkImportRowResult
                {
                    RowNumber = rowNumber, TourName = name, Success = true,
                    TourId = created.TourId, Message = "Imported"
                });
            }
            catch (Exception ex)
            {
                _log.LogWarning("Tour import failed at sheet row {Row} ({Name}): {Message}",
                                rowNumber, name, ex.Message);
                results.Add(new BulkImportRowResult
                {
                    RowNumber = rowNumber, TourName = name, Success = false,
                    Message = string.IsNullOrWhiteSpace(ex.Message)
                        ? "Could not import this row" : ex.Message
                });
            }
        }

        return new BulkImportResponse
        {
            Total = rows.Count,
            Imported = imported,
            Failed = rows.Count - imported,
            Rows = results
        };
    }

    public async Task<TourDetailDTO> CreateTourAsync(TourRequestDTO request)
    {
        var tour = _mapper.Map<Tour>(request);
        SyncCities(tour);

        await _tours.AddAsync(tour);
        _log.LogInformation("Admin created tour {TourId} {TourName}", tour.TourId, tour.TourName);

        await SetPrimaryImageAsync(tour, request.PrimaryImageUrl);
        return await _tourDetails.GetTourDetailsAsync(tour.TourId);
    }

    public async Task<TourDetailDTO> UpdateTourAsync(int tourId, TourRequestDTO request)
    {
        var tour = await _tours.Query(tracked: true)
            .Include(t => t.Cities)
            .FirstOrDefaultAsync(t => t.TourId == tourId)
            ?? throw new ResourceNotFoundException("Tour", tourId);

        _mapper.Map(request, tour);
        SyncCities(tour);
        await _tours.SaveChangesAsync();

        _log.LogInformation("Admin updated tour {TourId}", tourId);

        await SetPrimaryImageAsync(tour, request.PrimaryImageUrl);
        return await _tourDetails.GetTourDetailsAsync(tourId);
    }

    public async Task DeleteTourAsync(int tourId)
    {
        var tour = await _tours.GetByIdAsync(tourId)
            ?? throw new ResourceNotFoundException("Tour", tourId);

        if (await _bookings.ExistsAsync(b => b.TourId == tourId))
        {
            var count = await _bookings.CountAsync(b => b.TourId == tourId);
            _log.LogWarning("Refused to delete tour {TourId} - it has {Count} booking(s)",
                tourId, count);

            throw new ConflictException(
                $"Tour {tourId} has {count} booking(s) against it and cannot be deleted. "
              + "Set its departures to CLOSED instead, so it can no longer be booked "
              + "while the booking history is preserved.");
        }

        var reviews     = await _reviews.FindAsync(r => r.TourId == tourId);
        var costs       = await _costs.FindAsync(c => c.TourId == tourId);
        var schedules   = await _schedules.FindAsync(s => s.TourId == tourId);
        var itineraries = await _itineraries.FindAsync(i => i.TourId == tourId);
        var journeys    = await _journeys.FindAsync(j => j.TourId == tourId);
        var images      = await _images.FindAsync(i => i.TourId == tourId);
        var cities      = await _cities.FindAsync(c => c.TourId == tourId);

        _log.LogInformation(
            "Deleting tour {TourId}: {Reviews} review(s), {Costs} fare band(s), "
          + "{Schedules} departure(s), {Days} itinerary day(s), {Journeys} journey(s), "
          + "{Images} image(s), {Cities} city row(s)",
            tourId, reviews.Count, costs.Count, schedules.Count, itineraries.Count,
            journeys.Count, images.Count, cities.Count);

        foreach (var r in reviews)     await _reviews.RemoveAsync(r);
        foreach (var c in costs)       await _costs.RemoveAsync(c);
        foreach (var s in schedules)   await _schedules.RemoveAsync(s);
        foreach (var i in itineraries) await _itineraries.RemoveAsync(i);
        foreach (var j in journeys)    await _journeys.RemoveAsync(j);
        foreach (var i in images)      await _images.RemoveAsync(i);
        foreach (var c in cities)      await _cities.RemoveAsync(c);

        await _tours.RemoveAsync(tour);
        _log.LogInformation("Tour {TourId} deleted", tourId);
    }

    private static void SyncCities(Tour tour)
    {
        tour.Cities.Clear();
        var order = 1;
        foreach (var name in CityNames.Split(tour.Destination))
            tour.Cities.Add(new TourCity { CityName = name, StopOrder = order++ });
    }

    private async Task SetPrimaryImageAsync(Tour tour, string url)
    {
        if (string.IsNullOrWhiteSpace(url)) return;

        var image = await _images.Query(tracked: true)
            .FirstOrDefaultAsync(i => i.TourId == tour.TourId && i.IsPrimary == true);

        if (image is null)
        {
            await _images.AddAsync(new TourImages
            {
                TourId = tour.TourId,
                IsPrimary = true,
                Source = url.Trim(),
                ImageTitle = tour.TourName,
                UploadDate = DateTime.Now
            });
            return;
        }

        image.Source = url.Trim();
        image.ImageTitle = tour.TourName;
        image.UploadDate = DateTime.Now;
        await _images.SaveChangesAsync();
    }

    public async Task<List<CostDTO>> GetCostsAsync(int tourId) =>
        _mapper.Map<List<CostDTO>>(await _costs.FindAsync(c => c.TourId == tourId));

    public async Task<CostDTO> CreateCostAsync(CostRequestDTO request)
    {
        await RequireTourAsync(request.TourId);

        var cost = _mapper.Map<Cost>(request);
        cost.IsActive ??= true;

        await _costs.AddAsync(cost);
        return _mapper.Map<CostDTO>(cost);
    }

    public async Task<CostDTO> UpdateCostAsync(int costId, CostRequestDTO request)
    {
        var cost = await _costs.GetByIdAsync(costId)
            ?? throw new ResourceNotFoundException("Cost", costId);

        _mapper.Map(request, cost);
        cost.IsActive ??= true;

        await _costs.UpdateAsync(cost);
        return _mapper.Map<CostDTO>(cost);
    }

    public async Task DeleteCostAsync(int costId)
    {
        var cost = await _costs.GetByIdAsync(costId)
            ?? throw new ResourceNotFoundException("Cost", costId);
        await _costs.RemoveAsync(cost);
    }

    public async Task<List<ScheduleDTO>> GetSchedulesAsync(int tourId) =>
        _mapper.Map<List<ScheduleDTO>>(
            await _schedules.Query().Where(s => s.TourId == tourId)
                .OrderBy(s => s.StartDate).ToListAsync());

    public async Task<ScheduleDTO> CreateScheduleAsync(ScheduleRequestDTO request)
    {
        await RequireTourAsync(request.TourId);

        var schedule = _mapper.Map<Schedule>(request);
        schedule.AvailableSeats ??= schedule.TotalSeats;
        schedule.Status ??= "OPEN";

        await _schedules.AddAsync(schedule);
        return _mapper.Map<ScheduleDTO>(schedule);
    }

    public async Task<ScheduleDTO> UpdateScheduleAsync(int scheduleId, ScheduleRequestDTO request)
    {
        var schedule = await _schedules.GetByIdAsync(scheduleId)
            ?? throw new ResourceNotFoundException("Schedule", scheduleId);

        _mapper.Map(request, schedule);
        await _schedules.UpdateAsync(schedule);
        return _mapper.Map<ScheduleDTO>(schedule);
    }

    public async Task DeleteScheduleAsync(int scheduleId)
    {
        var schedule = await _schedules.GetByIdAsync(scheduleId)
            ?? throw new ResourceNotFoundException("Schedule", scheduleId);

        if (schedule.AvailableSeats < schedule.TotalSeats)
        {
            _log.LogWarning("Refused to delete schedule {ScheduleId} - it has bookings", scheduleId);
            throw new ConflictException(
                $"Schedule {scheduleId} already has bookings. Set it CLOSED instead.");
        }

        await _schedules.RemoveAsync(schedule);
    }

    public async Task<List<ItineraryDTO>> GetItinerariesAsync(int tourId) =>
        _mapper.Map<List<ItineraryDTO>>(
            await _itineraries.Query().Where(i => i.TourId == tourId)
                .OrderBy(i => i.DayNumber).ToListAsync());

    public async Task<ItineraryDTO> CreateItineraryAsync(ItineraryRequestDTO request)
    {
        await RequireTourAsync(request.TourId);

        var day = _mapper.Map<Itinerary>(request);
        await _itineraries.AddAsync(day);
        return _mapper.Map<ItineraryDTO>(day);
    }

    public async Task<ItineraryDTO> UpdateItineraryAsync(int itineraryId, ItineraryRequestDTO request)
    {
        var day = await _itineraries.GetByIdAsync(itineraryId)
            ?? throw new ResourceNotFoundException("Itinerary", itineraryId);

        _mapper.Map(request, day);
        await _itineraries.UpdateAsync(day);
        return _mapper.Map<ItineraryDTO>(day);
    }

    public async Task DeleteItineraryAsync(int itineraryId)
    {
        var day = await _itineraries.GetByIdAsync(itineraryId)
            ?? throw new ResourceNotFoundException("Itinerary", itineraryId);
        await _itineraries.RemoveAsync(day);
    }

    private async Task RequireTourAsync(int? tourId)
    {
        if (tourId is null || !await _tours.ExistsAsync(t => t.TourId == tourId))
            throw new ResourceNotFoundException("Tour", tourId);
    }
}
