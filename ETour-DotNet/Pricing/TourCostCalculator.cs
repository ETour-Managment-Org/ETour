using ETour.Api.Models;

namespace ETour.Api.Pricing;

public class TourCostCalculator
{
    public decimal RateFor(Cost cost, FareBand? band)
    {
        if (cost is null || band is null) return 0m;

        return band.Value switch
        {
            FareBand.TWIN_SHARING => cost.AdultPrice ?? 0m,
            FareBand.SINGLE => cost.SinglePersonPrice ?? cost.AdultPrice ?? 0m,
            FareBand.EXTRA_PERSON => cost.ExtraPersonPrice ?? cost.AdultPrice ?? 0m,
            FareBand.CHILD_WITH_BED => cost.ChildWithBedPrice ?? cost.AdultPrice ?? 0m,
            FareBand.CHILD_WITHOUT_BED => cost.ChildWithoutBedPrice ?? cost.AdultPrice ?? 0m,
            _ => 0m
        };
    }

    public decimal TwinSharingRate(Cost cost) => cost?.AdultPrice ?? 0m;

    public bool IsValidOn(Cost cost, DateOnly? onDate)
    {
        if (cost is null || onDate is null) return false;
        var fromOk = cost.ValidFrom is null || onDate >= cost.ValidFrom;
        var toOk = cost.ValidTo is null || onDate <= cost.ValidTo;
        return fromOk && toOk;
    }
}
