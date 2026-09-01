import { AbsoluteFill, Easing, Img, interpolate, staticFile, useCurrentFrame } from "remotion";

export const BarSuper: React.FC<{
  text: string;
  opacity: number;
  bottom?: number;
}> = ({ text, opacity, bottom = 538 }) => {
  return (
    <AbsoluteFill
      style={{
        pointerEvents: "none",
        justifyContent: "flex-end",
        alignItems: "center",
        paddingBottom: bottom,
        opacity,
      }}
    >
      <div
        style={{
          backgroundColor: "#f3fbff",
          color: "#0a0e12",
          fontFamily: '"Segoe UI", Arial, sans-serif',
          fontWeight: 800,
          fontSize: 56,
          letterSpacing: "-0.03em",
          lineHeight: 1.15,
          padding: "10px 28px",
          textAlign: "center",
        }}
      >
        {text}
      </div>
    </AbsoluteFill>
  );
};

export const SpecCard: React.FC = () => {
  const frame = useCurrentFrame();
  const line = (from: number) =>
    interpolate(frame, [from, from + 6], [0, 1], {
      extrapolateLeft: "clamp",
      extrapolateRight: "clamp",
      easing: Easing.bezier(0.16, 1, 0.3, 1),
    });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: "#0a0e12",
        backgroundImage:
          "linear-gradient(rgba(243,251,255,0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(243,251,255,0.08) 1px, transparent 1px)",
        backgroundSize: "48px 48px",
        paddingLeft: 88,
        paddingTop: 640,
      }}
    >
      <AbsoluteFill
        style={{
          width: 48,
          height: 48,
          top: 520,
          left: 180,
          backgroundColor: "#3df0ff",
          filter: "blur(1.5px)",
          opacity: interpolate(frame, [0, 12], [0.25, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
        }}
      />
      <div
        style={{
          fontFamily: "Consolas, 'Courier New', monospace",
          fontSize: 64,
          color: "#3df0ff",
          opacity: line(0),
        }}
      >
        1.20.1
      </div>
      <div
        style={{
          fontFamily: '"Segoe UI", Arial, sans-serif',
          fontWeight: 800,
          fontSize: 120,
          color: "#f3fbff",
          letterSpacing: "-0.05em",
          lineHeight: 0.92,
          marginTop: 12,
          opacity: line(4),
        }}
      >
        ОКЕАН
      </div>
      <div
        style={{
          fontFamily: "Georgia, 'Times New Roman', serif",
          fontStyle: "italic",
          fontSize: 72,
          color: "#f3fbff",
          marginTop: 16,
          opacity: line(8),
        }}
      >
        свой плот
      </div>
    </AbsoluteFill>
  );
};

export const Vignette: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        pointerEvents: "none",
        background:
          "radial-gradient(ellipse at 50% 42%, rgba(0,0,0,0) 38%, rgba(0,0,0,0.62) 100%)",
      }}
    />
  );
};

export const Grain: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        pointerEvents: "none",
        opacity: 0.07,
        mixBlendMode: "overlay",
        backgroundImage:
          "repeating-radial-gradient(circle at 18% 28%, #ffffff 0px, #ffffff 0.6px, transparent 0.8px, transparent 3px)",
      }}
    />
  );
};

export const Scanlines: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        pointerEvents: "none",
        opacity: 0.09,
        backgroundImage:
          "repeating-linear-gradient(0deg, transparent 0px, transparent 3px, rgba(0,0,0,0.55) 3px, rgba(0,0,0,0.55) 4px)",
      }}
    />
  );
};

export const CutFlash: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: "#f3fbff",
        opacity: 0.22,
        pointerEvents: "none",
      }}
    />
  );
};

export const CyanFrame: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        pointerEvents: "none",
        boxShadow: "inset 0 0 0 3px rgba(126,233,242,0.55), inset 0 0 80px rgba(0,20,28,0.45)",
      }}
    />
  );
};

export const InvSlam: React.FC = () => {
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
          opacity: interpolate(frame, [0, 1, 3], [0.4, 0.08, 0], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
        }}
      />
      <Img
        name="Inventory"
        src={staticFile("inv.png")}
        style={{
          width: 1000,
          imageRendering: "pixelated",
          rotate: interpolate(frame, [0, 6], ["-4deg", "0deg"], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
            easing: Easing.spring({ damping: 160 }),
          }),
          scale: interpolate(frame, [0, 7], [1.28, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
            easing: Easing.spring({ damping: 170 }),
            output: "perceptual-scale",
          }),
        }}
      />
    </AbsoluteFill>
  );
};
