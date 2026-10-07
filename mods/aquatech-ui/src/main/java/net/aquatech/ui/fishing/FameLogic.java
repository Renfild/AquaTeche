package net.aquatech.ui.fishing;

import java.util.Map;

/** Чистая логика стены славы: рекорд сервера по весу для каждого вида рыбы. Без Minecraft, чтобы её можно было тестировать. */
public final class FameLogic {

    public static final class Record {
        public String uuid;
        public String name;
        public String fish;
        public int grams;
        public int cm;
        public String date;

        public Record() {
        }

        public Record(String uuid, String name, String fish, int grams, int cm, String date) {
            this.uuid = uuid;
            this.name = name;
            this.fish = fish;
            this.grams = grams;
            this.cm = cm;
            this.date = date;
        }
    }

    public enum Outcome {
        /** Улов не дотянул до рекорда. */
        NONE,
        /** Первый рекорд вида. */
        FIRST,
        /** Игрок побил собственный рекорд. */
        OWN,
        /** Игрок отобрал рекорд у другого. */
        TAKEN
    }

    private FameLogic() {
    }

    /**
     * Первый рекорд вида требует вес не меньше среднего (иначе рекордом стал бы самый первый улов);
     * дальше рекорд побивается только строго большим весом.
     */
    public static Outcome judge(Record current, String uuid, int grams, int averageGrams) {
        if (grams <= 0) {
            return Outcome.NONE;
        }
        if (current == null) {
            return grams >= averageGrams ? Outcome.FIRST : Outcome.NONE;
        }
        if (grams <= current.grams) {
            return Outcome.NONE;
        }
        return current.uuid.equals(uuid) ? Outcome.OWN : Outcome.TAKEN;
    }

    /** Применяет улов к таблице рекордов; возвращает исход, прежний рекорд (для TAKEN/OWN) положен в {@code previousOut[0]}. */
    public static Outcome apply(Map<String, Record> records, String species, Record candidate, int averageGrams,
                                Record[] previousOut) {
        Record current = records.get(species);
        Outcome outcome = judge(current, candidate.uuid, candidate.grams, averageGrams);
        if (outcome != Outcome.NONE) {
            if (previousOut != null && previousOut.length > 0) {
                previousOut[0] = current;
            }
            records.put(species, candidate);
        }
        return outcome;
    }
}
