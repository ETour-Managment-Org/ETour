using System.Net.Http.Json;
using System.Text.Json;
using ETour.Api.DTO;

namespace ETour.Api.Service;

public class JavaMicroserviceClient
{
    private readonly HttpClient _http;
    private readonly ILogger<JavaMicroserviceClient> _log;

    private static readonly JsonSerializerOptions Json =
        new(JsonSerializerDefaults.Web);

    public JavaMicroserviceClient(HttpClient http, ILogger<JavaMicroserviceClient> log)
    {
        _http = http;
        _log = log;
    }

    public async Task<string> GetDataAsync(string path, CancellationToken ct = default)
    {
        _log.LogInformation("Calling Java service {BaseAddress}{Path}", _http.BaseAddress, path);

        var response = await _http.GetAsync(path, ct);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadAsStringAsync(ct);
    }

    public async Task<List<TourListDTO>> GetToursAsync(CancellationToken ct = default)
    {
        var tours = await _http.GetFromJsonAsync<List<TourListDTO>>("/api/tours", Json, ct);
        _log.LogInformation("Java service returned {Count} tours", tours?.Count ?? 0);
        return tours ?? new List<TourListDTO>();
    }

    public async Task<bool> IsReachableAsync(CancellationToken ct = default)
    {
        try
        {
            using var response = await _http.GetAsync("/api/tours/cities", ct);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _log.LogWarning("Java service unreachable: {Message}", ex.Message);
            return false;
        }
    }
}
