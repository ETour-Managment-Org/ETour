namespace ETour.Api.Common;

public static class BookingStatus
{
    public const string Pending = "PENDING";
    public const string Confirmed = "CONFIRMED";
    public const string Completed = "COMPLETED";
    public const string Cancelled = "CANCELLED";
}

public static class PaymentStatus
{
    public const string Pending = "PENDING";
    public const string Success = "SUCCESS";
    public const string Failed = "FAILED";
    public const string Refunded = "REFUNDED";
}

public static class RefundStatus
{
    public const string Pending = "PENDING";
    public const string Processed = "PROCESSED";
    public const string NotApplicable = "NOT_APPLICABLE";
}

public static class VerificationStatus
{
    public const string Verified = "VERIFIED";
    public const string Pending = "PENDING";
    public const string Rejected = "REJECTED";
}

public static class RoleName
{
    public const string Customer = "CUSTOMER";
    public const string Admin = "ADMIN";
    public const string RoleCustomer = "ROLE_CUSTOMER";
    public const string RoleAdmin = "ROLE_ADMIN";
}

public static class Gender
{
    public const string Male = "MALE";
    public const string Female = "FEMALE";
    public const string Other = "OTHER";

    public static readonly IReadOnlyList<string> All = new[] { Male, Female, Other };

    public static bool IsValid(string value) =>
        string.IsNullOrWhiteSpace(value) || All.Contains(value.Trim().ToUpperInvariant());

    public static string Normalise(string value) =>
        string.IsNullOrWhiteSpace(value) ? null : value.Trim().ToUpperInvariant();
}

public static class FeedbackCategory
{
    public const string Suggestion = "SUGGESTION";
    public const string Problem = "PROBLEM";
    public const string Compliment = "COMPLIMENT";
    public const string Other = "OTHER";

    public const string New = "NEW";
    public const string Reviewed = "REVIEWED";
    public const string Closed = "CLOSED";

    public static readonly IReadOnlyList<string> All =
        new[] { Suggestion, Problem, Compliment, Other };

    public static bool IsValidCategory(string value) =>
        value is not null && All.Contains(value.Trim().ToUpperInvariant());
}
