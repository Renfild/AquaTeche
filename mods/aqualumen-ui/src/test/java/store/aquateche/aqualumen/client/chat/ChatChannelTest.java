package store.aquateche.aqualumen.client.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatChannelTest {

    @Test
    void privateMessagesGoToPersonalNoMatterWhatTheyLookLike() {
        assertEquals("pm", ChatChannel.classify("Renfild шепчет вам: привет", false, true));
        assertEquals("pm", ChatChannel.classify("[Турнир] притворяюсь событием", true, true));
    }

    @Test
    void marketTagsGoToTrade() {
        assertEquals("trade", ChatChannel.classify("[Рынок] Экскаватор продан за 10000", true, false));
        assertEquals("trade", ChatChannel.classify("[Торговля] новый лот", true, false));
    }

    @Test
    void oceanEventTagsGoToEventsIncludingTheDeepChest() {
        for (String tag : new String[]{"Кот-рыболов", "Турнир", "Косяк", "Рыбак", "Атлас", "Контракт", "Улов",
                "Приманка", "Сокровище глубин", "Золотая рыба", "Золотая буря"}) {
            assertEquals("events", ChatChannel.classify("[" + tag + "] что-то случилось", true, false), tag);
        }
    }

    @Test
    void untaggedOrUnknownSystemLinesStayInTheGeneralChannel() {
        assertEquals("all", ChatChannel.classify("[AquaTech] добро пожаловать", true, false));
        assertEquals("all", ChatChannel.classify("Игрок зашёл в игру", true, false));
    }

    @Test
    void aPlayerTypingAnEventTagCannotSpoofTheEventsTab() {
        assertEquals("all", ChatChannel.classify("sol: [Турнир] я выиграл", false, false));
        assertEquals("all", ChatChannel.classify("[Турнир] я выиграл", false, false));
    }

    @Test
    void tagOfReadsTheLeadingBracketOnly() {
        assertEquals("Турнир", ChatChannel.tagOf("[Турнир] Итоги недели"));
        assertEquals("Сокровище глубин", ChatChannel.tagOf("[Сокровище глубин] Из глубин поднялся сундук"));
        assertEquals("", ChatChannel.tagOf("Турнир [не тег] текст"));
        assertEquals("", ChatChannel.tagOf("[" + "x".repeat(40) + "] слишком длинный"));
        assertEquals("", ChatChannel.tagOf(""));
    }

    @Test
    void stripCodesRemovesLegacyColourAndFormatCodes() {
        assertEquals("[Турнир] Итоги", ChatChannel.stripCodes("§6§l[Турнир] §eИтоги"));
    }
}
