# Project Rules & Development Standards: Minecraft 1.20.1 (Forge + Mohist)

## Technical Architecture & Environment
- **Minecraft Version**: 1.20.1
- **Mod Loader**: Forge (Forge MDK 47.x)
- **Java Runtime**: Java 17
- **Build System**: Gradle 8.8 (ForgeGradle)
- **Server Environment**: Mohist 1.20.1 (Hybrid Forge + Bukkit API)

---

## Strict Execution Protocol (Research -> Deep Thinking -> Double Check -> Dual Test Verification)
1. **Deep Research First**: Always inspect actual source files, investigate existing models/APIs, and find all relevant context before planning.
2. **Thorough Risk Analysis & Thinking**: Analyze side-effects and constraints before drafting any code or response.
3. **Pre-Action Verification**: Double-check that no unrelated or user-crafted files are modified.
4. **Mandatory Dual Testing**: Run build/compile tests (`./gradlew build`, `dotnet build`), execute smoke tests, and perform explicit post-run validation of outputs.

---

## Mod Development Guidelines

### 1. Registration API & Mod ID
- Mod ID: `aquatech_ui`
- Use modern Forge `DeferredRegister<T>` and `RegistryObject<T>` for all items, blocks, block entities, menu types, and creative tabs.
- Always include complete package structure (`net.aquatech.ui...`).

### 2. GUI Architecture (Menu + Screen Separation)
- **Server Logic (`AbstractContainerMenu`)**: Handles slots, container data sync, and item validation.
- **Client Render (`AbstractContainerScreen`)**: Handles texture rendering (176x166 or 256x256), slot coordinates, and widget layout.
- **Menu Registration**: DeferredRegister<MenuType<?>>.
- **Screen Opening**: Call `NetworkHooks.openScreen` on server with buffer serialization.

### 3. Network Synchronization
- Use `SimpleChannel` with explicit `S2C` (Server-to-Client) and `C2S` (Client-to-Server) packet classes registered in `NetworkHandler`.

### 4. Models, Visuals & Language Keys
- Provide blockstates, 3D block JSON models, item JSON models, and native recipe JSONs in `src/main/resources/data/` and `src/main/resources/assets/`.
- Ensure 100% matching localization keys in `ru_ru.json` and `en_us.json`.

### 5. Mohist Compatibility
- Keep core mod logic as pure Forge without unnecessary `org.bukkit.*` imports unless explicit Mohist Bukkit Bridge integration is requested.
