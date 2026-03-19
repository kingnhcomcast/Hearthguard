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

HearthGuard loads its config from `config/hearthguard/config.json` and also exposes a client UI.

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

1. Install Fabric Loader for Minecraft `1.21.1`
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

- Minecraft `1.21.1`
- Fabric Loader `0.18.2`
- Fabric API `0.116.9+1.21.1`
- Mod Menu `17.0.0-beta.2` (optional, for config UI)
- Cloth Config `15.0.130` (optional, for config UI)

## Known Issues

- Datapack-defined mobs that reuse vanilla entity IDs cannot be selected independently in the mob list.
- Slimes (and magma cubes) are not affected
- Changing config in game to select mobs, does not apply to already existing mobs. reloading game does
- Loot chance 100%, blazes dont seem to drop anything?

## License

See `LICENSE`.
