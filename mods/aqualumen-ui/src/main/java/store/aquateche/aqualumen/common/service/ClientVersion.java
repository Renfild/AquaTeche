package store.aquateche.aqualumen.common.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Compares the mod version a client reports in its hello packet. Pure, no Minecraft types. */
public final class ClientVersion {

    private static final Pattern NUMERIC = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)");

    private ClientVersion() {
    }

    /** True when {@code version} (like "0.3.72-alpha") is at least major.minor.patch; unknown versions are not. */
    public static boolean atLeast(String version, int major, int minor, int patch) {
        if (version == null) {
            return false;
        }
        Matcher m = NUMERIC.matcher(version.trim());
        if (!m.find()) {
            return false;
        }
        long have = Long.parseLong(m.group(1)) * 1_000_000L + Long.parseLong(m.group(2)) * 1_000L + Long.parseLong(m.group(3));
        long need = major * 1_000_000L + minor * 1_000L + patch;
        return have >= need;
    }
}
