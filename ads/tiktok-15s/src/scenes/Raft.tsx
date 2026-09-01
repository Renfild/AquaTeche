import { AbsoluteFill, Easing, Sequence, interpolate, useCurrentFrame } from "remotion";
import { CoverClip } from "../CoverClip";
import { BarSuper, Grain, InvSlam } from "../gfx";

export const SceneRaft: React.FC = () => {
  const frame = useCurrentFrame();

  return (
    <AbsoluteFill style={{ backgroundColor: "#020b12" }}>
      <Sequence name="Inventory" durationInFrames={32} layout="none">
        <InvSlam />
      </Sequence>
      <Sequence name="Loot tables" from={32} durationInFrames={40} layout="none">
        <CoverClip file="loot.mp4" name="Rod loot site" trimBefore={210} fit="panel" zoom={2.05} />
      </Sequence>
      <Grain />
      <BarSuper
        text="Копать не надо."
        opacity={interpolate(frame, [2, 6, 28, 32], [0, 1, 1, 0], {
          extrapolateLeft: "clamp",
          extrapolateRight: "clamp",
          easing: Easing.bezier(0.16, 1, 0.3, 1),
        })}
      />
      <BarSuper
        text="Ловишь."
        opacity={interpolate(frame, [32, 36, 68, 72], [0, 1, 1, 0], {
          extrapolateLeft: "clamp",
          extrapolateRight: "clamp",
          easing: Easing.bezier(0.16, 1, 0.3, 1),
        })}
      />
    </AbsoluteFill>
  );
};
