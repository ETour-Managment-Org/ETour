using System.Text.RegularExpressions;

namespace ETour.Api.Common;

public static class CityNames
{
    private static readonly Regex Separators =
        new(@"\s+[-–—]\s+|\s*,\s*|\s*/\s*|\s+to\s+",
            RegexOptions.Compiled | RegexOptions.IgnoreCase);

    public static List<string> Split(string destination)
    {
        var outList = new List<string>();
        if (string.IsNullOrWhiteSpace(destination))
        {
            return outList;
        }

        var seen = new HashSet<string>();
        foreach (var part in Separators.Split(destination))
        {
            var city = part.Trim();
            if (city.Length == 0) continue;
            if (city.Length > 100) city = city[..100];
            if (seen.Add(city)) outList.Add(city);
        }
        return outList;
    }
}
