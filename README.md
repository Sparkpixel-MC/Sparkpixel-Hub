<div align="center">

# ⚡ Sparkpixel-Hub

**The lobby plugin powering the Sparkpixel network — now on Paper 1.21.11**

[![Paper](https://img.shields.io/badge/Paper-1.21.11-3b82f6?style=for-the-badge&logo=minecraft&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Version](https://img.shields.io/badge/Version-4.0.0-22c55e?style=for-the-badge)](https://github.com)
[![License](https://img.shields.io/badge/License-GPL--3.0-red?style=for-the-badge)](LICENSE)

**English** | [简体中文](README.zh-CN.md)

</div>

---

## 📖 Overview

**Sparkpixel-Hub** is the modular, fully-featured lobby plugin built for the **Sparkpixel** network. It powers everything from region-based PvP arenas and a full retention layer to parkour, quests, cosmetics, and a rich guide storyline — all on **Paper 1.21.11** with **Adventure / MiniMessage** text.

> Built for Java 21, designed for scale, and tuned for a modern Minecraft server experience.

---

## 🚀 Features

- 🧩 **Modular configuration** — split `modules/*.yml` files with automatic missing-key migration
- 🌐 **Per-client localization** — messages served from `lang/en_us.yml` and `lang/zh_cn.yml`
- ⚔️ **Region-based PvP arena** — with a fully configurable kit
- 🎫 **LuckPerms VIP trampoline** — VIP-gated launch pad integration
- 🎆 **Join effects** — fireworks, sounds, and particles
- 🔊 **Configurable announcement sounds**
- 🖥️ **Modern menus** — built on Adventure
- 🎯 **Retention layer** — main menu, daily login, quests, parkour, achievements, collectibles, shop, leaderboard, events, and guide
- 📜 **Full guide storyline** — see [GUIDE_STORY.md](GUIDE_STORY.md)
- ♟️ **SparkChess compatibility** — no trampoline placement on boards, lobby items hidden during games, board interactions bypass lobby protection
- 🛡️ **Lobby toolkit** — protection, spawn, build mode, launchpads, double jump, holograms, player hider, teleportation bow, and more

👉 See [FEATURES.md](FEATURES.md) for the full feature list and configuration details.

---

## 🔧 Build

```bash
./gradlew build
```

**Output:**

```text
build/libs/Sparkpixel-Hub-4.0.0.jar
```

---

## ⌨️ Commands

| Command | Description |
| --- | --- |
| `/sparkpixelhub` · `/sphub` | Main command |
| `/sparkpixelhub reload` | Reload configs, menus, modules, and languages |
| `/sparkpixelhub open <menu>` | Open a custom menu |
| `/sparkpixelhub pvp pos1\|pos2\|info` | Configure the PvP region |
| `/sparkpixelhub trampoline` | Place a trampoline as an administrator |
| `/sparkpixelhub menu` | Open the hub menu |
| `/sparkpixelhub parkour` | Start / stop parkour |
| `/sparkpixelhub feedback <text>` | Submit feedback |
| `/sparkpixelhub hologram ...` | Manage holograms |

---

## 🧱 Technical Stack

| Component | Version / Detail |
| --- | --- |
| **Minecraft** | Paper 1.21.11 |
| **Java** | 21 |
| **Plugin version** | 4.0.0 |
| **Text engine** | Adventure / MiniMessage |
| **Permission prefix** | `sparkpixelhub.*` |

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0**.
See [LICENSE](LICENSE) for details.

<div align="center">

*Made with ⚡ for the Sparkpixel network*

</div>