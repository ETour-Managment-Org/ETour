using System.Text;
using ETour.Api.Models;
using ETour.Api.Options;

namespace ETour.Api.Service;

public static class EmailTemplates
{
    private const string DateFmt = "d MMMM yyyy";
    private const string StampFmt = "d MMMM yyyy 'at' HH:mm";

    private const string Primary = "#d95800";
    private const string Ink = "#241b13";
    private const string Muted = "#72665e";
    private const string Line = "#e4ddd3";
    private const string Cream = "#fffbf6";

    public static string WelcomeSubject() => "Welcome to e-Tour";

    public static string Welcome(User user, MailOptions p) =>
        Shell("Welcome aboard", $"""
            <p style="font-size:16px;margin:0 0 16px">Hello {DisplayName(user)},</p>
            <p style="margin:0 0 16px">
              Your e-Tour account is ready. You can now browse tours, compare fares
              across passenger types and book in a few minutes.
            </p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:24px 0">
              <tr><td style="padding:14px 16px;background:{Cream};border:1px solid {Line};border-radius:10px">
                <div style="font-size:12px;color:{Muted};text-transform:uppercase;letter-spacing:.08em">Your username</div>
                <div style="font-size:18px;font-weight:600;color:{Ink}">{Safe(user.Username)}</div>
              </td></tr>
            </table>
            <p style="margin:0 0 8px;font-weight:600">A few things worth knowing</p>
            <ul style="margin:0 0 20px;padding-left:20px;color:{Muted}">
              <li style="margin-bottom:6px">Prices are quoted per person on twin sharing.</li>
              <li style="margin-bottom:6px">Each traveller's fare is set by their age <b>on the departure date</b>, not today.</li>
              <li style="margin-bottom:6px">Cancel 7 or more days before departure for a full refund.</li>
            </ul>
            <p style="margin:0">Happy travels,<br><b>The e-Tour team</b></p>
            """, p);

    public static string PasswordChangedSubject() => "Your e-Tour password was changed";

    public static string PasswordChanged(User user, MailOptions p, bool changedByAdmin, string siteUrl)
    {
        var who = changedByAdmin
            ? "An administrator has reset the password on your account."
            : "The password on your account was just changed from your profile page.";

        return Shell("Your password was changed", $"""
            <p style="font-size:16px;margin:0 0 16px">Hello {DisplayName(user)},</p>
            <p style="margin:0 0 16px">{who}</p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:24px 0">
              <tr><td style="padding:14px 16px;background:{Cream};border:1px solid {Line};border-radius:10px">
                <div style="font-size:12px;color:{Muted};text-transform:uppercase;letter-spacing:.08em">Account</div>
                <div style="font-size:18px;font-weight:600;color:{Ink};margin-bottom:14px">{Safe(user.Username)}</div>
                <div style="font-size:12px;color:{Muted};text-transform:uppercase;letter-spacing:.08em">Changed on</div>
                <div style="font-size:15px;font-weight:600;color:{Ink}">{DateTime.Now.ToString(StampFmt)}</div>
              </td></tr>
            </table>
            <p style="margin:0 0 20px">
              You can sign in with the new password straight away. Your bookings and
              saved details are unchanged.
            </p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 24px">
              <tr><td align="center" style="padding:4px">
                <a href="{siteUrl}/login" style="display:inline-block;background:{Primary};color:#fffbf4;
                   text-decoration:none;padding:13px 30px;border-radius:8px;font-weight:600;font-size:15px">Sign in</a>
              </td></tr>
            </table>
            <p style="margin:0 0 6px;font-size:13px;color:{Muted}">
              <b>If this was not you</b>, write to {p.SupportEmail} straight away and we will
              secure the account while we look into it.
            </p>
            <p style="margin:0;font-size:13px;color:{Muted}">
              e-Tour will never ask you for your password by e-mail or over the phone.
            </p>
            """, p);
    }

    public static string InvoiceSubject(Booking b) =>
        $"Booking confirmed - {b.Tour?.TourName ?? "your tour"} (#{b.BookingId})";

    public static string Invoice(Booking b, MailOptions p)
    {
        var rows = new StringBuilder();
        foreach (var pax in b.Passengers ?? new List<PassengerDetails>())
        {
            rows.Append($"""
                <tr>
                  <td style="padding:10px 12px;border-bottom:1px solid {Line}">{Safe(pax.FullName)}</td>
                  <td style="padding:10px 12px;border-bottom:1px solid {Line};text-align:center">{pax.Age}</td>
                  <td style="padding:10px 12px;border-bottom:1px solid {Line}">{BandLabel(pax.PaxType)}</td>
                </tr>
                """);
        }

        return Shell("Booking confirmed", $"""
            <p style="font-size:16px;margin:0 0 16px">Hello {BookingContact(b)},</p>
            <p style="margin:0 0 24px">Your booking is confirmed. The details are below.</p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 24px">
              <tr><td style="padding:16px;background:{Cream};border:1px solid {Line};border-radius:10px">
                <div style="font-size:12px;color:{Muted};text-transform:uppercase;letter-spacing:.08em">Booking reference</div>
                <div style="font-size:20px;font-weight:700;color:{Primary};margin-bottom:12px">#{b.BookingId}</div>
                <div style="color:{Ink};font-weight:600">{Safe(b.Tour?.TourName)}</div>
                <div style="color:{Muted};font-size:14px">{Safe(b.Tour?.Destination)}</div>
                <div style="color:{Muted};font-size:14px;margin-top:8px">
                  Departs {b.Schedule?.StartDate?.ToString(DateFmt) ?? "-"}
                  &middot; Booked {b.BookingDate?.ToString(DateFmt) ?? "-"}
                </div>
              </td></tr>
            </table>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 20px">
              <tr style="background:{Cream}">
                <th align="left" style="padding:10px 12px;font-size:12px;color:{Muted};text-transform:uppercase">Traveller</th>
                <th style="padding:10px 12px;font-size:12px;color:{Muted};text-transform:uppercase">Age</th>
                <th align="left" style="padding:10px 12px;font-size:12px;color:{Muted};text-transform:uppercase">Fare band</th>
              </tr>
              {rows}
            </table>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 24px">
              <tr><td style="padding:14px 16px;background:{Cream};border:1px solid {Ink}20;border-radius:10px">
                <span style="color:{Muted}">Total paid</span>
                <span style="float:right;font-size:20px;font-weight:700;color:{Primary}">{Money(b.TotalAmount)}</span>
              </td></tr>
            </table>
            <p style="margin:0;font-size:13px;color:{Muted}">
              Questions? Call us on {p.SupportPhone} or reply to this e-mail.
            </p>
            """, p);
    }

    public static string CancellationSubject(Booking b) =>
        $"Booking cancelled - {b.Tour?.TourName ?? "your tour"} (#{b.BookingId})";

    public static string Cancellation(Booking b, MailOptions p, decimal refundAmount, string policyNote) =>
        Shell("Your booking has been cancelled", $"""
            <p style="font-size:16px;margin:0 0 16px">Hello {BookingContact(b)},</p>
            <p style="margin:0 0 24px">
              Booking <b>#{b.BookingId}</b> for <b>{Safe(b.Tour?.TourName)}</b> has been cancelled.
            </p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 24px">
              <tr><td style="padding:16px;background:{Cream};border:1px solid {Line};border-radius:10px">
                <div style="font-size:12px;color:{Muted};text-transform:uppercase;letter-spacing:.08em">Refund amount</div>
                <div style="font-size:22px;font-weight:700;color:{Primary};margin-bottom:10px">{Money(refundAmount)}</div>
                <div style="color:{Muted};font-size:14px">{Safe(policyNote)}</div>
              </td></tr>
            </table>
            <p style="margin:0 0 8px;font-weight:600">Our cancellation policy</p>
            <ul style="margin:0 0 20px;padding-left:20px;color:{Muted}">
              <li style="margin-bottom:6px">7 or more days before departure - 100 percent refunded.</li>
              <li style="margin-bottom:6px">3 to 6 days before departure - 50 percent refunded.</li>
              <li style="margin-bottom:6px">Under 3 days before departure - no refund.</li>
            </ul>
            <p style="margin:0;font-size:13px;color:{Muted}">
              Refunds reach the original payment method in 5 to 7 working days.
              Call {p.SupportPhone} if you have not seen it by then.
            </p>
            """, p);

    public static string CompletedSubject(Booking b) =>
        $"How was {b.Tour?.TourName ?? "your tour"}?";

    public static string TourCompleted(Booking b, MailOptions p, string siteUrl) =>
        Shell("Thank you for travelling with us", $"""
            <p style="font-size:16px;margin:0 0 16px">Hello {BookingContact(b)},</p>
            <p style="margin:0 0 16px">
              Welcome home. We hope <b>{Safe(b.Tour?.TourName)}</b> lived up to expectations.
            </p>
            <p style="margin:0 0 24px">
              Would you take a moment to tell other travellers how it went? Your review
              appears on the tour page and genuinely helps somebody choosing their next trip.
            </p>
            <table role="presentation" style="width:100%;border-collapse:collapse;margin:0 0 24px">
              <tr><td align="center" style="padding:20px;background:{Cream};border:1px solid {Line};border-radius:10px">
                <div style="font-size:13px;color:{Muted};margin-bottom:4px">{Safe(b.Tour?.Destination)}</div>
                <div style="font-size:18px;font-weight:700;color:{Ink};margin-bottom:16px">{Safe(b.Tour?.TourName)}</div>
                <a href="{siteUrl}/dashboard?review={b.BookingId}"
                   style="display:inline-block;background:{Primary};color:#fffbf4;text-decoration:none;
                          padding:13px 30px;border-radius:8px;font-weight:600;font-size:15px">Write your review</a>
              </td></tr>
            </table>
            <p style="margin:0 0 6px;font-size:13px;color:{Muted}">
              It takes about a minute - a star rating and a sentence or two is plenty.
            </p>
            <p style="margin:0;font-size:13px;color:{Muted}">
              If the button does not work, sign in and open <b>My bookings</b>.
            </p>
            """, p);

    private static string Shell(string heading, string body, MailOptions p) => $"""
        <!DOCTYPE html>
        <html><body style="margin:0;padding:24px;background:#f4f1ec;
             font-family:Arial,Helvetica,sans-serif;color:{Ink};line-height:1.55">
          <table role="presentation" style="max-width:600px;margin:0 auto;
                 border-collapse:collapse;background:#ffffff;border-radius:14px;overflow:hidden">
            <tr><td style="background:{Primary};padding:22px 28px">
              <div style="color:#fffbf4;font-size:22px;font-weight:700">e-Tour</div>
              <div style="color:#ffe6d2;font-size:13px">{heading}</div>
            </td></tr>
            <tr><td style="padding:28px">{body}</td></tr>
            <tr><td style="padding:18px 28px;border-top:1px solid {Line};font-size:12px;color:{Muted}">
              e-Tour by IndiaTour Pvt. Ltd. &middot; 177, Vidyanidhi, Vile Parle, Mumbai 400600<br>
              {p.SupportEmail} &middot; {p.SupportPhone}
            </td></tr>
          </table>
        </body></html>
        """;

    private static string DisplayName(User user) =>
        user is null ? "there" : Safe(user.DisplayName());

    private static string BookingContact(Booking b)
    {
        if (b is null) return "there";
        if (!string.IsNullOrWhiteSpace(b.ContactName)) return Safe(b.ContactName.Trim());
        return DisplayName(b.User);
    }

    private static string Money(decimal? amount) =>
        amount is null ? "-" : "Rs " + Math.Round(amount.Value, 0, MidpointRounding.AwayFromZero);

    private static string BandLabel(string paxType) => paxType switch
    {
        "TWIN_SHARING" => "Twin sharing",
        "SINGLE" => "Single occupancy",
        "EXTRA_PERSON" => "Extra person",
        "CHILD_WITH_BED" => "Child with bed",
        "CHILD_WITHOUT_BED" => "Child without bed",
        null => "-",
        _ => paxType
    };

    private static string Safe(string value) =>
        value is null ? "-" : value.Replace("&", "&amp;").Replace("<", "&lt;").Replace(">", "&gt;");
}
