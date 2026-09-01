import { AbsoluteFill, Easing, Sequence, interpolate, useCurrentFrame } from "remotion";
import { CoverClip } from "../CoverClip";
import { BarSuper, Grain, SpecCard } from "../gfx";

export const SceneMontage: React.FC = () => {
  const frame = useCurrentFrame();

  return (
    <AbsoluteFill style={{ backgroundColor: "#020b12" }}>
      <Sequence name="Spec card" durationInFrames={34} layout="none">
        <SpecCard />
      </Sequence>
      <Sequence name="Menu profile" from={34} durationInFrames={42} layout="none">
        <CoverClip file="menu.mp4" name="AquaLumen profile" trimBefore={30} fit="panel" zoom={2.22} />
      </Sequence>
      <Sequence name="Case" from={76} durationInFrames={40} layout="none">
        <CoverClip file="menu.mp4" name="Ocean Pioneer" trimBefore={240} fit="panel" zoom={2.28} />
      </Sequence>
      <Grain />
      <BarSuper
        text="Океан. 1.20.1. Свой плот."
        opacity={interpolate(frame, [4, 10, 30, 34], [0, 1, 1, 0], {
          extrapolateLeft: "clamp",
          extrapolateRight: "clamp",
          easing: Easing.bezier(0.16, 1, 0.3, 1),
        })}
      />
    </AbsoluteFill>
  );
};
