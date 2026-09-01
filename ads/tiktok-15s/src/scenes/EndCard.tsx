import { AbsoluteFill, Easing, Img, interpolate, staticFile, useCurrentFrame } from "remotion";
import { BarSuper } from "../gfx";

export const SceneEndCard: React.FC = () => {
  const frame = useCurrentFrame();

  return (
    <AbsoluteFill
      style={{
        backgroundColor: "#000000",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <AbsoluteFill
        style={{
          backgroundColor: "#f3fbff",
          opacity: interpolate(frame, [1, 2, 4], [0, 0.4, 0], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
        }}
      />
      <Img
        name="Logo"
        src={staticFile("logo.png")}
        style={{
          width: 300,
          height: 300,
          imageRendering: "pixelated",
          opacity: interpolate(frame, [2, 6], [0, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
          scale: interpolate(frame, [2, 12], [1.16, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
            easing: Easing.spring({ damping: 180 }),
            output: "perceptual-scale",
          }),
        }}
      />
      <BarSuper
        text="aquateche.store"
        bottom={620}
        opacity={interpolate(frame, [8, 14], [0, 1], {
          extrapolateLeft: "clamp",
          extrapolateRight: "clamp",
          easing: Easing.bezier(0.16, 1, 0.3, 1),
        })}
      />
    </AbsoluteFill>
  );
};
