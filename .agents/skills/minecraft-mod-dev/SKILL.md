---
name: minecraft-mod-dev
description: Expert Minecraft 1.20.1 Mod Development agent skill for Forge, Mohist, Fabric, and NeoForge. Use when creating, modifying, debugging, building, or architecting Minecraft mods, custom blocks, items, tile entities, GUI screens, networking, or CraftTweaker scripts.
---

# Minecraft Mod Development Skill (Minecraft 1.20.1 Forge + Mohist)

This skill provides comprehensive instructions, architecture guidelines, and standards for developing production-grade Minecraft mods on 1.20.1 (Forge MDK 47.x / Mohist hybrid server).

---

## 🛠️ Key Architectural Principles

### 1. Registries & Modern Forge DeferredRegister
- Always use `DeferredRegister<T>` and `RegistryObject<T>` for all mod elements:
  - Items: `DeferredRegister<Item>`
  - Blocks: `DeferredRegister<Block>`
  - Block Entities: `DeferredRegister<BlockEntityType<?>>`
  - Menu Types: `DeferredRegister<MenuType<?>>`
  - Recipe Types & Serializers: `DeferredRegister<RecipeSerializer<?>>`
  - Creative Tabs: `DeferredRegister<CreativeModeTab>`

### 2. Client-Server GUI Separation (Menu + Screen Architecture)
- **Server-Side Container (`AbstractContainerMenu`)**:
  - Manages item slots, container data synchronization (`ContainerData`), stack validation (`mayPlace`), and quick-transfer logic (`quickMoveStack`).
- **Client-Side Rendering (`AbstractContainerScreen` / `GuiGraphics`)**:
  - Handles GUI texture rendering (`RenderSystem.setShaderTexture`), progress bars, energy gauges, tooltips, and widget overlays.
- **Screen Registration**:
  - Register menu screens in `@SubscribeEvent` on `FMLClientSetupEvent` using `MenuScreens.register(...)`.
- **Opening Container**:
  - Call `NetworkHooks.openScreen((ServerPlayer) player, menuProvider, blockPos)` on the server side.

### 3. Network Synchronization & SimpleChannel
- Use Forge `SimpleChannel` with explicit `S2C` (Server-to-Client) and `C2S` (Client-to-Server) packet classes.
- Encode/decode buffer methods using `FriendlyByteBuf`.
- Always handle packet execution on `ctx.get().enqueueWork(...)`.

### 4. Mohist Compatibility
- Keep core mod code as pure Forge without hard dependencies on Spigot/Bukkit imports (`org.bukkit.*`) unless explicitly invoking Mohist Bukkit Bridge functionality.

### 5. Assets & Data Generation
- **Models & Blockstates**: Standard 1.20.1 JSON blockstates, 3D block models, and item models in `assets/<mod_id>/models/`.
- **Localization**: Complete 1:1 matching keys in `ru_ru.json` and `en_us.json`.
- **Pixel-Art GUI Alignment**: Ensure background textures (256x256) match exact Java menu slot coordinates (`x, y`).

---

## 📖 Standard Project Structure
```
src/main/
├── java/net/<domain>/<mod_id>/
│   ├── AquaTechUI.java (Mod Init & Event Registration)
│   ├── block/ (Block classes)
│   ├── block/entity/ (BlockEntity classes & Energy/Inventory Capabilities)
│   ├── item/ (Custom Items & Fishing Rods)
│   ├── inventory/ (AbstractContainerMenu subclasses)
│   ├── client/gui/ (AbstractContainerScreen subclasses)
│   ├── network/ (NetworkHandler & SimpleChannel Packets)
│   └── registry/ (ModBlocks, ModItems, ModBlockEntities, ModMenuTypes)
└── resources/
    ├── META-INF/mods.toml
    ├── assets/<mod_id>/
    │   ├── lang/ (ru_ru.json, en_us.json)
    │   ├── models/ (block/ & item/)
    │   ├── textures/ (block/, item/, gui/)
    │   └── blockstates/
    └── data/<mod_id>/
        └── recipes/
```
