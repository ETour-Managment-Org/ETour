using ETour.Api.Common;
using ETour.Api.Options;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;

namespace ETour.Api.Controllers;

[ApiController]
[Authorize(Roles = RoleName.Admin)]
[Route("api/admin/uploads")]
public class UploadController : ControllerBase
{
    private static readonly string[] Allowed = { "png", "jpg", "jpeg", "webp", "gif" };
    private const long MaxBytes = 5L * 1024 * 1024;

    private readonly string _configuredDir;
    private readonly ILogger<UploadController> _log;

    public UploadController(IOptions<SiteOptions> site, ILogger<UploadController> log)
    {
        _configuredDir = site.Value.ImagesDir;
        _log = log;
    }

    [HttpPost("image")]
    [RequestSizeLimit(MaxBytes)]
    public async Task<ActionResult<Dictionary<string, string>>> UploadImage(IFormFile file)
    {
        if (file is null || file.Length == 0)
            throw new ArgumentException("No file was uploaded");

        if (file.Length > MaxBytes)
            throw new ArgumentException("Image must be 5 MB or smaller");

        var original = file.FileName ?? "";
        var extension = Extension(original);

        if (!Allowed.Contains(extension))
            throw new ArgumentException(
                $"Only {string.Join(", ", Allowed)} images are allowed");

        var fileName = $"{Guid.NewGuid():N}.{extension}";
        var folder = ResolveToursFolder();
        Directory.CreateDirectory(folder);

        var target = Path.Combine(folder, fileName);
        await using (var stream = System.IO.File.Create(target))
        {
            await file.CopyToAsync(stream);
        }

        _log.LogInformation("Uploaded {Original} as {Stored}", original, fileName);

        return Ok(new Dictionary<string, string>
        {
            ["url"] = $"/images/tours/{fileName}",
            ["fileName"] = fileName,
            ["originalName"] = original
        });
    }

    private static string Extension(string name)
    {
        var dot = name.LastIndexOf('.');
        if (dot < 0 || dot == name.Length - 1) return "";
        return name[(dot + 1)..].ToLowerInvariant();
    }

    private string ResolveToursFolder()
    {
        var candidate = _configuredDir;
        return Path.IsPathRooted(candidate)
            ? Path.GetFullPath(Path.Combine(candidate, "tours"))
            : Path.GetFullPath(Path.Combine(Directory.GetCurrentDirectory(), candidate, "tours"));
    }
}
