using System.Text;

namespace AquaTechLauncher.Core;

public static class ServerListHelper
{
    public static void EnsureServerEntry(string gameDir, string serverAddress, string serverName = "AquaTech")
    {
        if (string.IsNullOrWhiteSpace(gameDir) || string.IsNullOrWhiteSpace(serverAddress))
            return;

        try
        {
            var serversDatPath = Path.Combine(gameDir, "servers.dat");
            if (!File.Exists(serversDatPath))
            {
                WriteSingleServerNbt(serversDatPath, serverName, serverAddress.Trim());
                return;
            }

            var existingBytes = File.ReadAllBytes(serversDatPath);
            var addressUtf8 = Encoding.UTF8.GetBytes(serverAddress.Trim());
            if (ContainsSequence(existingBytes, addressUtf8))
            {
                // Сервер уже присутствует в servers.dat
                return;
            }

            // Если servers.dat пустой или поврежден
            if (existingBytes.Length < 10)
            {
                WriteSingleServerNbt(serversDatPath, serverName, serverAddress.Trim());
                return;
            }

            // Если файл существует и валиден, но не содержит наш сервер, запишем или обновим
            WriteSingleServerNbt(serversDatPath, serverName, serverAddress.Trim());
        }
        catch
        {
            /* игнорируем любые ошибки доступа к файлу */
        }
    }

    public static byte[] BuildSingleServerNbtBytes(string serverName, string serverAddress)
    {
        using var ms = new MemoryStream();
        using var w = new BinaryWriter(ms);

        // Root TAG_Compound (ID = 10)
        w.Write((byte)10);
        WriteShortBigEndian(w, 0); // Root name is "" (length 0)

        // TAG_List "servers" (ID = 9)
        w.Write((byte)9);
        WriteStringUtf8(w, "servers");
        w.Write((byte)10); // List item type = TAG_Compound (10)
        WriteIntBigEndian(w, 1); // 1 server in list

        // --- Server Compound ---
        // TAG_String "name"
        w.Write((byte)8);
        WriteStringUtf8(w, "name");
        WriteStringUtf8(w, serverName);

        // TAG_String "ip"
        w.Write((byte)8);
        WriteStringUtf8(w, "ip");
        WriteStringUtf8(w, serverAddress);

        // TAG_Byte "acceptTextures" = 1
        w.Write((byte)1);
        WriteStringUtf8(w, "acceptTextures");
        w.Write((byte)1);

        // TAG_Byte "hidden" = 0
        w.Write((byte)1);
        WriteStringUtf8(w, "hidden");
        w.Write((byte)0);

        // End of Server Compound (TAG_End = 0)
        w.Write((byte)0);

        // End of Root Compound (TAG_End = 0)
        w.Write((byte)0);

        w.Flush();
        return ms.ToArray();
    }

    private static void WriteSingleServerNbt(string path, string serverName, string serverAddress)
    {
        var bytes = BuildSingleServerNbtBytes(serverName, serverAddress);
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        File.WriteAllBytes(path, bytes);
    }

    private static void WriteStringUtf8(BinaryWriter w, string text)
    {
        var bytes = Encoding.UTF8.GetBytes(text);
        WriteShortBigEndian(w, (short)bytes.Length);
        w.Write(bytes);
    }

    private static void WriteShortBigEndian(BinaryWriter w, short val)
    {
        var b = BitConverter.GetBytes(val);
        if (BitConverter.IsLittleEndian) Array.Reverse(b);
        w.Write(b);
    }

    private static void WriteIntBigEndian(BinaryWriter w, int val)
    {
        var b = BitConverter.GetBytes(val);
        if (BitConverter.IsLittleEndian) Array.Reverse(b);
        w.Write(b);
    }

    private static bool ContainsSequence(byte[] source, byte[] pattern)
    {
        if (pattern.Length == 0 || source.Length < pattern.Length) return false;
        for (var i = 0; i <= source.Length - pattern.Length; i++)
        {
            var match = true;
            for (var j = 0; j < pattern.Length; j++)
            {
                if (source[i + j] != pattern[j])
                {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }
}
