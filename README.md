# 🔥 HearthGuard

> **Turn campfires into protection.**  
> Hostile mobs fear the fire — they panic, turn, and flee from lit campfires.

***

> Built for vanilla+ gameplay — no new blocks, no clutter, just smarter mobs.

---

> Works with [CinderStride](https://www.curseforge.com/minecraft/mc-mods/cinderstride) — upgrade campfires into powerful Hearthfires that repel mobs even more effectively.

---
## 🌿 Why it feels vanilla

*   Mobs **run away** instead of being blocked or burned
*   Behavior-driven — **no artificial rules or spawn prevention**
*   Creates a more **natural, immersive survival experience**

> Designed to feel like a missing Minecraft mechanic.

---

## 🎯 Why use HearthGuard?

Vanilla Minecraft treats fire as damage — not something mobs fear.

HearthGuard changes that:

*   Campfires become a **real defensive tool**
*   Nights feel **less binary and more tactical**
*   Mobs behave in a way that feels **alive and reactive**

> It doesn’t make the game easier — it makes it smarter.

---

## 🛡️ Features

### 🔥 Core Behavior
*   🔥 Campfires become a **true defensive mechanic**
*   Hostile mobs **flee from nearby lit campfires**
*   💡 Mobs panic faster the closer they are to the fire
*   Requires **line of sight** — no fleeing through walls

### 🎲 Immersion Details
*   Optional **fear drop** (panic reaction, once per mob)
*   Mobs react **naturally**, not through artificial blocking
*   🎭 **Multi-stage reactions** (notice → panic → flee)

### ⚙️ Control & Compatibility
*   **Per-mob allowlist / blocklist**
*   🧾 Command system for runtime configuration
*   Optional UI via **Mod Menu + Cloth Config**
*   ✔️ Lightweight & server-safe
*   ✔️ Fabric & NeoForge (1.21.1, 1.21.11, 26.x)

### 🔥 Hearthfire ([CinderStride](https://www.curseforge.com/minecraft/mc-mods/cinderstride) Integration)
*   Upgrade campfires into Hearthfires using CinderStride
*   +50% fear radius
---

## 🚧 Planned Features

*   🧠 **Adaptive fear system** (mobs can become resistant over time)
*   🎭 **Advanced panic behaviors**
*   🧾 **Expanded command system** for runtime configuration and debugging

---

## 📸 Behavior in action

> **Mobs don’t just stop — they react.**

**Startled**  
![Startled](https://media.forgecdn.net/attachments/1588/27/startled-png.png)

**Jumping**  
![Jumping](https://media.forgecdn.net/attachments/1588/25/jumping-png.png)

**Fleeing**  
![Fleeing](https://media.forgecdn.net/attachments/1588/26/running-png.png)

---

## ⚙️ Configuration

Config file:
`config/hearthguard/config.json`

Also accessible via in-game UI (Mod Menu + Cloth Config).

### Settings

*   **Range** — Campfire detection distance (blocks)
*   **Flee Fast Speed** — Speed when close to the fire
*   **Flee Slow Speed** — Speed when farther away
*   **Mode** — `Whitelist` or `Blacklist`
*   **Mobs** — Affected entity IDs
*   **Drop Item Chance** — % chance (0–100) to drop one item on first fear response

### Notes

*   `Drop Item Chance = 0` disables fear drops
*   Each mob can only drop once (persisted across saves)
*   Only applies in the Overworld (by design)

---

## ⚠️ Known Issues

- Datapack-defined mobs that reuse vanilla entity IDs cannot be selected independently
- Slimes and magma cubes are not affected

---

## 📝 Changelog
### 1.1.0
* Added integration with [CinderStride](https://www.curseforge.com/minecraft/mc-mods/cinderstride) Hearthfires now increase mob fear radius by 50%.
### 1.0.3
* Added Command system for runtime configuration
* Support for Minecraft v26.1.2
* Fixed: Config changes now correctly apply to previously spawned mobs

### 1.0.2
* Support for Minecraft v26.1.1

## 🔒 License

See `LICENSE`.

You are allowed to:

*   Use this mod in modpacks
*   Use in videos and content

---

## ❤️ Support

If you enjoy the mod, consider leaving a comment or adding it to your modpack!