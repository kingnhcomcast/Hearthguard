# 🔥 HearthGuard

> **Mobs fear the fire.**  
> Hostile creatures panic, turn, and flee from lit campfires.

***

> Built for vanilla+ gameplay — no new blocks, no clutter, just smarter mobs.

## 🧠 What makes HearthGuard different?
*   Mobs **run away** instead of being blocked or burned
*   Creates a more **natural, immersive survival experience**
*   Works through behavior, not spawn prevention

> Designed to feel like a natural Minecraft mechanic — no artificial rules, just creatures reacting to fire.

## 🎯 Why use HearthGuard?

Vanilla Minecraft treats fire as damage — not something mobs fear.

HearthGuard changes that:

*   Campfires become a **defensive tool**
*   Nights feel **less binary and more tactical**
*   Mobs behave in a way that feels **alive and reactive**

> It doesn’t make the game easier — it makes it smarter.


***

## 🛡️ Features
### 🔥 Core Behavior
*   Hostile mobs **flee from nearby lit campfires**
*   Requires **line of sight** — no fleeing through walls
*   💡 Mobs don’t just flee — they panic faster the closer they are to the fire.

### 🎲 Immersion Details
*   Optional **fear drop** (panic reaction, once per mob)
*   Mobs react **naturally**, not through artificial blocking

### ⚙️ Control & Compatibility
*   **Per-mob allowlist / blocklist**
*   Full **command-based configuration**
*   Optional UI via **Mod Menu + Cloth Config**
*   Works on Fabric & NeoForge (1.21.1, 1.21.11, 26.x)


## 🚧 Planned Features
*   🔥 **Hearthfire upgrade system** 
*   🧠 **Advanced panic behaviors** 
*   🔗 **Cross-mod integration** (including upcoming companion mods)

***

## 📸 Behavior in action

> **Mobs don’t just stop—they react.**  
> They panic, turn, and flee from the fire.

### Startled

**Hostile mob noticing a lit campfire.** ![Startled](https://media.forgecdn.net/attachments/1588/27/startled-png.png)

### Jumping

**Mob reacting in alarm.** ![Jumping](https://media.forgecdn.net/attachments/1588/25/jumping-png.png)

### Fleeing

**Mob running away from the campfire.** ![Fleeing](https://media.forgecdn.net/attachments/1588/26/running-png.png)

***

## ⚙️ Configuration

HearthGuard loads its config from:

`config/hearthguard/config.json`

Also accessible via in-game UI (if Mod Menu + Cloth Config are installed).

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
*   Campfire fear only runs in the Overworld

***

## ⚠️ Known Issues

- Datapack-defined mobs that reuse vanilla entity IDs cannot be selected independently in the mob list.
- Slimes (and magma cubes) are not affected

***

## 🔒 License

See `LICENSE`.

You are allowed to:

*   Use this mod in modpacks
*   Use in videos and content

***

## ❤️ Support

If you enjoy the mod, consider leaving a comment or adding it to your modpack!
