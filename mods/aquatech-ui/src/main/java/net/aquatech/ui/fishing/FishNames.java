package net.aquatech.ui.fishing;

import java.util.regex.Pattern;

/**
 * Starcatcher оборачивает имя любой рыбы с весом в разметку редкости вида {@code <sccommon>Окунь</sccommon>}; на клиенте
 * её превращает в цвет сам мод, а в серверных сообщениях она остаётся текстом. Эта очистка убирает теги.
 */
public final class FishNames {

    private static final Pattern MARKUP = Pattern.compile("</?[A-Za-z][A-Za-z0-9_]*>");

    private FishNames() {
    }

    public static String strip(String name) {
        return name == null ? "" : MARKUP.matcher(name).replaceAll("").trim();
    }
}
