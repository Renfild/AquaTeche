import "./index.css";
import { Composition, Folder } from "remotion";
import { TikTokAd } from "./TikTokAd";
import { SceneEndCard } from "./scenes/EndCard";
import { SceneHook } from "./scenes/Hook";
import { SceneMontage } from "./scenes/Montage";
import { SceneRaft } from "./scenes/Raft";

const FPS = 30;
const WIDTH = 1080;
const HEIGHT = 1920;

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Folder name="TikTok-Scenes">
        <Composition
          id="SceneHook"
          component={SceneHook}
          durationInFrames={36}
          fps={FPS}
          width={WIDTH}
          height={HEIGHT}
        />
        <Composition
          id="SceneRaft"
          component={SceneRaft}
          durationInFrames={72}
          fps={FPS}
          width={WIDTH}
          height={HEIGHT}
        />
        <Composition
          id="SceneMontage"
          component={SceneMontage}
          durationInFrames={116}
          fps={FPS}
          width={WIDTH}
          height={HEIGHT}
        />
        <Composition
          id="SceneEnd"
          component={SceneEndCard}
          durationInFrames={61}
          fps={FPS}
          width={WIDTH}
          height={HEIGHT}
        />
      </Folder>
      <Composition
        id="TikTokAd"
        component={TikTokAd}
        durationInFrames={285}
        fps={FPS}
        width={WIDTH}
        height={HEIGHT}
      />
    </>
  );
};
