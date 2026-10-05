// AquaTech: обучение первых 10 минут. Панель слева (рисует мод aqualumen, OnboardingHud) ведёт новичка
// по 6 шагам: остров (/is), набор, 3 рыбы, подарочный кейс, продажа улова, руда. Награды за шаги 3, 5 и 6. У клиентов без свежего мода подсказка идёт в action bar. Включается только на самом первом входе
// (41_first_join_hint.js ставит aquatech_onboard = 1), старые игроки его не видят.
// Шаг хранится в persistentData.aquatech_onboard: 1..6 — текущий шаг, 99 — пройдено.
// Номера шагов сдвинуты на 1 (новый шаг 1 — /is): aquatech_onboard_v = 2 ставится новичку при входе,
// у тех, кто застрял на старой нумерации (v не стоит), шаг переносится один раз в ServerEvents.tick.

const ONBOARD_KEY = 'aquatech_onboard'
const ONBOARD_BASE = 'aquatech_onboard_base'
const ONBOARD_DONE = 99
const ONBOARD_VERSION_KEY = 'aquatech_onboard_v'
const ONBOARD_VERSION = 2
const ONBOARD_IS_USED = 'aquatech_is_used'
const ONBOARD_FISH_STEP = 3
const ONBOARD_FISH_GOAL = 3
const ONBOARD_ORES = ['minecraft:copper_ore', 'industrialupgrade:classicore/tin', 'minecraft:iron_ore', 'minecraft:coal_ore']

// /is ставит игроку метку (тег сущности) и флаг в Forge-данных. Тег виден отсюда напрямую. player.persistentData в
// KubeJS это отдельный компонент KubeJSPersistentData, Forge-флаг там не виден: тем, кто нажал /is до появления тега,
// флаг читаем из ForgeData в nbt игрока. После /onboarding reset старый флаг игнорируется, нужен новый /is.
const ONBOARD_IS_RESET = 'aquatech_is_reset'

function onboardIsUsed(player) {
  try {
    if (player.tags.contains(ONBOARD_IS_USED)) return true
    if (player.persistentData.getBoolean(ONBOARD_IS_RESET)) return false
    return !!player.nbt.getCompound('ForgeData').getBoolean(ONBOARD_IS_USED)
  } catch (err) {
    console.warn('[AquaTech] onboarding: cannot read ' + ONBOARD_IS_USED + ': ' + err)
    return false
  }
}

let OnboardingService = null
try {
  OnboardingService = Java.loadClass('store.aquateche.aqualumen.common.service.OnboardingService')
} catch (err) {
  console.warn('[AquaTech] onboarding: OnboardingService not available, action bar only: ' + err)
}

// true — клиент нарисовал панель, false — у игрока старый мод, нужен запасной вариант
function onboardHud(player, step, have, doneStep, reward) {
  if (OnboardingService == null) return false
  try {
    return !!OnboardingService.send(player, step, have, doneStep, reward || '')
  } catch (err) {
    return false
  }
}

let HubEconomy = null
try {
  HubEconomy = Java.loadClass('store.aquateche.aqualumen.common.service.HubEconomy')
} catch (err) {
  console.warn('[AquaTech] onboarding: HubEconomy not available, coins fall back to /eco: ' + err)
}

function onboardScore(server, name, objective) {
  try {
    const board = server.scoreboard
    const obj = board.getObjective(objective)
    if (obj == null || !board.hasPlayerScore(name, obj)) return 0
    return board.getOrCreatePlayerScore(name, obj).getScore()
  } catch (err) {
    return 0
  }
}

// Улов считаем тем же числом, что показывает F4: любая удочка StarCatcher (HubEconomy.fishCaught).
function onboardFish(player) {
  if (HubEconomy != null) {
    try {
      return Number(HubEconomy.fishCaught(player))
    } catch (err) {}
  }
  return onboardScore(player.server, player.username, 'aquatech_fish')
}

function onboardCoins(player) {
  if (HubEconomy != null) {
    try {
      return Number(HubEconomy.coins(player))
    } catch (err) {}
  }
  return onboardScore(player.server, player.username, 'coins')
}

function onboardGiveCoins(player, amount) {
  if (HubEconomy != null) {
    try {
      HubEconomy.grantCoins(player, amount)
      return
    } catch (err) {}
  }
  player.server.runCommandSilent(`eco give ${player.username} ${amount}`)
}

function onboardHasAny(player, ids) {
  for (const id of ids) {
    try {
      if (player.inventory.count(id) > 0) return true
    } catch (err) {}
  }
  return false
}

function onboardStarterKeys(player) {
  return player.persistentData.getCompound('aqualumen_case_keys').getInt('starter')
}

function onboardBoosterActive(player) {
  const tag = player.persistentData.getCompound('aqualumen_boosters')
  return tag.contains('until') && tag.getLong('until') > Date.now()
}

// Каждый шаг: подсказка для запасной строки, проверка выполнения, награда.
const ONBOARD_STEPS = {
  1: {
    hint: () => '§b1/6 §fНапиши в чате §e/is§f: появится твой личный остров',
    base: () => 0,
    done: (player) => onboardIsUsed(player),
    reward: () => ''
  },
  2: {
    hint: () => '§b2/6 §fЗабери стартовый набор: нажми §eF4§f → вкладка «Киты»',
    base: () => 0,
    done: (player) => onboardHasAny(player, ['starcatcher:bamboo_rod', 'starcatcher:tackle_box']),
    reward: () => ''
  },
  3: {
    hint: (player, base) => {
      const caught = Math.max(0, onboardFish(player) - base)
      return `§b3/6 §fВозьми удочку и поймай рыбу: §e${Math.min(caught, ONBOARD_FISH_GOAL)}/${ONBOARD_FISH_GOAL}`
    },
    base: (player) => onboardFish(player),
    done: (player, base) => onboardFish(player) - base >= ONBOARD_FISH_GOAL,
    reward: (player) => {
      onboardGiveCoins(player, 500)
      return '§6+500 монет'
    }
  },
  4: {
    hint: () => '§b4/6 §fНажми §eF4§f → «Кейсы» и открой подарочный кейс',
    base: () => 0,
    done: (player) => onboardStarterKeys(player) <= 0,
    reward: () => ''
  },
  5: {
    hint: () => '§b5/6 §fПродай улов: подойди к §eторговцу рыбой§f на спавне и нажми по нему §eПКМ§f',
    base: (player) => onboardCoins(player),
    done: (player, base) => onboardCoins(player) > base,
    reward: (player) => {
      player.server.runCommandSilent(`booster give ${player.username} small 1`)
      return '§6малый бустер скупщика §7(включить: /booster)'
    }
  },
  6: {
    hint: () => '§b6/6 §fПоймай руду: медь, олово, железо или уголь',
    base: () => 0,
    done: (player) => onboardHasAny(player, ONBOARD_ORES),
    reward: (player) => {
      onboardGiveCoins(player, 1500)
      return '§6+1500 монет'
    }
  }
}

function onboardAdvance(player, step) {
  const data = player.persistentData
  const reward = ONBOARD_STEPS[step].reward(player)
  const name = player.username
  player.server.runCommandSilent(`playsound minecraft:entity.player.levelup master ${name} ${player.x} ${player.y} ${player.z} 0.8 1.3`)

  const next = step + 1
  const finished = !ONBOARD_STEPS[next]
  // панель сама покажет плашку «Шаг N пройден» с наградой; в чат пишем только тем, у кого панели нет
  const shown = onboardHud(player, next, 0, step, reward)
  if (!shown) player.tell(Text.of(`§a✔ Шаг ${step} пройден!` + (reward ? ` §fНаграда: ${reward}` : '')))

  if (!finished) {
    data.putInt(ONBOARD_KEY, next)
    data.putLong(ONBOARD_BASE, ONBOARD_STEPS[next].base(player))
    return
  }
  data.putInt(ONBOARD_KEY, ONBOARD_DONE)
  data.remove(ONBOARD_BASE)
  player.server.runCommandSilent(`title ${name} times 10 70 20`)
  player.server.runCommandSilent(`title ${name} subtitle {"text":"Дальше путь в квестах и в меню F4","color":"aqua"}`)
  player.server.runCommandSilent(`title ${name} title {"text":"Старт пройден!","color":"gold","bold":true}`)
  player.server.runCommandSilent(`playsound minecraft:ui.toast.challenge_complete master ${name} ${player.x} ${player.y} ${player.z} 1.0 1.0`)
  player.tell(Text.of('§b[AquaTech] §fТы освоился! Дальше: §eквесты§f (книга в инвентаре) ведут к плавильне, пару и электричеству. ' +
    'Новые удочки открывают новые руды. Турнир по рыбалке каждые выходные, новости на §eaquateche.store'))
}

ServerEvents.tick((event) => {
  const { server } = event
  if (server.tickCount % 20 !== 0) return
  server.players.forEach((player) => {
    const data = player.persistentData
    if (data.getInt(ONBOARD_VERSION_KEY) < ONBOARD_VERSION) {
      const old = data.getInt(ONBOARD_KEY)
      if (old >= 1 && old < ONBOARD_DONE) {
        data.putInt(ONBOARD_KEY, old + 1)
        data.remove(ONBOARD_BASE)
      }
      data.putInt(ONBOARD_VERSION_KEY, ONBOARD_VERSION)
    }
    const step = data.getInt(ONBOARD_KEY)
    const def = ONBOARD_STEPS[step]
    if (!def) return
    if (!data.contains(ONBOARD_BASE)) data.putLong(ONBOARD_BASE, def.base(player))
    const base = Number(data.getLong(ONBOARD_BASE))
    if (def.done(player, base)) {
      onboardAdvance(player, step)
      return
    }
    const have = step === ONBOARD_FISH_STEP ? Math.max(0, onboardFish(player) - base) : 0
    if (step === ONBOARD_FISH_STEP) {
      // диагностика: видно в логе KubeJS, считает ли улов (число должно расти при каждой рыбе)
      const seen = 'aquatech_onboard_seen'
      if (data.getInt(seen) !== have) {
        data.putInt(seen, have)
        console.log('[AquaTech] onboarding ' + player.username + ': fish ' + onboardFish(player) + ' (base ' + base + ', counted ' + have + ')')
      }
    }
    if (onboardHud(player, step, Math.min(have, ONBOARD_FISH_GOAL), 0, '')) return
    // запасной вариант без панели: action bar, бустер пишет в ту же строку — не перебиваем его
    if (!onboardBoosterActive(player)) {
      player.displayClientMessage(Text.of(def.hint(player, base)), true)
    }
  })
})

// /onboarding reset — для теста: начать обучение заново (оператор, на себе).
ServerEvents.commandRegistry((event) => {
  const { commands: Commands } = event
  event.register(Commands.literal('onboarding')
    .requires((src) => src.hasPermission(2))
    .then(Commands.literal('reset')
      .executes((ctx) => {
        const player = ctx.source.player
        if (!player) return 0
        player.persistentData.putInt(ONBOARD_KEY, 1)
        player.persistentData.putInt(ONBOARD_VERSION_KEY, ONBOARD_VERSION)
        player.persistentData.remove(ONBOARD_BASE)
        player.removeTag(ONBOARD_IS_USED)
        player.persistentData.putBoolean(ONBOARD_IS_RESET, true)
        player.tell(Text.of('§b[AquaTech] §fОбучение начато заново.'))
        return 1
      })))
})
