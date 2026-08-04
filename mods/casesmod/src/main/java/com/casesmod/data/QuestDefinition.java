package com.casesmod.data;

public class QuestDefinition {
    public String id = "mine_stone";
    public String displayName = "§eДобытчик";
    public String description = "Добудьте 64 камня";
    /** Этап роадмапа, например "1 · Берег" */
    public String stage = "1 · Берег";
    /** Порядок сортировки этапов (1..5) */
    public int stageOrder = 1;
    public String iconItemId = "minecraft:stone_pickaxe";
    /** MINE_BLOCK, KILL_MOB, COLLECT_ITEM */
    public String type = "MINE_BLOCK";
    /** id блока/моба/предмета */
    public String target = "minecraft:stone";
    public int requiredAmount = 64;
    public String rewardItemId = "minecraft:diamond";
    public int rewardCount = 5;
    public String rewardCommand = "";
}
