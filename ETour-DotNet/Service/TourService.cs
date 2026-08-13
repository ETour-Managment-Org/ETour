using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class TourService : ITourService
{
    private readonly ETourDbContext _db;

    public TourService(ETourDbContext db) => _db = db;

    public async Task<List<TourListDTO>> GetAllToursAsync() =>
        await ToListDtosAsync(await _db.Tours.AsNoTracking().OrderBy(t => t.TourId).ToListAsync());

    public async Task<List<TourListDTO>> SearchToursAsync(DateOnly? startDate, DateOnly? endDate,
        double? minPrice, double? maxPrice, int? minDuration, int? maxDuration, string city)
    {
        if (startDate is not null && endDate is not null && startDate > endDate)
            throw new ArgumentException("startDate must not be after endDate");
        if (minPrice is not null && maxPrice is not null && minPrice > maxPrice)
            throw new ArgumentException("minPrice must not be greater than maxPrice");
        if (minDuration is not null && maxDuration is not null && minDuration > maxDuration)
            throw new ArgumentException("minDuration must not be greater than maxDuration");

        var term = BlankToNull(city);
        var min = minPrice is null ? (decimal?)null : (decimal)minPrice.Value;
        var max = maxPrice is null ? (decimal?)null : (decimal)maxPrice.Value;

        var query = _db.Tours
            .AsNoTracking()
            .Where(t =>
                (startDate == null || t.Schedules.Any(s => s.StartDate >= startDate)) &&
                (endDate == null || t.Schedules.Any(s => s.StartDate <= endDate)) &&
                (min == null || t.Costs.Any(c => c.AdultPrice >= min)) &&
                (max == null || t.Costs.Any(c => c.AdultPrice <= max)) &&
                (minDuration == null || t.Days >= minDuration) &&
                (maxDuration == null || t.Days <= maxDuration) &&
                (term == null
                    || t.Cities.Any(c => c.CityName.ToLower().Contains(term.ToLower()))
                    || t.Destination.ToLower().Contains(term.ToLower())
                    || t.Location.ToLower().Contains(term.ToLower())))
            .OrderBy(t => t.TourId);

        return await ToListDtosAsync(await query.ToListAsync());
    }

    public async Task<List<TourListDTO>> GetToursByCityAsync(string city)
    {
        var term = BlankToNull(city);
        if (term is null) return new List<TourListDTO>();

        var lower = term.ToLower();
        var tours = await _db.Tours.AsNoTracking()
            .Where(t => t.Cities.Any(c => c.CityName.ToLower().Contains(lower))
                     || t.Destination.ToLower().Contains(lower)
                     || t.Location.ToLower().Contains(lower))
            .OrderBy(t => t.TourId)
            .ToListAsync();

        return await ToListDtosAsync(tours);
    }

    public async Task<List<string>> GetAllCitiesAsync() =>
        await _db.TourCities.AsNoTracking()
            .Select(c => c.CityName)
            .Distinct()
            .OrderBy(n => n)
            .ToListAsync();

    public async Task<TourDetailDTO> GetTourDetailsAsync(int tourId)
    {
        var tour = await _db.Tours.AsNoTracking()
            .Include(t => t.Category)
            .Include(t => t.SubCategory)
            .FirstOrDefaultAsync(t => t.TourId == tourId)
            ?? throw new ResourceNotFoundException("Tour", tourId);

        var itineraries = await _db.Itineraries.AsNoTracking()
            .Where(i => i.TourId == tourId).OrderBy(i => i.DayNumber)
            .Select(i => new ItineraryDTO
            {
                ItineraryId = i.ItineraryId, DayNumber = i.DayNumber,
                Description = i.Description, Location = i.Location
            }).ToListAsync();

        var today = DateOnly.FromDateTime(DateTime.Today);

        var schedules = await _db.Schedules.AsNoTracking()
            .Where(s => s.TourId == tourId && s.StartDate != null && s.StartDate >= today)
            .OrderBy(s => s.StartDate)
            .Select(s => new ScheduleDTO
            {
                ScheduleId = s.ScheduleId, StartDate = s.StartDate,
                AvailableSeats = s.AvailableSeats, TotalSeats = s.TotalSeats, Status = s.Status
            }).ToListAsync();

        var costs = await _db.Costs.AsNoTracking()
            .Where(c => c.TourId == tourId)
            .Select(c => new CostDTO
            {
                CostId = c.CostId, AdultPrice = c.AdultPrice,
                SinglePersonPrice = c.SinglePersonPrice, ExtraPersonPrice = c.ExtraPersonPrice,
                ChildWithBedPrice = c.ChildWithBedPrice,
                ChildWithoutBedPrice = c.ChildWithoutBedPrice,
                ValidFrom = c.ValidFrom, ValidTo = c.ValidTo, IsActive = c.IsActive
            }).ToListAsync();

        var images = await _db.TourImages.AsNoTracking()
            .Where(i => i.TourId == tourId)
            .Select(i => new TourImageDTO
            {
                ImageId = i.ImageId, Source = i.Source, ImageTitle = i.ImageTitle,
                IsPrimary = i.IsPrimary, UploadDate = i.UploadDate
            }).ToListAsync();

        var ratings = await _db.Reviews.AsNoTracking()
            .Where(r => r.TourId == tourId && r.Rating != null)
            .Select(r => (double)r.Rating.Value).ToListAsync();

        return new TourDetailDTO
        {
            TourId = tour.TourId,
            TourName = tour.TourName,
            Destination = tour.Destination,
            Days = tour.Days,
            Nights = tour.Nights,
            Description = tour.Description,
            Price = tour.Price,
            Location = tour.Location,
            TourType = tour.TourType,
            DurationLabel = DurationLabel(tour),
            AverageRating = ratings.Count == 0 ? null : RoundRating(ratings.Average()),
            ReviewCount = ratings.Count,
            CategoryId = tour.CategoryId,
            CategoryName = tour.Category?.CategoryName,
            SubCategoryId = tour.SubcatId,
            SubCategoryName = tour.SubCategory?.SubcatName,
            StayAndMeals = tour.StayAndMeals,
            AddOns = tour.AddOns,
            PassportAndVisa = tour.PassportAndVisa,
            Weather = tour.Weather,
            DoAndDont = tour.DoAndDont,
            PrimaryImageUrl = images.FirstOrDefault(i => i.IsPrimary == true)?.Source
                              ?? images.FirstOrDefault()?.Source,
            Itineraries = itineraries,
            Schedules = schedules,
            Costs = costs,
            Images = images
        };
    }

    public async Task<TourDetailDTO> CreateTourAsync(TourCreateRequestDTO r)
    {
        var tour = new Tour
        {
            TourName = r.TourName, Destination = r.Destination,
            Days = r.Days, Nights = r.Nights, Description = r.Description,
            Price = r.Price, Location = r.Location, TourType = r.TourType,
            StayAndMeals = r.StayAndMeals, AddOns = r.AddOns,
            PassportAndVisa = r.PassportAndVisa, Weather = r.Weather, DoAndDont = r.DoAndDont
        };

        SyncCities(tour);

        if (r.CategoryId is int catId)
        {
            _ = await _db.Categories.FindAsync(catId)
                ?? throw new ResourceNotFoundException("Category", catId);
            tour.CategoryId = catId;
        }

        if (r.SubCategoryId is int subId)
        {
            var sub = await _db.SubCategories.FindAsync(subId)
                ?? throw new ResourceNotFoundException("SubCategory", subId);
            tour.SubcatId = subId;
            tour.CategoryId ??= sub.CatId;
        }

        foreach (var c in r.Costs)
        {
            tour.Costs.Add(new Cost
            {
                AdultPrice = c.AdultPrice, SinglePersonPrice = c.SinglePersonPrice,
                ExtraPersonPrice = c.ExtraPersonPrice, ChildWithBedPrice = c.ChildWithBedPrice,
                ChildWithoutBedPrice = c.ChildWithoutBedPrice,
                ValidFrom = c.ValidFrom, ValidTo = c.ValidTo,
                IsActive = c.IsActive ?? true
            });
        }

        foreach (var sc in r.Schedules)
        {
            tour.Schedules.Add(new Schedule
            {
                StartDate = sc.StartDate,
                TotalSeats = sc.TotalSeats,
                AvailableSeats = sc.AvailableSeats ?? sc.TotalSeats,
                Status = sc.Status ?? "OPEN"
            });
        }

        foreach (var it in r.Itineraries)
        {
            tour.Itineraries.Add(new Itinerary
            {
                DayNumber = it.DayNumber, Description = it.Description, Location = it.Location
            });
        }

        var primaryAssigned = false;
        foreach (var img in r.Images)
        {
            var wantsPrimary = img.IsPrimary == true;
            tour.Images.Add(new TourImages
            {
                Source = img.Source,
                ImageTitle = img.ImageTitle,
                IsPrimary = wantsPrimary && !primaryAssigned,
                UploadDate = DateTime.Now
            });
            if (wantsPrimary && !primaryAssigned) primaryAssigned = true;
        }

        if (!primaryAssigned && tour.Images.Count > 0)
        {
            tour.Images.First().IsPrimary = true;
        }

        _db.Tours.Add(tour);
        await _db.SaveChangesAsync();

        return await GetTourDetailsAsync(tour.TourId);
    }

    public async Task<List<TourListDTO>> GetToursByCategoryAsync(int categoryId)
    {
        if (!await _db.Categories.AnyAsync(c => c.CategoryId == categoryId))
            throw new ResourceNotFoundException("Category", categoryId);

        var all = await _db.Categories.AsNoTracking().ToListAsync();
        var scope = CategoryScope.Resolve(categoryId, all);

        var matches = await _db.Tours.AsNoTracking()
            .Where(t => t.CategoryId != null && scope.Contains(t.CategoryId.Value))
            .OrderBy(t => t.TourId)
            .ToListAsync();

        return await ToListDtosAsync(matches);
    }

    public async Task<List<TourListDTO>> GetToursBySubCategoryAsync(int subCategoryId)
    {
        if (!await _db.SubCategories.AnyAsync(s => s.SubcatId == subCategoryId))
            throw new ResourceNotFoundException("SubCategory", subCategoryId);

        var tours = await _db.Tours.AsNoTracking()
            .Where(t => t.SubcatId == subCategoryId).OrderBy(t => t.TourId).ToListAsync();

        return await ToListDtosAsync(tours);
    }

    private async Task<List<TourListDTO>> ToListDtosAsync(List<Tour> tours)
    {
        var minPriceByTour = await _db.Costs.AsNoTracking()
            .Where(c => c.TourId != null && c.AdultPrice != null)
            .GroupBy(c => c.TourId.Value)
            .Select(g => new { TourId = g.Key, Min = g.Min(x => x.AdultPrice) })
            .ToDictionaryAsync(x => x.TourId, x => x.Min);

        var ratingByTour = await _db.Reviews.AsNoTracking()
            .Where(r => r.TourId != null && r.Rating != null)
            .GroupBy(r => r.TourId.Value)
            .Select(g => new
            {
                TourId = g.Key,
                Avg = g.Average(x => (double)x.Rating.Value),
                Count = (long)g.Count()
            })
            .ToDictionaryAsync(x => x.TourId, x => x);

        var primaryImageByTour = await _db.TourImages.AsNoTracking()
            .Where(i => i.IsPrimary == true && i.TourId != null)
            .GroupBy(i => i.TourId.Value)
            .Select(g => new { TourId = g.Key, Source = g.First().Source })
            .ToDictionaryAsync(x => x.TourId, x => x.Source);

        return tours.Select(t => new TourListDTO
        {
            TourId = t.TourId,
            TourName = t.TourName,
            Destination = t.Destination,
            TourType = t.TourType,
            Days = t.Days,
            Nights = t.Nights,
            StartingPrice = minPriceByTour.TryGetValue(t.TourId, out var min) && min is not null
                ? min
                : t.Price is null ? null : (decimal)t.Price.Value,
            PrimaryImageUrl = primaryImageByTour.GetValueOrDefault(t.TourId),
            DurationLabel = DurationLabel(t),
            AverageRating = ratingByTour.TryGetValue(t.TourId, out var r)
                ? RoundRating(r.Avg) : null,
            ReviewCount = ratingByTour.TryGetValue(t.TourId, out var r2) ? r2.Count : 0L
        }).ToList();
    }

    private static void SyncCities(Tour tour)
    {
        tour.Cities.Clear();
        var order = 1;
        foreach (var name in CityNames.Split(tour.Destination))
        {
            tour.Cities.Add(new TourCity { CityName = name, StopOrder = order++ });
        }
    }

    private static string DurationLabel(Tour tour) =>
        tour.Nights is null || tour.Days is null ? null : $"{tour.Nights}N/{tour.Days}D";

    private static double RoundRating(double value) =>
        Math.Round(value, 1, MidpointRounding.AwayFromZero);

    private static string BlankToNull(string value) =>
        string.IsNullOrWhiteSpace(value) ? null : value.Trim();
}
