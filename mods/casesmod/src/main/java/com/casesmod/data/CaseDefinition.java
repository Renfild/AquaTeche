package com.casesmod.data;

import java.util.ArrayList;
import java.util.List;

/** Описание одного кейса — полностью задаётся JSON-файлом в config/casesmod/cases/*.json */
public class CaseDefinition {
    public String id = "example_case";
    public String displayName = "Пример кейса";
    /** Стоимость открытия в игровой валюте. 0 = открывается бесплатно. Физические ключи больше не используются. */
    public long price = 100;
    /** Иконка кейса в меню кейсов */
    public String iconItemId = "minecraft:chest";

    /**
     * Pity-система (гарантия редкого приза): после стольких открытий подряд без выпадения
     * приза редкости >= pityRarity, следующее открытие гарантированно даст такую редкость.
     * 0 = pity отключена (обычная случайность без гарантий).
     */
    public int pityThreshold = 0;
    public String pityRarity = "EPIC";

    /**
     * Сезонность/ограничение по времени: даты в формате "yyyy-MM-dd". Пустая строка = без ограничения
     * с этой стороны. Если сейчас вне окна [availableFrom, availableUntil] — кейс виден в меню, но
     * заблокирован для открытия (например, ивентовый кейс на Хэллоуин/Новый год).
     */
    public String availableFrom = "";
    public String availableUntil = "";

    public List<CaseItem> items = new ArrayList<>();

    public double totalWeight() {
        double sum = 0;
        for (CaseItem it : items) sum += it.weight;
        return sum;
    }
}
