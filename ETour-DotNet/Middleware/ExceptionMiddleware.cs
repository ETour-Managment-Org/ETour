using System.Net;
using System.Text.Json;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Middleware;

public class ExceptionMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionMiddleware> _log;

    private static readonly JsonSerializerOptions Json =
        new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };

    public ExceptionMiddleware(RequestDelegate next, ILogger<ExceptionMiddleware> log)
    {
        _next = next;
        _log = log;
    }

    public async Task InvokeAsync(HttpContext ctx)
    {
        try
        {
            await _next(ctx);
        }
        catch (Exception ex)
        {
            var (status, message) = Map(ex);

            if (status == HttpStatusCode.InternalServerError)
            {
                _log.LogError(ex, "Unhandled error on {Path}", ctx.Request.Path);
            }
            else
            {
                _log.LogWarning("{Status} on {Path}: {Message}",
                    (int)status, ctx.Request.Path, message);
            }

            ctx.Response.Clear();
            ctx.Response.StatusCode = (int)status;
            ctx.Response.ContentType = "application/json";

            var body = new ApiError
            {
                Timestamp = DateTime.Now,
                Status = (int)status,
                Error = ReasonPhrase(status),
                Message = message,
                Path = ctx.Request.Path
            };

            await ctx.Response.WriteAsync(JsonSerializer.Serialize(body, Json));
        }
    }

    private static (HttpStatusCode, string) Map(Exception ex)
    {
        if (ex is DbUpdateException dbEx)
        {
            var reason = dbEx.GetBaseException().Message;

            if (reason.Contains("foreign key constraint fails",
                                StringComparison.OrdinalIgnoreCase))
            {
                return (HttpStatusCode.Conflict,
                    "This record is still referenced by other data and cannot be removed. "
                  + "Remove the linked records first, or deactivate it instead. "
                  + "(" + reason + ")");
            }

            if (reason.Contains("Duplicate entry", StringComparison.OrdinalIgnoreCase))
            {
                return (HttpStatusCode.Conflict,
                    "That value already exists. Please use a different one.");
            }

            return (HttpStatusCode.Conflict, "The database rejected this change: " + reason);
        }

        return MapCore(ex);
    }

    private static (HttpStatusCode, string) MapCore(Exception ex) => ex switch
    {
        ResourceNotFoundException => (HttpStatusCode.NotFound, ex.Message),
        BadCredentialsException => (HttpStatusCode.Unauthorized, ex.Message),
        AccessDeniedException => (HttpStatusCode.Forbidden,
            "You do not have permission to perform this action"),
        ConflictException => (HttpStatusCode.Conflict, ex.Message),
        InvalidOperationException => (HttpStatusCode.Conflict, ex.Message),
        ArgumentException => (HttpStatusCode.BadRequest, ex.Message),
        _ => (HttpStatusCode.InternalServerError, $"Unexpected error: {ex.Message}")
    };

    private static string ReasonPhrase(HttpStatusCode status) => status switch
    {
        HttpStatusCode.BadRequest => "Bad Request",
        HttpStatusCode.Unauthorized => "Unauthorized",
        HttpStatusCode.Forbidden => "Forbidden",
        HttpStatusCode.NotFound => "Not Found",
        HttpStatusCode.Conflict => "Conflict",
        _ => "Internal Server Error"
    };
}
