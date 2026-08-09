import { bad } from "../../_lib/http.js";

/** Disabled: open account creation was a footgun. Use site register. */
export async function onRequestPost() {
  return bad("ensure-nick отключён — зарегистрируйся на сайте", 410);
}
