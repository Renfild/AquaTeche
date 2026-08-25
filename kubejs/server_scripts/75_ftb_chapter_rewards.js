// priority: 75
// ============================================================================
// AquaTech — FTB Quests Chapter Completion & Era Title Rewards
// Автоматическая выдача кастомных титулов, монет, ключей и трофеев за эры.
// ============================================================================

const CHAPTER_REWARDS = {
    // 1. Первобытная / Вводная эра
    "primitive": {
        title: "АКВАНАВТ",
        color: "§b",
        coins: 500,
        keys: 1,
        keyName: "Ключ от Океанического Кейса",
        trophyItem: "minecraft:heart_of_the_sea",
        trophyName: "§b✦ Трофей: Покоритель Глубин ✦",
        desc: "Завершено Океаническое Пробуждение!"
    },
    // 2. Паровая эпоха
    "steam": {
        title: "ГИДРОТЕХ",
        color: "§6",
        coins: 1000,
        keys: 2,
        keyName: "Ключ от Инженерного Кейса",
        trophyItem: "industrialupgrade:basemachine3/steamboiler",
        trophyName: "§6⚙ Трофей: Паровой Мастер ⚙",
        desc: "Полностью освоена Паровая Эпоха!"
    },
    // 3. Базовая электрическая эра
    "basic_electric": {
        title: "ИНЖЕНЕР",
        color: "§9",
        coins: 2500,
        keys: 3,
        keyName: "Ключ от Технологического Кейса",
        trophyItem: "industrialupgrade:basemachine2/compressor",
        trophyName: "§9⚡ Трофей: Энергетический Инженер ⚡",
        desc: "Полностью освоена Базовая Электрическая Эра!"
    },
    // 4. Улучшенная электрическая эра
    "improved_electric": {
        title: "КИБЕР-ГИДРО",
        color: "§d",
        coins: 5000,
        keys: 5,
        keyName: "Ключ от Квантового Кейса",
        trophyItem: "industrialupgrade:quantum_solar_panel",
        trophyName: "§d✧ Трофей: Кибернетический Архитектор ✧",
        desc: "Полностью освоена Улучшенная Электрическая Эра!"
    }
};

/**
 * Выдает игроку награду за эру: титул, монеты, ключи, трофей и оповещение на сервер.
 */
function grantEraReward(player, eraKey) {
    if (!player || !player.server) return;
    let reward = CHAPTER_REWARDS[eraKey];
    if (!reward) return;

    let pData = player.persistentData;
    if (!pData.eraRewards) pData.eraRewards = {};
    if (pData.eraRewards[eraKey]) {
        return; // Уже получено
    }
    pData.eraRewards[eraKey] = true;

    let nick = player.username;
    let server = player.server;

    // 1. Выдача монет через scoreboard
    try {
        server.runCommandSilent(`scoreboard players add ${nick} coins ${reward.coins}`);
    } catch (e) {}

    // 2. Выдача титула в LuckPerms / префикса
    try {
        server.runCommandSilent(`lp user ${nick} meta setprefix 100 "${reward.color}[${reward.title}] "`);
    } catch (e) {}

    // 3. Выдача физических ключей от кейсов
    for (let i = 0; i < reward.keys; i++) {
        let keyItem = Item.of("minecraft:tripwire_hook", {
            display: {
                Name: `{"text":"${reward.keyName}","color":"gold","bold":true}`,
                Lore: [
                    `{"text":"Используется для открытия кейсов в Меню (F4)","color":"gray"}`
                ]
            },
            AquaCaseKey: eraKey
        });
        player.give(keyItem);
    }

    // 4. Выдача памятного трофея
    let trophy = Item.of(reward.trophyItem, {
        display: {
            Name: `{"text":"${reward.trophyName}","bold":true}`,
            Lore: [
                `{"text":"Награда за прохождение эры","color":"yellow"}`,
                `{"text":"Владелец: ${nick}","color":"aqua"}`
            ]
        },
        Enchantments: [{ id: "minecraft:unbreaking", lvl: 1 }]
    });
    player.give(trophy);

    // 5. Звук и праздничное оповещение на весь сервер
    server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete master @a ${player.x} ${player.y} ${player.z} 1.0 1.0`);
    server.runCommandSilent(`tellraw @a ["",{"text":"[AQUATECH] ","color":"aqua","bold":true},{"text":"Игрок ","color":"white"},{"text":"${nick}","color":"yellow","bold":true},{"text":" завершил эру и получил титул ","color":"white"},{"text":"[${reward.title}]","color":"${reward.color.replace('§','') || 'gold'}","bold":true},{"text":"!","color":"white"}]`);

    player.tell(`§a§l✔ Поздравляем! §fВам начислен титул ${reward.color}[${reward.title}]§f, §e${reward.coins} монет§f и §6${reward.keys}x ${reward.keyName}§f!`);
}

// Слушатель событий кастомных наград FTB Quests
FTBQuestsEvents.customReward(event => {
    let id = event.reward.id;
    let player = event.player;
    if (!player) return;

    if (id.includes("primitive_era_reward") || id.includes("era_primitive")) {
        grantEraReward(player, "primitive");
    } else if (id.includes("steam_era_reward") || id.includes("era_steam")) {
        grantEraReward(player, "steam");
    } else if (id.includes("basic_electric_reward") || id.includes("era_basic_electric")) {
        grantEraReward(player, "basic_electric");
    } else if (id.includes("improved_electric_reward") || id.includes("era_improved_electric")) {
        grantEraReward(player, "improved_electric");
    }
});

// Слушатель завершения квестов для проверки ключевых катализаторов эпох
FTBQuestsEvents.completed(event => {
    let player = event.player;
    if (!player) return;
    let quest = event.object;
    if (!quest) return;

    let chapter = quest.chapter;
    if (!chapter) return;

    let filename = chapter.filename;
    if (!filename) return;

    // Если закрыта паровая эпоха
    if (filename === "steam_era" && chapter.isComplete(event.data)) {
        grantEraReward(player, "steam");
    } else if (filename === "basic_electric_era" && chapter.isComplete(event.data)) {
        grantEraReward(player, "basic_electric");
    } else if (filename === "improved_electric_era" && chapter.isComplete(event.data)) {
        grantEraReward(player, "improved_electric");
    } else if (filename === "1" && chapter.isComplete(event.data)) {
        grantEraReward(player, "primitive");
    }
});

// Дополнительная консольная/админская команда для ручной проверки или выдачи: /era_reward <ник> <primitive|steam|basic_electric|improved_electric>
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event;
    event.register(
        Commands.literal('era_reward')
            .requires(s => s.hasPermission(2))
            .then(Commands.argument('player', Arguments.PLAYER.create(event))
                .then(Commands.argument('era', Arguments.STRING.create(event))
                    .executes(ctx => {
                        let player = Arguments.PLAYER.getResult(ctx, 'player');
                        let era = Arguments.STRING.getResult(ctx, 'era');
                        grantEraReward(player, era);
                        return 1;
                    })
                )
            )
    );
});
