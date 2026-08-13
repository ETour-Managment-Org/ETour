using ETour.Api.Models;
using ETour.Api.Options;
using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;

namespace ETour.Api.Service;

public class EmailService : IEmailService
{
    private readonly MailOptions _mail;
    private readonly SiteOptions _site;
    private readonly ILogger<EmailService> _log;

    public EmailService(IOptions<MailOptions> mail, IOptions<SiteOptions> site,
                        ILogger<EmailService> log)
    {
        _mail = mail.Value;
        _site = site.Value;
        _log = log;
    }

    public void SendWelcomeEmail(User user)
    {
        if (string.IsNullOrWhiteSpace(user?.Email)) return;
        Send(user.Email, EmailTemplates.WelcomeSubject(),
             EmailTemplates.Welcome(user, _mail), "welcome");
    }

    public void SendPasswordChangedEmail(User user, bool changedByAdmin)
    {
        if (string.IsNullOrWhiteSpace(user?.Email)) return;
        Send(user.Email, EmailTemplates.PasswordChangedSubject(),
             EmailTemplates.PasswordChanged(user, _mail, changedByAdmin, _site.SiteUrl),
             $"password change notice for {user.Username}");
    }

    public void SendBookingInvoice(Booking booking)
    {
        var to = RecipientFor(booking);
        if (to is null)
        {
            _log.LogWarning("Booking {Id} has no e-mail address, invoice not sent",
                booking?.BookingId);
            return;
        }
        Send(to, EmailTemplates.InvoiceSubject(booking),
             EmailTemplates.Invoice(booking, _mail),
             $"invoice for booking {booking.BookingId}");
    }

    public void SendCancellationEmail(Booking booking, decimal refundAmount, string policyNote)
    {
        var to = RecipientFor(booking);
        if (to is null) return;
        Send(to, EmailTemplates.CancellationSubject(booking),
             EmailTemplates.Cancellation(booking, _mail, refundAmount, policyNote),
             $"cancellation notice for booking {booking.BookingId}");
    }

    public void SendTourCompletedEmail(Booking booking)
    {
        var to = RecipientFor(booking);
        if (to is null) return;
        Send(to, EmailTemplates.CompletedSubject(booking),
             EmailTemplates.TourCompleted(booking, _mail, _site.SiteUrl),
             $"tour completed notice for booking {booking.BookingId}");
    }

    private static string RecipientFor(Booking booking)
    {
        var email = booking?.ResolveNotificationEmail();
        return string.IsNullOrWhiteSpace(email) ? null : email;
    }

    private void Send(string to, string subject, string html, string what)
    {
        var recipient = _mail.HasOverride() ? _mail.OverrideRecipient : to;

        if (!_mail.Enabled)
        {
            _log.LogInformation("[MAIL DISABLED] would send '{Subject}' to {To} ({What})",
                subject, recipient, what);
            return;
        }

        _ = Task.Run(async () =>
        {
            try
            {
                var message = new MimeMessage();
                message.From.Add(new MailboxAddress(_mail.FromName, _mail.From));
                message.To.Add(MailboxAddress.Parse(recipient));
                message.Subject = subject;
                message.Body = new BodyBuilder { HtmlBody = html }.ToMessageBody();

                using var client = new SmtpClient();
                await client.ConnectAsync(_mail.Host, _mail.Port, SecureSocketOptions.StartTls);

                if (!string.IsNullOrWhiteSpace(_mail.Username))
                {
                    await client.AuthenticateAsync(_mail.Username, _mail.Password);
                }

                await client.SendAsync(message);
                await client.DisconnectAsync(true);

                _log.LogInformation("Sent '{Subject}' to {To}", subject, recipient);
            }
            catch (Exception ex)
            {
                _log.LogError("Could not send {What} to {To}: {Message}",
                    what, recipient, ex.Message);
            }
        });
    }
}
