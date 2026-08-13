using ETour.Api.Models;
using ETour.Api.Options;
using Microsoft.Extensions.Options;

namespace ETour.Api.Service;

public interface IReceiptService
{
    string GenerateAndSendReceipt(Booking booking);
}

public class ReceiptService : IReceiptService
{
    private readonly IEmailService _email;
    private readonly MailOptions _mail;
    private readonly ILogger<ReceiptService> _log;

    public ReceiptService(IEmailService email, IOptions<MailOptions> mail,
                          ILogger<ReceiptService> log)
    {
        _email = email;
        _mail = mail.Value;
        _log = log;
    }

    public string GenerateAndSendReceipt(Booking booking)
    {
        var recipient = booking.ResolveNotificationEmail();
        var tourName = booking.Tour?.TourName ?? "unknown";

        _log.LogInformation("Receipt for booking {Id} ({Tour}) queued for {To}",
            booking.BookingId, tourName, recipient);

        _email.SendBookingInvoice(booking);

        if (recipient is null)
        {
            return "Booking confirmed. No e-mail address on file, so no invoice was sent.";
        }

        if (!_mail.Enabled)
        {
            return $"Booking confirmed. Your invoice would be e-mailed to {recipient} "
                 + "(e-mail delivery is switched off in this environment).";
        }

        return $"Booking confirmed. Your invoice has been e-mailed to {recipient}.";
    }
}
