using System.Security.Claims;
using ETour.Api.Common;

namespace ETour.Api.Security;

public static class CurrentUser
{
    public static string Username(this ClaimsPrincipal principal) =>
        principal?.FindFirst("sub")?.Value
        ?? principal?.FindFirst(ClaimTypes.NameIdentifier)?.Value
        ?? principal?.Identity?.Name;

    public static bool IsAdmin(this ClaimsPrincipal principal) =>
        principal?.IsInRole(RoleName.Admin) == true;
}
