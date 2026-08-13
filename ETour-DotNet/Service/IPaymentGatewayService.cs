namespace ETour.Api.Service;

public record PaymentResult(bool Success, string Reference, string Message);

public record OrderResult(string Provider, string OrderId, string KeyId,
                          long AmountPaise, string Currency, string Receipt);

public record VerificationRequest(string OrderId, string PaymentId, string Signature);

public interface IPaymentGatewayService
{
    string Provider { get; }

    PaymentResult Charge(decimal amount, string method, int bookingId);

    Task<OrderResult> CreateOrderAsync(decimal amount, string receipt);

    Task<PaymentResult> VerifyAsync(VerificationRequest request, decimal expectedAmount);
}

public class MockPaymentGatewayService : IPaymentGatewayService
{
    private readonly ILogger<MockPaymentGatewayService> _log;

    public MockPaymentGatewayService(ILogger<MockPaymentGatewayService> log) => _log = log;

    public string Provider => "mock";

    public PaymentResult Charge(decimal amount, string method, int bookingId)
    {
        var reference = $"MOCK-{bookingId}-{DateTime.Now:yyyyMMddHHmmss}";
        _log.LogInformation("Mock gateway charged {Amount} by {Method} for booking {Booking} ({Ref})",
            amount, method, bookingId, reference);
        return new PaymentResult(true, reference, "Payment accepted by the mock gateway");
    }

    public Task<OrderResult> CreateOrderAsync(decimal amount, string receipt) =>
        Task.FromResult(new OrderResult("mock",
            $"order_MOCK{DateTime.Now:yyyyMMddHHmmss}", "mock_key",
            (long)decimal.Round(amount * 100m, 0), "INR", receipt));

    public Task<PaymentResult> VerifyAsync(VerificationRequest request, decimal expectedAmount)
    {
        _log.LogInformation("Mock gateway verify - nothing to check, accepting {Ref}", request.PaymentId);
        return Task.FromResult(new PaymentResult(true, request.PaymentId, "Payment accepted."));
    }
}
