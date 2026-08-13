using AutoMapper;
using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class ReviewService : IReviewService
{
    private readonly ETourDbContext _db;
    private readonly IMapper _mapper;

    public ReviewService(ETourDbContext db, IMapper mapper)
    {
        _db = db;
        _mapper = mapper;
    }

    public async Task<ReviewResponseDTO> CreateReviewAsync(ReviewRequestDTO request, string username)
    {
        var customer = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException($"No account found for {username}");

        var booking = await _db.Bookings
            .Include(b => b.User).Include(b => b.Tour).Include(b => b.Schedule)
            .FirstOrDefaultAsync(b => b.BookingId == request.BookingId)
            ?? throw new ResourceNotFoundException("Booking", request.BookingId);

        AssertOwnedBy(booking, customer);
        AssertTravelled(booking);

        if (await _db.Reviews.AnyAsync(r => r.BookingId == booking.BookingId))
        {
            throw new ConflictException(
                $"You have already reviewed booking {booking.BookingId}. "
              + "Edit the existing review instead.");
        }

        var review = new Review
        {
            BookingId = booking.BookingId,
            CustomerId = customer.UserId,
            TourId = booking.TourId,
            Rating = request.Rating,
            ReviewTitle = request.ReviewTitle,
            ReviewDescription = request.ReviewDescription,
            ReviewDate = DateOnly.FromDateTime(DateTime.Today),
            VerificationStatus = VerificationStatus.Verified
        };

        _db.Reviews.Add(review);
        await _db.SaveChangesAsync();

        review.Customer = customer;
        review.Tour = booking.Tour;
        return _mapper.Map<ReviewResponseDTO>(review);
    }

    public async Task<ReviewSummaryDTO> GetTourReviewsAsync(int tourId)
    {
        var reviews = await _db.Reviews.AsNoTracking()
            .Include(r => r.Customer).Include(r => r.Tour)
            .Where(r => r.TourId == tourId)
            .OrderByDescending(r => r.ReviewDate)
            .ToListAsync();

        long total = reviews.Count;

        double? average = reviews.Count == 0
            ? null
            : Math.Round(reviews.Where(r => r.Rating != null).Average(r => (double)r.Rating.Value),
                         1, MidpointRounding.AwayFromZero);

        var counts = reviews.Where(r => r.Rating != null)
            .GroupBy(r => r.Rating.Value)
            .ToDictionary(g => g.Key, g => (long)g.Count());

        var distribution = new List<RatingBucketDTO>();
        for (var stars = 5; stars >= 1; stars--)
        {
            var count = counts.GetValueOrDefault(stars, 0L);
            var percentage = total == 0
                ? 0d
                : Math.Round(count * 100.0 / total, 1, MidpointRounding.AwayFromZero);

            distribution.Add(new RatingBucketDTO
            {
                Stars = stars, Count = count, Percentage = percentage
            });
        }

        return new ReviewSummaryDTO
        {
            TourId = tourId,
            AverageRating = average,
            TotalReviews = total,
            Distribution = distribution,
            Reviews = reviews.Select(_mapper.Map<ReviewResponseDTO>).ToList()
        };
    }

    public async Task<List<ReviewResponseDTO>> GetMyReviewsAsync(string username)
    {
        var customer = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException($"No account found for {username}");

        var reviews = await _db.Reviews.AsNoTracking()
            .Include(r => r.Customer).Include(r => r.Tour)
            .Where(r => r.CustomerId == customer.UserId)
            .OrderByDescending(r => r.ReviewDate)
            .ToListAsync();

        return reviews.Select(_mapper.Map<ReviewResponseDTO>).ToList();
    }

    public async Task<List<int>> GetReviewableBookingIdsAsync(string username)
    {
        var customer = await _db.Users.FirstOrDefaultAsync(u => u.Username == username)
            ?? throw new ResourceNotFoundException($"No account found for {username}");

        var bookings = await _db.Bookings.AsNoTracking()
            .Include(b => b.Tour).Include(b => b.Schedule)
            .Where(b => b.UserId == customer.UserId)
            .OrderByDescending(b => b.BookingDate)
            .ToListAsync();

        var reviewed = await _db.Reviews.AsNoTracking()
            .Where(r => r.BookingId != null)
            .Select(r => r.BookingId.Value).ToListAsync();

        return bookings
            .Where(HasTravelled)
            .Where(b => !reviewed.Contains(b.BookingId))
            .Select(b => b.BookingId)
            .ToList();
    }

    public async Task<ReviewResponseDTO> UpdateReviewAsync(int reviewId, ReviewRequestDTO request,
                                                           string username)
    {
        var review = await _db.Reviews
            .Include(r => r.Customer).Include(r => r.Tour)
            .FirstOrDefaultAsync(r => r.ReviewId == reviewId)
            ?? throw new ResourceNotFoundException("Review", reviewId);

        AssertAuthor(review, username);

        review.Rating = request.Rating;
        review.ReviewTitle = request.ReviewTitle;
        review.ReviewDescription = request.ReviewDescription;
        review.ReviewDate = DateOnly.FromDateTime(DateTime.Today);

        await _db.SaveChangesAsync();
        return _mapper.Map<ReviewResponseDTO>(review);
    }

    public async Task DeleteReviewAsync(int reviewId, string username)
    {
        var review = await _db.Reviews.Include(r => r.Customer)
            .FirstOrDefaultAsync(r => r.ReviewId == reviewId)
            ?? throw new ResourceNotFoundException("Review", reviewId);

        AssertAuthor(review, username);

        _db.Reviews.Remove(review);
        await _db.SaveChangesAsync();
    }

    private static void AssertOwnedBy(Booking booking, User customer)
    {
        if (booking.UserId != customer.UserId)
            throw new AccessDeniedException("This booking does not belong to you");
    }

    private static void AssertTravelled(Booking booking)
    {
        if (booking.BookingStatus == BookingStatus.Cancelled)
            throw new ConflictException("A cancelled booking cannot be reviewed");

        if (booking.BookingStatus == BookingStatus.Pending)
            throw new ConflictException($"Booking {booking.BookingId} has not been paid for yet");

        if (!HasTravelled(booking))
        {
            var departure = booking.Schedule?.StartDate;
            var returnDate = TourDates.ReturnDate(booking);

            throw new ConflictException(departure is not null
                ? $"This tour departs on {departure:yyyy-MM-dd} and returns on "
                  + $"{returnDate:yyyy-MM-dd}. You can review it once you are back."
                : "This booking has no departure date, so it cannot be reviewed yet.");
        }
    }

    private static bool HasTravelled(Booking booking)
    {
        if (booking.BookingStatus == BookingStatus.Completed) return true;
        if (booking.BookingStatus != BookingStatus.Confirmed) return false;
        return TourDates.HasFinished(booking, DateOnly.FromDateTime(DateTime.Today));
    }

    private static void AssertAuthor(Review review, string username)
    {
        if (review.Customer?.Username != username)
            throw new AccessDeniedException("This review does not belong to you");
    }

}
