# HearthGuard

HearthGuard is a Fabric mod for Minecraft that makes hostile mobs fear and flee from lit campfires in the Overworld.

## Features

- Hostile mobs react to nearby lit campfires and flee
- Line-of-sight requirement so mobs don't flee through walls
- Configurable detection range and flee speeds
- Per-mob allowlist or blocklist
- Optional one-time fear drop per mob
- Client config UI via Mod Menu + Cloth Config

## Configuration

HearthGuard loads its config from `config/hearthguard.json` and also exposes a client UI.

Settings:
- `Range`: Campfire detection distance (blocks)
- `Flee Fast Speed`: Speed when the mob is close to the fire
- `Flee Slow Speed`: Speed when the mob is farther from the fire
- `Mode`: `Whitelist` or `Blacklist`
- `Mobs`: The set of affected mob entity IDs
- `Drop Item Chance`: Percent chance (0-100) to drop one loot item on the first fear response

Notes:
- `Drop Item Chance = 0` disables fear drops
- Each mob can only drop once (persisted across saves)
- Campfire fear only runs in the Overworld

## Install

1. Install Fabric Loader for Minecraft `1.21.11`
2. Install Fabric API
3. Drop the HearthGuard jar into your `mods` folder

## Development

Build:
```bash
./gradlew build
```

Run client:
```bash
./gradlew runClient
```

## Compatibility

- Minecraft `1.21.11`
- Fabric Loader `0.18.4`
- Fabric API `0.141.3+1.21.11`
- Mod Menu `17.0.0-beta.2` (optional, for config UI)
- Cloth Config `21.11.153` (optional, for config UI)

## License

See `LICENSE`.
