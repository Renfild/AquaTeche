/** @param {unknown} data @param {number} [status] */
export function json(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      ...extraHeaders,
    },
  });
}

export function bad(message, status = 400) {
  return json({ ok: false, error: message }, status);
}

export function purchasesDisabled() {
  return bad("Покупки временно отключены", 403);
}

/** @param {Request} request */
export async function readJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}
