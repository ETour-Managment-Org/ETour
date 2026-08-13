using ETour.Api.Options;
using Microsoft.Extensions.Options;

namespace ETour.Api.Pricing;

public class FareBandPolicy
{
    public int ChildMaxAge { get; }

    public FareBandPolicy(IOptions<PricingOptions> options)
    {
        ChildMaxAge = options.Value.ChildMaxAge;
    }

    public int CalculateAgeAtDeparture(DateOnly? birthDate, DateOnly? departureDate)
    {
        if (birthDate is null)
        {
            throw new ArgumentException("birthDate is required for every passenger");
        }
        if (departureDate is null)
        {
            throw new ArgumentException("departure date is required to price a booking");
        }
        if (birthDate > departureDate)
        {
            throw new ArgumentException(
                $"birthDate {birthDate:yyyy-MM-dd} is after the departure date {departureDate:yyyy-MM-dd}");
        }

        var b = birthDate.Value;
        var d = departureDate.Value;

        var age = d.Year - b.Year;
        if (d < b.AddYears(age)) age--;
        return age;
    }

    public bool IsChild(int age) => age <= ChildMaxAge;

    public FareBand Resolve(int age, bool withBed, string occupancy)
    {
        if (IsChild(age))
        {
            return withBed ? FareBand.CHILD_WITH_BED : FareBand.CHILD_WITHOUT_BED;
        }

        var requested = FareBandExtensions.FromOccupancy(occupancy);
        if (requested == FareBand.SINGLE || requested == FareBand.EXTRA_PERSON)
        {
            return requested.Value;
        }
        return FareBand.TWIN_SHARING;
    }
}
