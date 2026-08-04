package com.casesmod.data;

import java.util.HashMap;
import java.util.Map;

/** Баланс валюты + количество купленных/выданных кейсов (ключи) у игрока. */
public class PlayerAccount {
    public long balance = 0;
    /** caseId -> сколько непройденных кейсов у игрока */
    public Map<String, Integer> cases = new HashMap<>();

    public int getCaseCount(String caseId) {
        return cases.getOrDefault(caseId, 0);
    }

    public void addCases(String caseId, int amount) {
        if (amount == 0) return;
        int next = Math.max(0, getCaseCount(caseId) + amount);
        if (next == 0) cases.remove(caseId);
        else cases.put(caseId, next);
    }

    public boolean consumeCase(String caseId) {
        int n = getCaseCount(caseId);
        if (n <= 0) return false;
        addCases(caseId, -1);
        return true;
    }
}
