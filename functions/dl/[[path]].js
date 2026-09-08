const LAUNCHER_FILES = {
  "aquatech.exe": "AquaTech.exe",
  "aquatechlauncher.zip": "AquaTechLauncher.zip",
};

export async function onRequest(context) {
  const { request, params, env } = context;
  const rawFile = Array.isArray(params.path) ? params.path.join("/") : String(params.path || "");
  const canonicalFile = LAUNCHER_FILES[rawFile.toLowerCase()];
  if (!canonicalFile) {
    return new Response("Not found", { status: 404 });
  }

  let tag = "client-2.9.91";
  try {
    const bootRes = env?.ASSETS
      ? await env.ASSETS.fetch(new URL("/bootstrap.json", request.url))
      : null;
    if (bootRes && bootRes.ok) {
      const boot = await bootRes.json();
      if (boot.version) tag = `client-${String(boot.version).trim()}`;
    }
  } catch {
    /* keep default tag */
  }

  const upstream = `https://github.com/Renfild/AquaTeche/releases/download/${tag}/${canonicalFile}`;
  const isHead = request.method === "HEAD";

  try {
    const gh = await fetch(upstream, {
      method: isHead ? "HEAD" : "GET",
      redirect: "follow",
      headers: { "user-agent": "AquaTechPortal/1.0", accept: "*/*" },
      cf: { cacheTtl: 3600, cacheEverything: true },
    });
    if (!gh.ok) {
      return Response.redirect(upstream, 302);
    }
    const headers = new Headers();
    headers.set("content-type", canonicalFile.endsWith(".zip") ? "application/zip" : "application/octet-stream");
    headers.set("content-disposition", `attachment; filename="${canonicalFile}"`);
    const len = gh.headers.get("content-length");
    if (len) headers.set("content-length", len);
    headers.set("cache-control", "public, max-age=300");
    headers.set("access-control-allow-origin", "*");
    return new Response(isHead ? null : gh.body, { status: gh.status, headers });
  } catch {
    return Response.redirect(upstream, 302);
  }
}
