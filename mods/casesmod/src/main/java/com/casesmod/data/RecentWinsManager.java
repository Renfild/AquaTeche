package com.casesmod.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Хранит последние N открытий кейсов на сервере (в памяти, не переживает рестарт —
 * это просто "живая" лента для атмосферы, а не история для аудита).
 * Новые записи добавляются в начало, старые вытесняются с конца.
 */
public class RecentWinsManager {
    public static final RecentWinsManager INSTANCE = new RecentWinsManager();
    private static final int MAX_ENTRIES = 20;

    private final Deque<WinEntry> wins = new ArrayDeque<>();

    public record WinEntry(String playerName, String itemDisplayName, String rarity, long timestamp) {}

    public synchronized void addWin(String playerName, String itemDisplayName, String rarity) {
        wins.addFirst(new WinEntry(playerName, itemDisplayName, rarity, System.currentTimeMillis()));
        while (wins.size() > MAX_ENTRIES) wins.removeLast();
    }

    public synchronized List<WinEntry> getAll() {
        return new ArrayList<>(wins);
    }
}
