using ETour.Api.Models;

namespace ETour.Api.Service;

public interface IEmailService
{
    void SendWelcomeEmail(User user);
    void SendPasswordChangedEmail(User user, bool changedByAdmin);
    void SendBookingInvoice(Booking booking);
    void SendTourCompletedEmail(Booking booking);
    void SendCancellationEmail(Booking booking, decimal refundAmount, string policyNote);
}
