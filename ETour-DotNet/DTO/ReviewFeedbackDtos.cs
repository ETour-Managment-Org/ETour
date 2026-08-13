using System.ComponentModel.DataAnnotations;

namespace ETour.Api.DTO;

public class ReviewRequestDTO
{
    [Required(ErrorMessage = "bookingId is required")]
    public int? BookingId { get; set; }

    [Required(ErrorMessage = "rating is required")]
    [Range(1, 5, ErrorMessage = "rating must be between 1 and 5")]
    public int? Rating { get; set; }

    [StringLength(150, ErrorMessage = "review title must be 150 characters or fewer")]
    public string ReviewTitle { get; set; }

    [StringLength(1000, ErrorMessage = "review must be 1000 characters or fewer")]
    public string ReviewDescription { get; set; }
}

public class ReviewResponseDTO
{
    public int? ReviewId { get; set; }
    public int? Rating { get; set; }
    public string ReviewTitle { get; set; }
    public string ReviewDescription { get; set; }
    public DateOnly? ReviewDate { get; set; }
    public string VerificationStatus { get; set; }
    public int? TourId { get; set; }
    public string TourName { get; set; }
    public int? BookingId { get; set; }
    public string CustomerName { get; set; }
}

public class RatingBucketDTO
{
    public int? Stars { get; set; }
    public long? Count { get; set; }
    public double? Percentage { get; set; }
}

public class ReviewSummaryDTO
{
    public int? TourId { get; set; }
    public double? AverageRating { get; set; }
    public long? TotalReviews { get; set; }
    public List<RatingBucketDTO> Distribution { get; set; } = new();
    public List<ReviewResponseDTO> Reviews { get; set; } = new();
}

public class FeedbackRequestDTO
{
    [StringLength(120)]
    public string Name { get; set; }

    [EmailAddress(ErrorMessage = "email is not a valid address")]
    public string Email { get; set; }

    public string Category { get; set; }

    [Range(1, 5, ErrorMessage = "rating must be between 1 and 5")]
    public int? Rating { get; set; }

    [Required(ErrorMessage = "message is required")]
    [StringLength(2000, MinimumLength = 5, ErrorMessage = "message must be 5 to 2000 characters")]
    public string Message { get; set; }

    public string PageUrl { get; set; }
}

public class FeedbackResponseDTO
{
    public int? FeedbackId { get; set; }
    public string Name { get; set; }
    public string Email { get; set; }
    public string Category { get; set; }
    public int? Rating { get; set; }
    public string Message { get; set; }
    public string Status { get; set; }
    public string PageUrl { get; set; }
    public DateTime? CreatedAt { get; set; }
    public int? UserId { get; set; }
    public string Username { get; set; }
    public bool FromRegisteredUser { get; set; }
    public bool Published { get; set; }
}

public class PublicFeedbackDTO
{
    public int? FeedbackId { get; set; }
    public string Name { get; set; }
    public string Category { get; set; }
    public int? Rating { get; set; }
    public string Message { get; set; }
    public DateOnly? SubmittedOn { get; set; }
    public bool FromRegisteredUser { get; set; }
}
