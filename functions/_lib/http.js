const SECURITY_HEADERS = {
  "x-content-type-options": "nosniff",
  "referrer-policy": "strict-origin-when-cross-origin",
  "x-frame-options": "SAMEORIGIN",
};

/** @param {unknown} data @param {number} [status] */
export function json(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      ...SECURITY_HEADERS,
      ...extraHeaders,
    },
  });
}

/** @param {Response} response */
export function withSecurityHeaders(response) {
  const headers = new Headers(response.headers);
  for (const [k, v] of Object.entries(SECURITY_HEADERS)) {
    if (!headers.has(k)) headers.set(k, v);
  }
  return new Response(response.body, {
    status: response.status,
    statusText: response.statusText,
    headers,
  });
}

export function bad(message, status = 400, extra = {}) {
  return json({ ok: false, error: message, ...extra }, status);
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
