package net.aquatech.ui.fishing;

/**
 * Чистая математика анимации сундука «Сокровища из глубин»: без Minecraft-классов, покрыта JUnit.
 * Квартернионы хранятся как [x, y, z, w], положительный угол вокруг X поднимает передний край (-Z).
 */
final class DeepChestAnimation {

    static final long EMERGE_MS = 3000L;
    static final long LID_OPEN_MS = 1200L;
    static final double BOB_BLOCKS = 0.12;
    static final double ROLL_DEG = 2.5;
    static final double PITCH_DEG = 1.5;
    static final double LID_IDLE_DEG = 14.0;
    static final double LID_NEAR_DEG = 24.0;
    static final double RATTLE_DEG = 6.0;
    /** Шарнир крышки в координатах модели (16 юнитов = блок): задний верхний край корпуса. */
    static final double[] HINGE_MODEL = {8.0, 12.2, 19.3};
    /** Победа: фейерверк держится, затем сундук уходит вниз. */
    static final long WON_MS = 2900L;
    /** Тайм-аут: крышка захлопывается, сундук тонет. */
    static final long FIZZLE_MS = 2400L;

    private static final long RATTLE_START_MS = EMERGE_MS + LID_OPEN_MS + 2000L;
    private static final double RATTLE_WINDOW_MS = 700.0;
    private static final double RATTLE_FAR_PERIOD_MS = 11_000.0;
    private static final double RATTLE_NEAR_PERIOD_MS = 4_000.0;

    record Pose(double[] translation, double[] rotation, double scale) {
    }

    private DeepChestAnimation() {
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static double smooth(double p) {
        p = clamp01(p);
        return p * p * (3.0 - 2.0 * p);
    }

    /** 0..1: сундук всплывает из глубины за {@link #EMERGE_MS}, мягко в начале и в конце. */
    static double emerge(long ageMs) {
        return smooth(ageMs / (double) EMERGE_MS);
    }

    static double bobY(long ageMs) {
        return BOB_BLOCKS * Math.sin(2.0 * Math.PI * ageMs / 3200.0);
    }

    static double rollDeg(long ageMs) {
        return ROLL_DEG * Math.sin(2.0 * Math.PI * ageMs / 4100.0 + 1.3);
    }

    static double pitchDeg(long ageMs) {
        return PITCH_DEG * Math.sin(2.0 * Math.PI * ageMs / 5300.0 + 0.4);
    }

    /** Угол крышки: закрыта, пока сундук всплывает, потом плавно приоткрывается и «дышит»; near 0..1 открывает шире. */
    static double lidAngleDeg(long ageMs, double near) {
        double open = smooth((ageMs - EMERGE_MS) / (double) LID_OPEN_MS);
        double target = LID_IDLE_DEG + (LID_NEAR_DEG - LID_IDLE_DEG) * clamp01(near);
        double breathing = 2.0 * Math.sin(2.0 * Math.PI * ageMs / 2300.0);
        return open * target + open * breathing;
    }

    /** Дрожь крышки: короткие затухающие всплески, рядом с игроком чаще. */
    static double rattleDeg(long ageMs, double near) {
        if (ageMs < RATTLE_START_MS) return 0.0;
        double period = RATTLE_FAR_PERIOD_MS + (RATTLE_NEAR_PERIOD_MS - RATTLE_FAR_PERIOD_MS) * clamp01(near);
        double phase = (ageMs - RATTLE_START_MS) % period;
        if (phase >= RATTLE_WINDOW_MS) return 0.0;
        double decay = 1.0 - phase / RATTLE_WINDOW_MS;
        return RATTLE_DEG * decay * Math.sin(2.0 * Math.PI * phase / 200.0 + 0.5);
    }

    /** Номер окна дрожи (-1 до первого): по смене номера сервис запускает звук и частицы. */
    static int rattleWindowIndex(long ageMs, double near) {
        if (ageMs < RATTLE_START_MS) return -1;
        double period = RATTLE_FAR_PERIOD_MS + (RATTLE_NEAR_PERIOD_MS - RATTLE_FAR_PERIOD_MS) * clamp01(near);
        return (int) ((ageMs - RATTLE_START_MS) / period);
    }

    /** Победа: крышка распахивается до 82° и оседает к 62°. */
    static double winLidDeg(long msSinceWin) {
        return 62.0 + 20.0 * Math.exp(-Math.max(0L, msSinceWin) / 450.0);
    }

    static double winSink(long msSinceWin) {
        return smooth((msSinceWin - 1500.0) / (WON_MS - 1500.0));
    }

    /** Тайм-аут: крышка за 300 мс захлопывается из текущего угла. */
    static double fizzleLidDeg(long msSinceFizzle, double fromDeg) {
        return fromDeg * (1.0 - smooth(msSinceFizzle / 300.0));
    }

    static double fizzleSink(long msSinceFizzle) {
        return smooth((msSinceFizzle - 500.0) / (FIZZLE_MS - 500.0));
    }

    /**
     * Поза корпуса: масштаб растёт при всплытии, sink 0..1 топит сундук (масштаб к 30% и вниз),
     * покачивание и крен гаснут вместе с появлением.
     */
    static Pose bodyPose(long ageMs, double modelScale, double sink) {
        double e = emerge(ageMs);
        double s = clamp01(sink);
        double scale = modelScale * (0.35 + 0.65 * e) * (1.0 - 0.7 * s);
        double y = 0.5 * scale + bobY(ageMs) * e * (1.0 - s) - (1.0 - e) * 0.55 - s * 0.6;
        double[] q = mul(quatZ(rollDeg(ageMs) * e), quatX(pitchDeg(ageMs) * e));
        return new Pose(new double[]{0.0, y, 0.0}, q, scale);
    }

    /** Поза крышки: поворот вокруг шарнира на angleDeg поверх позы корпуса. */
    static Pose lidPose(double[] bodyTranslation, double[] bodyRotation, double scale, double angleDeg) {
        double[] hc = {HINGE_MODEL[0] / 16.0 - 0.5, HINGE_MODEL[1] / 16.0 - 0.5, HINGE_MODEL[2] / 16.0 - 0.5};
        double[] qx = quatX(angleDeg);
        double[] turned = rotate(qx, hc);
        double[] delta = {(hc[0] - turned[0]) * scale, (hc[1] - turned[1]) * scale, (hc[2] - turned[2]) * scale};
        double[] shift = rotate(bodyRotation, delta);
        double[] translation = {bodyTranslation[0] + shift[0], bodyTranslation[1] + shift[1], bodyTranslation[2] + shift[2]};
        return new Pose(translation, mul(bodyRotation, qx), scale);
    }

    static double[] quatX(double deg) {
        double h = Math.toRadians(deg) / 2.0;
        return new double[]{Math.sin(h), 0.0, 0.0, Math.cos(h)};
    }

    static double[] quatZ(double deg) {
        double h = Math.toRadians(deg) / 2.0;
        return new double[]{0.0, 0.0, Math.sin(h), Math.cos(h)};
    }

    /** a * b: сначала применяется b, затем a. */
    static double[] mul(double[] a, double[] b) {
        return new double[]{
                a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
                a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
                a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
                a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]};
    }

    static double[] rotate(double[] q, double[] v) {
        double ux = q[0], uy = q[1], uz = q[2], w = q[3];
        double cx = uy * v[2] - uz * v[1];
        double cy = uz * v[0] - ux * v[2];
        double cz = ux * v[1] - uy * v[0];
        double dx = uy * cz - uz * cy;
        double dy = uz * cx - ux * cz;
        double dz = ux * cy - uy * cx;
        return new double[]{v[0] + 2.0 * (w * cx + dx), v[1] + 2.0 * (w * cy + dy), v[2] + 2.0 * (w * cz + dz)};
    }
}
