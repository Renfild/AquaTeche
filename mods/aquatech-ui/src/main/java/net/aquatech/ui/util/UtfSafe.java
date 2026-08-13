package net.aquatech.ui.util;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Safe UTF writes for network packets — strips unpaired surrogates / NULs
 * that can trigger UTFDataFormatException on encode.
 */
public final class UtfSafe {
    private UtfSafe() {
    }

    public static String clean(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 0) continue;
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                    out.append(c).append(s.charAt(++i));
                }
                // drop lone high surrogate
                continue;
            }
            if (Character.isLowSurrogate(c)) continue;
            out.append(c);
        }
        return out.toString();
    }

    public static void write(FriendlyByteBuf buf, String s, int maxChars) {
        String cleaned = clean(s);
        if (cleaned.length() > maxChars) {
            cleaned = cleaned.substring(0, maxChars);
        }
        buf.writeUtf(cleaned, maxChars);
    }
}
