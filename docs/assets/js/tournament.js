(function (root) {
  "use strict";

  var PRIZES = [
    { coins: 2500, key: "IV" },
    { coins: 1000, key: "III" },
    { coins: 500, key: "II" },
  ];

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function pad(n) {
    return n < 10 ? "0" + n : String(n);
  }

  function formatWeight(weight) {
    return Number(weight).toFixed(2).replace(".", ",") + " кг";
  }

  function formatCountdown(ms) {
    var total = Math.max(0, Math.floor(ms / 1000));
    var days = Math.floor(total / 86400);
    var hms = pad(Math.floor((total % 86400) / 3600)) + ":" + pad(Math.floor((total % 3600) / 60)) + ":" + pad(total % 60);
    return days > 0 ? days + "д " + hms : hms;
  }

  function statusView(current, offsetMs, nowMs) {
    if (!current) return { label: "Турнир ещё не проводился", remainingMs: null };
    var now = nowMs + offsetMs;
    if (current.status === "active") {
      return { label: "До конца турнира", remainingMs: Date.parse(current.ends_at) - now };
    }
    return {
      label: "До следующего турнира",
      remainingMs: current.next_starts_at ? Date.parse(current.next_starts_at) - now : null,
    };
  }

  function prizeText(place) {
    var prize = PRIZES[place - 1];
    return prize ? "+" + prize.coins + " монет · ключ Кейса " + prize.key : "";
  }

  function boardRowsHtml(top) {
    if (!top || top.length === 0) {
      return '<p class="t-empty">Пока никто не поймал рыбу. Станьте первым!</p>';
    }
    return top
      .map(function (e, i) {
        var place = i + 1;
        var prize = prizeText(place);
        return (
          '<div class="t-row' + (place <= 3 ? " t-row--prize t-row--p" + place : "") + '">' +
          '<span class="t-place">' + place + "</span>" +
          '<span class="t-who"><b>' + escapeHtml(e.nick) + "</b><small>" + escapeHtml(e.fish || "") + "</small></span>" +
          '<span class="t-weight">' + formatWeight(e.weight) + "</span>" +
          (prize ? '<span class="t-prize">' + prize + "</span>" : "") +
          "</div>"
        );
      })
      .join("");
  }

  function heroHtml(entry, isFinal) {
    if (!entry) {
      return '<div class="t-hero-empty">Ждём первую рыбу недели</div>';
    }
    return (
      '<div class="t-hero-card">' +
      '<div class="t-hero-kicker">' + (isFinal ? "Топ-1 недели" : "Лидер прямо сейчас") + "</div>" +
      '<div class="t-hero-nick">' + escapeHtml(entry.nick) + "</div>" +
      '<div class="t-hero-weight">' + formatWeight(entry.weight) + "</div>" +
      '<div class="t-hero-fish">' + escapeHtml(entry.fish || "") + "</div>" +
      "</div>"
    );
  }

  function weekLabel(week) {
    var s = String(week);
    return s.slice(0, 4) + " · неделя " + Number(s.slice(4));
  }

  function hallHtml(history) {
    if (!history || !history.weeks || history.weeks.length === 0) {
      return '<p class="t-empty">Архив появится после первого завершённого турнира.</p>';
    }
    var parts = [];
    if (history.record) {
      parts.push(
        '<div class="t-record"><span>Рекорд за всё время</span><b>' + formatWeight(history.record.weight) +
        "</b><small>" + escapeHtml(history.record.nick) + " · " + escapeHtml(history.record.fish || "") + "</small></div>"
      );
    }
    if (history.wins && history.wins.length) {
      parts.push(
        '<div class="t-wins"><span>Больше всего побед</span>' +
        history.wins.map(function (w) { return "<div><b>" + escapeHtml(w.nick) + "</b> — " + Number(w.wins) + "</div>"; }).join("") +
        "</div>"
      );
    }
    parts.push(
      '<div class="t-weeks">' +
      history.weeks
        .map(function (w) {
          return (
            '<article class="t-week"><h4>' + weekLabel(w.week) + "</h4>" +
            w.podium.map(function (p) {
              return '<div class="t-week-row"><span>' + p.place + "</span><b>" + escapeHtml(p.nick) + "</b><em>" + formatWeight(p.weight) + "</em></div>";
            }).join("") +
            "</article>"
          );
        })
        .join("") +
      "</div>"
    );
    return parts.join("");
  }

  var api = {
    escapeHtml: escapeHtml,
    formatWeight: formatWeight,
    formatCountdown: formatCountdown,
    statusView: statusView,
    boardRowsHtml: boardRowsHtml,
    heroHtml: heroHtml,
    hallHtml: hallHtml,
  };

  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  }
  if (typeof document === "undefined") return;

  var REFRESH_MS = 30000;
  var state = { current: null, offsetMs: 0 };

  function $(id) {
    return document.getElementById(id);
  }

  function renderCountdown() {
    var view = statusView(state.current, state.offsetMs, Date.now());
    $("t-status-label").textContent = view.label;
    $("t-countdown").textContent = view.remainingMs === null ? "—" : formatCountdown(view.remainingMs);
  }

  function render(data) {
    state.current = data.current;
    state.offsetMs = Date.parse(data.server_time) - Date.now();
    var top = data.current ? data.current.top : [];
    var isFinal = !!data.current && data.current.status === "finalized";
    $("t-hero").innerHTML = heroHtml(top[0], isFinal);
    $("t-board").innerHTML = boardRowsHtml(top);
    $("t-hall").innerHTML = hallHtml(data.history);
    renderCountdown();
  }

  function load() {
    fetch("/api/tournament?history=12", { headers: { accept: "application/json" } })
      .then(function (res) {
        if (!res.ok) throw new Error("HTTP " + res.status);
        return res.json();
      })
      .then(render)
      .catch(function () {
        $("t-board").innerHTML = '<p class="t-empty">Не удалось загрузить таблицу. Обновим через 30 секунд.</p>';
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    load();
    setInterval(load, REFRESH_MS);
    setInterval(renderCountdown, 1000);
  });
})(typeof window !== "undefined" ? window : globalThis);
