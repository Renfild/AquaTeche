-- Shorter player-facing catalog copy
UPDATE catalog_items SET description = 'Префикс, цветной ник, +1 дом. Купить на сайте пока нельзя.', perks_json = '["Префикс VIP в чате","+1 дом /sethome","Цветной ник","Приоритет в очереди"]' WHERE slug = 'vip';
UPDATE catalog_items SET description = 'Всё из VIP, кейс в день на сервере, приоритет входа.', perks_json = '["Всё из VIP","Кейс в день (в игре)","Приоритет входа","Доп. слот варпа"]' WHERE slug = 'premium';
UPDATE catalog_items SET description = 'Бонус к улову и рамка профиля. Оплата на сайте выключена.', perks_json = '["Всё из Premium","Рамка профиля","Бонус к улову","Бейдж Deluxe"]' WHERE slug = 'deluxe';
UPDATE catalog_items SET description = 'Максимум привилегий на сервере. Оплата на сайте позже.', perks_json = '["Всё из Deluxe","Бейдж Ultimate","Максимум домов","Приоритет в поддержке"]' WHERE slug = 'ultimate';
UPDATE catalog_items SET description = 'Монеты и расходники. Открывается в игре (F4).', perks_json = '["AquaCoins","Расходники","Мелкий буст"]' WHERE slug = 'ocean';
UPDATE catalog_items SET description = 'Лут под StarCatcher. Рулетки на сайте нет.', perks_json = '["Ресурсы улова","Буст удочки","Монеты"]' WHERE slug = 'fisher';
UPDATE catalog_items SET description = 'Редкая косметика и пробные привилегии. Только сервер.', perks_json = '["Рамка профиля","Пробная привилегия","Крупный запас монет"]' WHERE slug = 'depth';
