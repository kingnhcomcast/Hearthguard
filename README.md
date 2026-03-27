# 🔥 HearthGuard

> **Mobs fear the fire.**  
> Hostile creatures panic, turn, and flee from lit campfires.

***

## 🧠 What makes HearthGuard different?

*   Mobs **run away** instead of being blocked or burned
*   Creates a more **natural, immersive survival experience**
*   Works through behavior, not spawn prevention

> Designed to feel like a natural Minecraft mechanic — no artificial rules, just creatures reacting to fire.

***

## 🛡️ Features

*   Hostile mobs **flee from nearby lit campfires**
*   **Line-of-sight required** (no fleeing through walls)
*   **Dynamic flee speeds** based on distance
*   **Optional fear drop** (mobs may panic and drop an item once)
*   **Per-mob allowlist / blocklist**
*   Fully **configurable**
*   Optional in-game config UI via Mod Menu + Cloth Config

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

## 🧩 Compatibility

*   Minecraft `1.21.1`
*   Fabric Loader `0.18.2`
*   Fabric API `0.116.9+1.21.1`
*   Mod Menu `17.0.0-beta.2` _(optional)_
*   Cloth Config `21.11.153` _(optional)_

***

## ⚠️ Known Issues

- Datapack-defined mobs that reuse vanilla entity IDs cannot be selected independently in the mob list.
- Slimes (and magma cubes) are not affected
- Changing config in game to select mobs, does not apply to already existing mobs. reloading game does
- Loot chance 100%, blazes dont seem to drop anything?

***

## 🔒 License

See `LICENSE`.

You are allowed to:

*   Use this mod in modpacks
*   Use in videos and content

***

## ❤️ Support

If you enjoy the mod, consider leaving a comment or adding it to your modpack!
