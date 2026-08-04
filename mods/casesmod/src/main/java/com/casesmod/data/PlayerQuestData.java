package com.casesmod.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Данные одного игрока по квестам — для сохранения в config/casesmod/quest_progress.json */
public class PlayerQuestData {
    public Map<String, Integer> progress = new HashMap<>();
    public List<String> claimed = new ArrayList<>();
}
