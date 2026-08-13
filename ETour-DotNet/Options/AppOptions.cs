namespace ETour.Api.Options;

public class JwtOptions
{
    public const string Section = "Jwt";
    public string Secret { get; set; } =
        "ZS1Ub3VyLWxvY2FsLWRldmVsb3BtZW50LXNlY3JldC1rZXktY2hhbmdlLWJlZm9yZS1wcm9kdWN0aW9uLTEyMzQ1Njc4OTA=";
    public long ExpirationMs { get; set; } = 86400000;
}

public class PricingOptions
{
    public const string Section = "ETour:Pricing";
    public int ChildMaxAge { get; set; } = 12;
}

public class CancellationOptions
{
    public const string Section = "ETour:Cancellation";
    public int FullRefundDays { get; set; } = 7;
    public int PartialRefundDays { get; set; } = 3;
    public decimal FullRefundPercent { get; set; } = 100m;
    public decimal PartialRefundPercent { get; set; } = 50m;
    public decimal NoRefundPercent { get; set; } = 0m;
}

public class MailOptions
{
    public const string Section = "ETour:Mail";
    public bool Enabled { get; set; } = false;
    public string From { get; set; } = "noreply@etour.co.in";
    public string FromName { get; set; } = "e-Tour by IndiaTour";
    public string SupportEmail { get; set; } = "support@etour.co.in";
    public string SupportPhone { get; set; } = "+91 98200 11223";
    public string OverrideRecipient { get; set; } = "";

    public string Host { get; set; } = "smtp.gmail.com";
    public int Port { get; set; } = 587;
    public string Username { get; set; } = "";
    public string Password { get; set; } = "";

    public bool HasOverride() => !string.IsNullOrWhiteSpace(OverrideRecipient);
}

public class OAuthOptions
{
    public const string Section = "ETour:OAuth";
    public const string NotConfigured = "not-configured";

    public bool Enabled { get; set; } = false;
    public string RedirectUri { get; set; } = "http://localhost:5173/oauth/callback";
    public string GoogleClientId { get; set; } = NotConfigured;
    public string GoogleClientSecret { get; set; } = NotConfigured;

    public bool IsConfigured() =>
        Enabled
        && !string.IsNullOrWhiteSpace(GoogleClientId)
        && !NotConfigured.Equals(GoogleClientId.Trim(), StringComparison.Ordinal);
}

public class SiteOptions
{
    public const string Section = "ETour";
    public string SiteUrl { get; set; } = "http://localhost:5173";
    public string ImagesDir { get; set; } = "images";
    public string CompletionSweepTime { get; set; } = "07:00";
}
