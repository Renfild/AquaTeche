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
  'Префикс, цветной ник, +1 дом. Купить на сайте пока нельзя.',
  '["Префикс VIP в чате","+1 дом /sethome","Цветной ник","Приоритет в очереди"]',
  1, 10
),
(
  'store', 'premium', 'Premium', 299,
  'Всё из VIP, кейс в день на сервере, приоритет входа.',
  '["Всё из VIP","Кейс в день (в игре)","Приоритет входа","Доп. слот варпа"]',
  1, 20
),
(
  'store', 'deluxe', 'Deluxe', 599,
  'Бонус к улову и рамка профиля. Оплата на сайте выключена.',
  '["Всё из Premium","Рамка профиля","Бонус к улову","Бейдж Deluxe"]',
  1, 30
),
(
  'store', 'ultimate', 'Ultimate', 1199,
  'Максимум привилегий на сервере. Оплата на сайте позже.',
  '["Всё из Deluxe","Бейдж Ultimate","Максимум домов","Приоритет в поддержке"]',
  1, 40
),
(
  'case', 'ocean', 'Океанский кейс', 0,
  'Монеты и расходники. Открывается в игре (F4).',
  '["AquaCoins","Расходники","Мелкий буст"]',
  1, 10
),
(
  'case', 'fisher', 'Кейс рыбака', 0,
  'Лут под StarCatcher. Рулетки на сайте нет.',
  '["Ресурсы улова","Буст удочки","Монеты"]',
  1, 20
),
(
  'case', 'depth', 'Глубинный кейс', 0,
  'Редкая косметика и пробные привилегии. Только сервер.',
  '["Рамка профиля","Пробная привилегия","Крупный запас монет"]',
  1, 30
);
