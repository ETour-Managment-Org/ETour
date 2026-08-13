using ETour.Api.Options;
using Microsoft.Extensions.Options;

namespace ETour.Api.Service;

public class TourCompletionWorker : BackgroundService
{
    private readonly IServiceProvider _services;
    private readonly SiteOptions _site;
    private readonly ILogger<TourCompletionWorker> _log;

    public TourCompletionWorker(IServiceProvider services, IOptions<SiteOptions> site,
                                ILogger<TourCompletionWorker> log)
    {
        _services = services;
        _site = site.Value;
        _log = log;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        if (!TimeOnly.TryParse(_site.CompletionSweepTime, out var runAt))
        {
            runAt = new TimeOnly(7, 0);
        }

        _log.LogInformation("Tour completion sweep scheduled daily at {Time}", runAt);

        while (!stoppingToken.IsCancellationRequested)
        {
            var now = DateTime.Now;
            var next = now.Date.Add(runAt.ToTimeSpan());
            if (next <= now) next = next.AddDays(1);

            try
            {
                await Task.Delay(next - now, stoppingToken);
            }
            catch (TaskCanceledException)
            {
                return;
            }

            try
            {
                using var scope = _services.CreateScope();
                var bookings = scope.ServiceProvider.GetRequiredService<IBookingService>();
                await bookings.CompleteElapsedBookingsAsync();
            }
            catch (Exception ex)
            {
                _log.LogError(ex, "Tour completion sweep failed");
            }
        }
    }
}
