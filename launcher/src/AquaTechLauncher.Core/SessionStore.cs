using System.Security.Cryptography;
using System.Text;

namespace AquaTechLauncher.Core;

/// <summary>Protect portal session at rest (DPAPI on Windows).</summary>
public static class SessionStore
{
    private const string Prefix = "dpapi:";

    public static string? Protect(string? plain)
    {
        if (string.IsNullOrWhiteSpace(plain))
            return null;
        if (!OperatingSystem.IsWindows())
            return plain.Trim();
        try
        {
            var raw = Encoding.UTF8.GetBytes(plain.Trim());
            var sealedBytes = ProtectedData.Protect(raw, optionalEntropy: null, DataProtectionScope.CurrentUser);
            return Prefix + Convert.ToBase64String(sealedBytes);
        }
        catch
        {
            return plain.Trim();
        }
    }

    public static string? Unprotect(string? stored)
    {
        if (string.IsNullOrWhiteSpace(stored))
            return null;
        var s = stored.Trim();
        if (!s.StartsWith(Prefix, StringComparison.Ordinal))
            return s;
        if (!OperatingSystem.IsWindows())
            return null;
        try
        {
            var sealedBytes = Convert.FromBase64String(s[Prefix.Length..]);
            var raw = ProtectedData.Unprotect(sealedBytes, optionalEntropy: null, DataProtectionScope.CurrentUser);
            return Encoding.UTF8.GetString(raw);
        }
        catch
        {
            return null;
        }
    }
}
