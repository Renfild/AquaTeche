import { AbsoluteFill, Sequence } from "remotion";
import { SceneEndCard } from "./scenes/EndCard";
import { SceneHook } from "./scenes/Hook";
import { SceneMontage } from "./scenes/Montage";
import { SceneRaft } from "./scenes/Raft";

export const TikTokAd: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: "#000000" }}>
      <Sequence name="Hook" durationInFrames={36}>
        <SceneHook />
      </Sequence>
      <Sequence name="Proof" from={36} durationInFrames={72}>
        <SceneRaft />
      </Sequence>
      <Sequence name="Unique" from={108} durationInFrames={116}>
        <SceneMontage />
      </Sequence>
      <Sequence name="End" from={224} durationInFrames={61}>
        <SceneEndCard />
      </Sequence>
    </AbsoluteFill>
  );
};
