using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace ETour.Api.Service;

public class RazorpayPaymentGatewayService : IPaymentGatewayService
{
    private const string Api = "https://api.razorpay.com/v1";

    private readonly HttpClient _http;
    private readonly ILogger<RazorpayPaymentGatewayService> _log;
    private readonly string _keyId;
    private readonly string _keySecret;
    private readonly string _currency;

    public RazorpayPaymentGatewayService(HttpClient http,
                                         IConfiguration config,
                                         ILogger<RazorpayPaymentGatewayService> log)
    {
        _http = http;
        _log = log;
        _keyId = config["ETour:Payment:Razorpay:KeyId"] ?? "";
        _keySecret = config["ETour:Payment:Razorpay:KeySecret"] ?? "";
        _currency = config["ETour:Payment:Currency"] ?? "INR";

        _http.BaseAddress = new Uri(Api + "/");
        if (!string.IsNullOrWhiteSpace(_keyId) && !string.IsNullOrWhiteSpace(_keySecret))
        {
            var raw = Encoding.UTF8.GetBytes($"{_keyId}:{_keySecret}");
            _http.DefaultRequestHeaders.Authorization =
                new AuthenticationHeaderValue("Basic", Convert.ToBase64String(raw));
        }

        if (string.IsNullOrWhiteSpace(_keyId) || string.IsNullOrWhiteSpace(_keySecret))
        {
            _log.LogError("Razorpay is selected but the keys are blank. Set " +
                          "ETour__Payment__Razorpay__KeyId and __KeySecret, or set " +
                          "ETour__Payment__Provider back to mock.");
        }
        else if (!_keyId.StartsWith("rzp_test_", StringComparison.Ordinal))
        {
            _log.LogWarning("Razorpay key does not start with rzp_test_. This project " +
                            "is meant to run in TEST mode only.");
        }
        else
        {
            _log.LogInformation("Razorpay gateway active in TEST mode.");
        }
    }

    public string Provider => "razorpay";

    public async Task<OrderResult> CreateOrderAsync(decimal amount, string receipt)
    {
        if (string.IsNullOrWhiteSpace(_keyId) || string.IsNullOrWhiteSpace(_keySecret))
            throw new InvalidOperationException(
                "Online payment is not configured on the server. Please try again later.");

        var paise = Paise(amount);

        var body = new Dictionary<string, object>
        {
            ["amount"] = paise,
            ["currency"] = _currency,
            ["receipt"] = receipt,
            ["payment_capture"] = 1
        };

        try
        {
            using var res = await _http.PostAsJsonAsync("orders", body);
            var json = await res.Content.ReadAsStringAsync();

            if (!res.IsSuccessStatusCode)
            {
                _log.LogError("Razorpay order creation failed ({Status}): {Body}",
                              (int)res.StatusCode, json);
                throw new InvalidOperationException(
                    "Could not start the payment. Please try again in a moment.");
            }

            using var doc = JsonDocument.Parse(json);
            if (!doc.RootElement.TryGetProperty("id", out var idEl))
                throw new InvalidOperationException("Razorpay returned no order id.");

            var orderId = idEl.GetString();
            _log.LogInformation("Razorpay order {Order} created for {Amount} {Currency} (receipt {Receipt})",
                                orderId, amount, _currency, receipt);

            return new OrderResult("razorpay", orderId, _keyId, paise, _currency, receipt);
        }
        catch (InvalidOperationException) { throw; }
        catch (Exception ex)
        {
            _log.LogError(ex, "Razorpay order creation threw");
            throw new InvalidOperationException(
                "Could not start the payment. Please try again in a moment.");
        }
    }

    public PaymentResult Charge(decimal amount, string method, int bookingId) =>
        new(false, null, "This booking was not paid for. Complete the Razorpay checkout first.");

    public async Task<PaymentResult> VerifyAsync(VerificationRequest request, decimal expectedAmount)
    {
        if (request is null
            || string.IsNullOrWhiteSpace(request.OrderId)
            || string.IsNullOrWhiteSpace(request.PaymentId)
            || string.IsNullOrWhiteSpace(request.Signature))
            return new PaymentResult(false, null, "Payment details are incomplete.");

        var expected = HmacSha256Hex($"{request.OrderId}|{request.PaymentId}", _keySecret);

        var ok = CryptographicOperations.FixedTimeEquals(
            Encoding.UTF8.GetBytes(expected),
            Encoding.UTF8.GetBytes(request.Signature.Trim().ToLowerInvariant()));

        if (!ok)
        {
            _log.LogWarning("Razorpay signature MISMATCH for order {Order} / payment {Payment} - rejecting",
                            request.OrderId, request.PaymentId);
            return new PaymentResult(false, null,
                "Payment could not be verified. You have not been charged for this booking.");
        }

        try
        {
            using var res = await _http.GetAsync($"payments/{request.PaymentId}");
            var json = await res.Content.ReadAsStringAsync();

            if (!res.IsSuccessStatusCode)
            {
                _log.LogError("Razorpay payment lookup failed ({Status}): {Body}",
                              (int)res.StatusCode, json);
                return new PaymentResult(false, null, "Payment could not be confirmed.");
            }

            using var doc = JsonDocument.Parse(json);
            var status = doc.RootElement.GetProperty("status").GetString();
            var paidPaise = doc.RootElement.GetProperty("amount").GetInt64();
            var wantPaise = Paise(expectedAmount);

            if (status != "captured" && status != "authorized")
                return new PaymentResult(false, null,
                    $"Payment status is '{status}'. The booking was not confirmed.");

            if (paidPaise != wantPaise)
            {
                _log.LogWarning("Razorpay amount mismatch: paid {Paid}, expected {Want}",
                                paidPaise, wantPaise);
                return new PaymentResult(false, null, "The amount paid does not match this booking.");
            }

            _log.LogInformation("Razorpay payment {Payment} verified and captured ({Paise} paise)",
                                request.PaymentId, paidPaise);
            return new PaymentResult(true, request.PaymentId, "Payment received.");
        }
        catch (Exception ex)
        {
            _log.LogError(ex, "Razorpay payment lookup threw for {Payment}", request.PaymentId);
            return new PaymentResult(false, null,
                "Could not confirm the payment with the gateway. " +
                "If money was deducted it will be refunded automatically.");
        }
    }

    private static long Paise(decimal amount) =>
        (long)(decimal.Round(amount, 2, MidpointRounding.AwayFromZero) * 100m);

    internal static string HmacSha256Hex(string data, string secret)
    {
        using var mac = new HMACSHA256(Encoding.UTF8.GetBytes(secret ?? ""));
        var hash = mac.ComputeHash(Encoding.UTF8.GetBytes(data));
        return Convert.ToHexString(hash).ToLowerInvariant();
    }
}
