using ETour.Api.Options;
using Microsoft.Extensions.Options;

namespace ETour.Api.Pricing;

public class RefundPolicy
{
    private readonly CancellationOptions _o;

    public RefundPolicy(IOptions<CancellationOptions> options) => _o = options.Value;

    public decimal CalculateRefund(decimal? amountPaid, DateOnly? departureDate)
    {
        if (amountPaid is null) return 0m;
        var percent = ResolvePercent(departureDate);
        return Math.Round(amountPaid.Value * percent / 100m, 2, MidpointRounding.AwayFromZero);
    }

    public decimal ResolvePercent(DateOnly? departureDate)
    {
        if (departureDate is null) return _o.NoRefundPercent;

        var daysNotice = departureDate.Value.DayNumber
                       - DateOnly.FromDateTime(DateTime.Today).DayNumber;

        if (daysNotice >= _o.FullRefundDays) return _o.FullRefundPercent;
        if (daysNotice >= _o.PartialRefundDays) return _o.PartialRefundPercent;
        return _o.NoRefundPercent;
    }

    public string Describe(DateOnly? departureDate)
    {
        if (departureDate is null) return "Departure date unknown; no refund applied.";

        var daysNotice = departureDate.Value.DayNumber
                       - DateOnly.FromDateTime(DateTime.Today).DayNumber;

        var percent = ResolvePercent(departureDate).ToString("0.##");
        return $"Cancelled {daysNotice} day(s) before departure. {percent} percent refunded per policy.";
    }
}
