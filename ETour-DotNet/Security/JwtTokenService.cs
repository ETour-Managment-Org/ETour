using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using ETour.Api.Options;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace ETour.Api.Security;

public class JwtTokenService
{
    private readonly JwtOptions _o;

    public JwtTokenService(IOptions<JwtOptions> options) => _o = options.Value;

    public SymmetricSecurityKey Key() =>
        new(Convert.FromBase64String(_o.Secret));

    public long ExpirationMs => _o.ExpirationMs;

    public string GenerateToken(string username, string roleName)
    {
        var credentials = new SigningCredentials(Key(), SecurityAlgorithms.HmacSha512);

        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Sub, username),
            new(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

        if (!string.IsNullOrWhiteSpace(roleName))
        {
            claims.Add(new Claim(ClaimTypes.Role, roleName));
        }

        var token = new JwtSecurityToken(
            claims: claims,
            notBefore: DateTime.UtcNow,
            expires: DateTime.UtcNow.AddMilliseconds(_o.ExpirationMs),
            signingCredentials: credentials);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
