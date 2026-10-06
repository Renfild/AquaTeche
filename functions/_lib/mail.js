/**
 * Отправка писем. Основной путь: Cloudflare Email Service (привязка send_email с именем EMAIL, домен onboard-ится
 * командой `wrangler email sending enable aquateche.store`). Запасной путь: Resend по ключу RESEND_API_KEY.
 * Без обоих mailConfigured() возвращает false, и сайт честно говорит, что почта пока не включена.
 */
const FROM = { email: "auth@aquateche.store", name: "AquaTech" };

export function mailConfigured(env) {
  return Boolean(env?.EMAIL?.send || env?.RESEND_API_KEY);
}

/** @returns {Promise<boolean>} true, если письмо принято провайдером */
export async function sendMail(env, { to, subject, text, html }) {
  if (env?.EMAIL?.send) {
    try {
      await env.EMAIL.send({ to, from: FROM, subject, text, html });
      return true;
    } catch (error) {
      console.error("mail: Cloudflare Email Service", error?.code, error?.message);
      return false;
    }
  }
  if (env?.RESEND_API_KEY) {
    try {
      const res = await fetch("https://api.resend.com/emails", {
        method: "POST",
        headers: { Authorization: `Bearer ${env.RESEND_API_KEY}`, "Content-Type": "application/json" },
        body: JSON.stringify({ from: `${FROM.name} <${FROM.email}>`, to: [to], subject, text, html }),
      });
      return res.ok;
    } catch (error) {
      console.error("mail: Resend", error?.message);
      return false;
    }
  }
  return false;
}

export function maskEmail(email) {
  if (!email || !email.includes("@")) return "***";
  const [user, domain] = email.split("@");
  return `${user.length > 2 ? user.slice(0, 2) + "***" : "***"}@${domain}`;
}

/** Письмо с шестизначным кодом: простое, без картинок, чтобы не попадать в спам. */
export function codeMail({ nick, code, purpose }) {
  const title = purpose === "bind" ? "Подтверждение почты на AquaTech" : "Сброс пароля на AquaTech";
  const lead =
    purpose === "bind"
      ? `Ты привязываешь эту почту к аккаунту ${nick}. Код подтверждения:`
      : `Для аккаунта ${nick} запросили сброс пароля. Код подтверждения:`;
  const tail =
    purpose === "bind"
      ? "Код действует 15 минут. Если это не ты, просто проигнорируй письмо."
      : "Код действует 15 минут. Если сброс запрашивал не ты, проигнорируй письмо: пароль не изменится.";
  return {
    subject: title,
    text: `${lead} ${code}\n\n${tail}`,
    html: `<p>${lead}</p><p style="font-size:24px;font-weight:700;letter-spacing:4px;margin:12px 0">${code}</p><p>${tail}</p><p style="color:#667">AquaTech · aquateche.store</p>`,
  };
}
