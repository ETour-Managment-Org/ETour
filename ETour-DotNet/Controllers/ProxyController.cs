using ETour.Api.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace ETour.Api.Controllers;

[ApiController]
[Route("api/proxy")]
public class ProxyController : ControllerBase
{
    private readonly JavaMicroserviceClient _java;

    public ProxyController(JavaMicroserviceClient java) => _java = java;

    [AllowAnonymous]
    [HttpGet("health")]
    public async Task<IActionResult> Health(CancellationToken ct)
    {
        var up = await _java.IsReachableAsync(ct);
        return Ok(new { javaServiceReachable = up });
    }

    [Authorize(Roles = Common.RoleName.Admin)]
    [HttpGet("forward")]
    public async Task<IActionResult> ForwardRequest(
        [FromQuery] string path = "/api/tours", CancellationToken ct = default)
    {
        var data = await _java.GetDataAsync(path, ct);
        return Content(data, "application/json");
    }

    [Authorize(Roles = Common.RoleName.Admin)]
    [HttpGet("compare/tours")]
    public async Task<IActionResult> CompareTours(
        [FromServices] ITourService dotnet, CancellationToken ct)
    {
        var javaTours = await _java.GetToursAsync(ct);
        var dotnetTours = await dotnet.GetAllToursAsync();

        return Ok(new
        {
            java = javaTours.Count,
            dotnet = dotnetTours.Count,
            match = javaTours.Count == dotnetTours.Count
        });
    }
}
