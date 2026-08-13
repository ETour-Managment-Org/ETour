using ETour.Api.Common;
using ETour.Api.DTO;
using FluentValidation;

namespace ETour.Api.Validators;

public class RegisterRequestValidator : AbstractValidator<RegisterRequest>
{
    public RegisterRequestValidator()
    {
        RuleFor(x => x.Username).NotEmpty().Length(3, 50);

        RuleFor(x => x.Password).NotEmpty().MinimumLength(8)
            .WithMessage("password must be at least 8 characters");

        RuleFor(x => x.Email).NotEmpty().EmailAddress();

        RuleFor(x => x.PhoneNumber).Matches(@"^[0-9]{10}$")
            .When(x => !string.IsNullOrWhiteSpace(x.PhoneNumber))
            .WithMessage("phone number must be exactly 10 digits");

        RuleFor(x => x.Gender).Must(Gender.IsValid)
            .WithMessage($"gender must be one of {string.Join(", ", Gender.All)} or left empty");
    }
}

public class LoginRequestValidator : AbstractValidator<LoginRequest>
{
    public LoginRequestValidator()
    {
        RuleFor(x => x.Username).NotEmpty();
        RuleFor(x => x.Password).NotEmpty();
    }
}

public class ChangePasswordRequestValidator : AbstractValidator<ChangePasswordRequest>
{
    public ChangePasswordRequestValidator()
    {
        RuleFor(x => x.CurrentPassword).NotEmpty();
        RuleFor(x => x.NewPassword).NotEmpty().MinimumLength(8);

        RuleFor(x => x.NewPassword).NotEqual(x => x.CurrentPassword)
            .WithMessage("The new password must be different from the current one");
    }
}

public class UpdateProfileRequestValidator : AbstractValidator<UpdateProfileRequest>
{
    public UpdateProfileRequestValidator()
    {
        RuleFor(x => x.Email).EmailAddress()
            .When(x => !string.IsNullOrWhiteSpace(x.Email));

        RuleFor(x => x.PhoneNumber).Matches(@"^[0-9]{10}$")
            .When(x => !string.IsNullOrWhiteSpace(x.PhoneNumber))
            .WithMessage("phone number must be exactly 10 digits");

        RuleFor(x => x.Gender).Must(Gender.IsValid);
    }
}

public class PassengerValidator : AbstractValidator<PassengerDTO>
{
    public PassengerValidator()
    {
        RuleFor(x => x.FullName).NotEmpty().MaximumLength(120);

        RuleFor(x => x.BirthDate).NotNull()
            .WithMessage("birthDate is required for every passenger");

        RuleFor(x => x.BirthDate)
            .LessThanOrEqualTo(DateOnly.FromDateTime(DateTime.Today))
            .When(x => x.BirthDate is not null)
            .WithMessage("birthDate cannot be in the future");

        RuleFor(x => x.Email).EmailAddress()
            .When(x => !string.IsNullOrWhiteSpace(x.Email));
    }
}

public class BookingRequestValidator : AbstractValidator<BookingRequestDTO>
{
    public BookingRequestValidator()
    {
        RuleFor(x => x.TourId).NotNull();
        RuleFor(x => x.ScheduleId).NotNull();

        RuleFor(x => x.Passengers).NotEmpty()
            .WithMessage("at least one passenger is required");

        RuleForEach(x => x.Passengers).SetValidator(new PassengerValidator());

        RuleFor(x => x.ContactEmail).EmailAddress()
            .When(x => !string.IsNullOrWhiteSpace(x.ContactEmail));

        RuleFor(x => x.ContactPhone).Matches(@"^[0-9]{10}$")
            .When(x => !string.IsNullOrWhiteSpace(x.ContactPhone))
            .WithMessage("contact phone must be exactly 10 digits");
    }
}

public class TourRequestValidator : AbstractValidator<TourRequestDTO>
{
    public TourRequestValidator()
    {
        RuleFor(x => x.TourName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.Destination).NotEmpty().MaximumLength(150);

        RuleFor(x => x.Days).NotNull().GreaterThan(0);
        RuleFor(x => x.Nights).NotNull().GreaterThanOrEqualTo(0);

        RuleFor(x => x.Nights).LessThan(x => x.Days)
            .When(x => x.Days is not null && x.Nights is not null)
            .WithMessage("a tour cannot have more nights than days");

        RuleFor(x => x.Price).GreaterThan(0).When(x => x.Price is not null);
        RuleFor(x => x.CategoryId).NotNull().WithMessage("pick a category");

        RuleFor(x => x.TourType).Must(t => t is "DOMESTIC" or "INTERNATIONAL")
            .When(x => !string.IsNullOrWhiteSpace(x.TourType))
            .WithMessage("tourType must be DOMESTIC or INTERNATIONAL");
    }
}

public class CostRequestValidator : AbstractValidator<CostRequestDTO>
{
    public CostRequestValidator()
    {
        RuleFor(x => x.TourId).NotNull();

        RuleFor(x => x.AdultPrice).NotNull().GreaterThan(0)
            .WithMessage("the twin sharing fare is required and must be above zero");

        RuleFor(x => x.ValidTo).GreaterThanOrEqualTo(x => x.ValidFrom)
            .When(x => x.ValidFrom is not null && x.ValidTo is not null)
            .WithMessage("valid-to cannot be before valid-from");
    }
}

public class ScheduleRequestValidator : AbstractValidator<ScheduleRequestDTO>
{
    public ScheduleRequestValidator()
    {
        RuleFor(x => x.TourId).NotNull();
        RuleFor(x => x.StartDate).NotNull();

        RuleFor(x => x.TotalSeats).GreaterThan(0)
            .When(x => x.TotalSeats is not null);

        RuleFor(x => x.AvailableSeats).LessThanOrEqualTo(x => x.TotalSeats)
            .When(x => x.AvailableSeats is not null && x.TotalSeats is not null)
            .WithMessage("seats left cannot exceed total seats");
    }
}

public class ReviewRequestValidator : AbstractValidator<ReviewRequestDTO>
{
    public ReviewRequestValidator()
    {
        RuleFor(x => x.BookingId).NotNull();
        RuleFor(x => x.Rating).NotNull().InclusiveBetween(1, 5);
        RuleFor(x => x.ReviewTitle).MaximumLength(150);
        RuleFor(x => x.ReviewDescription).MaximumLength(1000);
    }
}

public class FeedbackRequestValidator : AbstractValidator<FeedbackRequestDTO>
{
    public FeedbackRequestValidator()
    {
        RuleFor(x => x.Message).NotEmpty().Length(5, 2000);

        RuleFor(x => x.Rating).InclusiveBetween(1, 5)
            .When(x => x.Rating is not null);

        RuleFor(x => x.Email).EmailAddress()
            .When(x => !string.IsNullOrWhiteSpace(x.Email));

        RuleFor(x => x.Category).Must(FeedbackCategory.IsValidCategory)
            .WithMessage($"category must be one of {string.Join(", ", FeedbackCategory.All)}");
    }
}
