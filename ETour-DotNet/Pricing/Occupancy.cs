namespace ETour.Api.Pricing;

public static class Occupancy
{
    public const int MaxPerRoom = 2;
    public const int MaxExtraBedsPerRoom = 1;
    public const int MaxOccupantsPerRoom = MaxPerRoom + MaxExtraBedsPerRoom;

    public const string TwinSharing = "TWIN_SHARING";
    public const string Single = "SINGLE";
    public const string ExtraPerson = "EXTRA_PERSON";

    public static string DefaultOccupancy() => TwinSharing;

    public static int RoomsRequired(int twinSharingCount, int singleCount, int extraBedCount)
    {
        var twinRooms = (int)Math.Ceiling(twinSharingCount / (double)MaxPerRoom);
        var extraBedCapacity = twinRooms * MaxExtraBedsPerRoom;
        var overflowBeds = Math.Max(0, extraBedCount - extraBedCapacity);
        return twinRooms + singleCount + overflowBeds;
    }
}
