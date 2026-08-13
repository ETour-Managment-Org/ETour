package com.etour.email;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.etour.entities.Booking;
import com.etour.entities.PassengerDetails;
import com.etour.entities.User;

public final class EmailTemplates {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'at' HH:mm");

    private static final String PRIMARY = "#d95800";
    private static final String INK = "#241b13";
    private static final String MUTED = "#72665e";
    private static final String LINE = "#e4ddd3";
    private static final String CREAM = "#fffbf6";

    private EmailTemplates() {
    }

    public static String welcomeSubject() {
        return "Welcome to e-Tour";
    }

    public static String welcome(User user, EmailProperties props) {
        String name = displayName(user);

        return shell("Welcome aboard", """
                <p style="font-size:16px;margin:0 0 16px">Hello %s,</p>

                <p style="margin:0 0 16px">
                  Your e-Tour account is ready. You can now browse tours, compare fares
                  across passenger types and book in a few minutes.
                </p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin:24px 0">
                  <tr>
                    <td style="padding:14px 16px;background:%s;border:1px solid %s;border-radius:10px">
                      <div style="font-size:12px;color:%s;text-transform:uppercase;letter-spacing:.08em">Your username</div>
                      <div style="font-size:18px;font-weight:600;color:%s">%s</div>
                    </td>
                  </tr>
                </table>

                <p style="margin:0 0 8px;font-weight:600">A few things worth knowing</p>
                <ul style="margin:0 0 20px;padding-left:20px;color:%s">
                  <li style="margin-bottom:6px">Prices are quoted per person on twin sharing.</li>
                  <li style="margin-bottom:6px">Each traveller's fare is set by their age <b>on the departure date</b>, not today.</li>
                  <li style="margin-bottom:6px">Cancel 7 or more days before departure for a full refund.</li>
                </ul>

                <p style="margin:0">Happy travels,<br><b>The e-Tour team</b></p>
                """.formatted(name, CREAM, LINE, MUTED, INK, user.getUsername(), MUTED), props);
    }

    public static String passwordChangedSubject() {
        return "Your e-Tour password was changed";
    }

    public static String passwordChanged(User user, EmailProperties props,
                                         boolean changedByAdmin, String siteUrl) {
        String name = displayName(user);
        String when = LocalDateTime.now().format(STAMP);
        String who = changedByAdmin
                ? "An administrator has reset the password on your account."
                : "The password on your account was just changed from your profile page.";

        return shell("Your password was changed", """
                <p style="font-size:16px;margin:0 0 16px">Hello %s,</p>

                <p style="margin:0 0 16px">%s</p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin:24px 0">
                  <tr>
                    <td style="padding:14px 16px;background:%s;border:1px solid %s;border-radius:10px">
                      <div style="font-size:12px;color:%s;text-transform:uppercase;letter-spacing:.08em">Account</div>
                      <div style="font-size:18px;font-weight:600;color:%s;margin-bottom:14px">%s</div>
                      <div style="font-size:12px;color:%s;text-transform:uppercase;letter-spacing:.08em">Changed on</div>
                      <div style="font-size:15px;font-weight:600;color:%s">%s</div>
                    </td>
                  </tr>
                </table>

                <p style="margin:0 0 20px">
                  You can sign in with the new password straight away. Your bookings and
                  saved details are unchanged.
                </p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin:0 0 24px">
                  <tr>
                    <td align="center" style="padding:4px">
                      <a href="%s"
                         style="display:inline-block;background:%s;color:#fffbf4;text-decoration:none;
                                padding:13px 30px;border-radius:8px;font-weight:600;font-size:15px">
                        Sign in
                      </a>
                    </td>
                  </tr>
                </table>

                <p style="margin:0 0 6px;font-size:13px;color:%s">
                  <b>If this was not you</b>, write to %s straight away and we will secure
                  the account while we look into it.
                </p>
                <p style="margin:0;font-size:13px;color:%s">
                  e-Tour will never ask you for your password by e-mail or over the phone.
                </p>
                """.formatted(name, who,
                              CREAM, LINE, MUTED, INK, safe(user.getUsername()),
                              MUTED, INK, when,
                              siteUrl + "/login", PRIMARY,
                              MUTED, props.getSupportEmail(), MUTED), props);
    }

    public static String invoiceSubject(Booking booking) {
        String tour = booking.getTour() != null ? booking.getTour().getTourName() : "your tour";
        return "Booking confirmed - " + tour + " (#" + booking.getBookingId() + ")";
    }

    public static String invoice(Booking booking, EmailProperties props) {
        String tourName = booking.getTour() != null ? booking.getTour().getTourName() : "-";
        String destination = booking.getTour() != null ? booking.getTour().getDestination() : "-";
        String departure = booking.getSchedule() != null && booking.getSchedule().getStartDate() != null
                ? booking.getSchedule().getStartDate().format(DATE) : "-";
        String bookedOn = booking.getBookingDate() != null
                ? booking.getBookingDate().format(DATE) : "-";
        String customer = bookingContact(booking);

        StringBuilder rows = new StringBuilder();
        if (booking.getPassengers() != null) {
            for (PassengerDetails p : booking.getPassengers()) {
                rows.append("""
                        <tr>
                          <td style="padding:10px 12px;border-bottom:1px solid %s">%s</td>
                          <td style="padding:10px 12px;border-bottom:1px solid %s;text-align:center">%s</td>
                          <td style="padding:10px 12px;border-bottom:1px solid %s">%s</td>
                        </tr>
                        """.formatted(LINE, safe(p.getFullName()),
                                      LINE, p.getAge() == null ? "-" : p.getAge(),
                                      LINE, bandLabel(p.getPaxType())));
            }
        }

        String total = money(booking.getTotalAmount());

        return shell("Booking confirmed", """
                <p style="font-size:16px;margin:0 0 16px">Hello %s,</p>

                <p style="margin:0 0 24px">
                  Your booking is confirmed. The details are below - please keep this
                  e-mail, it is your invoice.
                </p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin-bottom:24px">
                  <tr><td style="padding:6px 0;color:%s;width:45%%">Booking reference</td>
                      <td style="padding:6px 0;font-weight:600">#%s</td></tr>
                  <tr><td style="padding:6px 0;color:%s">Tour</td>
                      <td style="padding:6px 0;font-weight:600">%s</td></tr>
                  <tr><td style="padding:6px 0;color:%s">Destination</td>
                      <td style="padding:6px 0">%s</td></tr>
                  <tr><td style="padding:6px 0;color:%s">Departure</td>
                      <td style="padding:6px 0;font-weight:600">%s</td></tr>
                  <tr><td style="padding:6px 0;color:%s">Booked on</td>
                      <td style="padding:6px 0">%s</td></tr>
                </table>

                <div style="font-weight:600;margin-bottom:8px">Travellers</div>
                <table role="presentation" style="width:100%%;border-collapse:collapse;margin-bottom:24px">
                  <tr style="background:%s">
                    <th align="left"  style="padding:10px 12px;font-size:12px;color:%s;text-transform:uppercase">Name</th>
                    <th align="center" style="padding:10px 12px;font-size:12px;color:%s;text-transform:uppercase">Age</th>
                    <th align="left"  style="padding:10px 12px;font-size:12px;color:%s;text-transform:uppercase">Fare band</th>
                  </tr>
                  %s
                </table>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin-bottom:24px">
                  <tr>
                    <td style="padding:14px 16px;background:%s;border-radius:10px">
                      <table role="presentation" style="width:100%%">
                        <tr>
                          <td style="font-size:16px;font-weight:600;color:%s">Total paid</td>
                          <td align="right" style="font-size:22px;font-weight:700;color:%s">%s</td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>

                <p style="margin:0 0 6px;font-size:13px;color:%s">
                  Age is calculated on the departure date, which is why a traveller's fare band
                  may differ from their age today.
                </p>
                <p style="margin:0;font-size:13px;color:%s">
                  Need to change something? Reply to this e-mail or call %s.
                </p>
                """.formatted(customer,
                              MUTED, booking.getBookingId(),
                              MUTED, safe(tourName),
                              MUTED, safe(destination),
                              MUTED, departure,
                              MUTED, bookedOn,
                              CREAM, MUTED, MUTED, MUTED,
                              rows.toString(),
                              CREAM, INK, PRIMARY, total,
                              MUTED, MUTED, props.getSupportPhone()), props);
    }

    private static String shell(String heading, String body, EmailProperties props) {
        return """
                <!DOCTYPE html>
                <html><body style="margin:0;padding:24px;background:#f4f1ec;
                     font-family:Arial,Helvetica,sans-serif;color:%s;line-height:1.55">
                  <table role="presentation" style="max-width:600px;margin:0 auto;
                         border-collapse:collapse;background:#ffffff;border-radius:14px;overflow:hidden">
                    <tr>
                      <td style="background:%s;padding:22px 28px">
                        <div style="color:#fffbf4;font-size:22px;font-weight:700">e-Tour</div>
                        <div style="color:#ffe6d2;font-size:13px">%s</div>
                      </td>
                    </tr>
                    <tr><td style="padding:28px">%s</td></tr>
                    <tr>
                      <td style="padding:18px 28px;border-top:1px solid %s;font-size:12px;color:%s">
                        e-Tour by IndiaTour Pvt. Ltd. &middot; 177, Vidyanidhi, Vile Parle, Mumbai 400600<br>
                        %s &middot; %s
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(INK, PRIMARY, heading, body, LINE, MUTED,
                              props.getSupportEmail(), props.getSupportPhone());
    }

    private static String bookingContact(Booking booking) {
        if (booking == null) {
            return "there";
        }
        String contact = booking.getContactName();
        if (contact != null && !contact.isBlank()) {
            return safe(contact.trim());
        }
        return displayName(booking.getUser());
    }

    private static String displayName(User user) {
        if (user == null) {
            return "there";
        }
        String fn = user.getFirstName() != null ? user.getFirstName() : "";
        String ln = user.getLastName() != null ? user.getLastName() : "";
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? user.getUsername() : full;
    }

    private static String money(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        return "Rs " + amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String bandLabel(String paxType) {
        if (paxType == null) {
            return "-";
        }
        return switch (paxType) {
            case "TWIN_SHARING" -> "Twin sharing";
            case "SINGLE" -> "Single occupancy";
            case "EXTRA_PERSON" -> "Extra person";
            case "CHILD_WITH_BED" -> "Child with bed";
            case "CHILD_WITHOUT_BED" -> "Child without bed";
            default -> paxType;
        };
    }

    private static String safe(String value) {
        if (value == null) {
            return "-";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

public static String cancellationSubject(Booking booking) {
        String tour = booking.getTour() != null ? booking.getTour().getTourName() : "your tour";
        return "Booking cancelled - " + tour + " (#" + booking.getBookingId() + ")";
    }

    public static String cancellation(Booking booking, EmailProperties props,
                                      BigDecimal refundAmount, String policyNote) {
        String tourName = booking.getTour() != null ? booking.getTour().getTourName() : "your tour";
        String destination = booking.getTour() != null ? booking.getTour().getDestination() : "";
        String customer = bookingContact(booking);

        String departure = booking.getSchedule() != null && booking.getSchedule().getStartDate() != null
                ? booking.getSchedule().getStartDate().toString() : "-";

        BigDecimal paid = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        boolean hasRefund = refundAmount != null && refundAmount.signum() > 0;

        String refundBlock = hasRefund
                ? """
                  <table role="presentation" style="width:100%%;border-collapse:collapse;margin:0 0 22px">
                    <tr>
                      <td style="padding:18px;background:%s;border:1px solid %s;border-radius:10px">
                        <div style="font-size:13px;color:%s;margin-bottom:4px">Refund due to you</div>
                        <div style="font-size:26px;font-weight:700;color:%s">%s</div>
                        <div style="font-size:12px;color:%s;margin-top:8px">%s</div>
                      </td>
                    </tr>
                  </table>
                  <p style="margin:0 0 18px;font-size:13px;color:%s">
                    The refund goes back to the original payment method and usually takes
                    5 to 7 working days to appear on your statement.
                  </p>
                  """.formatted(CREAM, LINE, MUTED, PRIMARY, money(refundAmount), MUTED,
                                safe(policyNote), MUTED)
                : """
                  <table role="presentation" style="width:100%%;border-collapse:collapse;margin:0 0 22px">
                    <tr>
                      <td style="padding:18px;background:%s;border:1px solid %s;border-radius:10px">
                        <div style="font-size:13px;color:%s;margin-bottom:4px">Refund due to you</div>
                        <div style="font-size:20px;font-weight:700;color:%s">No refund applies</div>
                        <div style="font-size:12px;color:%s;margin-top:8px">%s</div>
                      </td>
                    </tr>
                  </table>
                  """.formatted(CREAM, LINE, MUTED, INK, MUTED, safe(policyNote));

        return shell("Your booking has been cancelled", """
                <p style="font-size:16px;margin:0 0 16px">Hello %s,</p>

                <p style="margin:0 0 22px">
                  We have cancelled your booking as requested. The details are below,
                  and nothing further is needed from you.
                </p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin:0 0 22px;font-size:14px">
                  <tr><td style="padding:7px 0;color:%s">Booking reference</td>
                      <td style="padding:7px 0;text-align:right;font-weight:600">#%s</td></tr>
                  <tr><td style="padding:7px 0;color:%s">Tour</td>
                      <td style="padding:7px 0;text-align:right;font-weight:600">%s</td></tr>
                  <tr><td style="padding:7px 0;color:%s">Destination</td>
                      <td style="padding:7px 0;text-align:right">%s</td></tr>
                  <tr><td style="padding:7px 0;color:%s">Was departing</td>
                      <td style="padding:7px 0;text-align:right">%s</td></tr>
                  <tr><td style="padding:7px 0;color:%s">Amount paid</td>
                      <td style="padding:7px 0;text-align:right">%s</td></tr>
                </table>

                %s

                <p style="margin:0;font-size:13px;color:%s">
                  Sorry to see this trip go. If plans change, everything is still on the
                  site and we would be glad to have you along another time.
                </p>
                """.formatted(safe(customer),
                              MUTED, booking.getBookingId(),
                              MUTED, safe(tourName),
                              MUTED, safe(destination),
                              MUTED, departure,
                              MUTED, money(paid),
                              refundBlock,
                              MUTED),
                props);
    }

    public static String completedSubject(Booking booking) {
        String tour = booking.getTour() != null ? booking.getTour().getTourName() : "your tour";
        return "How was " + tour + "?";
    }

    public static String tourCompleted(Booking booking, EmailProperties props, String siteUrl) {
        String tourName = booking.getTour() != null ? booking.getTour().getTourName() : "your tour";
        String destination = booking.getTour() != null ? booking.getTour().getDestination() : "";
        String customer = bookingContact(booking);
        String reviewLink = siteUrl + "/dashboard?review=" + booking.getBookingId();

        return shell("Thank you for travelling with us", """
                <p style="font-size:16px;margin:0 0 16px">Hello %s,</p>

                <p style="margin:0 0 16px">
                  Welcome home. We hope <b>%s</b> lived up to expectations.
                </p>

                <p style="margin:0 0 24px">
                  Would you take a moment to tell other travellers how it went? Your review
                  appears on the tour page and genuinely helps somebody choosing their
                  next trip.
                </p>

                <table role="presentation" style="width:100%%;border-collapse:collapse;margin:0 0 24px">
                  <tr>
                    <td align="center" style="padding:20px;background:%s;border:1px solid %s;border-radius:10px">
                      <div style="font-size:13px;color:%s;margin-bottom:4px">%s</div>
                      <div style="font-size:18px;font-weight:700;color:%s;margin-bottom:16px">%s</div>
                      <a href="%s"
                         style="display:inline-block;background:%s;color:#fffbf4;text-decoration:none;
                                padding:13px 30px;border-radius:8px;font-weight:600;font-size:15px">
                        Write your review
                      </a>
                    </td>
                  </tr>
                </table>

                <p style="margin:0 0 6px;font-size:13px;color:%s">
                  It takes about a minute - a star rating and a sentence or two is plenty.
                </p>
                <p style="margin:0;font-size:13px;color:%s">
                  If the button does not work, sign in and open <b>My bookings</b>.
                </p>
                """.formatted(customer, safe(tourName),
                              CREAM, LINE, MUTED, safe(destination), INK, safe(tourName),
                              reviewLink, PRIMARY, MUTED, MUTED), props);
    }
}
