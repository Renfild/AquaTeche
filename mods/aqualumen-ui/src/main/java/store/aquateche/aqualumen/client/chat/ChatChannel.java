package store.aquateche.aqualumen.client.chat;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Раскладка входящих строк по вкладкам веб-чата: общий, торговля, события, личные. */
final class ChatChannel {

    private static final Pattern TAG = Pattern.compile("^\\s*\\[([^\\]]{1,30})\\]");
    private static final Pattern CODES = Pattern.compile("§.");
    private static final Set<String> TRADE_TAGS = Set.of("Рынок", "Торговля");
    private static final Set<String> EVENT_TAGS = Set.of("Кот-рыболов", "Турнир", "Косяк", "Рыбак", "Атлас", "Контракт",
            "Улов", "Приманка", "Сокровище глубин", "Золотая рыба", "Золотая буря");

    private ChatChannel() {
    }

    static String stripCodes(String legacy) {
        return CODES.matcher(legacy).replaceAll("");
    }

    /** Ведущий тег в квадратных скобках или пустая строка. */
    static String tagOf(String plain) {
        Matcher m = TAG.matcher(plain);
        return m.find() ? m.group(1) : "";
    }

    /**
     * Личные сообщения всегда в «личные». Теги событий и торговли действуют только для системных строк,
     * чтобы игрок не мог подделать вкладку «События», набрав «[Турнир]» в обычном чате.
     */
    static String classify(String plain, boolean system, boolean privateMessage) {
        if (privateMessage) return "pm";
        if (!system) return "all";
        String tag = tagOf(plain);
        if (TRADE_TAGS.contains(tag)) return "trade";
        if (EVENT_TAGS.contains(tag)) return "events";
        return "all";
    }
}
