import { onRequestPatch as __api_admin_catalog__id__js_onRequestPatch } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\catalog\\[id].js"
import { onRequestDelete as __api_admin_news__id__js_onRequestDelete } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\news\\[id].js"
import { onRequestPatch as __api_admin_news__id__js_onRequestPatch } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\news\\[id].js"
import { onRequestPatch as __api_admin_users__nick__js_onRequestPatch } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\users\\[nick].js"
import { onRequestPost as __api_profiles__nick__like_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\profiles\\[nick]\\like.js"
import { onRequestGet as __api_admin_catalog_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\catalog.js"
import { onRequestPost as __api_admin_catalog_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\catalog.js"
import { onRequestGet as __api_admin_me_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\me.js"
import { onRequestGet as __api_admin_news_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\news.js"
import { onRequestPost as __api_admin_news_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\news.js"
import { onRequestGet as __api_admin_settings_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\settings.js"
import { onRequestPatch as __api_admin_settings_js_onRequestPatch } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\settings.js"
import { onRequestGet as __api_admin_users_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\admin\\users.js"
import { onRequestPost as __api_launcher_ensure_nick_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\launcher\\ensure-nick.js"
import { onRequestGet as __api_launcher_session_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\launcher\\session.js"
import { onRequestPost as __api_launcher_session_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\launcher\\session.js"
import { onRequestPost as __api_launcher_verify_token_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\launcher\\verify-token.js"
import { onRequestPost as __api_sync_player_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\sync\\player.js"
import { onRequestGet as __api_profiles__nick__js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\profiles\\[nick].js"
import { onRequestPatch as __api_profiles__nick__js_onRequestPatch } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\profiles\\[nick].js"
import { onRequestGet as __api_catalog_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\catalog.js"
import { onRequestPost as __api_login_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\login.js"
import { onRequestPost as __api_logout_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\logout.js"
import { onRequestGet as __api_me_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\me.js"
import { onRequestGet as __api_news_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\news.js"
import { onRequestGet as __api_players_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\players.js"
import { onRequestGet as __api_purchase_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\purchase.js"
import { onRequestPost as __api_purchase_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\purchase.js"
import { onRequestPost as __api_register_js_onRequestPost } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\register.js"
import { onRequestGet as __api_server_status_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\server-status.js"
import { onRequestGet as __api_site_js_onRequestGet } from "C:\\Users\\xieto\\Desktop\\AquaTech\\functions\\api\\site.js"

export const routes = [
    {
      routePath: "/api/admin/catalog/:id",
      mountPath: "/api/admin/catalog",
      method: "PATCH",
      middlewares: [],
      modules: [__api_admin_catalog__id__js_onRequestPatch],
    },
  {
      routePath: "/api/admin/news/:id",
      mountPath: "/api/admin/news",
      method: "DELETE",
      middlewares: [],
      modules: [__api_admin_news__id__js_onRequestDelete],
    },
  {
      routePath: "/api/admin/news/:id",
      mountPath: "/api/admin/news",
      method: "PATCH",
      middlewares: [],
      modules: [__api_admin_news__id__js_onRequestPatch],
    },
  {
      routePath: "/api/admin/users/:nick",
      mountPath: "/api/admin/users",
      method: "PATCH",
      middlewares: [],
      modules: [__api_admin_users__nick__js_onRequestPatch],
    },
  {
      routePath: "/api/profiles/:nick/like",
      mountPath: "/api/profiles/:nick",
      method: "POST",
      middlewares: [],
      modules: [__api_profiles__nick__like_js_onRequestPost],
    },
  {
      routePath: "/api/admin/catalog",
      mountPath: "/api/admin",
      method: "GET",
      middlewares: [],
      modules: [__api_admin_catalog_js_onRequestGet],
    },
  {
      routePath: "/api/admin/catalog",
      mountPath: "/api/admin",
      method: "POST",
      middlewares: [],
      modules: [__api_admin_catalog_js_onRequestPost],
    },
  {
      routePath: "/api/admin/me",
      mountPath: "/api/admin",
      method: "GET",
      middlewares: [],
      modules: [__api_admin_me_js_onRequestGet],
    },
  {
      routePath: "/api/admin/news",
      mountPath: "/api/admin",
      method: "GET",
      middlewares: [],
      modules: [__api_admin_news_js_onRequestGet],
    },
  {
      routePath: "/api/admin/news",
      mountPath: "/api/admin",
      method: "POST",
      middlewares: [],
      modules: [__api_admin_news_js_onRequestPost],
    },
  {
      routePath: "/api/admin/settings",
      mountPath: "/api/admin",
      method: "GET",
      middlewares: [],
      modules: [__api_admin_settings_js_onRequestGet],
    },
  {
      routePath: "/api/admin/settings",
      mountPath: "/api/admin",
      method: "PATCH",
      middlewares: [],
      modules: [__api_admin_settings_js_onRequestPatch],
    },
  {
      routePath: "/api/admin/users",
      mountPath: "/api/admin",
      method: "GET",
      middlewares: [],
      modules: [__api_admin_users_js_onRequestGet],
    },
  {
      routePath: "/api/launcher/ensure-nick",
      mountPath: "/api/launcher",
      method: "POST",
      middlewares: [],
      modules: [__api_launcher_ensure_nick_js_onRequestPost],
    },
  {
      routePath: "/api/launcher/session",
      mountPath: "/api/launcher",
      method: "GET",
      middlewares: [],
      modules: [__api_launcher_session_js_onRequestGet],
    },
  {
      routePath: "/api/launcher/session",
      mountPath: "/api/launcher",
      method: "POST",
      middlewares: [],
      modules: [__api_launcher_session_js_onRequestPost],
    },
  {
      routePath: "/api/launcher/verify-token",
      mountPath: "/api/launcher",
      method: "POST",
      middlewares: [],
      modules: [__api_launcher_verify_token_js_onRequestPost],
    },
  {
      routePath: "/api/sync/player",
      mountPath: "/api/sync",
      method: "POST",
      middlewares: [],
      modules: [__api_sync_player_js_onRequestPost],
    },
  {
      routePath: "/api/profiles/:nick",
      mountPath: "/api/profiles",
      method: "GET",
      middlewares: [],
      modules: [__api_profiles__nick__js_onRequestGet],
    },
  {
      routePath: "/api/profiles/:nick",
      mountPath: "/api/profiles",
      method: "PATCH",
      middlewares: [],
      modules: [__api_profiles__nick__js_onRequestPatch],
    },
  {
      routePath: "/api/catalog",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_catalog_js_onRequestGet],
    },
  {
      routePath: "/api/login",
      mountPath: "/api",
      method: "POST",
      middlewares: [],
      modules: [__api_login_js_onRequestPost],
    },
  {
      routePath: "/api/logout",
      mountPath: "/api",
      method: "POST",
      middlewares: [],
      modules: [__api_logout_js_onRequestPost],
    },
  {
      routePath: "/api/me",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_me_js_onRequestGet],
    },
  {
      routePath: "/api/news",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_news_js_onRequestGet],
    },
  {
      routePath: "/api/players",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_players_js_onRequestGet],
    },
  {
      routePath: "/api/purchase",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_purchase_js_onRequestGet],
    },
  {
      routePath: "/api/purchase",
      mountPath: "/api",
      method: "POST",
      middlewares: [],
      modules: [__api_purchase_js_onRequestPost],
    },
  {
      routePath: "/api/register",
      mountPath: "/api",
      method: "POST",
      middlewares: [],
      modules: [__api_register_js_onRequestPost],
    },
  {
      routePath: "/api/server-status",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_server_status_js_onRequestGet],
    },
  {
      routePath: "/api/site",
      mountPath: "/api",
      method: "GET",
      middlewares: [],
      modules: [__api_site_js_onRequestGet],
    },
  ]