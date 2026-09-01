import { AbsoluteFill, staticFile } from "remotion";
import { Video } from "@remotion/media";

export const CoverClip: React.FC<{
  file: string;
  name: string;
  trimBefore: number;
  muted?: boolean;
  objectPosition?: string;
  zoom?: number;
  fit?: "cover" | "panel";
}> = ({
  file,
  name,
  trimBefore,
  muted = true,
  objectPosition = "50% 42%",
  zoom = 1,
  fit = "cover",
}) => {
  if (fit === "panel") {
    const panelZoom = zoom === 1 ? 2.18 : zoom;
    return (
      <AbsoluteFill
        style={{
          backgroundColor: "#020b12",
          overflow: "hidden",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <AbsoluteFill
          style={{
            justifyContent: "center",
            alignItems: "center",
            scale: panelZoom,
          }}
        >
          <Video
            name={name}
            src={staticFile(file)}
            trimBefore={trimBefore}
            muted={muted}
            premountFor={12}
            style={{
              width: "100%",
              height: "auto",
            }}
          />
        </AbsoluteFill>
      </AbsoluteFill>
    );
  }

  return (
    <AbsoluteFill style={{ backgroundColor: "#020b12", overflow: "hidden" }}>
      <AbsoluteFill
        style={{
          scale: zoom,
          transformOrigin: objectPosition,
        }}
      >
        <Video
          name={name}
          src={staticFile(file)}
          trimBefore={trimBefore}
          objectFit="cover"
          muted={muted}
          premountFor={12}
          style={{
            width: "100%",
            height: "100%",
            objectPosition,
          }}
        />
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
