# Expanded Vanilla Interactions

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/ModLoader-Fabric-blue.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Expanded Vanilla Interactions** is a modular Minecraft mod designed to enhance vanilla interactions in an intuitive, vanilla-friendly style.

Its primary initial feature is **Boat Mooring**: allowing players to tie boats to leads and anchor them to fence posts to keep them in place at docks, harbors, and waterways.

---

## ⚓ Features: Boat Mooring

- **Tie Boats with Leads**: Right-click any boat with a **Lead** to leash it to yourself. You can pull boats behind you across water and land.
- **Anchor to Fence Posts**: While holding a leashed boat, right-click any **Fence Post** to anchor the boat to that post. A leash knot appears on the fence, and the boat stays anchored within dock range.
- **Emergent Mooring Post**: When a boat is tied to a lead or fence post, a small wooden **mooring post (bollard)** emerges at the front bow of the boat, using the **original wood color scheme** of that boat:
  - 🌲 **Oak**
  - 🌲 **Spruce**
  - 🌲 **Birch**
  - 🌴 **Jungle**
  - 🌳 **Acacia**
  - 🌸 **Cherry**
  - 🌲 **Dark Oak**
  - 🪵 **Mangrove**
  - 🎍 **Bamboo Rafts**
- **Wrapped Rope Knot**: A knotted rope wraps snugly around the emergent post (matching the fence knot appearance), with the 3D lead rope stretching between the post and the anchor.
- **Boarding While Moored**: You can still right-click and sit in a moored boat without untying it. If you paddle, the boat will be held by the lead anchor.
- **Easy Unleashing**:
  - **Shift + Right-Click** the boat with an empty hand to untie it and retrieve the lead.
  - Or right-click the fence post knot to untie all boats attached to it.
  - Breaking the fence post or knot automatically detaches the lead and drops it.
  - If a boat is pulled beyond 10 blocks (e.g. violent current or explosion), the lead safely snaps and drops.

---

## 🏗️ Modular Architecture (Extensible for Sibling Interactions)

This mod is architected so new vanilla-style interactions can be added without tangling existing features:

```
src/main/java/net/expandedvanilla/
├── ExpandedVanillaInteractions.java           # Mod entrypoint & interaction dispatcher
├── client/
│   └── ExpandedVanillaInteractionsClient.java # Client entrypoint & feature dispatcher
├── interactions/
│   └── boat_mooring/                          # Isolated boat mooring feature folder
│       ├── BoatMooringHandler.java            # Interaction, knot, and item drop logic
│       ├── BoatMooringPhysics.java            # Tether tension, distance checks, anchor physics
│       ├── LeashableBoat.java                 # Duck interface for BoatEntity
│       └── client/
│           ├── BoatMooringClient.java         # Client layer registration
│           ├── BoatVariantTextures.java       # Variant wood textures for posts
│           ├── MooringPostModel.java          # 3D model for bollard & rope knot
│           └── MooringPostRenderer.java       # Custom rendering for post and 3D lead rope
└── mixin/
    └── boat_mooring/                          # Mixins scoped specifically to this interaction
        ├── BoatEntityMixin.java
        ├── LeadItemMixin.java
        ├── LeashKnotEntityMixin.java
        ├── EntityTrackerEntryMixin.java
        └── client/
            ├── BoatEntityRendererMixin.java
            └── ClientPlayNetworkHandlerMixin.java
```

### Adding New Interactions in the Future

To add a new interaction feature:
1. Create a sibling folder under `src/main/java/net/expandedvanilla/interactions/<your_feature>/`
2. Implement your interaction logic, handlers, and optional client subpackage.
3. Add any scoped mixins under `src/main/java/net/expandedvanilla/mixin/<your_feature>/` and declare them in `expanded_vanilla_interactions.mixins.json`.
4. Call your feature's initialization from `ExpandedVanillaInteractions.java` (and `ExpandedVanillaInteractionsClient.java` if client-sided).

---

## 🚀 Building & Running

### Requirements
- Java 17 or higher
- Windows, macOS, or Linux

### Build Mod JAR
```bash
./gradlew build
```
The compiled JAR will be in `build/libs/expanded_vanilla_interactions-1.0.0.jar`.

### Launch Minecraft Client in Dev Environment
```bash
./gradlew runClient
```
