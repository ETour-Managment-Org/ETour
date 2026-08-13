using System.ComponentModel.DataAnnotations;
using ETour.Api.Security;
using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize]
[Route("api/payments")]
public class PaymentController : ControllerBase
{
    private readonly IPaymentGatewayService _gateway;

    public PaymentController(IPaymentGatewayService gateway) => _gateway = gateway;

    public class OrderRequest
    {
        [Required(ErrorMessage = "amount is required")]
        [Range(1.0, 10_000_000.0, ErrorMessage = "amount must be at least 1.00")]
        public decimal? Amount { get; set; }

        public int? TourId { get; set; }
    }

    [AllowAnonymous]
    [HttpGet("config")]
    public IActionResult Config() => Ok(new { provider = _gateway.Provider });

    [HttpPost("order")]
    public async Task<ActionResult<OrderResult>> CreateOrder([FromBody] OrderRequest request)
    {
        var receipt = $"etour_{User.Username()}_{DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()}";
        if (receipt.Length > 40) receipt = receipt[..40];

        return Ok(await _gateway.CreateOrderAsync(request.Amount!.Value, receipt));
    }
}
