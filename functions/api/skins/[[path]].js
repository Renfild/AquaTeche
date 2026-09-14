// Pages file-based routing has no subpath match for functions/api/skins.js,
// so /api/skins/<nick>[/<kind>] needs this adapter (the worker router maps it explicitly).
import { onRequestGet as skinsGet } from "../skins.js";

export async function onRequestGet(context) {
  const path = context.params?.path || [];
  return skinsGet({
    ...context,
    params: { nick: path[0] || "", kind: path[1] || "" },
  });
}

// Skin fetchers (MineSkin via SkinsRestorer) probe with HEAD; without this they get a 404.
export async function onRequestHead(context) {
  return onRequestGet(context);
}
