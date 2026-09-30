package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepChestAnimationTest {

    @Test
    void emergeRunsFromZeroToOneWithASmoothEnd() {
        assertEquals(0.0, DeepChestAnimation.emerge(0), 1e-9);
        assertEquals(0.0, DeepChestAnimation.emerge(-500), 1e-9);
        assertEquals(1.0, DeepChestAnimation.emerge(DeepChestAnimation.EMERGE_MS), 1e-9);
        assertEquals(1.0, DeepChestAnimation.emerge(DeepChestAnimation.EMERGE_MS * 4), 1e-9);
        double mid = DeepChestAnimation.emerge(DeepChestAnimation.EMERGE_MS / 2);
        assertEquals(0.5, mid, 1e-9);
        assertTrue(DeepChestAnimation.emerge(100) < 0.05, "starts slowly");
        assertTrue(DeepChestAnimation.emerge(DeepChestAnimation.EMERGE_MS - 100) > 0.95, "ends slowly");
    }

    @Test
    void bobAndTiltStayInsideTheirLimitsForAnyAge() {
        for (long age = 0; age < 120_000; age += 137) {
            assertTrue(Math.abs(DeepChestAnimation.bobY(age)) <= DeepChestAnimation.BOB_BLOCKS + 1e-9);
            assertTrue(Math.abs(DeepChestAnimation.rollDeg(age)) <= DeepChestAnimation.ROLL_DEG + 1e-9);
            assertTrue(Math.abs(DeepChestAnimation.pitchDeg(age)) <= DeepChestAnimation.PITCH_DEG + 1e-9);
        }
    }

    @Test
    void lidStaysClosedWhileTheChestRisesThenOpensAndOpensWiderWhenSomeoneIsNear() {
        assertEquals(0.0, DeepChestAnimation.lidAngleDeg(0, 0.0), 1e-9);
        assertEquals(0.0, DeepChestAnimation.lidAngleDeg(DeepChestAnimation.EMERGE_MS, 0.0), 1e-9);
        long settled = DeepChestAnimation.EMERGE_MS + DeepChestAnimation.LID_OPEN_MS + 20_000;
        double far = averageLid(settled, 0.0);
        double near = averageLid(settled, 1.0);
        assertEquals(DeepChestAnimation.LID_IDLE_DEG, far, 0.6);
        assertEquals(DeepChestAnimation.LID_NEAR_DEG, near, 0.6);
        assertTrue(near > far);
    }

    private static double averageLid(long from, double near) {
        double sum = 0;
        int n = 0;
        for (long t = from; t < from + 60_000; t += 50) {
            sum += DeepChestAnimation.lidAngleDeg(t, near);
            n++;
        }
        return sum / n;
    }

    @Test
    void rattleOnlyHappensInsideItsWindowAndNeverBeforeTheLidOpens() {
        double calm = 0;
        double shaken = 0;
        int windows = 0;
        for (long t = 0; t < 120_000; t += 25) {
            double r = DeepChestAnimation.rattleDeg(t, 0.0);
            assertTrue(Math.abs(r) <= DeepChestAnimation.RATTLE_DEG + 1e-9, "amplitude " + r);
            if (t < DeepChestAnimation.EMERGE_MS + DeepChestAnimation.LID_OPEN_MS) {
                assertEquals(0.0, r, 1e-9, "no rattle before the lid is open, t=" + t);
            }
            if (r != 0.0) windows++;
            else calm++;
            shaken += Math.abs(DeepChestAnimation.rattleDeg(t, 1.0));
        }
        assertTrue(windows > 0, "rattles at least once in two minutes");
        assertTrue(calm > windows * 5, "mostly quiet");
        assertTrue(shaken > 0);
    }

    @Test
    void nearChestRattlesMoreOftenThanAFarOne() {
        int far = 0;
        int near = 0;
        for (long t = 20_000; t < 140_000; t += 25) {
            if (DeepChestAnimation.rattleDeg(t, 0.0) != 0.0) far++;
            if (DeepChestAnimation.rattleDeg(t, 1.0) != 0.0) near++;
        }
        assertTrue(near > far * 1.5, "near=" + near + " far=" + far);
    }

    @Test
    void rattleWindowIndexCountsPeriodsAfterTheRattleStart() {
        long start = DeepChestAnimation.EMERGE_MS + DeepChestAnimation.LID_OPEN_MS + 2000L;
        assertEquals(-1, DeepChestAnimation.rattleWindowIndex(0, 0.0));
        assertEquals(-1, DeepChestAnimation.rattleWindowIndex(start - 1, 0.0));
        assertEquals(0, DeepChestAnimation.rattleWindowIndex(start, 0.0));
        assertEquals(0, DeepChestAnimation.rattleWindowIndex(start + 10_999, 0.0));
        assertEquals(1, DeepChestAnimation.rattleWindowIndex(start + 11_000, 0.0));
        assertEquals(2, DeepChestAnimation.rattleWindowIndex(start + 8_000, 1.0));
    }

    @Test
    void winFlingsTheLidWideThenSettlesAndSinksAfterTheFireworks() {
        assertEquals(82.0, DeepChestAnimation.winLidDeg(0), 1e-6);
        assertEquals(62.0, DeepChestAnimation.winLidDeg(6000), 0.1);
        assertEquals(0.0, DeepChestAnimation.winSink(0), 1e-9);
        assertEquals(0.0, DeepChestAnimation.winSink(1500), 1e-9);
        assertEquals(1.0, DeepChestAnimation.winSink(DeepChestAnimation.WON_MS), 1e-9);
        assertTrue(DeepChestAnimation.winSink(2200) > 0.0 && DeepChestAnimation.winSink(2200) < 1.0);
    }

    @Test
    void timeoutSlamsTheLidShutThenSinksTheChest() {
        assertEquals(20.0, DeepChestAnimation.fizzleLidDeg(0, 20.0), 1e-9);
        assertEquals(0.0, DeepChestAnimation.fizzleLidDeg(400, 20.0), 1e-9);
        assertEquals(0.0, DeepChestAnimation.fizzleSink(300), 1e-9);
        assertEquals(1.0, DeepChestAnimation.fizzleSink(DeepChestAnimation.FIZZLE_MS), 1e-9);
        assertTrue(DeepChestAnimation.fizzleSink(1400) > 0.0 && DeepChestAnimation.fizzleSink(1400) < 1.0);
    }

    @Test
    void quaternionAboutXRotatesYTowardZ() {
        double[] q = DeepChestAnimation.quatX(90);
        double[] v = DeepChestAnimation.rotate(q, new double[]{0, 1, 0});
        assertArrayEquals(new double[]{0, 0, 1}, v, 1e-9);
        double[] front = DeepChestAnimation.rotate(q, new double[]{0, 0, -1});
        assertArrayEquals(new double[]{0, 1, 0}, front, 1e-9, "positive angle lifts the front (-z) edge");
    }

    @Test
    void multiplyingQuaternionsComposesRotations() {
        double[] qz = DeepChestAnimation.quatZ(90);
        double[] qx = DeepChestAnimation.quatX(90);
        double[] q = DeepChestAnimation.mul(qz, qx);
        double[] direct = DeepChestAnimation.rotate(qz, DeepChestAnimation.rotate(qx, new double[]{0, 1, 0}));
        assertArrayEquals(direct, DeepChestAnimation.rotate(q, new double[]{0, 1, 0}), 1e-9);
    }

    @Test
    void hingePointDoesNotMoveWhenTheLidRotates() {
        double scale = 0.7;
        double[] bodyT = {0.0, 0.5 * scale + 0.08, 0.0};
        double[] bodyQ = DeepChestAnimation.mul(DeepChestAnimation.quatZ(2.0), DeepChestAnimation.quatX(-1.0));
        double[] hinge = DeepChestAnimation.HINGE_MODEL;
        double[] atClosed = worldOf(hinge, DeepChestAnimation.lidPose(bodyT, bodyQ, scale, 0.0));
        for (double angle : new double[]{8, 14, 22, 45, 75}) {
            double[] atOpen = worldOf(hinge, DeepChestAnimation.lidPose(bodyT, bodyQ, scale, angle));
            assertArrayEquals(atClosed, atOpen, 1e-9, "hinge drifted at " + angle);
        }
    }

    @Test
    void openingTheLidRaisesItsFrontEdge() {
        double scale = 0.7;
        double[] bodyT = {0.0, 0.5 * scale, 0.0};
        double[] bodyQ = {0, 0, 0, 1};
        double[] front = {8, 13.0, -2.0};
        double yClosed = worldOf(front, DeepChestAnimation.lidPose(bodyT, bodyQ, scale, 0.0))[1];
        double yOpen = worldOf(front, DeepChestAnimation.lidPose(bodyT, bodyQ, scale, 30.0))[1];
        assertTrue(yOpen > yClosed + 0.3, "closed " + yClosed + " open " + yOpen);
    }

    /** Where a model-space point (16 units per block) ends up for an ItemDisplay with item_display none. */
    private static double[] worldOf(double[] model, DeepChestAnimation.Pose pose) {
        double[] centred = {model[0] / 16.0 - 0.5, model[1] / 16.0 - 0.5, model[2] / 16.0 - 0.5};
        double[] scaled = {centred[0] * pose.scale(), centred[1] * pose.scale(), centred[2] * pose.scale()};
        double[] rotated = DeepChestAnimation.rotate(pose.rotation(), scaled);
        return new double[]{rotated[0] + pose.translation()[0], rotated[1] + pose.translation()[1], rotated[2] + pose.translation()[2]};
    }
}
