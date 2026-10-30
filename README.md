# Sparkpixel-Hub

Paper 26.3 lobby plugin for the Sparkpixel network.

- **Minecraft:** Paper 26.3
- **Java:** 25
- **Plugin version:** 4.0.0
- **Text engine:** Adventure / MiniMessage
- **Permission prefix:** `sparkpixelhub.*`
- **Recommended:** ProtocolLib for the drift input lock

## Features

- Modular `modules/*.yml` configuration with automatic missing-key migration
- Per-client-language messages in `lang/en_us.yml` and `lang/zh_cn.yml`
- Region-based PvP arena with a fully configurable kit
- LuckPerms VIP trampoline
- Server-authoritative river drifting with ProtocolLib input lock
- Join firework, sound and particle effects
- Configurable announcement sounds
- Modern Adventure-based menus
- Lobby protection, spawn, build mode, launchpads, double jump, holograms,
  player hider, teleportation bow and more

See [FEATURES.md](FEATURES.md) for the full feature list and configuration details.

## Build

```bash
./gradlew build
```

Output:

```text
build/libs/Sparkpixel-Hub-4.0.0.jar
```

## Commands

| Command | Description |
| --- | --- |
| `/sparkpixelhub`, `/sphub` | Main command |
| `/sparkpixelhub reload` | Reload configs, menus, modules and languages |
| `/sparkpixelhub open <menu>` | Open a custom menu |
| `/sparkpixelhub pvp pos1\|pos2\|info` | Configure the PvP region |
| `/sparkpixelhub trampoline` | Place a trampoline as an administrator |
| `/sparkpixelhub drift` | Spawn a drift boat |
| `/sparkpixelhub hologram ...` | Manage holograms |

## License

This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE).