namespace ETour.Api.Pricing;

public enum FareBand
{
    TWIN_SHARING,
    SINGLE,
    EXTRA_PERSON,
    CHILD_WITH_BED,
    CHILD_WITHOUT_BED
}

public static class FareBandExtensions
{
    public static string Label(this FareBand band) => band switch
    {
        FareBand.TWIN_SHARING => "Twin sharing (per person)",
        FareBand.SINGLE => "Single occupancy",
        FareBand.EXTRA_PERSON => "Extra person in room",
        FareBand.CHILD_WITH_BED => "Child with bed",
        FareBand.CHILD_WITHOUT_BED => "Child without bed",
        _ => band.ToString()
    };

    public static FareBand? FromOccupancy(string value)
    {
        if (string.IsNullOrWhiteSpace(value)) return null;

        var v = value.Trim().ToUpperInvariant();
        if (v == "ADULT") return FareBand.TWIN_SHARING;
        if (v == "SINGLE_PERSON") return FareBand.SINGLE;

        return Enum.TryParse<FareBand>(v, out var parsed) ? parsed : null;
    }
}
