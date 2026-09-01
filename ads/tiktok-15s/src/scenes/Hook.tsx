import { Audio } from "@remotion/media";
import { AbsoluteFill, Easing, Interactive, Sequence, interpolate, useCurrentFrame } from "remotion";
import { CoverClip } from "../CoverClip";
import { Grain, Vignette } from "../gfx";

const SRC = "src.mp4";

export const SceneHook: React.FC = () => {
  const frame = useCurrentFrame();
  const punchLocal = frame - 10;
  const flyLocal = frame - 18;
  const shakeX =
    punchLocal >= 0 && punchLocal < 5
      ? interpolate(punchLocal, [0, 1, 2, 3, 4, 5], [0, -12, 10, -8, 5, 0])
      : flyLocal >= 0 && flyLocal < 5
        ? interpolate(flyLocal, [0, 1, 2, 3, 4, 5], [0, 14, -11, 8, -4, 0])
        : 0;
  const shakeY =
    punchLocal >= 0 && punchLocal < 5
      ? interpolate(punchLocal, [0, 1, 2, 3, 4, 5], [0, 10, -14, 8, -4, 0])
      : flyLocal >= 0 && flyLocal < 5
        ? interpolate(flyLocal, [0, 1, 2, 3, 4, 5], [0, -12, 10, -6, 3, 0])
        : 0;

  return (
    <AbsoluteFill style={{ backgroundColor: "#000000" }}>
      <Sequence name="Bobber" durationInFrames={10} layout="none">
        <CoverClip file={SRC} name="Night bobber" trimBefore={180} zoom={1.12} />
      </Sequence>
      <Sequence name="Dunk" from={10} durationInFrames={8} layout="none">
        <AbsoluteFill
          style={{
            translate: `${shakeX}px ${shakeY}px`,
            rotate: punchLocal < 4 ? "-1.4deg" : "0deg",
          }}
        >
          <CoverClip file={SRC} name="Camera punch" trimBefore={450} zoom={1.22} />
        </AbsoluteFill>
      </Sequence>
      <Sequence from={10} durationInFrames={10} layout="none">
        <Audio src="https://remotion.media/whoosh.wav" />
      </Sequence>
      <Sequence name="In your face" from={18} durationInFrames={8} layout="none">
        <AbsoluteFill
          style={{
            translate: `${shakeX}px ${shakeY}px`,
            rotate: flyLocal < 4 ? "1.6deg" : "0deg",
          }}
        >
          <CoverClip
            file={SRC}
            name="Strike in face"
            trimBefore={696}
            zoom={1.45}
            objectPosition="50% 48%"
          />
        </AbsoluteFill>
      </Sequence>
      <Sequence name="Slam" from={26} durationInFrames={10} layout="none">
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
              opacity: interpolate(frame, [26, 27, 28], [0.55, 0.12, 0], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
              }),
            }}
          />
          <Interactive.Div
            name="SHAHT ghost R"
            style={{
              position: "absolute",
              color: "#ff3b3b",
              fontFamily: '"Segoe UI", Arial, sans-serif',
              fontWeight: 900,
              fontSize: 128,
              letterSpacing: "-0.05em",
              lineHeight: 0.88,
              opacity: interpolate(frame, [26, 28, 29], [0.7, 0.35, 0], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
              }),
              translate: "-6px 0px",
            }}
          >
            ШАХТ НЕТ.
          </Interactive.Div>
          <Interactive.Div
            name="SHAHT ghost B"
            style={{
              position: "absolute",
              color: "#3df0ff",
              fontFamily: '"Segoe UI", Arial, sans-serif',
              fontWeight: 900,
              fontSize: 128,
              letterSpacing: "-0.05em",
              lineHeight: 0.88,
              opacity: interpolate(frame, [26, 28, 29], [0.7, 0.35, 0], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
              }),
              translate: "6px 0px",
            }}
          >
            ШАХТ НЕТ.
          </Interactive.Div>
          <Interactive.Div
            name="SHAHT NET"
            style={{
              color: "#f3fbff",
              fontFamily: '"Segoe UI", Arial, sans-serif',
              fontWeight: 900,
              fontSize: 128,
              letterSpacing: "-0.05em",
              textAlign: "center",
              lineHeight: 0.88,
              opacity: interpolate(frame, [26, 28], [0, 1], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
                easing: Easing.bezier(0.16, 1, 0.3, 1),
              }),
              scale: interpolate(frame, [26, 32], [1.22, 1], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
                easing: Easing.spring({ damping: 200 }),
                output: "perceptual-scale",
              }),
            }}
          >
            ШАХТ НЕТ.
          </Interactive.Div>
        </AbsoluteFill>
      </Sequence>
      <Sequence from={26} durationInFrames={10} layout="none">
        <Audio src="https://remotion.media/mouse-click.wav" />
      </Sequence>
      <Vignette />
      <Grain />
    </AbsoluteFill>
  );
};
