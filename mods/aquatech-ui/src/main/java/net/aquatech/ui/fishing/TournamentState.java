package net.aquatech.ui.fishing;

import java.util.ArrayList;
import java.util.List;

/** Состояние недельного турнира, хранится в config/aquatech_tournament.json (Gson). */
final class TournamentState {
    int week = -1;
    long endsAt;
    boolean awarded;
    boolean finalSent = true;
    List<TournamentLogic.Entry> top = new ArrayList<>();
    /** Категория «по числу уловов»; в файле прежних недель поля нет, при загрузке станет пустым списком. */
    List<TournamentLogic.CountEntry> counts = new ArrayList<>();
    List<TournamentLogic.Prize> pending = new ArrayList<>();
}
