using ETour.Api.Data;
using ETour.Api.Middleware;
using ETour.Api.Options;
using ETour.Api.Pricing;
using ETour.Api.Repository;
using ETour.Api.Security;
using ETour.Api.Service;
using ETour.Api.Validators;
using FluentValidation;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.Google;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.HttpOverrides;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.FileProviders;
using Microsoft.IdentityModel.Tokens;
using Serilog;
using System.Text.Json.Serialization;

Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .MinimumLevel.Override("Microsoft.AspNetCore", Serilog.Events.LogEventLevel.Warning)
    .Enrich.FromLogContext()
    .Enrich.WithProperty("Application", "ETour.Api")
    .WriteTo.Console(outputTemplate:
        "[{Timestamp:HH:mm:ss} {Level:u3}] {Message:lj}{NewLine}{Exception}")
    .WriteTo.File("logs/etour-.log",
        rollingInterval: RollingInterval.Day,
        retainedFileCountLimit: 14,
        outputTemplate:
        "{Timestamp:yyyy-MM-dd HH:mm:ss.fff zzz} [{Level:u3}] {SourceContext} {Message:lj}{NewLine}{Exception}")
    .CreateLogger();

try
{
    Log.Information("e-Tour API starting up");

    var builder = WebApplication.CreateBuilder(args);
    builder.Host.UseSerilog();

    builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection(JwtOptions.Section));
    builder.Services.Configure<PricingOptions>(builder.Configuration.GetSection(PricingOptions.Section));
    builder.Services.Configure<CancellationOptions>(builder.Configuration.GetSection(CancellationOptions.Section));
    builder.Services.Configure<MailOptions>(builder.Configuration.GetSection(MailOptions.Section));
    builder.Services.Configure<OAuthOptions>(builder.Configuration.GetSection(OAuthOptions.Section));
    builder.Services.Configure<SiteOptions>(builder.Configuration.GetSection(SiteOptions.Section));

    var oauth = builder.Configuration.GetSection(OAuthOptions.Section).Get<OAuthOptions>() ?? new();

    var connection = builder.Configuration.GetConnectionString("ETour");
    builder.Services.AddDbContext<ETourDbContext>(options =>
        options.UseMySql(connection, ServerVersion.AutoDetect(connection)));

    builder.Services.AddAutoMapper(cfg => cfg.AddMaps(typeof(Program).Assembly));

    builder.Services.AddScoped(typeof(IGenericRepository<>), typeof(GenericRepository<>));

    builder.Services.AddValidatorsFromAssemblyContaining<RegisterRequestValidator>();

    var javaBaseUrl = builder.Configuration["ETour:JavaService:BaseUrl"]
                      ?? "http://localhost:8081";

    builder.Services.AddHttpClient<JavaMicroserviceClient>(client =>
    {
        client.BaseAddress = new Uri(javaBaseUrl);
        client.Timeout = TimeSpan.FromSeconds(10);
        client.DefaultRequestHeaders.Add("Accept", "application/json");
    });

    builder.Services.AddSingleton<JwtTokenService>();
    builder.Services.AddSingleton<TourCostCalculator>();
    builder.Services.AddSingleton<FareBandPolicy>();
    builder.Services.AddSingleton<RefundPolicy>();
    builder.Services.AddSingleton<IEmailService, EmailService>();
    if (string.Equals(builder.Configuration["ETour:Payment:Provider"], "razorpay",
                      StringComparison.OrdinalIgnoreCase))
    {
        builder.Services.AddHttpClient<IPaymentGatewayService, RazorpayPaymentGatewayService>();
    }
    else
    {
        builder.Services.AddSingleton<IPaymentGatewayService, MockPaymentGatewayService>();
    }

    builder.Services.AddScoped<IReceiptService, ReceiptService>();
    builder.Services.AddScoped<IAuthService, AuthService>();
    builder.Services.AddScoped<ITourService, TourService>();
    builder.Services.AddScoped<ICategoryService, CategoryService>();
    builder.Services.AddScoped<IBookingService, BookingService>();
    builder.Services.AddScoped<ICancellationService, CancellationService>();
    builder.Services.AddScoped<IReviewService, ReviewService>();
    builder.Services.AddScoped<IFeedbackService, FeedbackService>();
    builder.Services.AddScoped<AdminCatalogueService>();
    builder.Services.AddScoped<IAdminUsersService, AdminUsersService>();

    builder.Services.AddHostedService<TourCompletionWorker>();

    var jwtOptions = builder.Configuration.GetSection(JwtOptions.Section).Get<JwtOptions>() ?? new();
    var signingKey = new SymmetricSecurityKey(Convert.FromBase64String(jwtOptions.Secret));

    var authBuilder = builder.Services
    .AddAuthentication(options =>
    {
        options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
        options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
    })
    .AddJwtBearer(options =>
    {
        options.MapInboundClaims = false;

        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = signingKey,
            NameClaimType = "sub",
            ClockSkew = TimeSpan.FromSeconds(30)
        };
    });

    if (oauth.IsConfigured())
    {
        authBuilder.AddCookie(CookieAuthenticationDefaults.AuthenticationScheme);
        authBuilder.AddGoogle(options =>
        {
            options.ClientId = oauth.GoogleClientId;
            options.ClientSecret = oauth.GoogleClientSecret;
            options.CallbackPath = "/signin-google";
            options.Scope.Add("email");
            options.Scope.Add("profile");

            options.SignInScheme = CookieAuthenticationDefaults.AuthenticationScheme;
        });
    }

    builder.Services.AddAuthorization();

    builder.Services
        .AddControllers(o => o.Filters.Add<ETour.Api.Middleware.ValidationFilter>())
        .AddJsonOptions(o => o.JsonSerializerOptions.DefaultIgnoreCondition =
            JsonIgnoreCondition.Never);

    builder.Services.Configure<ApiBehaviorOptions>(options =>
    {
        options.InvalidModelStateResponseFactory = context =>
        {
            var message = string.Join("; ", context.ModelState
                .Where(kv => kv.Value?.Errors.Count > 0)
                .Select(kv => $"{kv.Key}: {kv.Value.Errors.First().ErrorMessage}"));

            Log.Warning("Validation failed on {Path}: {Message}",
                context.HttpContext.Request.Path, message);

            return new BadRequestObjectResult(new ETour.Api.Exceptions.ApiError
            {
                Timestamp = DateTime.Now,
                Status = StatusCodes.Status400BadRequest,
                Error = "Bad Request",
                Message = string.IsNullOrWhiteSpace(message) ? "Validation failed" : message,
                Path = context.HttpContext.Request.Path
            });
        };
    });

    builder.Services.AddOpenApi();

    var app = builder.Build();

    app.UseMiddleware<ExceptionMiddleware>();

    var forwardedHeadersOptions = new ForwardedHeadersOptions
    {
        ForwardedHeaders = ForwardedHeaders.XForwardedFor |
                        ForwardedHeaders.XForwardedProto |
                        ForwardedHeaders.XForwardedHost
    };

    forwardedHeadersOptions.KnownNetworks.Clear();
    forwardedHeadersOptions.KnownProxies.Clear();

    app.UseForwardedHeaders(forwardedHeadersOptions);

    app.UseSerilogRequestLogging(options =>
    {
        options.MessageTemplate =
            "{RequestMethod} {RequestPath} responded {StatusCode} in {Elapsed:0.0} ms";
    });

    if (app.Environment.IsDevelopment())
    {
        app.MapOpenApi();
    }

    var imagesDir = builder.Configuration["ETour:ImagesDir"] ?? "images";
    var imagesPath = Path.IsPathRooted(imagesDir)
        ? imagesDir
        : Path.Combine(app.Environment.ContentRootPath, imagesDir);

    Directory.CreateDirectory(Path.Combine(imagesPath, "tours"));

    app.UseStaticFiles(new StaticFileOptions
    {
        FileProvider = new PhysicalFileProvider(imagesPath),
        RequestPath = "/images"
    });

    app.UseAuthentication();
    app.UseAuthorization();
    app.MapControllers();

    Log.Information("Images served from {Path}. Java service expected at {Java}",
        imagesPath, javaBaseUrl);

    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "e-Tour API failed to start");
    throw;
}
finally
{
    Log.CloseAndFlush();
}
