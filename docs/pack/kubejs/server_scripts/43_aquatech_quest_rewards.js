// AquaTech: команды для наград FTB Quests (глава «Начало» и «Экономика»).
// Квест выдаёт награду типом «command»: /quest_reward coins {p} 500 или /quest_reward key {p} starter 1.
// Монеты идут через HubEconomy (кошелёк F4), ключи кладутся в тот же NBT, что читает хаб (aqualumen_case_keys).

let QuestHubEconomy = null
try {
  QuestHubEconomy = Java.loadClass('store.aquateche.aqualumen.common.service.HubEconomy')
} catch (err) {
  console.warn('[AquaTech] quest rewards: HubEconomy not available, coins fall back to /eco: ' + err)
}

const QUEST_KEYS_TAG = 'aqualumen_case_keys'

function questGiveCoins(player, amount) {
  if (QuestHubEconomy != null) {
    try {
      QuestHubEconomy.grantCoins(player, amount)
      return
    } catch (err) {
      console.warn('[AquaTech] quest rewards: grantCoins failed, using /eco: ' + err)
    }
  }
  player.server.runCommandSilent(`eco give ${player.username} ${amount}`)
}

function questGiveKey(player, caseId, amount) {
  const keys = player.persistentData.getCompound(QUEST_KEYS_TAG)
  keys.putInt(caseId, keys.getInt(caseId) + amount)
  player.persistentData.put(QUEST_KEYS_TAG, keys)
}

ServerEvents.commandRegistry((event) => {
  const { commands: Commands, arguments: Arguments } = event
  event.register(Commands.literal('quest_reward')
    .requires((src) => src.hasPermission(2))
    .then(Commands.literal('coins')
      .then(Commands.argument('player', Arguments.PLAYER.create(event))
        .then(Commands.argument('amount', Arguments.INTEGER.create(event))
          .executes((ctx) => {
            const player = Arguments.PLAYER.getResult(ctx, 'player')
            const amount = Arguments.INTEGER.getResult(ctx, 'amount')
            if (!player || amount <= 0) return 0
            questGiveCoins(player, amount)
            player.tell(Text.of('§6[AquaTech] §fНаграда: §e' + amount + ' монет'))
            return 1
          }))))
    .then(Commands.literal('key')
      .then(Commands.argument('player', Arguments.PLAYER.create(event))
        .then(Commands.argument('case', Arguments.STRING.create(event))
          .then(Commands.argument('amount', Arguments.INTEGER.create(event))
            .executes((ctx) => {
              const player = Arguments.PLAYER.getResult(ctx, 'player')
              const caseId = Arguments.STRING.getResult(ctx, 'case')
              const amount = Arguments.INTEGER.getResult(ctx, 'amount')
              if (!player || amount <= 0) return 0
              questGiveKey(player, caseId, amount)
              player.tell(Text.of('§6[AquaTech] §fНаграда: §eключ кейса §7(F4 → Кейсы)'))
              return 1
            }))))))
})
