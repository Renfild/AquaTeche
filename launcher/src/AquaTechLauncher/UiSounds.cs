using System.Media;

namespace AquaTechLauncher;

/// <summary>Soft synthesized UI clicks (short sine blips, different pitch per action).</summary>
public static class UiSounds
{
    public enum Kind { Auth, Copy, Play }

    public static void Play(Kind kind)
    {
        if (!OperatingSystem.IsWindows()) return;
        try
        {
            var (freq, ms) = kind switch
            {
                Kind.Auth => (520.0, 70),
                Kind.Copy => (880.0, 40),
                _ => (660.0, 55),
            };
            var wav = BuildWav(freq, ms, amplitude: 0.09);
            using var msStream = new MemoryStream(wav);
            using var player = new SoundPlayer(msStream);
            player.Play();
        }
        catch
        {
            /* audio optional */
        }
    }

    private static byte[] BuildWav(double freqHz, int durationMs, double amplitude)
    {
        const int sampleRate = 22050;
        var samples = Math.Max(1, sampleRate * durationMs / 1000);
        var data = new byte[44 + samples * 2];
        void W32(int o, int v) { data[o] = (byte)v; data[o + 1] = (byte)(v >> 8); data[o + 2] = (byte)(v >> 16); data[o + 3] = (byte)(v >> 24); }
        void W16(int o, short v) { data[o] = (byte)v; data[o + 1] = (byte)(v >> 8); }

        data[0] = (byte)'R'; data[1] = (byte)'I'; data[2] = (byte)'F'; data[3] = (byte)'F';
        W32(4, 36 + samples * 2);
        data[8] = (byte)'W'; data[9] = (byte)'A'; data[10] = (byte)'V'; data[11] = (byte)'E';
        data[12] = (byte)'f'; data[13] = (byte)'m'; data[14] = (byte)'t'; data[15] = (byte)' ';
        W32(16, 16);
        W16(20, 1);
        W16(22, 1);
        W32(24, sampleRate);
        W32(28, sampleRate * 2);
        W16(32, 2);
        W16(34, 16);
        data[36] = (byte)'d'; data[37] = (byte)'a'; data[38] = (byte)'t'; data[39] = (byte)'a';
        W32(40, samples * 2);

        for (var i = 0; i < samples; i++)
        {
            var t = i / (double)sampleRate;
            var env = 1.0;
            var attack = (int)(sampleRate * 0.004);
            var release = (int)(sampleRate * 0.02);
            if (i < attack) env = i / (double)attack;
            else if (i > samples - release) env = (samples - i) / (double)release;
            var sample = Math.Sin(2 * Math.PI * freqHz * t) * amplitude * env;
            var s = (short)Math.Clamp((int)(sample * short.MaxValue), short.MinValue, short.MaxValue);
            W16(44 + i * 2, s);
        }
        return data;
    }
}
