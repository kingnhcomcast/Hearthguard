# HearthGuard

> When the sun sets without shelter you no longer need fear hostile mobs as you can light a campfire which causes them to panic and flee in fear. 

***

<img alt="CinderStride boots on item frame" height="64" src="https://github.com/kingnhcomcast/CinderStride/blob/26.1/media/icon.png?raw=true" title="CinderStride" width="64"/><br>
Works with [CinderStride](https://www.curseforge.com/minecraft/mc-mods/cinderstride) Upgrade campfires into Hearthfires that increase the fear range.

---
## Vanilla Friendly

*   Mobs visibly show panic, fear, and flight 
*   Behavior is modified rather than artificial rules
*   Works with vanilla campfire

---

## Features
*   Works with modded mobs too!
*   Mob behavior is modified to fear lit campfires
*   Mobs freeze, jump in panic, then flee
*   Loot may be dropped when they panic  
*   Requires actual line of sight

## Behavior in action

> **Mobs react realistically and naturally rather than being pushed**

**Startled**  
![Startled](https://media.forgecdn.net/attachments/1588/27/startled-png.png)

**Jumping**  
![Jumping](https://media.forgecdn.net/attachments/1588/25/jumping-png.png)

**Fleeing**  
![Fleeing](https://media.forgecdn.net/attachments/1588/26/running-png.png)

---

## Configuration

Config file:
`config/hearthguard/config.json`

UI via Mod Menu (Fabric) and Cloth Config.

### Settings

*   **Range**: Campfire detection distance in blocks
*   **Flee Fast Speed**: Speed when close to the fire
*   **Flee Slow Speed**: Speed when farther away
*   **Mode**: `Whitelist` or `Blacklist`
*   **Mobs**: Affected entity IDs
*   **Drop Item Chance**: % chance (0–100) to drop one item on first fear only

### Notes

*   `Drop Item Chance = 0` disables fear drops
*   Each mob can only drop once (persisted across saves)
*   Only applies in the Overworld

---

## Known Issues

- Datapack defined mobs that reuse vanilla entity IDs cannot be selected independently

---

## Changelog
### 1.1.0
* Added integration with [CinderStride](https://www.curseforge.com/minecraft/mc-mods/cinderstride) Hearthfires now increase mob fear radius by 50%.
### 1.0.3
* Added Command system for runtime configuration
* Support for Minecraft v26.1.2
* Fixed: Config changes now correctly apply to previously spawned mobs

### 1.0.2
* Support for Minecraft v26.1.1

---

License: MIT

Source: https://github.com/kingnhcomcast/hearthguard

Permissions: You are welcome to use this mod in your modpack. If you do, feel free to leave a comment and let me know.

If you want support for other minecraft versions or feature requests, leave a comment!

Enjoy!