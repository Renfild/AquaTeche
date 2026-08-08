-- AquaTech portal schema (Cloudflare D1 / SQLite)

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nick TEXT NOT NULL COLLATE NOCASE UNIQUE,
  password_hash TEXT NOT NULL,
  password_salt TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE IF NOT EXISTS profiles (
  user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  bio TEXT NOT NULL DEFAULT 'Исследователь глубин AquaTech.',
  theme TEXT NOT NULL DEFAULT 'ocean',
  privilege TEXT NOT NULL DEFAULT 'Игрок',
  coins INTEGER NOT NULL DEFAULT 0,
  likes INTEGER NOT NULL DEFAULT 0,
  fish INTEGER NOT NULL DEFAULT 0,
  playtime_hours INTEGER NOT NULL DEFAULT 0,
  views INTEGER NOT NULL DEFAULT 0,
  badges_json TEXT NOT NULL DEFAULT '[]',
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE IF NOT EXISTS sessions (
  id TEXT PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_user ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires ON sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_profiles_likes ON profiles(likes DESC);
CREATE INDEX IF NOT EXISTS idx_profiles_fish ON profiles(fish DESC);
CREATE INDEX IF NOT EXISTS idx_profiles_coins ON profiles(coins DESC);

CREATE TABLE IF NOT EXISTS catalog_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  kind TEXT NOT NULL CHECK (kind IN ('store', 'case')),
  slug TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  price_rub INTEGER NOT NULL DEFAULT 0,
  description TEXT NOT NULL,
  perks_json TEXT NOT NULL DEFAULT '[]',
  enabled INTEGER NOT NULL DEFAULT 1,
  sort_order INTEGER NOT NULL DEFAULT 0
);

INSERT OR IGNORE INTO catalog_items (kind, slug, title, price_rub, description, perks_json, enabled, sort_order) VALUES
(
  'store', 'vip', 'VIP', 149,
  'Базовая поддержка сервера. Префикс и удобства на AquaTech; покупка на сайте временно закрыта — привилегии выдаются админами после оплаты вне сайта.',
  '["Префикс VIP в чате","+1 дом /sethome","Цветной ник","Приоритет в очереди входа"]',
  1, 10
),
(
  'store', 'premium', 'Premium', 299,
  'Расширенный ранг для регулярных игроков: всё из VIP плюс ежедневный кейс на сервере и приоритет входа. Оплата на сайте пока недоступна.',
  '["Всё из VIP","Кейс в день (на сервере)","Приоритет входа","Доп. слот варпа"]',
  1, 20
),
(
  'store', 'deluxe', 'Deluxe', 599,
  'Продвинутый донат: бонусы к улову, рамка профиля на сайте и визуал в Tab. Покупка через сайт отключена до подключения оплаты.',
  '["Всё из Premium","Рамка профиля","Бонус к улову рыбалки","Уникальный бейдж Deluxe"]',
  1, 30
),
(
  'store', 'ultimate', 'Ultimate', 1199,
  'Максимальный ранг поддержки проекта. Полный набор привилегий и статус на сервере. Онлайн-оплата на сайте будет позже; сейчас — только витрина.',
  '["Всё из Deluxe","Бейдж Ultimate","Максимум домов и варпов","Приоритетная поддержка"]',
  1, 40
),
(
  'case', 'ocean', 'Океанский кейс', 0,
  'Базовый кейс AquaTech с монетами и расходниками. Открытие на сайте отключено — кейсы крутятся в игре через casesmod / F4.',
  '["AquaCoins","Расходники для старта","Шанс на мелкий буст"]',
  1, 10
),
(
  'case', 'fisher', 'Кейс рыбака', 0,
  'Награды под рыбалку StarCatcher: ресурсы и шанс на временный буст удочки. Сайтовая рулетка выключена.',
  '["Ресурсы улова","Шанс на буст удочки","Монеты"]',
  1, 20
),
(
  'case', 'depth', 'Глубинный кейс', 0,
  'Редкий кейс с косметикой профиля и пробными привилегиями. Открывается только на сервере; сайт показывает состав витрины.',
  '["Редкая рамка профиля","Пробная привилегия","Крупный запас монет"]',
  1, 30
);
